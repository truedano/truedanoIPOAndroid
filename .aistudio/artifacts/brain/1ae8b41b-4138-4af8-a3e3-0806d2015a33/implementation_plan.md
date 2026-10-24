# 台股 IPO 競拍蜜月行情預判決策引擎 (TWSE IPO Decision Engine)

將原有的 Python「台股 IPO 競拍蜜月行情預判決策引擎」重構成原生 Android 原生金融決策工具。基於台灣證交所（TWSE）真實法人得標籌碼數據，強制對齊台股 6 檔升降單位（Tick Size），破除散戶興櫃定錨幻覺，並提供三段出場情境與安全邊際試算。

## User Review & Critical Decisions

> [!IMPORTANT]
> 請確認以下已對齊的核心決策與預設邏輯。若確認無誤，請點擊「Proceed」以依序啟動階段 1 至階段 4 的完整程式碼建置。

- **經典歷史案例內建**：內建 7825 和亞、7856 漢測、7734 巧新等實戰經典案例，支援一鍵切換展示、即時推演與離線驗證，同時支援 TWSE 開標 API 自動連線拉取與使用者自訂手動覆寫。
- **純抽籤戶與多張數/折讓計算**：支援單張（成本 70 元規費）與多張進階自訂試算，並提供券商手續費折讓（例如 2.8 折、5 折、6 折、全額）彈性切換，精準計算證券交易稅 0.3% 與賣出手續費 0.1425%。
- **視覺風格**：採用 Bloomberg / 現代專業量化金融深色終端風格（深墨底 `#0B0E14`、警示紅與安全綠、籌碼定錨階梯色彩標識），支援流暢 Material 3 響應式佈局。

---

## 1. Overview & Core Concept

- **What It Does**: 提供台股上市櫃 IPO 競拍開標後，散戶與機構投資人進行「蜜月行情推演」與「中籤賣出策略預判」的量化決策平台。透過投標倍數熱度、安全邊際倒掛檢驗、三段價格廊道與興櫃泡沫辨識，提供客觀科學的進出場依據。
- **Target Audience / Persona**: 台股抽籤族、競拍投標機構法人、價值投資者與興櫃轉上市櫃交易者。
- **Key Value**: 告別憑感覺抱股或盲信興櫃價格的盲點，以開標真金白銀的機構法人成本作為定錨支撐，提供明確的出場停利防守策略。

---

## 2. User Experience & Visual Design

### Key User Flows
1. **首頁數據看板 (Dashboard & Analysis)**：
   - 頂部：股票代號/名稱快速選擇器（和亞 7825、漢測 7856、即時查詢或自訂輸入），提供開標資訊卡片。
   - 核心指標矩陣：合格投標倍數（熱度標籤：燙手/中性/偏冷）、安全邊際數值與警示狀態（安全/危險倒掛）、黃金公式（符合/不符合徽章）。
   - 籌碼定錨階梯圖（Visual Price Corridor）：視覺化標示下檔支撐點（最低得標價）、多空中樞價（得標加權均價）、上檔壓力點（最高得標價）、興櫃泡沫警示線與公開申購價。
2. **純抽籤戶實戰指南 (Exit Scenarios & Profit Simulator)**：
   - 準備本金：申購價 × 1000 + 70 元規費（支援張數與折讓微調）。
   - 三段出場情境試算（保守 / 基準 / 樂觀）：清晰呈現預估賣出價（強制對齊 Tick Size）、扣除稅費後實拿淨利潤、以及精確預估 ROI %。
3. **歷史開標與收藏清單 (History & Database)**：
   - 檢視歷史標案、搜尋 TWSE 歷年開標標的，支援離線保存與點擊重新推演。
4. **手動參數沙盒 (Parameter Sandbox)**：
   - 自由填入或微調競拍總量、合格投標量、最低/均價/最高得標價、承銷價、興櫃現價，即時反映各項指標變化。

### Visual Identity & Theme
- **Aesthetic Direction**: 專業量化深色金融終端（Quant Financial Terminal），高資訊密度、高可讀性，消除繁複裝飾，以清晰數據階梯為核心。
- **Color Palette**:
  - `Background`: 深邃黑灰 `#0A0D14`
  - `Surface`: 模組卡片深藍灰 `#121824`、`SurfaceVariant`: `#1A2234`
  - `Primary / Accent`: 金融電光青綠 `#00E5FF`、黃金獲利公式金色 `#FFD700`
  - `Status Safe (安全/買進)`: 翠綠 `#00E676`
  - `Status Danger (危險/倒掛/泡沫)`: 熾紅 `#FF5252`
  - `Status Neutral (中性)`: 琥珀金 `#FFB300`
