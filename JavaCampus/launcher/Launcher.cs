using System;
using System.Diagnostics;
using System.IO;
using System.IO.Compression;
using System.Reflection;
using System.Text;
using System.Windows.Forms;

namespace JavaCampus.Launcher
{
    /// <summary>
    /// Windows 原生單檔可攜啟動器進入點。
    /// 負責秒開環境檢查、資源解封裝、存檔目錄初始化與子進程喚起。
    /// </summary>
    public static class Program
    {
        // 核心應用程式版本常數，決定隔離快取目錄路徑
        private const string AppVersion = "0.1.2";

        // 內嵌封裝資源名稱與檔案命名常數
        private const string ResourcePayloadName = "JavaCampus.Payload";
        private const string ExeFileName = "JavaCampus.exe";
        private const string ReadyMarkerFileName = ".ready";

        /// <summary>
        /// 應用程式主進入點（無控制台黑視窗 GUI 子系統模式）。
        /// </summary>
        /// <param name="args">外部命令列傳入參數</param>
        /// <returns>子進程結束代碼或例外代碼</returns>
        [STAThread]
        public static int Main(string[] args)
        {
            try
            {
                // 快取目錄隔離於 LocalAppData，避免存取權限問題並支援多版本並存
                string localAppData = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData);
                string runtimeDir = Path.Combine(localAppData, "JavaCampus", "runtime-v" + AppVersion);
                string targetExePath = Path.Combine(runtimeDir, ExeFileName);

                EnsureRuntimeExtracted(runtimeDir, targetExePath);
                EnsureSaveDirectory();

                return LaunchProcess(targetExePath, runtimeDir, args);
            }
            catch (Exception ex)
            {
                // 阻絕無訊息閃退，向使用者顯示結構化繁體中文錯誤資訊
                string errorMessage = string.Format(
                    "啟動 JavaCampus 遭遇未預期的錯誤：\n\n{0}\n\n詳細呼叫堆疊：\n{1}",
                    ex.Message,
                    ex.StackTrace
                );

                MessageBox.Show(
                    errorMessage,
                    "JavaCampus 啟動錯誤",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error
                );

                return 1;
            }
        }

        /// <summary>
        /// 檢查快取完整性；若未就緒則自動自內嵌資源解開執行環境。
        /// </summary>
        /// <param name="runtimeDir">版本專屬快取目錄</param>
        /// <param name="targetExePath">目標主執行檔絕對路徑</param>
        private static void EnsureRuntimeExtracted(string runtimeDir, string targetExePath)
        {
            string readyFilePath = Path.Combine(runtimeDir, ReadyMarkerFileName);

            // 秒開檢查機制：標記檔與主程式皆存在時直接略過解壓縮以達瞬間啟動
            if (File.Exists(readyFilePath) && File.Exists(targetExePath))
            {
                return;
            }

            if (!Directory.Exists(runtimeDir))
            {
                Directory.CreateDirectory(runtimeDir);
            }

            // 讀取內嵌 ZIP 資源流並解封裝至快取目錄
            using (Stream payloadStream = GetPayloadResourceStream())
            using (ZipArchive archive = new ZipArchive(payloadStream, ZipArchiveMode.Read))
            {
                string normalizedRuntimeDir = Path.GetFullPath(runtimeDir);
                if (!normalizedRuntimeDir.EndsWith(Path.DirectorySeparatorChar.ToString()))
                {
                    normalizedRuntimeDir += Path.DirectorySeparatorChar;
                }

                for (int i = 0; i < archive.Entries.Count; i++)
                {
                    ZipArchiveEntry entry = archive.Entries[i];
                    string destinationPath = Path.GetFullPath(Path.Combine(runtimeDir, entry.FullName));

                    // 防範 Zip Slip 路徑穿越弱點，確保所有解壓檔案皆侷限在快取目錄內
                    if (!destinationPath.StartsWith(normalizedRuntimeDir, StringComparison.OrdinalIgnoreCase))
                    {
                        continue;
                    }

                    // 目錄條目處理
                    if (string.IsNullOrEmpty(entry.Name))
                    {
                        if (!Directory.Exists(destinationPath))
                        {
                            Directory.CreateDirectory(destinationPath);
                        }
                        continue;
                    }

                    // 確保檔案父層目錄完整建立
                    string parentDir = Path.GetDirectoryName(destinationPath);
                    if (!string.IsNullOrEmpty(parentDir) && !Directory.Exists(parentDir))
                    {
                        Directory.CreateDirectory(parentDir);
                    }

                    // 串流寫入並支援同名檔案覆寫以復原損毀狀態
                    using (Stream entryStream = entry.Open())
                    using (FileStream fileStream = new FileStream(destinationPath, FileMode.Create, FileAccess.Write, FileShare.None))
                    {
                        entryStream.CopyTo(fileStream);
                    }
                }
            }

            if (!File.Exists(targetExePath))
            {
                throw new FileNotFoundException("解壓縮封裝包後未找到主執行檔：" + ExeFileName);
            }

            // 寫入就緒標記檔以供下次啟動直接跳過解壓
            File.WriteAllText(readyFilePath, DateTime.UtcNow.ToString("o"), Encoding.UTF8);
        }

