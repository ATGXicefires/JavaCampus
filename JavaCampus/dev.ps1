param(
    [Parameter(Position = 0)]
    [ValidateSet('setup', 'build', 'run', 'test', 'package', 'clean')]
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

$AppVersion = '0.1.0'
# 打包版自帶的執行環境要放進的模組；jdk.jshell 跑玩家程式的遠端 JVM 要靠 jdk.jdwp.agent 連線
$RuntimeModules = 'java.desktop,java.sql,javafx.controls,jdk.jshell,jdk.jdwp.agent,jdk.charsets'

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
    & java @FxArgs '--enable-native-access=javafx.graphics' '-Dfile.encoding=UTF-8' '-Dstdout.encoding=UTF-8' -jar $junit execute `
        --class-path $runCp --scan-classpath out/test-classes --disable-banner --details=tree
    if ($LASTEXITCODE -ne 0) { throw '測試失敗' }
}

function Get-JdkTool([string]$Name) {
    # jar、jlink、jpackage 不在 Oracle 安裝程式加進 PATH 的 javapath 裡，改從 java.home 找
    $line = cmd /c 'java -XshowSettings:properties -version 2>&1' | Select-String 'java.home = '
    $tool = Join-Path $line.ToString().Split('=', 2)[1].Trim() "bin/$Name.exe"
    if (-not (Test-Path $tool)) { throw "找不到 $tool，打包需要 JDK" }
    $tool
}

function Invoke-Package {
    Invoke-Build
    $pkg = 'out/package'
    if (Test-Path $pkg) { Remove-Item -Recurse -Force $pkg }
    $inDir = "$pkg/input"
    New-Item -ItemType Directory -Force $inDir | Out-Null
    & (Get-JdkTool jar) --create --file "$inDir/JavaCampus.jar" -C out/classes .
    if ($LASTEXITCODE -ne 0) { throw 'jar 失敗' }
    Copy-Item lib/app/*.jar $inDir
    Copy-Item -Recurse fonts, lessons $inDir

    # JShell 會用同一套執行環境另開 JVM 跑玩家的程式，所以不能用 --strip-native-commands 拿掉 bin\java.exe
    & (Get-JdkTool jlink) --module-path lib/fx --add-modules $RuntimeModules --output "$pkg/runtime" `
        --strip-debug --no-man-pages --no-header-files
    if ($LASTEXITCODE -ne 0) { throw 'jlink 失敗' }

    & (Get-JdkTool jpackage) --type app-image --name JavaCampus --app-version $AppVersion `
        --input $inDir --main-jar JavaCampus.jar --main-class campus.App --runtime-image "$pkg/runtime" `
        --java-options '-Dcampus.home=$APPDIR' --java-options '--enable-native-access=javafx.graphics' `
        --java-options '-Dfile.encoding=UTF-8' --dest $pkg
    if ($LASTEXITCODE -ne 0) { throw 'jpackage 失敗' }
    # 遊戲從 campus.home（app 資料夾）往上一層找 story/scripts
    New-Item -ItemType Directory -Force "$pkg/JavaCampus/story" | Out-Null
    Copy-Item -Recurse ../story/scripts "$pkg/JavaCampus/story/"

    $zip = Join-Path (Resolve-Path $pkg) "JavaCampus-Demo-v$AppVersion-windows-x64.zip"
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    [IO.Compression.ZipFile]::CreateFromDirectory((Resolve-Path "$pkg/JavaCampus"), $zip, 'Optimal', $true)
    Write-Host "已打包：$zip"
}

switch ($Task) {
    'setup' { Invoke-Setup }
    'build' { Invoke-Build }
    'run'   { Invoke-Run }
    'test'  { Invoke-Test }
    'package' { Invoke-Package }
    'clean' { if (Test-Path out) { Remove-Item -Recurse -Force out } }
}
