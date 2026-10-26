using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.IO.Compression;
using System.Net;
using System.Reflection;
using System.Runtime.ExceptionServices;
using System.Security.Cryptography;
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
        private const string AppVersion = "0.2.0";

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
        // 遊戲拿這個網址查新版本；環境裡已經有就沿用，本機測試時可以指向自己的伺服器
        private const string EnvUpdateUrl = "JAVACAMPUS_UPDATE_URL";

        // releases/latest/download/ 是網頁轉址，不算 GitHub API 的次數；電腦教室共用一個對外 IP 時，API 每小時 60 次很快就用完
        private const string UpdateUrl = "https://github.com/ATGXicefires/JavaCampus/releases/latest/download/update.txt";
        // 玩家在標題畫面按了「更新」，遊戲就以這個代碼結束（遊戲端的 campus.Update.EXIT_CODE）
        private const int UpdateExitCode = 75;
        // 玩家手上的啟動器不會跟著更新：update.txt 改成不相容的格式時換這個數字，舊啟動器會請玩家重新下載，而不是裝出壞掉的版本
        private const string ManifestFormat = "1";
        private const string ManifestFileName = "update.txt";
        // 遊戲內更新裝好的版本，內容是「版本\t目錄名稱」
        private const string CurrentFileName = "current.txt";

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
                // 目錄名稱帶上建置身分：同版號的新建置解壓到新目錄，不會去動還在執行的舊建置
                string embeddedDir = Path.Combine(baseDir, RuntimeDirPrefix + AppVersion + "-" + GetBuildId());
                string identity = GetPayloadIdentity();

                string runtimeDir = FindUpdatedRuntime(baseDir);
                if (runtimeDir == null)
                {
                    runtimeDir = embeddedDir;
                    EnsureRuntime(runtimeDir, Path.Combine(runtimeDir, ExeFileName), identity, false);
                }
                string saveDir = EnsureSaveDirectory();
                string updateUrl = GetUpdateUrl();
                StartCleanup(baseDir, Path.GetFileName(runtimeDir));

                long elapsedMs;
                int exitCode = LaunchProcess(runtimeDir, saveDir, updateUrl, args, out elapsedMs);

                // 啟動後立即異常結束，可能是快取檔案被刪除或損毀：詢問是否修復並重試一次
                if (exitCode != 0 && exitCode != UpdateExitCode && elapsedMs < QuickFailThresholdMs)
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
                        if (runtimeDir != embeddedDir)
                        {
                            // 壞掉的是遊戲內更新的版本：退回這個 exe 內嵌的版本，回到標題畫面時會再提示更新
                            TryDeleteFile(Path.Combine(baseDir, CurrentFileName));
                            runtimeDir = embeddedDir;
                        }
                        EnsureRuntime(runtimeDir, Path.Combine(runtimeDir, ExeFileName), identity, true);
                        exitCode = LaunchProcess(runtimeDir, saveDir, updateUrl, args, out elapsedMs);
                    }
                }

                // 更新失敗也要開回原本的版本，玩家才不會按了「更新」遊戲就消失
                while (exitCode == UpdateExitCode)
                {
                    string updated = TryUpdate(baseDir, runtimeDir, updateUrl);
                    if (updated != null)
                    {
                        runtimeDir = updated;
                        StartCleanup(baseDir, Path.GetFileName(runtimeDir));
                    }
                    exitCode = LaunchProcess(runtimeDir, saveDir, updateUrl, args, out elapsedMs);
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

        /// <summary>
        /// 取 MVID 前 8 碼當作快取目錄名稱的一部分，每次建置都不同。
        /// </summary>
        private static string GetBuildId()
        {
            return Assembly.GetExecutingAssembly().ManifestModule.ModuleVersionId.ToString("N").Substring(0, 8);
        }

        /// <summary>
        /// 遊戲內更新裝好的版本比這個 exe 內嵌的新、檔案也完整時，回傳它的目錄；否則回傳 null，改用內嵌的版本。
        /// 玩家後來下載了更新的 exe 時，內嵌的比較新，就不會被舊的更新蓋過。
        /// </summary>
        private static string FindUpdatedRuntime(string baseDir)
        {
            try
            {
                string pointer = Path.Combine(baseDir, CurrentFileName);
                if (!File.Exists(pointer))
                {
                    return null;
                }

                string[] fields = File.ReadAllText(pointer, Encoding.UTF8).Trim().Split('\t');
                if (fields.Length < 2 || !IsNewer(fields[0], AppVersion))
                {
                    return null;
                }

                string dir = Path.Combine(baseDir, fields[1]);
                if (!IsRuntimeReady(dir, Path.Combine(dir, ExeFileName), fields[1]))
                {
                    return null;
                }

                Log("開啟遊戲內更新的版本：" + fields[1]);
                return dir;
            }
            catch
            {
                // 指標檔讀不懂就當作沒更新過，照常開內嵌的版本
                return null;
            }
        }

        private static string GetUpdateUrl()
        {
            string url = Environment.GetEnvironmentVariable(EnvUpdateUrl);
            return string.IsNullOrEmpty(url) ? UpdateUrl : url;
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

            RunWithProgress("首次啟動，正在準備遊戲檔案…", delegate(Action<int, string> report)
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
                report(-1, "正在等待其他啟動程序完成準備…");
                WaitForMutex(mutex, "等待其他啟動程序釋放解壓鎖逾時。");

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
                            // 先整個改名再刪；同一份遊戲還開著時改名會失敗，就不會刪到一半
                            string trash;
                            if (!TryMoveAway(runtimeDir, out trash))
                            {
                                throw new InvalidOperationException("JavaCampus 正在執行中，請先關閉所有遊戲視窗後再試。");
                            }
                            TryDeleteDirectory(trash);
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

            using (Stream payloadStream = GetPayloadResourceStream())
            using (ZipArchive archive = new ZipArchive(payloadStream, ZipArchiveMode.Read))
            {
                ExtractEntries(archive, destDir, TrackBytes(TotalLength(archive), report));
            }

            if (!File.Exists(Path.Combine(destDir, ExeFileName)))
            {
                throw new FileNotFoundException("解壓縮封裝包後未找到主執行檔：" + ExeFileName);
            }

            // 標記檔最後才寫入；且整個目錄是在暫存區完成後才改名，因此對外永遠是完整狀態
            WriteReadyMarker(destDir, identity);
        }

        /// <summary>
        /// 把 zip 裡的檔案逐一寫到 destDir 底下，每寫一段就把位元組數交給 progress。內嵌封裝和遊戲內更新共用。
        /// </summary>
        private static void ExtractEntries(ZipArchive archive, string destDir, Action<long> progress)
        {
            string root = Path.GetFullPath(destDir);
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

                using (Stream entryStream = entry.Open())
                using (FileStream fileStream = new FileStream(destinationPath, FileMode.Create, FileAccess.Write, FileShare.None))
                {
                    int read;
                    while ((read = entryStream.Read(buffer, 0, buffer.Length)) > 0)
                    {
                        fileStream.Write(buffer, 0, read);
                        progress(read);
                    }
                }
            }
        }

        private static long TotalLength(ZipArchive archive)
        {
            long total = 0;
            foreach (ZipArchiveEntry e in archive.Entries)
            {
                total += e.Length;
            }
            return total;
        }

        /// <summary>
        /// 把陸續完成的位元組數換成百分比交給進度視窗；百分比有變才回報，不必每 80 KB 就排一次畫面更新。
        /// </summary>
        private static Action<long> TrackBytes(long totalBytes, Action<int, string> report)
        {
            long doneBytes = 0;
            int lastPercent = -1;
            return delegate(long bytes)
            {
                doneBytes += bytes;
                if (totalBytes <= 0)
                {
                    return;
                }

                int percent = (int)(doneBytes * 100 / totalBytes);
                if (percent != lastPercent)
                {
                    lastPercent = percent;
                    report(percent, null);
                }
            };
        }

        /// <summary>
        /// 寫入就緒標記檔：第一行是身分，之後每行是一個檔案的「大小\t相對路徑」，啟動時只比對大小以維持秒開。
        /// </summary>
        private static void WriteReadyMarker(string dir, string identity)
        {
            List<string> lines = new List<string>();
            lines.Add(identity);

            string root = Path.GetFullPath(dir).TrimEnd(Path.DirectorySeparatorChar) + Path.DirectorySeparatorChar;
            foreach (string file in Directory.GetFiles(dir, "*", SearchOption.AllDirectories))
            {
                lines.Add(new FileInfo(file).Length + "\t" + Path.GetFullPath(file).Substring(root.Length));
            }

            File.WriteAllLines(Path.Combine(dir, ReadyMarkerFileName), lines.ToArray(), Encoding.UTF8);
        }

        /// <summary>
        /// 等待具名 Mutex 最多五分鐘。
        /// </summary>
        private static void WaitForMutex(Mutex mutex, string timeoutMessage)
        {
            bool acquired;
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
                throw new TimeoutException(timeoutMessage);
            }
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

        /// <summary>
        /// 玩家在標題畫面按了「更新」：在進度視窗裡下載並安裝，成功時回傳新版本的目錄。
        /// 失敗或其實沒有新版時回傳 null，由呼叫端開回原本的版本。
        /// </summary>
        private static string TryUpdate(string baseDir, string currentDir, string updateUrl)
        {
            Log("玩家要求更新。");
            string updated = null;
            try
            {
                RunWithProgress("正在檢查新版本…", delegate(Action<int, string> report)
                {
                    updated = InstallUpdate(baseDir, currentDir, updateUrl, report);
                });
            }
            catch (Exception ex)
            {
                Log("更新失敗：" + ex);

                MessageBox.Show(
                    "更新失敗：\n\n" + DescribeError(ex) + "\n\n先開啟目前的版本。",
                    "JavaCampus",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Warning);

                return null;
            }
            return updated;
        }

        /// <summary>
        /// 只下載內容雜湊跟目前版本不同的部分，其餘從目前版本複製；在暫存目錄組成完整的新版本後才改名就位，最後寫入指標檔。
        /// </summary>
        private static string InstallUpdate(string baseDir, string currentDir, string updateUrl, Action<int, string> report)
        {
            using (Mutex mutex = new Mutex(false, @"Local\JavaCampus.Update"))
            {
                WaitForMutex(mutex, "等待其他啟動程序完成更新逾時。");

                try
                {
                    // csc 直接編、沒指定目標框架的程式，.NET Framework 預設只開 SSL3 和 TLS 1.0（實測），GitHub 會拒絕連線
                    ServicePointManager.SecurityProtocol = SecurityProtocolType.Tls12;

                    byte[] manifestBytes = DownloadBytes(updateUrl);
                    string format;
                    string version;
                    List<UpdatePart> parts = ParseManifest(Encoding.UTF8.GetString(manifestBytes), out format, out version);
                    if (format != ManifestFormat)
                    {
                        throw new NotSupportedException("這次的更新需要新版的啟動器，請到 GitHub 重新下載最新版的 JavaCampus。");
                    }
                    if (version == null || parts.Count == 0)
                    {
                        throw new IOException("更新資訊不完整，請稍後再試。");
                    }

                    List<UpdatePart> installed = new List<UpdatePart>();
                    string installedVersion = null;
                    string installedManifest = Path.Combine(currentDir, ManifestFileName);
                    if (File.Exists(installedManifest))
                    {
                        string installedFormat;
                        installed = ParseManifest(File.ReadAllText(installedManifest, Encoding.UTF8), out installedFormat, out installedVersion);
                    }
                    if (installedVersion == null)
                    {
                        installedVersion = AppVersion;
                    }
                    if (!IsNewer(version, installedVersion))
                    {
                        Log("目前的 " + installedVersion + " 已經是最新版（網路上是 " + version + "）。");
                        return null;
                    }

                    string targetName;
                    using (SHA256 sha = SHA256.Create())
                    {
                        // 同一份 update.txt 對到同一個目錄：另一個視窗剛裝好的話就不用再裝一次
                        targetName = RuntimeDirPrefix + version + "-" + ToHex(sha.ComputeHash(manifestBytes)).Substring(0, 8);
                    }
                    string targetDir = Path.Combine(baseDir, targetName);
                    if (!IsRuntimeReady(targetDir, Path.Combine(targetDir, ExeFileName), targetName))
                    {
                        BuildUpdatedRuntime(currentDir, targetDir, targetName, manifestBytes, parts, installed, report);
                    }

                    File.WriteAllText(Path.Combine(baseDir, CurrentFileName), version + "\t" + targetName, Encoding.UTF8);
                    return targetDir;
                }
                finally
                {
                    mutex.ReleaseMutex();
                }
            }
        }

        private static void BuildUpdatedRuntime(string currentDir, string targetDir, string targetName, byte[] manifestBytes,
            List<UpdatePart> parts, List<UpdatePart> installed, Action<int, string> report)
        {
            Dictionary<string, string> installedHashes = new Dictionary<string, string>();
            foreach (UpdatePart p in installed)
            {
                installedHashes[p.Name] = p.Hash;
            }

            List<UpdatePart> changed = new List<UpdatePart>();
            long downloadBytes = 0;
            foreach (UpdatePart p in parts)
            {
                string hash;
                if (!installedHashes.TryGetValue(p.Name, out hash) || hash != p.Hash)
                {
                    changed.Add(p);
                    downloadBytes += p.Size;
                }
            }

            string tempDir = targetDir + TempDirInfix + Guid.NewGuid().ToString("N");
            // 下載的 zip 放在版本目錄外面；名稱裡有 .tmp-，萬一沒刪掉 StartCleanup 一天後會清
            string zipDir = tempDir + "-zips";
            try
            {
                Directory.CreateDirectory(tempDir);
                Directory.CreateDirectory(zipDir);

                report(0, "正在下載新版本…");
                Action<long> downloaded = TrackBytes(downloadBytes, report);
                foreach (UpdatePart p in changed)
                {
                    DownloadPart(p, Path.Combine(zipDir, p.Name + ".zip"), downloaded);
                }

                report(0, "正在套用更新…");
                List<string> unchangedFiles = ListUnchangedFiles(currentDir, parts, changed);
                long applyBytes = 0;
                foreach (string rel in unchangedFiles)
                {
                    applyBytes += new FileInfo(Path.Combine(currentDir, rel)).Length;
                }
                foreach (UpdatePart p in changed)
                {
                    using (ZipArchive archive = ZipFile.OpenRead(Path.Combine(zipDir, p.Name + ".zip")))
                    {
                        applyBytes += TotalLength(archive);
                    }
                }

                Action<long> applied = TrackBytes(applyBytes, report);
                foreach (string rel in unchangedFiles)
                {
                    string dest = Path.Combine(tempDir, rel);
                    Directory.CreateDirectory(Path.GetDirectoryName(dest));
                    File.Copy(Path.Combine(currentDir, rel), dest);
                    applied(new FileInfo(dest).Length);
                }
                foreach (UpdatePart p in changed)
                {
                    using (ZipArchive archive = ZipFile.OpenRead(Path.Combine(zipDir, p.Name + ".zip")))
                    {
                        ExtractEntries(archive, tempDir, applied);
                    }
                }

                File.WriteAllBytes(Path.Combine(tempDir, ManifestFileName), manifestBytes);
                if (!File.Exists(Path.Combine(tempDir, ExeFileName)))
                {
                    throw new IOException("更新內容缺少主執行檔，請稍後再試。");
                }
                WriteReadyMarker(tempDir, targetName);

                if (Directory.Exists(targetDir))
                {
                    // 同名但不完整的目錄先整個改名再刪；那份遊戲還開著時改名會失敗，就不會刪到一半
                    string trash;
                    if (!TryMoveAway(targetDir, out trash))
                    {
                        throw new InvalidOperationException("JavaCampus 正在執行中，請先關閉所有遊戲視窗後再試。");
                    }
                    TryDeleteDirectory(trash);
                }

                Directory.Move(tempDir, targetDir);
            }
            catch
            {
                TryDeleteDirectory(tempDir);
                throw;
            }
            finally
            {
                TryDeleteDirectory(zipDir);
            }

            Log("已更新到 " + targetName + "，下載了 " +
                string.Join("、", changed.ConvertAll(delegate(UpdatePart p) { return p.Name; })) +
                "（" + downloadBytes + " bytes）。");
        }

        /// <summary>
        /// 目前版本裡屬於沒變那幾塊的檔案（相對路徑）。照新 update.txt 的前綴分類、最長的優先，和打包時的切法一致；
        /// update.txt 和就緒標記檔每個版本各寫各的，不複製。
        /// </summary>
        private static List<string> ListUnchangedFiles(string dir, List<UpdatePart> parts, List<UpdatePart> changed)
        {
            List<string> files = new List<string>();
            string root = Path.GetFullPath(dir).TrimEnd(Path.DirectorySeparatorChar) + Path.DirectorySeparatorChar;
            foreach (string file in Directory.GetFiles(dir, "*", SearchOption.AllDirectories))
            {
                string rel = Path.GetFullPath(file).Substring(root.Length);
                if (rel == ReadyMarkerFileName || rel == ManifestFileName)
                {
                    continue;
                }

                UpdatePart owner = PartOf(rel.Replace('\\', '/'), parts);
                if (owner != null && !changed.Contains(owner))
                {
                    files.Add(rel);
                }
            }
            return files;
        }

        private static UpdatePart PartOf(string path, List<UpdatePart> parts)
        {
            UpdatePart best = null;
            foreach (UpdatePart p in parts)
            {
                if (path.StartsWith(p.Prefix, StringComparison.OrdinalIgnoreCase) &&
                    (best == null || p.Prefix.Length > best.Prefix.Length))
                {
                    best = p;
                }
            }
            return best;
        }

        /// <summary>
        /// update.txt 裡的一塊：版本目錄裡哪個前綴底下的檔案、內容雜湊，以及下載用的 zip。
        /// </summary>
        private sealed class UpdatePart
        {
            public string Name;
            public string Prefix;
            public string Hash;
            public long Size;
            public string Sha256;
            public string Url;
        }

        /// <summary>
        /// 解析 update.txt。不認得的行和多出來的欄位都略過，之後的版本才能加東西又不弄壞舊啟動器。
        /// </summary>
        private static List<UpdatePart> ParseManifest(string text, out string format, out string version)
        {
            format = null;
            version = null;
            List<UpdatePart> parts = new List<UpdatePart>();

            foreach (string line in text.TrimStart('﻿').Split(new char[] { '\r', '\n' }, StringSplitOptions.RemoveEmptyEntries))
            {
                string[] fields = line.Split('\t');
                if (fields[0] == "format" && fields.Length >= 2)
                {
                    format = fields[1];
                }
                else if (fields[0] == "version" && fields.Length >= 2)
                {
                    version = fields[1];
                }
                else if (fields[0] == "part")
                {
                    long size;
                    if (fields.Length < 7 || !long.TryParse(fields[4], out size))
                    {
                        throw new IOException("更新資訊不完整，請稍後再試。");
                    }

                    UpdatePart part = new UpdatePart();
                    part.Name = fields[1];
                    part.Prefix = fields[2];
                    part.Hash = fields[3];
                    part.Size = size;
                    part.Sha256 = fields[5];
                    part.Url = fields[6];
                    parts.Add(part);
                }
            }

            return parts;
        }

        private static HttpWebRequest CreateRequest(string url)
        {
            HttpWebRequest request = (HttpWebRequest)WebRequest.Create(url);
            request.UserAgent = "JavaCampus/" + AppVersion;
            // Timeout 只管等到回應開頭；大檔案下載中途卡住，靠 ReadWriteTimeout 限制每一次讀取
            request.Timeout = 30000;
            request.ReadWriteTimeout = 30000;
            return request;
        }

        private static byte[] DownloadBytes(string url)
        {
            using (WebResponse response = CreateRequest(url).GetResponse())
            using (Stream input = response.GetResponseStream())
            using (MemoryStream output = new MemoryStream())
            {
                input.CopyTo(output);
                return output.ToArray();
            }
        }

        /// <summary>
        /// 下載一塊更新到 path，邊寫邊算 SHA-256；大小或雜湊跟 update.txt 不同就中止。
        /// </summary>
        private static void DownloadPart(UpdatePart part, string path, Action<long> progress)
        {
            using (WebResponse response = CreateRequest(part.Url).GetResponse())
            using (Stream input = response.GetResponseStream())
            using (FileStream output = new FileStream(path, FileMode.Create, FileAccess.Write, FileShare.None))
            using (SHA256 sha = SHA256.Create())
            {
                byte[] buffer = new byte[81920];
                long total = 0;
                int read;
                while ((read = input.Read(buffer, 0, buffer.Length)) > 0)
                {
                    total += read;
                    if (total > part.Size)
                    {
                        throw new IOException("下載的更新檔不完整，請稍後再試。");
                    }

                    output.Write(buffer, 0, read);
                    sha.TransformBlock(buffer, 0, read, null, 0);
                    progress(read);
                }
                sha.TransformFinalBlock(buffer, 0, 0);

                if (total != part.Size || !string.Equals(ToHex(sha.Hash), part.Sha256, StringComparison.OrdinalIgnoreCase))
                {
                    throw new IOException("下載的更新檔不完整，請稍後再試。");
                }
            }
        }

        private static string ToHex(byte[] bytes)
        {
            return BitConverter.ToString(bytes).Replace("-", "").ToLowerInvariant();
        }

        private static bool IsNewer(string candidate, string current)
        {
            Version a;
            Version b;
            return Version.TryParse(candidate, out a) && Version.TryParse(current, out b) && a > b;
        }

        // ------------------------------------------------------------------
        // 進度視窗
        // ------------------------------------------------------------------

        /// <summary>
        /// 顯示進度視窗並在背景執行緒執行工作；工作中的例外會於 UI 結束後原樣重新擲出。
        /// </summary>
        private static void RunWithProgress(string status, Action<Action<int, string>> work)
        {
            Exception error = null;

            using (ProgressForm form = new ProgressForm(status))
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

            public ProgressForm(string status)
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
                statusLabel.Text = status;

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

                        // 舊版本可能還開著，改名失敗就留到下次再清
                        string trash;
                        if (TryMoveAway(dir, out trash))
                        {
                            TryDeleteDirectory(trash);
                        }
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

        /// <summary>
        /// 把目錄改成暫存名稱。裡面有檔案正被執行中的遊戲使用時 Windows 不允許改名，回傳 false。
        /// </summary>
        private static bool TryMoveAway(string dir, out string trash)
        {
            trash = dir + TempDirInfix + Guid.NewGuid().ToString("N");
            try
            {
                Directory.Move(dir, trash);
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

        private static void TryDeleteFile(string path)
        {
            try
            {
                File.Delete(path);
            }
            catch
            {
            }
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
        private static int LaunchProcess(string runtimeDir, string saveDir, string updateUrl, string[] args, out long elapsedMs)
        {
            ProcessStartInfo startInfo = new ProcessStartInfo();
            startInfo.FileName = Path.Combine(runtimeDir, ExeFileName);
            startInfo.WorkingDirectory = runtimeDir;
            startInfo.Arguments = FormatArguments(args);
            startInfo.UseShellExecute = false;
            startInfo.EnvironmentVariables[EnvSaveDir] = saveDir;
            startInfo.EnvironmentVariables[EnvLauncherDir] = Path.GetDirectoryName(Application.ExecutablePath);
            startInfo.EnvironmentVariables[EnvUpdateUrl] = updateUrl;

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

            if (ex is WebException)
            {
                return "連不上更新伺服器，請確認網路連線後再試。";
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