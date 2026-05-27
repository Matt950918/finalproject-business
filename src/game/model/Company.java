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

    public void summarizeLedger(int month) {
        ledger.clear();
        ledger.add("=================================");
        ledger.add("📅 【" + this.name + " 第 " + month + " 季財務月報】");
        ledger.add("過往流水帳明細已由會計部封存歸檔。");
        ledger.add("=================================");
    }

    // ==========================================
    // 📊 股市報價模型與優化公式
    // ==========================================

    /**
     * 常規每日大盤股價計算 (包含台股 ±10% 漲跌停板限制)
     */
    public void updateStockPrice(int currentTurn, MarketEvent currentEvent) {
        double previousPrice = this.stockPrice;
        double effect = (currentEvent != null) ? currentEvent.getEffect() : (0.99 + Math.random() * 0.02);
        double theoreticalPrice = previousPrice * effect;
        double baseValue = (this.cash * 0.0000005) + (this.reputation * 0.08) + (this.level * 1.0);

        // 估值引力調控
        if (baseValue > theoreticalPrice) {
            theoreticalPrice += (baseValue - theoreticalPrice) * 0.005;
        } else if (baseValue < theoreticalPrice) {
            theoreticalPrice -= (theoreticalPrice - baseValue) * 0.005;
        }

        // 台股 10% 漲跌停機制
        double maxLimit = previousPrice * 1.10;
        double minLimit = previousPrice * 0.90;

        if (theoreticalPrice > maxLimit) {
            this.stockPrice = maxLimit;
            System.out.println("📈 觸及漲停板！");
        } else if (theoreticalPrice < minLimit) {
            this.stockPrice = minLimit;
            System.out.println("📉 觸及跌停板！");
        } else {
            this.stockPrice = theoreticalPrice;
        }

        this.stockPrice = Math.max(1.0, this.stockPrice);
        this.stockHistory.add(new StockRecord(currentTurn, this.stockPrice));
    }

    /**
     * 大富翁特殊隨機事件：引入精準百分比調整與爆棚波動加成
     */
    public void applyStockPercentChange(double percent) {
        double oldPrice = this.getStockPrice();
        double change = oldPrice * percent;

        // 如果此時具備「投資人信心爆棚」Debuff/Buff，波動效果直接雙倍增幅！
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