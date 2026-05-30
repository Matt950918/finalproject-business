package game.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 玩家企業核心資料模型
 * 整合財務流水帳、台股漲跌機制、以及大富翁隨機事件狀態 Buff 系統
 */
public class Company implements Serializable {
    private static final long serialVersionUID = 1L;

    // ==========================================
    // 🏢 企業基礎屬性
    // ==========================================
    private String name;
    private IndustryType industry;
    private double cash;
    private int reputation;
    private int level;
    private double brandImage;         // 品牌形象數值
    private double researchEfficiency; // 研發效率

    // ==========================================
    // 📖 財務流水帳與季報系統
    // ==========================================
    private List<String> ledger = new ArrayList<>();

    // ==========================================
    // 📈 股市與行情觀測屬性
    // ==========================================
    private double stockPrice;
    private List<StockRecord> stockHistory;

    // ==========================================
    // 🎲 大富翁擴充：戰略抽卡與狀態型事件欄位
    // ==========================================
    private int lastGachaTurn = -5;          // 記錄上一次抽卡的回合天數 (初始設為 -5 確保首日可抽)
    private int revenueModifierTurns = 0;    // 收益修正剩餘回合
    private double revenueModifierRate = 0.0;// 收益修正比例 (例如 +0.1 代表收益 +10%)
    private int damageReductionTurns = 0;    // 負面事件傷害減免剩餘回合
    private double damageReductionRate = 0.0;// 傷害減免比例 (例如 0.2 代表負面扣錢少 20%)
    private int volatilityDoubleTurns = 0;   // 股價波動加倍剩餘回合

    private boolean hasBankrupted = false; // 是否已經破產過

    public boolean isHasBankrupted() {
        return hasBankrupted;
    }

    public void setHasBankrupted(boolean hasBankrupted) {
        this.hasBankrupted = hasBankrupted;
    }

    // ==========================================
    // 🚀 建構子 (Constructor)
    // ==========================================
    public Company(String name, IndustryType industry) {
        this.name = name;
        this.industry = industry;
        this.cash = 50000000.0; // 起始資金 5,000 萬
        this.reputation = 50;
        this.level = 1;
        this.brandImage = 0.0;
        this.researchEfficiency = 0.0;

        // 股市初始化
        this.stockPrice = 10.0;
        this.stockHistory = new ArrayList<>();
        this.stockHistory.add(new StockRecord(0, this.stockPrice));
    }

    // ==========================================
    // 💸 現金流管理運作
    // ==========================================
    public boolean spendCash(double amount) {
        if (this.cash >= amount) {
            this.cash -= amount;
            return true;
        }
        return false;
    }

    public void earnCash(double amount) {
        if (amount > 0) {
            this.cash += amount;
        }
    }

    // ==========================================
    // 📜 流水帳內部明細操作
    // ==========================================
    public void recordTransaction(String detail) {
        ledger.add(detail);
    }


    // ==========================================
    // 📊 股市報價模型與優化公式
    // ==========================================

    /**
     * 常規每日大盤股價計算 (整合估值引力與台股 ±10% 漲跌停限制)
     */
    public void updateStockPrice(int currentTurn, MarketEvent currentEvent) {
        double previousPrice = this.stockPrice;
        double effect;

        // 1. 控管事件波動範圍
        if (currentEvent == null) {
            // 常規換日：骰出精準落在 -2% 到 +2% 之間的微幅市場雜訊
            double marketNoise = (Math.random() * 0.04) - 0.02;
            effect = 1.0 + marketNoise;
            System.out.println(String.format("📊 今日無市場突發事件，大盤常規微幅變動: %.2f%%", marketNoise * 100));
        } else {
            // 突發新聞事件：嚴格吃 Excel 劇本設定的漲跌幅比例
            effect = currentEvent.getEffect();
        }

        // 2. 計算事件影響後的理論股價
        double theoreticalPrice = previousPrice * effect;

        // 3. 🎯 整合基本面估值引力 (結合原 StockService 邏輯，避免數學溢出)
        // 使用流動現金對數模型 + 商譽加權 + 等級加權 = 公司的核心內在價值
        double cashScore = Math.log(Math.max(0, this.cash) + 1) * 2.0;
        double repScore = this.reputation * 1.5;
        double levelScore = this.level * 10.0;
        double baseIntrinsicValue = cashScore + repScore + levelScore;

        // 將核心價值換算為合理的引力常數（縮小引力權重防止大盤暴震）
        double basePriceGravitation = Math.max(10.0, baseIntrinsicValue * 0.1);

        // 讓理論股價向公司基本面靠攏 (估值引力拉扯)
        if (basePriceGravitation > theoreticalPrice) {
            theoreticalPrice += (basePriceGravitation - theoreticalPrice) * 0.02;
        } else if (basePriceGravitation < theoreticalPrice) {
            theoreticalPrice -= (theoreticalPrice - basePriceGravitation) * 0.02;
        }

        // 4. 台股 10% 漲跌停板硬限制
        double maxLimit = previousPrice * 1.10;
        double minLimit = previousPrice * 0.90;

        if (theoreticalPrice > maxLimit) {
            this.stockPrice = maxLimit;
            System.out.println("📈 買盤強勁！觸及當日漲停板！");
        } else if (theoreticalPrice < minLimit) {
            this.stockPrice = minLimit;
            System.out.println("📉 賣壓沉重！觸及當日跌停板！");
        } else {
            this.stockPrice = theoreticalPrice;
        }

        // 確保股價絕對不會跌破 1 元全額交割股防線
        this.stockPrice = Math.max(1.0, this.stockPrice);

        // 5. 精準寫入歷史紀錄 (防止圖表崩潰)
        this.stockHistory.add(new StockRecord(currentTurn, this.stockPrice));
    }

