using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.IO.Compression;
using System.Reflection;
using System.Runtime.ExceptionServices;
using System.Text;
using System.Threading;
using System.Windows.Forms;

namespace JavaCampus.Launcher
{
    /// <summary>
    /// Windows 原生單檔可攜啟動器進入點。
    /// 負責秒開環境檢查、資源解封裝（含進度視窗、多實例互斥、原子替換）、
    /// 存檔目錄初始化、舊版快取清理與子進程喚起。
    /// </summary>
    public static class Program
    {
        // 核心應用程式版本常數，決定隔離快取目錄路徑
        private const string AppVersion = "0.1.2";

        // 內嵌封裝資源名稱與檔案命名常數
        private const string ResourcePayloadName = "JavaCampus.Payload";
        private const string ExeFileName = "JavaCampus.exe";
        private const string ReadyMarkerFileName = ".ready";
        private const string RuntimeDirPrefix = "runtime-v";
        private const string TempDirInfix = ".tmp-";

        // 傳遞給遊戲本體的環境變數，避免兩端各自硬編碼路徑
        private const string EnvSaveDir = "JAVACAMPUS_SAVE_DIR";
        // 啟動器所在資料夾；新 exe 放在舊版 zip 解壓資料夾裡時，遊戲會從這裡搬舊存檔
        private const string EnvLauncherDir = "JAVACAMPUS_LAUNCHER_DIR";

        // 子進程在此毫秒數內以非零代碼結束，視為「可能檔案損毀」並提示修復
        private const long QuickFailThresholdMs = 5000;

        private const long MaxLogBytes = 512 * 1024;

        /// <summary>
        /// 應用程式主進入點（無控制台黑視窗 GUI 子系統模式）。
        /// </summary>
        [STAThread]
        public static int Main(string[] args)
        {
            try
            {
                Application.EnableVisualStyles();
                Application.SetCompatibleTextRenderingDefault(false);

                string baseDir = GetBaseDir();
                string runtimeName = RuntimeDirPrefix + AppVersion;
                string runtimeDir = Path.Combine(baseDir, runtimeName);
                string targetExePath = Path.Combine(runtimeDir, ExeFileName);
                string identity = GetPayloadIdentity();

                EnsureRuntime(runtimeDir, targetExePath, identity, false);
                string saveDir = EnsureSaveDirectory();
                StartCleanup(baseDir, runtimeName);

                long elapsedMs;
                int exitCode = LaunchProcess(targetExePath, runtimeDir, saveDir, args, out elapsedMs);

                // 啟動後立即異常結束，可能是快取檔案被刪除或損毀：詢問是否修復並重試一次
                if (exitCode != 0 && elapsedMs < QuickFailThresholdMs)
                {
                    Log("遊戲於 " + elapsedMs + " ms 內以代碼 " + exitCode + " 結束，詢問是否修復。");

                    DialogResult answer = MessageBox.Show(
                        "遊戲啟動後立即異常結束（代碼 " + exitCode + "），可能是遊戲檔案損毀或被刪除。\n\n是否重新解壓遊戲檔案並再試一次？\n（存檔不會被刪除）",
                        "JavaCampus",
                        MessageBoxButtons.YesNo,
                        MessageBoxIcon.Question);

                    if (answer == DialogResult.Yes)
                    {
                        Log("使用者選擇修復，重新解壓。");
                        EnsureRuntime(runtimeDir, targetExePath, identity, true);
                        exitCode = LaunchProcess(targetExePath, runtimeDir, saveDir, args, out elapsedMs);
                    }
                }

                return exitCode;
            }
            catch (Exception ex)
            {
                Log("啟動失敗：" + ex);

                MessageBox.Show(
                    "啟動 JavaCampus 失敗：\n\n" + DescribeError(ex) +
                    "\n\n詳細資訊已寫入記錄檔：\n" + GetLogPath(),
                    "JavaCampus 啟動錯誤",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error);

                return 1;
            }
        }

        // ------------------------------------------------------------------
        // 路徑與身分
        // ------------------------------------------------------------------

        private static string GetBaseDir()
        {
            string localAppData = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData);
            return Path.Combine(localAppData, "JavaCampus");
        }

        private static string GetLogPath()
        {
            return Path.Combine(GetBaseDir(), "launcher.log");
        }

        /// <summary>
        /// 產生本次建置的 Payload 身分字串。
        /// 以版本號搭配組件 MVID（每次建置皆不同），即使忘了更新 AppVersion 也能偵測到新版。
        /// </summary>
        private static string GetPayloadIdentity()
        {
            Guid mvid = Assembly.GetExecutingAssembly().ManifestModule.ModuleVersionId;
            return AppVersion + "|" + mvid.ToString("N");
        }