        /// <summary>
        /// 自組件載入內嵌封裝串流，支援標準名稱與結尾匹配之容錯處理。
        /// </summary>
        /// <returns>ZIP 封裝包串流</returns>
        private static Stream GetPayloadResourceStream()
        {
            Assembly assembly = Assembly.GetExecutingAssembly();
            Stream stream = assembly.GetManifestResourceStream(ResourcePayloadName);
            if (stream != null)
            {
                return stream;
            }

            // 尋找符合 Payload 命名的內嵌資源以提升編譯命名相容性
            string[] resourceNames = assembly.GetManifestResourceNames();
            for (int i = 0; i < resourceNames.Length; i++)
            {
                if (resourceNames[i].EndsWith("Payload", StringComparison.OrdinalIgnoreCase) ||
                    resourceNames[i].EndsWith("Payload.zip", StringComparison.OrdinalIgnoreCase))
                {
                    return assembly.GetManifestResourceStream(resourceNames[i]);
                }
            }

            throw new InvalidOperationException(
                string.Format("找不到內嵌的遊戲封裝資源 ({0})，請確認建置設定是否已正確內嵌 Payload。", ResourcePayloadName)
            );
        }

        /// <summary>
        /// 確保全域使用者存檔目錄存在，供遊戲本體讀寫設定與存檔。
        /// </summary>
        private static void EnsureSaveDirectory()
        {
            string appData = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
            string saveDir = Path.Combine(appData, "JavaCampus", "save");
            if (!Directory.Exists(saveDir))
            {
                Directory.CreateDirectory(saveDir);
            }
        }

        /// <summary>
        /// 喚起主遊戲進程並等待執行完畢。
        /// </summary>
        /// <param name="exePath">主程式路徑</param>
        /// <param name="workingDir">工作目錄</param>
        /// <param name="args">外部命令列參數陣列</param>
        /// <returns>遊戲結束代碼</returns>
        private static int LaunchProcess(string exePath, string workingDir, string[] args)
        {
            ProcessStartInfo startInfo = new ProcessStartInfo();
            startInfo.FileName = exePath;
            startInfo.WorkingDirectory = workingDir;
            startInfo.Arguments = FormatArguments(args);
            startInfo.UseShellExecute = false;

            using (Process process = Process.Start(startInfo))
            {
                if (process == null)
                {
                    throw new InvalidOperationException("無法啟動遊戲進程。");
                }

                process.WaitForExit();
                return process.ExitCode;
            }
        }

        /// <summary>
        /// 將命令列參數陣列轉換為標準 Windows 引數格式字串。
        /// </summary>
        /// <param name="args">命令列參數陣列</param>
        /// <returns>格式化後的引數字串</returns>
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

                string arg = args[i];
                if (string.IsNullOrEmpty(arg))
                {
                    builder.Append("\"\"");
                    continue;
                }

                bool needsQuotes = arg.IndexOfAny(new char[] { ' ', '\t', '\"' }) >= 0;
                if (needsQuotes)
                {
                    builder.Append('\"');
                    for (int j = 0; j < arg.Length; j++)
                    {
                        if (arg[j] == '\"')
                        {
                            builder.Append("\\\"");
                        }
                        else
                        {
                            builder.Append(arg[j]);
                        }
                    }
                    builder.Append('\"');
                }
                else
                {
                    builder.Append(arg);
                }
            }

            return builder.ToString();
        }
    }
}