- **Typography**: 清晰等寬金融數字字體樣式，價格檔位對齊視覺節奏。

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: 台股 6 檔 Tick Size 強制對齊計算機**
  - *Chosen Approach*: 在 Domain 層純 Kotlin 實作 `TickSizeAligner`，嚴格遵守台灣證券交易所 6 個級距規則（<10: 0.01、10~50: 0.05、50~100: 0.1、100~500: 0.5、500~1000: 1.0、>=1000: 5.0），支援向下（支撐）與向上（壓力）精確對齊。
  - *Why*: 避免虛假連續小數（如 123.456 元在 100~500 元級距根本無法掛單買賣），符合真實交易市場撮合限制。
- **Decision 2: 離線優先 (Offline-First) + TWSE 爬蟲 + 歷史快取**
  - *Chosen Approach*: 整合 Room Database，預載經典驗證案例（7825 和亞、7856 漢測等完整欄位），即使無網路或證交所 API 伺服器臨時維護亦能 100% 正常運作。連網時透過 Retrofit 查詢 TWSE 最新拍賣資料並解析。
  - *Why*: 保證金融工具的高可用性與即時計算反應，使用者零延遲獲得決策分析。
- **Decision 3: 領域邏輯零 Android 依賴 (Pure Clean Architecture)**
  - *Chosen Approach*: `domain` 層僅包含純 Kotlin Data Classes、Calculate UseCases 與評級矩陣 Rules，可直接以 JVM Unit Test 進行 100% 邏輯覆蓋率測試。

---

## 4. Technical Architecture & Data Strategy

### Architecture & Component Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Presentation Layer (Jetpack Compose)            │
│  ┌───────────────────────┐  ┌───────────────────────┐  ┌─────────────┐ │
│  │ IpoDashboardScreen    │  │ ExitScenarioCards     │  │ SandboxView │ │
│  │ - PriceCorridorLadder │  │ - Conservative / Base │  │ - Inputs    │ │
│  │ - GoldenFormulaBadge  │  │   / Optimistic ROI    │  │ - Overrides │ │
│  └───────────┬───────────┘  └───────────┬───────────┘  └──────┬──────┘ │
│              └──────────────────┐       │       ┌─────────────┘        │
│                                 ▼       ▼       ▼                      │
│                        ┌────────────────────────────────┐              │
│                        │       IpoDecisionViewModel     │              │
│                        │ (StateFlow<IpoUiState>, Events)│              │
│                        └───────────────┬────────────────┘              │
└────────────────────────────────────────┼───────────────────────────────┘
                                         ▼
┌────────────────────────────────────────────────────────────────────────┐
│                          Domain Layer (Pure Kotlin)                    │
│  ┌─────────────────────────┐  ┌──────────────────────────────────────┐ │
│  │ Entities & Value Objects│  │ Decision Engines & Use Cases         │ │
│  │ - IpoAuctionRecord      │  │ - TickSizeAligner (6-tier rules)     │ │
│  │ - MarketHeatEvaluation  │  │ - SafetyMarginCalculator             │ │
│  │ - PriceCorridor         │  │ - GoldenFormulaEvaluator             │ │
│  │ - ExitScenarioResult    │  │ - ExitProfitCalculator (Tax & Fees)  │ │
│  └─────────────────────────┘  └──────────────────────────────────────┘ │
│                                        ▲                               │
└────────────────────────────────────────┼───────────────────────────────┘
                                         │
┌────────────────────────────────────────┴───────────────────────────────┐
│                           Data Layer (Room + Retrofit)                 │
│  ┌───────────────────────────┐         ┌─────────────────────────────┐ │
│  │ Local DataSource (Room)   │         │ Remote DataSource (Retrofit)│ │
│  │ - IpoAuctionDao           │◄───┐    │ - TwseAuctionApiService     │ │
│  │ - IpoAuctionEntity        │    │    │ - TwseResponseParser        │ │
│  │ - IpoDatabase             │    │    └──────────────┬──────────────┘ │
│  └─────────────┬─────────────┘    │                   │                │
│                └──────────────────┼───────────────────┘                │
│                                   │                                    │
│                 ┌─────────────────┴───────────────────┐                │
│                 │      IpoAuctionRepositoryImpl       │                │
│                 └─────────────────────────────────────┘                │
└────────────────────────────────────────────────────────────────────────┘
```

### 交付階段執行路徑
1. **階段 1**：配置 `build.gradle.kts`，實作 `domain` 核心層（`TickSizeAligner`、`IpoAuctionRecord`、`EvaluationRules`、`ExitProfitCalculator`），並撰寫單元測試驗證邏輯與 6 檔升降單位對齊。
2. **階段 2**：`data` 層建置（TWSE API 介接、HTML/JSON 解析容錯器、Room Entity/Dao/Database、預置歷史標案與 Repository 實作）。
3. **階段 3**：`presentation/ui` 層建置（`IpoDecisionViewModel`、籌碼定錨階梯階梯視覺化 `PriceCorridorLadder`、三段出場卡片 `ExitScenarioCards`、主畫面與參數沙盒）。
4. **階段 4**：整合驗證與案例測試（自動驗證 7825 和亞、7856 漢測測試案例，確認符合真實得標數據與預期推演結果）。