        // ------------------------------------------------------------------
        // 快取檢查
        // ------------------------------------------------------------------

        /// <summary>
        /// 快速檢查快取是否可用：身分相符，且標記檔清單中每個檔案都存在、大小一致。
        /// 只做 stat 不讀內容，維持秒開。
        /// </summary>
        private static bool IsRuntimeReady(string runtimeDir, string targetExePath, string identity)
        {
            try
            {
                string readyFilePath = Path.Combine(runtimeDir, ReadyMarkerFileName);
                if (!File.Exists(readyFilePath) || !File.Exists(targetExePath))
                {
                    return false;
                }

                string[] lines = File.ReadAllLines(readyFilePath, Encoding.UTF8);
                if (lines.Length == 0 || lines[0] != identity)
                {
                    return false;
                }

                for (int i = 1; i < lines.Length; i++)
                {
                    string line = lines[i];
                    int tab = line.IndexOf('\t');
                    if (tab <= 0)
                    {
                        return false;
                    }

                    long expectedSize;
                    if (!long.TryParse(line.Substring(0, tab), out expectedSize))
                    {
                        return false;
                    }

                    FileInfo info = new FileInfo(Path.Combine(runtimeDir, line.Substring(tab + 1)));
                    if (!info.Exists || info.Length != expectedSize)
                    {
                        return false;
                    }
                }

                return true;
            }
            catch (IOException)
            {
                return false;
            }
            catch (UnauthorizedAccessException)
            {
                return false;
            }
        }

        /// <summary>
        /// 確保執行環境就緒。快取有效時零 UI 直接返回；否則顯示進度視窗並在背景執行緒解壓。
        /// </summary>
        private static void EnsureRuntime(string runtimeDir, string targetExePath, string identity, bool force)
        {
            if (!force && IsRuntimeReady(runtimeDir, targetExePath, identity))
            {
                return;
            }

            RunWithProgress(delegate(Action<int, string> report)
            {
                PrepareRuntime(runtimeDir, targetExePath, identity, force, report);
            });
        }

        /// <summary>
        /// 於具名 Mutex 保護下解壓：先解到暫存目錄，成功後整個目錄改名，確保結果為原子且不留半成品。
        /// </summary>
        private static void PrepareRuntime(string runtimeDir, string targetExePath, string identity, bool force, Action<int, string> report)
        {
            Directory.CreateDirectory(Path.GetDirectoryName(runtimeDir));

            using (Mutex mutex = new Mutex(false, @"Local\JavaCampus.Extract." + AppVersion))
            {
                bool acquired = false;
                report(-1, "正在等待其他啟動程序完成準備…");

                try
                {
                    acquired = mutex.WaitOne(TimeSpan.FromMinutes(5));
                }
                catch (AbandonedMutexException)
                {
                    // 前一個實例異常結束，目前執行緒已取得所有權，繼續進行
                    acquired = true;
                }

                if (!acquired)
                {
                    throw new TimeoutException("等待其他啟動程序釋放解壓鎖逾時。");
                }

                try
                {
                    // 取得鎖後再檢查一次：可能已有其他實例完成解壓
                    if (!force && IsRuntimeReady(runtimeDir, targetExePath, identity))
                    {
                        return;
                    }

                    report(0, "首次啟動，正在準備遊戲檔案…");

                    string tempDir = runtimeDir + TempDirInfix + Guid.NewGuid().ToString("N");
                    try
                    {
                        ExtractPayload(tempDir, identity, report);

                        if (Directory.Exists(runtimeDir))
                        {
                            Directory.Delete(runtimeDir, true);
                        }

                        Directory.Move(tempDir, runtimeDir);
                    }
                    catch
                    {
                        TryDeleteDirectory(tempDir);
                        throw;
                    }
                }
                finally
                {
                    mutex.ReleaseMutex();
                }
            }
        }

