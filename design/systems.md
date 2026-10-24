# 系統設計：劇本執行、數值、結局、存檔

## 內容檔案的位置

| 內容 | 位置 | 格式 |
|---|---|---|
| 劇本 | `story/scripts/chXX/` | 使用者原本的劇本格式加上演出指令，見 `story/scripts/README.md` |
| 文件（PDF、README、紙條⋯⋯） | `story/scripts/docs/<id>.md` | 第一行寫外觀，其餘是簡單的 Markdown |
| 章節清單 | `story/scripts/chapters.json` | 章號、章名、劇本資料夾、專題進度 |
| 程式題 | `JavaCampus/lessons/<id>/` | 現有格式：`lesson.json` 加上每一步的 `.solution.jsh`、`.skeleton.jsh`、`.starter.jsh` |
| 筆記條目 | `JavaCampus/lessons/handbook.json` | 條目 id、章、標題、內文 |
| 搭檔反應台詞 | `JavaCampus/lessons/reactions.json` | 依搭檔和結果分組 |
| 圖檔（之後才有） | `JavaCampus/art/` | 見 `design/visuals.md` |

劇本放在 `story/`，是為了讓你繼續在原本的資料夾寫劇本。遊戲啟動時從 `JavaCampus` 往上一層讀 `story/scripts/`。

## 劇本執行

### 檔案的分工
- `chXX-common.md`：共通場景庫，每個場景用 `[場景名稱]` 開頭。
- `chXX-senpai.md`、`chXX-kouhai.md`：各視角**從頭到尾的播放順序**。用 `@共通 [場景名稱]` 插入共通場景，獨有場景直接寫在檔案裡。
- 共通場景裡如果有只屬於某個視角的內容（內心 OS、看到的細節），用 `@只限 學長` … `@結束` 包起來。

### 執行模型
- `ScriptParser` 把一章的三個檔案讀成 `Script`，裡面是場景，場景裡是一行一行的指令（台詞、旁白、選項、演出指令）。解析錯誤會標明檔名和行號，例如「ch01-common.md 第 42 行：找不到場景 [留下的選擇]」。
- `StoryRunner` 依序執行：
  - 台詞、旁白：交給畫面顯示，等玩家前進。
  - 演出指令（背景、音樂、登場⋯⋯）：立刻執行，接著跑下一行。
  - 會擋住的指令：選項、`@手機` 的訊息、`@文件`、`@程式`、`@章末`。這些要等玩家操作完才繼續。
  - `@共通`：跳進共通場景，播完回到原處。
  - `@跳到`、選項的 `→ [場景]`：跳到那個場景，不回來。
- 位置用「檔案、場景、行號」記錄，存檔和讀檔都用這個。

## 數值

依 `story/outline/branching.md`：

- AP（好感度）、TP（信任值）從 10 開始，限制在 0～100。
- 選項後面寫效果：`AP+8 TP+5`。
- 關鍵抉擇用 `★` 標記，選到的那一邊會記一個關鍵旗標。第一章的關鍵抉擇大綱寫「任一選項均不影響」，所以兩邊都不加 ★。
- 數值不顯示，只在選完時飄出愛心（AP）和小鎖頭（TP）。加得越多圖示越大：
  - 小：加 1～5
  - 中：加 6～10
  - 大：加 11 以上
- 寫程式的表現不影響數值。

### 日常對話選擇
大綱的選擇表只列了行動選擇和關鍵抉擇。`branching.md` 寫每章另有 2～3 次日常對話選擇（AP、TP 各 +3～+8），這些要寫進劇本。第一章劇本會先加 1～2 個，寫完列給你看。

## 結局判定

`Endings` 依序檢查，符合第一個就是那個結局：

| 順序 | 結局 | 條件 |
|---|---|---|
| 1 | True End `1+1>2` | AP≥80、TP≥80、關鍵旗標 ≥2 |
| 2 | Good End `Merge Request Approved` | AP≥60、TP≥60 |
| 3 | Normal End `Pull Request Pending` | AP≥40 或 TP≥40 |
| 4 | Comedy Bad End `Stack Overflow` | 其他（AP<40 且 TP<40） |

