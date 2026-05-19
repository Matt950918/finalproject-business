package game.model;

/**
 * 公司主體類別（玩家化身）
 * 包含核心屬性，並實作因經營行為（如拒絕放貸）導致聲望與股價變動的連動機制。
 */

public class Company {

    private IndustryType industry;
    private double cash;
    private int reputation;
    private int level;
    private double stockPrice;     // 📈 新增：當前股價

    public Company(IndustryType industry) {
        this.industry = industry;
        this.cash = 10000000.0;    // 初始 1,000 萬
        this.reputation = 60;      // 初始聲望 60 分
        this.level = 1;
        this.stockPrice = 100.0;   // 初始股價 100 元
    }

    // 花錢的方法：會檢查餘額
    public boolean spendCash(double amount) {
        if (this.cash >= amount) {
            this.cash -= amount;
            return true;
        }
        return false;
    }

    // 賺錢的方法
    public void earnCash(double amount) {
        if (amount > 0) {
            this.cash += amount;
        }
    }

    // 改變聲望：加入與股價的即時連動
    public void addReputation(int amount) {
        this.reputation += amount;
        // 限制在 0~100 之間
        this.reputation = Math.max(0, Math.min(100, this.reputation));

        // 💥 連動機制：如果因為拒絕放貸導致聲望受損，股價會跟著受到懲罰（例如每扣1點聲望，股價跌0.5%）
        if (amount < 0) {
            this.stockPrice *= (1.0 + (amount * 0.005));
        } else {
            // 聲望上漲也會微幅帶動股價
            this.stockPrice *= (1.0 + (amount * 0.002));
        }
    }

    // --- Getter 與 Setter ---
    public IndustryType getIndustry() { return industry; }
    public double getCash() { return cash; }
    public int getReputation() { return reputation; }
    public int getLevel() { return level; }
    public void levelUp() { this.level += 1; }
    public double getStockPrice() { return stockPrice; }
    public void setStockPrice(double stockPrice) { this.stockPrice = stockPrice; }
}