        /// <summary>
        /// 將內嵌 ZIP 解開至指定目錄，並寫入含身分與檔案清單的就緒標記檔。
        /// </summary>
        private static void ExtractPayload(string destDir, string identity, Action<int, string> report)
        {
            Directory.CreateDirectory(destDir);

            List<string> markerLines = new List<string>();
            markerLines.Add(identity);

            string root = Path.GetFullPath(destDir);

            using (Stream payloadStream = GetPayloadResourceStream())
            using (ZipArchive archive = new ZipArchive(payloadStream, ZipArchiveMode.Read))
            {
                long totalBytes = 0;
                foreach (ZipArchiveEntry e in archive.Entries)
                {
                    totalBytes += e.Length;
                }

                long doneBytes = 0;
                int lastPercent = -1;
                byte[] buffer = new byte[81920];

                foreach (ZipArchiveEntry entry in archive.Entries)
                {
                    string destinationPath = Path.GetFullPath(Path.Combine(destDir, entry.FullName));

                    // 防範 Zip Slip：路徑穿越代表封裝異常或遭竄改，直接中止而非悄悄略過
                    if (!IsPathInside(root, destinationPath))
                    {
                        throw new InvalidDataException("封裝包含不安全的路徑：" + entry.FullName);
                    }

                    // 目錄條目
                    if (string.IsNullOrEmpty(entry.Name))
                    {
                        Directory.CreateDirectory(destinationPath);
                        continue;
                    }

                    Directory.CreateDirectory(Path.GetDirectoryName(destinationPath));

                    long written = 0;
                    using (Stream entryStream = entry.Open())
                    using (FileStream fileStream = new FileStream(destinationPath, FileMode.Create, FileAccess.Write, FileShare.None))
                    {
                        int read;
                        while ((read = entryStream.Read(buffer, 0, buffer.Length)) > 0)
                        {
                            fileStream.Write(buffer, 0, read);
                            written += read;
                            doneBytes += read;

                            if (totalBytes > 0)
                            {
                                int percent = (int)(doneBytes * 100 / totalBytes);
                                if (percent != lastPercent)
                                {
                                    lastPercent = percent;
                                    report(percent, null);
                                }
                            }
                        }
                    }

                    markerLines.Add(written + "\t" + entry.FullName.Replace('/', '\\'));
                }
            }

            if (!File.Exists(Path.Combine(destDir, ExeFileName)))
            {
                throw new FileNotFoundException("解壓縮封裝包後未找到主執行檔：" + ExeFileName);
            }

            // 標記檔最後才寫入；且整個目錄是在暫存區完成後才改名，因此對外永遠是完整狀態
            File.WriteAllLines(Path.Combine(destDir, ReadyMarkerFileName), markerLines.ToArray(), Encoding.UTF8);
        }

        /// <summary>
        /// 判斷完整路徑是否位於根目錄內（同時涵蓋 \ 與 / 分隔符）。
        /// </summary>
        private static bool IsPathInside(string root, string path)
        {
            char[] separators = new char[] { Path.DirectorySeparatorChar, Path.AltDirectorySeparatorChar };
            string normalizedRoot = root.TrimEnd(separators) + Path.DirectorySeparatorChar;
            string normalizedPath = path.TrimEnd(separators) + Path.DirectorySeparatorChar;
            return normalizedPath.StartsWith(normalizedRoot, StringComparison.OrdinalIgnoreCase);
        }

        /// <summary>
        /// 自組件載入內嵌封裝串流，支援標準名稱與結尾匹配之容錯處理。
        /// </summary>
        private static Stream GetPayloadResourceStream()
        {
            Assembly assembly = Assembly.GetExecutingAssembly();
            Stream stream = assembly.GetManifestResourceStream(ResourcePayloadName);
            if (stream != null)
            {
                return stream;
            }

            string[] resourceNames = assembly.GetManifestResourceNames();
            for (int i = 0; i < resourceNames.Length; i++)
            {
                if (resourceNames[i].EndsWith("Payload", StringComparison.OrdinalIgnoreCase) ||
                    resourceNames[i].EndsWith("Payload.zip", StringComparison.OrdinalIgnoreCase))
                {
                    Stream fallback = assembly.GetManifestResourceStream(resourceNames[i]);
                    if (fallback != null)
                    {
                        return fallback;
                    }
                }
            }

            throw new InvalidOperationException(
                string.Format("找不到內嵌的遊戲封裝資源 ({0})，請確認建置設定是否已正確內嵌 Payload。", ResourcePayloadName)
            );
        }

        // ------------------------------------------------------------------
        // 進度視窗
        // ------------------------------------------------------------------

        /// <summary>
        /// 顯示進度視窗並在背景執行緒執行工作；工作中的例外會於 UI 結束後原樣重新擲出。
        /// </summary>
        private static void RunWithProgress(Action<Action<int, string>> work)
        {
            Exception error = null;

            using (ProgressForm form = new ProgressForm())
            {
                form.Shown += delegate
                {
                    Thread worker = new Thread(delegate()
                    {
                        try
                        {
                            work(form.Report);
                        }
                        catch (Exception ex)
                        {
                            error = ex;
                        }
                        finally
                        {
                            form.CloseSafe();
                        }
                    });
                    worker.IsBackground = true;
                    worker.Start();
                };

                Application.Run(form);
            }

            if (error != null)
            {
                ExceptionDispatchInfo.Capture(error).Throw();
            }
        }