- 第五章之後沒有新選擇，所以在第六章「最後的電腦教室夜晚」判定一次，劇本用 `@如果 結局=True → [場景]` 分流，第七章沿用同一個結果。
- 已經玩過的結局會記錄下來（每個視角各四個），給之後的結局回顧用。結局回顧畫面等做到第七章再做。

> **待確認：** `branching.md` 的數值模擬寫「關鍵旗標：3/3 ✓」，但第一章的關鍵抉擇不設旗標，所以實際上最多只有 2 個（第三、四章）。True End 的條件「≥2」等於「第三、四章都選 A」，和 `branching.md` 那一句的說明一致，只是模擬表的 3/3 應該是 2/2。遊戲依「第三、四章都選 A」實作。

## 視角

- `Route` 有兩個值：學長（`SENPAI`）、學妹（`KOUHAI`）。
- 兩個視角的劇情進度、數值、旗標**各自獨立**。
- 已讀台詞和筆記解鎖**兩個視角共用**：共通場景在一個視角讀過，另一個視角也能快進；同一份 PDF 本來就是同一份筆記。

## 存檔

沿用 `SaveStore`：寫到 `save/progress.json`，先寫暫存檔再原子搬移，壞掉的存檔改名成 `.broken`。

```json
{
  "lastRoute": "KOUHAI",
  "routes": {
    "KOUHAI": {
      "chapter": "ch01",
      "at": { "file": "ch01-kouhai.md", "scene": "轉科的念頭", "line": 12 },
      "returnStack": [],
      "ap": 18, "tp": 15,
      "flags": [],
      "endings": [],
      "chapterStarts": { "ch01": { "ap": 10, "tp": 10, "flags": [] } },
      "cleared": ["ch01"]
    }
  },
  "read": ["ch01-common.md#抽籤現場#3", "…"],
  "notes": ["println", "quotes", "…"],
  "lessons": { "ch01-k1": { "solved": ["say"], "hints": {}, "code": {} } },
  "settings": { "…": "…" }
}
```

### 自動存檔時機
- 每個場景開始時
- 每次做完選擇後
- 進入程式題、完成程式題時
- 章末畫面出現時

### 繼續與重玩
- 「繼續」載入 `lastRoute` 的位置。
- 「新遊戲」選了一個已經有存檔的視角時，先確認，再清掉那個視角的進度。另一個視角的進度、已讀台詞、筆記、題目都保留。
- 「章節選擇」只列出那個視角玩過的章節。重玩第 N 章：數值和旗標還原成 `chapterStarts` 裡第 N 章開頭的樣子，之後的進度以這次為準。
- 已經做過的程式題在重玩時直接是完成狀態，按「繼續」就能往下。

## 程式題與劇情的銜接

- 劇本寫 `@程式 ch01-k2`，畫面就切到 `LessonView`。過關後按「繼續」，回到劇情的下一行。
- `lesson.json` 寫明 `partner`（誰在旁邊）和 `notes`（過關時解鎖哪些筆記條目）。
- 沿用現有題目格式的檢查（`LessonContentTest`）：參考解答要通過、起始碼要失敗、骨架要有 `____`。

## 測試

| 測試 | 內容 |
|---|---|
| `SandboxTest`、`VerifierTest`、`ProgramModeTest` | 沿用 |
| `LessonContentTest` | 由 `CaseContentTest` 改名 |
| `ScriptParserTest` | 每種語法、錯誤時的行號 |
| `ScriptContentTest` | 所有劇本都能解析；跳轉的場景都存在；`@程式` 的題目都存在；背景和角色名稱都有繪製器；兩個視角都能走到 `@章末`；窮舉所有選項組合算出 AP/TP 範圍 |
| `EndingsTest` | 各結局門檻的邊界值 |
| `StoryStateTest` | 存檔讀檔、重玩章節時還原開頭的數值 |
