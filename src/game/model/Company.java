package game.model;

import java.util.ArrayList;
import java.util.List;

public class Company {

    private IndustryType industry;
    private double cash;
    private int reputation;
    private int level;
    // ==========================================
    // 📖 帳務明細與月報系統
    // ==========================================
    private List<String> ledger = new ArrayList<>();

    // 寫入一筆帳務明細
    public void recordTransaction(String detail) {
        ledger.add(detail);
    }

    // 取得完整帳本
    public List<String> getLedger() {
        return ledger;
    }

    // 🌟 月報結算機制 (每 30 天由主控制器呼叫一次)
    public void summarizeLedger(int month) {
        ledger.clear(); // 清空前面的所有流水帳，維持效能
        ledger.add("=================================");
        ledger.add("📅 【遠東集團 第 " + month + " 季財務月報】");
        ledger.add("過往流水帳明細已由會計部封存歸檔。");
        ledger.add("=================================");
    }

    // ==========================================
    // 📈 新增：與市場機制串接的屬性
    // ==========================================
    private double stockPrice;               // 當前股價
    private List<StockRecord> stockHistory;  // 股價歷史走勢紀錄（你趴呢寫的類別在這邊發揮作用！）

    public Company(IndustryType industry) {
        this.industry = industry;
        this.cash = 50000000.0;
        this.reputation = 50;      // 初始聲望
        this.level = 1;            // 初始 Lv.1

        this.stockPrice = 10.0;    // 假設初始上市股價都是 10 元
        this.stockHistory = new ArrayList<>();
        // 記錄第 0 回合的初始股價
        this.stockHistory.add(new StockRecord(0, this.stockPrice));
    }

    // ==========================================
    // 🔮 擬真版：加入台股 ±10% 漲跌停板限制
    // ==========================================
    public void updateStockPrice(int currentTurn, MarketEvent currentEvent) {

        // 1. 記錄昨收價 (計算 10% 限制的基準)
        double previousPrice = this.stockPrice;

        // 2. 取得事件倍率 (無事件則給予 -1% ~ +1% 隨機日常波動)
        double effect = (currentEvent != null) ? currentEvent.getEffect() : (0.99 + Math.random() * 0.02);

        // 3. 算出「理論上的新股價」
        double theoreticalPrice = previousPrice * effect;

        // 4. 體質校準 (計算真實價值)
        double baseValue = (this.cash * 0.0000005) + (this.reputation * 0.08) + (this.level * 1.0);

        // 5. 基本面長線拉扯
        if (baseValue > theoreticalPrice) {
            theoreticalPrice += (baseValue - theoreticalPrice) * 0.005;
        } else if (baseValue < theoreticalPrice) {
            theoreticalPrice -= (theoreticalPrice - baseValue) * 0.005;
        }

        // ==========================================
        // 🇹🇼 核心機制：台股 10% 漲跌幅限制 (Limit Up / Limit Down)
        // ==========================================
        double maxLimit = previousPrice * 1.10; // 漲停板
        double minLimit = previousPrice * 0.90; // 跌停板

        // 如果理論股價突破漲跌停限制，強制鎖死在 10%
        if (theoreticalPrice > maxLimit) {
            this.stockPrice = maxLimit;
            System.out.println("📈 觸及漲停板！");
        } else if (theoreticalPrice < minLimit) {
            this.stockPrice = minLimit;
            System.out.println("📉 觸及跌停板！");
        } else {
            this.stockPrice = theoreticalPrice;
        }

        // 6. 安全檢查：股價底線 1 元，變成壁紙前最後的掙扎
        this.stockPrice = Math.max(1.0, this.stockPrice);

        // 7. 存入歷史紀錄
        this.stockHistory.add(new StockRecord(currentTurn, this.stockPrice));
    }

    // --- 以下是 Getter 與 Setter 方法 ---

    public double getStockPrice() {
        return stockPrice;
    }

    public List<StockRecord> getStockHistory() {
        return stockHistory;
    }

    public IndustryType getIndustry() { return industry; }
    public double getCash() { return cash; }

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

    public int getReputation() { return reputation; }

    public void addReputation(int amount) {
        this.reputation += amount;
        this.reputation = Math.max(0, Math.min(100, this.reputation));
    }

    public int getLevel() { return level; }
    public void levelUp() { this.level += 1; }
}