        private sealed class ProgressForm : Form
        {
            private readonly Label statusLabel;
            private readonly ProgressBar progressBar;

            public ProgressForm()
            {
                Text = "JavaCampus";
                FormBorderStyle = FormBorderStyle.FixedDialog;
                StartPosition = FormStartPosition.CenterScreen;
                ControlBox = false;
                MaximizeBox = false;
                MinimizeBox = false;
                ShowInTaskbar = true;
                ClientSize = new System.Drawing.Size(380, 92);

                statusLabel = new Label();
                statusLabel.AutoSize = false;
                statusLabel.Left = 16;
                statusLabel.Top = 16;
                statusLabel.Width = 348;
                statusLabel.Height = 20;
                statusLabel.Text = "首次啟動，正在準備遊戲檔案…";

                progressBar = new ProgressBar();
                progressBar.Left = 16;
                progressBar.Top = 48;
                progressBar.Width = 348;
                progressBar.Height = 22;
                progressBar.Minimum = 0;
                progressBar.Maximum = 100;

                Controls.Add(statusLabel);
                Controls.Add(progressBar);
            }

            /// <summary>
            /// 可由任意執行緒呼叫。percent 小於 0 表示不更新進度；status 為 null 表示不更新文字。
            /// </summary>
            public void Report(int percent, string status)
            {
                try
                {
                    if (IsDisposed || !IsHandleCreated)
                    {
                        return;
                    }

                    BeginInvoke(new MethodInvoker(delegate
                    {
                        if (percent >= 0)
                        {
                            progressBar.Value = Math.Min(100, percent);
                        }
                        if (status != null)
                        {
                            statusLabel.Text = status;
                        }
                    }));
                }
                catch (InvalidOperationException)
                {
                    // 視窗正在關閉，忽略
                }
            }

            public void CloseSafe()
            {
                try
                {
                    if (!IsDisposed && IsHandleCreated)
                    {
                        BeginInvoke(new MethodInvoker(Close));
                    }
                }
                catch (InvalidOperationException)
                {
                }
            }
        }

        // ------------------------------------------------------------------
        // 存檔目錄、清理、啟動
        // ------------------------------------------------------------------

        /// <summary>
        /// 確保全域使用者存檔目錄存在，並回傳路徑（將以環境變數傳給遊戲）。
        /// 注意：位於 Roaming，若存檔很大且在網域漫遊設定檔環境，可能被同步。
        /// </summary>
        private static string EnsureSaveDirectory()
        {
            string appData = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
            string saveDir = Path.Combine(appData, "JavaCampus", "save");
            if (!Directory.Exists(saveDir))
            {
                Directory.CreateDirectory(saveDir);
            }
            return saveDir;
        }

        /// <summary>
        /// 背景清理舊版本快取與過期暫存目錄；任何失敗（例如舊版仍在執行）皆忽略。
        /// </summary>
        private static void StartCleanup(string baseDir, string currentRuntimeName)
        {
            Thread thread = new Thread(delegate()
            {
                try
                {
                    foreach (string dir in Directory.GetDirectories(baseDir, RuntimeDirPrefix + "*"))
                    {
                        string name = Path.GetFileName(dir);
                        if (string.Equals(name, currentRuntimeName, StringComparison.OrdinalIgnoreCase))
                        {
                            continue;
                        }

                        if (name.IndexOf(TempDirInfix, StringComparison.OrdinalIgnoreCase) >= 0)
                        {
                            // 暫存目錄可能屬於正在解壓的其他實例，僅清除超過一天的
                            if (DateTime.UtcNow - Directory.GetLastWriteTimeUtc(dir) < TimeSpan.FromDays(1))
                            {
                                continue;
                            }
                        }

                        TryDeleteDirectory(dir);
                    }
                }
                catch
                {
                    // 清理失敗不影響啟動
                }
            });
            thread.IsBackground = true;
            thread.Start();
        }

        private static void TryDeleteDirectory(string path)
        {
            try
            {
                if (Directory.Exists(path))
                {
                    Directory.Delete(path, true);
                }
            }
            catch
            {
            }
        }

