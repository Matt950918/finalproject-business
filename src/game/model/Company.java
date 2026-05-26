package game.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Company implements Serializable {

    private String name;
    private IndustryType industry;
    private double cash;
    private int reputation;
    private int level;

    // ==========================================
    // 📖 帳務明細與月報系統
    // ==========================================
    private List<String> ledger = new ArrayList<>();

    public void recordTransaction(String detail) {
        ledger.add(detail);
    }

    public List<String> getLedger() {
        return ledger;
    }

    public void summarizeLedger(int month) {
        ledger.clear();
        ledger.add("=================================");
        ledger.add("📅 【" + this.name + " 第 " + month + " 季財務月報】");
        ledger.add("過往流水帳明細已由會計部封存歸檔。");
        ledger.add("=================================");
    }

    // ==========================================
    // 📈 與市場機制串接的屬性
    // ==========================================
    private double stockPrice;
    private List<StockRecord> stockHistory;

    public Company(String name, IndustryType industry) {
        this.name = name;
        this.industry = industry;
        this.cash = 50000000.0;
        this.reputation = 50;
        this.level = 1;

        this.stockPrice = 10.0;
        this.stockHistory = new ArrayList<>();
        this.stockHistory.add(new StockRecord(0, this.stockPrice));
    }

    // ==========================================
    // 🔮 擬真版：加入台股 ±10% 漲跌停板限制
    // ==========================================
    public void updateStockPrice(int currentTurn, MarketEvent currentEvent) {
        double previousPrice = this.stockPrice;
        double effect = (currentEvent != null) ? currentEvent.getEffect() : (0.99 + Math.random() * 0.02);
        double theoreticalPrice = previousPrice * effect;
        double baseValue = (this.cash * 0.0000005) + (this.reputation * 0.08) + (this.level * 1.0);

        if (baseValue > theoreticalPrice) {
            theoreticalPrice += (baseValue - theoreticalPrice) * 0.005;
        } else if (baseValue < theoreticalPrice) {
            theoreticalPrice -= (theoreticalPrice - baseValue) * 0.005;
        }

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

        // 記錄歷史股價 (使用你夥伴寫的 StockRecord！)
        this.stockHistory.add(new StockRecord(currentTurn, this.stockPrice));
    }

    // --- 以下是 Getter 與 Setter 方法 ---
    public String getName() {
        return name;
    }

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
    // 💡 讓外部可以自由修改公司名稱
    public void setName(String newName) {
        this.name = newName;
    }
    public int getLevel() { return level; }

    public void levelUp() { this.level += 1; }
}