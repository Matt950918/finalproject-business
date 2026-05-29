package game.model.bio;

import java.io.Serializable;

public class Drug implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum DrugType {
        PREVENTIVE, COLD, SPECIAL, NARCOTIC
    }

    private String name;
    private DrugType type;

    private double baseSuccessRate;
    private double cost;
    private double price;
    private double rewardMultiplier;

    private boolean isDiscovered;
    private int dailyResearchCount = 0;
    private int totalSuccessCount = 0;
    private double refreshRateMultiplier = 1.0;

    // ⏱️ 研發工期鎖（冷卻時間）
    private int baseDaysRequired;
    private int remainingCooldownDays = 0;

    // 🎯 新增：永久上市狀態標記，避免重登讀檔狀態錯亂導致按鈕鎖死
    private boolean isLaunched = false;

    public Drug(String name, DrugType type, double baseSuccessRate, double cost, double price, double rewardMultiplier, int baseDaysRequired) {
        this.name = name;
        this.type = type;
        this.baseSuccessRate = baseSuccessRate;
        this.cost = cost;
        this.price = price;
        this.rewardMultiplier = rewardMultiplier;
        this.baseDaysRequired = baseDaysRequired;
        this.isDiscovered = false;
        this.isLaunched = false; // 預設皆為未上市
    }

    /**
     * 🔬 啟動研發工期
     * 🎯 完美對接：傳入科技樹第一項的 rndLevel，每級減少 5% 工期，向上取整
     */
    public void calculateAndSetCooldown(double rndLevel) {
        double reductionPercent = rndLevel * 0.05; // 每級少 5% 天數
        double calculatedDays = this.baseDaysRequired * (1.0 - reductionPercent);

        this.remainingCooldownDays = (int) Math.ceil(calculatedDays);
        if (this.remainingCooldownDays < 1) this.remainingCooldownDays = 1;

        this.dailyResearchCount++;
    }

    /**
     * 🎲 點擊當下立刻判定成功或失敗
     */
    public boolean tryDevelop(double globalSuccessBonus) {
        double successPenalty = dailyResearchCount * 0.05;
        double historyBonus = totalSuccessCount * 0.05;

        double finalRate = baseSuccessRate + globalSuccessBonus + historyBonus - successPenalty;
        finalRate = Math.max(0.0, Math.min(1.0, finalRate));

        double rand = Math.random();
        if (rand < finalRate) {
            isDiscovered = true;
            totalSuccessCount++;
            return true;
        }
        return false;
    }

    /**
     * 🌅 每日換日減少 1 天冷卻工期
     */
    public void advanceDay() {
        if (this.remainingCooldownDays > 0) {
            this.remainingCooldownDays--;
        }
    }

    /**
     * 💰 動態成本
     * 🎯 完美對接：除了藥物自身的專利折讓，另外扣除科技樹第三項傳進來的全球成本折讓（globalDiscount）
     */
    public double getDynamicCost(double globalDiscount) {
        double historyCostDiscount = totalSuccessCount * 0.05;
        historyCostDiscount = Math.min(0.90, historyCostDiscount);

        // 總折讓 = 歷史成功折讓 + 科技樹生產效率折讓
        double totalDiscount = historyCostDiscount + globalDiscount;
        totalDiscount = Math.min(0.95, totalDiscount); // 最高折讓 95% 防歸零

        return this.cost * (1.0 - totalDiscount);
    }

    /**
     * ⚡ 刷新機制（回補當天次數，但該藥物的刷新機率腰斬！）
     */
    public void refundDailyCount() {
        if (this.dailyResearchCount > 0) {
            this.dailyResearchCount--;
            this.refreshRateMultiplier *= 0.5;
        }
    }

    public void resetDailyCount() {
        this.dailyResearchCount = 0;
        this.refreshRateMultiplier = 1.0;
    }

    // ==========================================
    // Getters & Setters
    // ==========================================
    public String getName() { return name; }
    public DrugType getType() { return type; }
    public double getPrice() { return price; }
    public double getCost() { return cost; }
    public double getRewardMultiplier() { return rewardMultiplier; }
    public boolean isDiscovered() { return isDiscovered; }
    public int getDailyResearchCount() { return dailyResearchCount; }
    public int getTotalSuccessCount() { return totalSuccessCount; }
    public double getRefreshRateMultiplier() { return refreshRateMultiplier; }
    public double getBaseSuccessRate() { return baseSuccessRate; }
    public int getRemainingCooldownDays() { return remainingCooldownDays; }
    public boolean isAvailable() { return remainingCooldownDays <= 0; }

    // 🎯 提供上市狀態的外部存取介面
    public boolean isLaunched() { return isLaunched; }
    public void setLaunched(boolean launched) { this.isLaunched = launched; }
}