        /// <summary>
        /// 喚起主遊戲進程並等待執行完畢。
        /// </summary>
        private static int LaunchProcess(string exePath, string workingDir, string saveDir, string[] args, out long elapsedMs)
        {
            ProcessStartInfo startInfo = new ProcessStartInfo();
            startInfo.FileName = exePath;
            startInfo.WorkingDirectory = workingDir;
            startInfo.Arguments = FormatArguments(args);
            startInfo.UseShellExecute = false;
            startInfo.EnvironmentVariables[EnvSaveDir] = saveDir;
            startInfo.EnvironmentVariables[EnvLauncherDir] = Path.GetDirectoryName(Application.ExecutablePath);

            Stopwatch stopwatch = Stopwatch.StartNew();

            using (Process process = Process.Start(startInfo))
            {
                if (process == null)
                {
                    throw new InvalidOperationException("無法啟動遊戲進程。");
                }

                // 需要等待以轉傳結束代碼（例如被腳本呼叫時）
                process.WaitForExit();
                stopwatch.Stop();
                elapsedMs = stopwatch.ElapsedMilliseconds;
                return process.ExitCode;
            }
        }

        // ------------------------------------------------------------------
        // 命令列引數（符合 CommandLineToArgvW / MSVCRT 規則）
        // ------------------------------------------------------------------

        private static string FormatArguments(string[] args)
        {
            if (args == null || args.Length == 0)
            {
                return string.Empty;
            }

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < args.Length; i++)
            {
                if (i > 0)
                {
                    builder.Append(' ');
                }
                AppendArgument(builder, args[i]);
            }
            return builder.ToString();
        }

        private static void AppendArgument(StringBuilder sb, string arg)
        {
            if (string.IsNullOrEmpty(arg))
            {
                sb.Append("\"\"");
                return;
            }

            if (arg.IndexOfAny(new char[] { ' ', '\t', '\n', '\v', '"' }) < 0)
            {
                sb.Append(arg);
                return;
            }

            sb.Append('"');
            int backslashes = 0;
            for (int i = 0; i < arg.Length; i++)
            {
                char c = arg[i];
                if (c == '\\')
                {
                    backslashes++;
                }
                else if (c == '"')
                {
                    // 引號前的反斜線加倍，再以一個反斜線跳脫引號
                    sb.Append('\\', backslashes * 2 + 1);
                    sb.Append('"');
                    backslashes = 0;
                }
                else
                {
                    sb.Append('\\', backslashes);
                    sb.Append(c);
                    backslashes = 0;
                }
            }

            // 結尾引號前的反斜線也必須加倍
            sb.Append('\\', backslashes * 2);
            sb.Append('"');
        }

        // ------------------------------------------------------------------
        // 記錄與錯誤訊息
        // ------------------------------------------------------------------

        private static void Log(string message)
        {
            try
            {
                string path = GetLogPath();
                Directory.CreateDirectory(Path.GetDirectoryName(path));

                FileInfo info = new FileInfo(path);
                if (info.Exists && info.Length > MaxLogBytes)
                {
                    info.Delete();
                }

                File.AppendAllText(
                    path,
                    DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss") + " [" + AppVersion + "] " + message + Environment.NewLine,
                    Encoding.UTF8);
            }
            catch
            {
                // 記錄失敗不應再造成新的錯誤
            }
        }

        /// <summary>
        /// 將常見例外轉為玩家看得懂、可採取行動的繁體中文說明。
        /// </summary>
        private static string DescribeError(Exception ex)
        {
            if (ex is UnauthorizedAccessException)
            {
                return "沒有權限寫入遊戲資料夾，可能是防毒軟體或資料夾權限設定造成。\n請將 JavaCampus 加入防毒軟體信任清單後再試。";
            }

            if (ex is PathTooLongException)
            {
                return "檔案路徑過長。請嘗試縮短 Windows 使用者名稱，或啟用 Windows 長路徑支援。";
            }

            if (ex is InvalidDataException)
            {
                return "遊戲封裝內容異常，檔案可能損毀。請重新下載遊戲。\n\n" + ex.Message;
            }

            if (ex is TimeoutException)
            {
                return "另一個 JavaCampus 正在準備檔案，等待逾時。請稍後再試。";
            }

            IOException io = ex as IOException;
            if (io != null)
            {
                int code = io.HResult & 0xFFFF;
                if (code == 112 || code == 39)
                {
                    return "磁碟空間不足，請清出空間後再試。";
                }
                if (code == 32 || code == 33)
                {
                    return "檔案被其他程式占用（常見於防毒軟體掃描中）。請稍候幾秒後再試。";
                }
            }

            return ex.Message;
        }
    }
}