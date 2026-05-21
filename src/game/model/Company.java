package game.model;

public class Company {

    // 屬性：公司的基本數值
    private IndustryType industry;
    private double cash;
    private int reputation;
    private int level;

    // 建構子：創立公司的那一瞬間
    public Company(IndustryType industry) {
        this.industry = industry;
        this.cash = 10000000.0;    // 絕對公平：大家都從 1,000 萬開局
        this.reputation = 50;      // 初始聲望及格邊緣
        this.level = 1;            // 大家都是 Lv.1
    }

    // --- 以下是 Getter 與 Setter 方法 ---

    public IndustryType getIndustry() {
        return industry;
    }

    public double getCash() {
        return cash;
    }

    // 花錢的方法：會檢查餘額
    public boolean spendCash(double amount) {
        if (this.cash >= amount) {
            this.cash -= amount;
            return true;  // 扣款成功
        }
        return false;     // 餘額不足
    }

    // 賺錢的方法
    public void earnCash(double amount) {
        if (amount > 0) {
            this.cash += amount;
        }
    }

    public int getReputation() {
        return reputation;
    }

    // 改變聲望：確保在 0~100 之間
    public void addReputation(int amount) {
        this.reputation += amount;
        this.reputation = Math.max(0, Math.min(100, this.reputation));
    }

    public int getLevel() {
        return level;
    }

    public void levelUp() {
        this.level += 1;
    }
}