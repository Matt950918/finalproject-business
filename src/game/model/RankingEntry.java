package game.model;

import java.io.Serializable;

/**
 * 🏆 全球企業排行榜：單筆帳號即時數據模型
 */
public class RankingEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;      // 玩家帳號
    private String companyName;   // 集團名稱
    private IndustryType industry;// 產業型態 (BANK, BIOTECH, TECH)
    private double cash;          // 總資產
    private double stockPrice;    // 最新股價

    public RankingEntry(String username, String companyName, IndustryType industry, double cash, double stockPrice) {
        this.username = username;
        this.companyName = companyName;
        this.industry = industry;
        this.cash = cash;
        this.stockPrice = stockPrice;
    }

    // ==========================================
    // ⚙️ Getters
    // ==========================================
    public String getUsername() { return username; }
    public String getCompanyName() { return companyName; }
    public IndustryType getIndustry() { return industry; }
    public double getCash() { return cash; }
    public double getStockPrice() { return stockPrice; }

    /**
     * 💡 方便在 UI 畫面上把產業英文轉成精美中文
     */
    public String getIndustryChinese() {
        if (industry == null) return "未創立";
        switch (industry) {
            case BANK: return "金融銀行業";
            case BIOTECH: return "生物科技業";
            case TECH: return "晶片科技業";
            default: return "未知產業";
        }
    }
}