    /**
     * 🎯 核心補回：大富翁特殊隨機事件與機會命運卡片效果所需的百分比調整方法
     * 解決外部編律錯誤，並完美連動「投資人信心爆棚」的雙倍波動狀態！
     */
    public void applyStockPercentChange(double percent) {
        double oldPrice = this.getStockPrice();
        double change = oldPrice * percent;

        // 如果此時具備「投資人信心爆棚」Buff/Debuff，波動效果直接雙倍增幅！
        if (this.volatilityDoubleTurns > 0) {
            change *= 2.0;
        }

        double newPrice = Math.max(1.0, oldPrice + change);
        this.setStockPrice(newPrice);

        // 記錄至歷史走勢圖
        if (this.stockHistory != null) {
            this.stockHistory.add(new StockRecord(this.stockHistory.size(), newPrice));
        }
    }

    // ==========================================
    // ⏳ 大富翁桌遊特殊狀態時效流動
    // ==========================================
    public void decrementBuffTurns() {
        if (revenueModifierTurns > 0) revenueModifierTurns--;
        if (damageReductionTurns > 0) damageReductionTurns--;
        if (volatilityDoubleTurns > 0) volatilityDoubleTurns--;
    }

    // ==========================================
    // ⚙️ 屬性存取器 (Getters & Setters)
    // ==========================================
    public String getName() { return name; }
    public void setName(String newName) { this.name = newName; }

    public IndustryType getIndustry() { return industry; }
    public double getCash() { return cash; }
    public void setCash(double amount) { this.cash = amount; }

    public int getReputation() { return reputation; }
    public void addReputation(int amount) {
        this.reputation += amount;
        this.reputation = Math.max(0, Math.min(100, this.reputation));
    }

    public int getLevel() { return level; }
    public void levelUp() { this.level += 1; }

    public double getBrandImage() { return brandImage; }
    public void setBrandImage(double val) { this.brandImage = val; }
    public void addBrandImage(double amount) { this.brandImage += amount; }

    public double getResearchEfficiency() { return researchEfficiency; }
    public void setResearchEfficiency(double researchEfficiency) { this.researchEfficiency = researchEfficiency; }

    public double getStockPrice() { return stockPrice; }
    public void setStockPrice(double stockPrice) { this.stockPrice = stockPrice; }

    public List<StockRecord> getStockHistory() { return stockHistory; }
    public List<String> getLedger() { return ledger; }

    // 抽卡冷卻與桌遊狀態控制接口
    public int getLastGachaTurn() { return lastGachaTurn; }
    public void setLastGachaTurn(int turn) { this.lastGachaTurn = turn; }

    public int getRevenueModifierTurns() { return revenueModifierTurns; }
    public double getRevenueModifierRate() { return revenueModifierRate; }
    public void setRevenueBuff(int turns, double rate) {
        this.revenueModifierTurns = turns;
        this.revenueModifierRate = rate;
    }

    public int getDamageReductionTurns() { return damageReductionTurns; }
    public double getDamageReductionRate() { return damageReductionRate; }
    public void setDamageReduction(int turns, double rate) {
        this.damageReductionTurns = turns;
        this.damageReductionRate = rate;
    }

    public int getVolatilityDoubleTurns() { return volatilityDoubleTurns; }
    public void setVolatilityDoubleTurns(int turns) { this.volatilityDoubleTurns = turns; }
}