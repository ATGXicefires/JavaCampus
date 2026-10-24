param(
    [Parameter(Position = 0)]
    [ValidateSet('setup', 'build', 'run', 'test', 'clean')]
    [string]$Task = 'run'
)

$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

$Maven = 'https://repo1.maven.org/maven2'
$FxVersion = '25.0.5'
$Deps = @(
    @{ Dir = 'lib/fx';   Path = "org/openjfx/javafx-base/$FxVersion/javafx-base-$FxVersion-win.jar" }
    @{ Dir = 'lib/fx';   Path = "org/openjfx/javafx-graphics/$FxVersion/javafx-graphics-$FxVersion-win.jar" }
    @{ Dir = 'lib/fx';   Path = "org/openjfx/javafx-controls/$FxVersion/javafx-controls-$FxVersion-win.jar" }
    @{ Dir = 'lib/app';  Path = 'org/fxmisc/richtext/richtextfx/0.11.7/richtextfx-0.11.7.jar' }
    @{ Dir = 'lib/app';  Path = 'org/fxmisc/flowless/flowless/0.7.4/flowless-0.7.4.jar' }
    @{ Dir = 'lib/app';  Path = 'org/reactfx/reactfx/2.0-M5/reactfx-2.0-M5.jar' }
    @{ Dir = 'lib/app';  Path = 'org/fxmisc/undo/undofx/2.1.1/undofx-2.1.1.jar' }
    @{ Dir = 'lib/app';  Path = 'org/fxmisc/wellbehaved/wellbehavedfx/0.3.3/wellbehavedfx-0.3.3.jar' }
    @{ Dir = 'lib/app';  Path = 'com/google/code/gson/gson/2.15.0/gson-2.15.0.jar' }
    @{ Dir = 'lib/test'; Path = 'org/junit/platform/junit-platform-console-standalone/6.1.3/junit-platform-console-standalone-6.1.3.jar' }
)

$FxArgs = @('--module-path', 'lib/fx', '--add-modules', 'javafx.controls')
$AppCp = 'lib/app/*'
$Sep = [IO.Path]::PathSeparator

function Invoke-Setup {
    foreach ($dep in $Deps) {
        New-Item -ItemType Directory -Force $dep.Dir | Out-Null
        $target = Join-Path $dep.Dir (Split-Path $dep.Path -Leaf)
        if (Test-Path $target) { continue }
        Write-Host "下載 $($dep.Path)"
        $expected = (Invoke-WebRequest -UseBasicParsing "$Maven/$($dep.Path).sha1").Content
        if ($expected -is [byte[]]) { $expected = [Text.Encoding]::ASCII.GetString($expected) }
        $expected = $expected.Trim().Split(' ')[0]
        $tmp = "$target.part"
        Invoke-WebRequest -UseBasicParsing "$Maven/$($dep.Path)" -OutFile $tmp
        $actual = (Get-FileHash -Algorithm SHA1 $tmp).Hash
        if ($actual -ne $expected.ToUpperInvariant()) {
            Remove-Item $tmp
            throw "SHA-1 不符：$($dep.Path)（預期 $expected，實際 $actual）"
        }
        Move-Item $tmp $target
    }
    Write-Host '依賴已就緒'
}

function Assert-Setup {
    foreach ($dep in $Deps) {
        if (-not (Test-Path (Join-Path $dep.Dir (Split-Path $dep.Path -Leaf)))) {
            throw '缺少依賴，請先執行 .\dev.ps1 setup'
        }
    }
}

function Invoke-Javac([string]$SourceRoot, [string]$OutDir, [string]$ClassPath) {
    $sources = Get-ChildItem -Recurse -Filter *.java $SourceRoot | ForEach-Object { $_.FullName }
    if (-not $sources) { return }
    New-Item -ItemType Directory -Force $OutDir | Out-Null
    $argFile = Join-Path $OutDir '..\sources.txt'
    # javac 的 @argfile 會把反斜線當跳脫字元，路徑要換成正斜線
    $sources | ForEach-Object { '"' + ($_ -replace '\\', '/') + '"' } | Set-Content -Encoding utf8 $argFile
    & javac -encoding UTF-8 -Xlint:all -Xlint:-serial -Xlint:-classfile -d $OutDir @FxArgs -cp $ClassPath "@$argFile"
    if ($LASTEXITCODE -ne 0) { throw 'javac 編譯失敗' }
}

function Invoke-Build {
    Assert-Setup
    if (Test-Path out/classes) { Remove-Item -Recurse -Force out/classes }
    Invoke-Javac 'src/main/java' 'out/classes' $AppCp
    if (Test-Path src/main/resources) {
        Copy-Item -Recurse -Force 'src/main/resources/*' 'out/classes/'
    }
}

function Invoke-Run {
    Invoke-Build
    & java @FxArgs '--enable-native-access=javafx.graphics' '-Dfile.encoding=UTF-8' `
        -cp "out/classes$Sep$AppCp" campus.App
}

function Invoke-Test {
    Invoke-Build
    if (Test-Path out/test-classes) { Remove-Item -Recurse -Force out/test-classes }
    $testCp = "out/classes$Sep$AppCp$Sep" + 'lib/test/*'
    Invoke-Javac 'src/test/java' 'out/test-classes' $testCp
    $junit = (Get-ChildItem lib/test/junit-platform-console-standalone-*.jar).FullName
    $runCp = (@('out/test-classes', 'out/classes') + (Get-ChildItem lib/app/*.jar).FullName) -join $Sep
    & java @FxArgs '-Dfile.encoding=UTF-8' '-Dstdout.encoding=UTF-8' -jar $junit execute `
        --class-path $runCp --scan-classpath out/test-classes --disable-banner --details=tree
    if ($LASTEXITCODE -ne 0) { throw '測試失敗' }
}

switch ($Task) {
    'setup' { Invoke-Setup }
    'build' { Invoke-Build }
    'run'   { Invoke-Run }
    'test'  { Invoke-Test }
    'clean' { if (Test-Path out) { Remove-Item -Recurse -Force out } }
}
