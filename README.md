# 台股 IPO 競拍與 SPO 現增抽籤決策系統 (truedanoIPOAndroid)

> **基於 TWSE 真實法人開標與公開申購數據，並嚴格對齊台股 6 檔升降單位（Tick Size）的量化預判與投資決策引擎。**

---

## 📌 專案定位與核心價值

在台灣股市中，初次上市櫃（IPO）競價拍賣與現金增資（SPO）公開申購蘊含顯著的折價紅利與蜜月行情空間，但投資人常面臨以下痛點：
1. **競拍定價無依據**：盲目跟隨興櫃未上市價格出價，易落入流動性折價或散戶溢價泡沫陷阱。
2. **升降單位未對齊**：投標或出場未嚴格遵循證交所規定的 6 檔升降單位（Tick Size），導致委託無效或滑價。
3. **資訊分散且繁雜**：TWSE 開標公告與公開申購日程分屬不同報表，難以即時關聯。
4. **現增折價風險忽視**：未評估申購日至撥券日期間的現股下跌風險，缺乏融券避險鎖利決策支援。

本專案提供一站式、量化驅動的 Android 原生決策工具，透過自動化資料串接、精確數學模型與清晰直觀的視覺儀表板，協助投資人制定最高期望值的投標與申購決策。

---

## 🚀 目前功能現況

專案已完成核心架構重構與 TWSE 即時資料整合，分為兩大主力決策看板：

### 1. 🎯 IPO 競拍蜜月決策看板 (`IpoDashboardScreen`)
* **TWSE 開標即時串接**：自動抓取並解析 TWSE 競價拍賣開標公告資料（`bcode=A`），支援動態欄位容錯匹配。
* **雙表關聯補全**：自動關聯公開申購日程表（`publicForm`），即時補齊各 IPO 標案的公開承銷價與抽籤時程。
* **進行中標案動態篩選**：智慧過濾歷史結案，優先顯示即將開標或進行中的投標標的。
* **市場熱度分級演算法**：依據合格投標張數與競拍總張數之比值（`bidRatio`），自動判定市場熱度（超熱門、熱門、中溫、冷門）。
* **安全邊際評估**：量化比較加權平均得標價與公開承銷價，標記安全邊際層級。
* **真實期待值廊道（6 檔 Tick Size 嚴格對齊）**：
  * **支撐價**：最低得標價（向下取至法定檔位）
  * **樞軸價**：加權平均得標價（最近法定檔位）
  * **壓力價**：最高得標價（向上取至法定檔位）
* **興櫃泡沫檢驗**：自動檢視興櫃現價是否大幅偏離最高得標價，警示散戶過度追價風險。
* **出價教戰守則與黃金出場公式**：
  * 提供「積極型、穩健型、保守型」三段式投標出價建議。
  * 提供掛牌後 T 日（首日開盤）、T+1 日（次日結算）、T+2 日（停利底線）之出場劇本。

### 2. 💰 SPO 現增抽籤決策看板 (`SpoDashboardScreen`)
* **TWSE 公開申購表即時解析**：串接證交所 `publicForm`，解析 18 欄位格式之公開申購日程。
* **普通股智慧篩選**：嚴格過濾非普通股標的，僅鎖定 4 碼普通股之「上市增資 / 上櫃增資」案件。
* **抽籤日過期過濾機制**：自動比對當前日期，排除抽籤日已逾期之舊資料，精準呈現進行中現增標的。
* **現增價差與安全墊評級**：
  * 計算現價與承銷價之折價幅度。
  * 評級安全邊際（極高防禦、標準安全、邊緣風險、高度倒掛）。
* **融券鎖利與套利可行性評估**：
  * 試算融券避險成本（券保證金、借券費、手續費與證券交易稅）。
  * 評估撥券還券的淨鎖利空間與期望報酬率。
* **時程與資金佈局卡片**：
  * 申購起始日、截止日、扣款日、抽籤日、撥券日完整時程視覺化。
  * 準備本金自動計算（包含手續費 20 元與中籤通知工本費 50 元）。

---

## 🏛 系統架構設計

專案嚴格遵循 **Clean Architecture** 與現代 Android 架構指南（MVI / MVVM）：

```
app/src/main/java/com/example/
├── data/                         # 資料層
│   ├── local/                    # Room 本地快取 (Entity, DAO, Database)
│   │   ├── IpoAuctionEntity.kt / IpoAuctionDao.kt / IpoDatabase.kt
│   │   └── SpoLotteryEntity.kt / SpoLotteryDao.kt / SpoDatabase.kt
│   ├── remote/                   # 遠端 API 與解析器
│   │   ├── TwseAuctionApiService.kt / TwseAuctionResponse.kt
│   │   ├── TwseSubscriptionApiService.kt / TwseSubscriptionResponse.kt
│   │   └── TwseParser.kt / TwseSubscriptionParser.kt
│   └── repository/               # Repository 實作 (網路優先 + 本地離線回退)
│       ├── IpoAuctionRepositoryImpl.kt
│       └── SpoLotteryRepositoryImpl.kt
├── di/                           # 依賴注入
│   └── AppContainer.kt           # 手動依賴注入容器 (輕量無額外反射成本)
├── domain/                       # 核心領域層 (純 Kotlin，無 Android 依賴)
│   ├── calculator/               # 演算法與決策引擎
│   │   ├── IpoDecisionEngine.kt  # IPO 競拍決策演算
│   │   ├── SpoDecisionEngine.kt  # 現增抽籤決策演算
│   │   └── ExitProfitCalculator.kt # 損益與稅費計算
│   ├── model/                    # 領域模型與升降單位對齊
│   │   ├── IpoAuctionRecord.kt / IpoEvaluationModels.kt
│   │   ├── SpoLotteryRecord.kt / SpoEvaluationModels.kt
│   │   └── TickSizeAligner.kt    # 台股 6 檔升降檔位對齊核心
│   └── repository/               # 抽象介面定義
├── ui/                           # 表現層 (Jetpack Compose)
│   ├── components/               # 模組化 UI 組件
│   │   ├── DecisionBanner.kt
│   │   ├── PriceLadderAndPlaybookSection.kt
│   │   ├── LotteryExitSection.kt
│   │   ├── StructuredStepCards.kt
│   │   ├── SpoComponents.kt
│   │   └── HistorySection.kt
│   ├── theme/                    # 專業金融級配色主題 (深/淺對比清晰)
│   ├── MainTabScreen.kt          # 雙主分頁導航容器
│   ├── IpoDashboardScreen.kt / IpoDecisionViewModel.kt
│   └── SpoDashboardScreen.kt / SpoDecisionViewModel.kt
└── MainActivity.kt               # 單 Activity 入口
```

---

## 🛠 技術堆疊 (Tech Stack)

| 領域 | 使用技術 |
| :--- | :--- |
| **作業系統 / 平台** | Android 7.0+ (minSdk 24, targetSdk 36, compileSdk 36) |
| **開發語言** | Kotlin 2.x |
| **UI 框架** | Jetpack Compose (BOM 2024+), Material 3 Design |
| **狀態與非同步管理** | Kotlin Coroutines, StateFlow, ViewModel |
| **本地持久化儲存** | Room Database (Room KTX, KSP 代碼生成) |
| **網路通訊與序列化** | Retrofit 2, OkHttp 3, Moshi (Kotlin Reflection / Codegen) |
| **架構模式** | Clean Architecture + MVVM + 單向資料流 (UDF) |
| **版本號管理** | Git Commit 自動動態遞增 (`versionCode` + `versionNameSuffix = "-debug"`) |
| **單元測試** | JUnit 4, Robolectric, Kotlinx Coroutines Test, Compose Test |

---

## 🧪 單元測試覆蓋現況

專案高度注重核心金融演算法與資料解析的正確性，具備完整的單元測試：

1. **升降單位對齊測試** (`TickSizeAlignerTest.kt`)：
   - 完整覆蓋台股 6 檔價格區間（未滿 10 元、10~50 元、50~100 元、100~500 元、500~1000 元、1000 元以上）。
   - 驗證 `alignDown`、`alignUp`、`alignNearest` 的邊界行為與精度。
2. **IPO 決策引擎測試** (`IpoDecisionEngineTest.kt`)：
   - 驗證各投標倍數區間的熱度判定。
   - 驗證安全邊際計算與興櫃溢價泡沫警示觸發條件。
3. **SPO 決策引擎測試** (`SpoDecisionEngineTest.kt`)：
   - 驗證折價幅度、安全墊評級與借券避險損益模型。
4. **TWSE 競拍資料解析測試** (`TwseParserTest.kt`)：
   - 模擬 TWSE 實際回傳之字串陣列格式，驗證欄位匹配與容錯轉型。
5. **TWSE 申購資料解析測試** (`TwseSubscriptionParserTest.kt`)：
   - 驗證普通股過濾、現增代碼比對及抽籤日逾期過濾規則。

---

## 🧭 未來規劃 (Roadmap)

- [ ] **即時股價自動更新**：整合證交所 OpenAPI / 櫃買中心 API，自動填補現股最新收盤價與興櫃成交價。
- [ ] **行事曆與推播提醒**：支援將投標扣款、申購開始/截止、抽籤日一鍵寫入系統行事曆，並加入本地提醒通知。
- [ ] **歷史得標率與報酬追蹤**：支援個人投標紀錄記帳，自動統計歷史得標率與實際實現損益。
- [ ] **深色模式完整優化**：針對夜間盯盤場景提供極致低對比的深色金融配色主題。
