package game.model.bio;

public class Drug {
    public enum DrugType {
        PREVENTIVE, COLD, SPECIAL
    }

    private String name;
    private DrugType type;

    private double baseSuccessRate;
    private double cost;
    private double price;

    private boolean isDiscovered; // 歷史上是否曾經成功過

    private int dailyResearchCount = 0; // 當天已研發次數 (上限 3 次)
    private int totalSuccessCount = 0;   // 歷史研發成功總次數

    // ⚡ 新增：當天這款藥物專屬的刷新率乘數 (預設 1.0，每次刷新就腰斬)
    private double refreshRateMultiplier = 1.0;

    public Drug(String name, DrugType type, double baseSuccessRate, double cost, double price) {
        this.name = name;
        this.type = type;
        this.baseSuccessRate = baseSuccessRate;
        this.cost = cost;
        this.price = price;
        this.isDiscovered = false;
    }

    /**
     * 🔬 研發成功判定（回歸最乾淨的原本邏輯，不影響成功率）
     */
    public boolean tryDevelop(double globalSuccessBonus) {
        double successPenalty = dailyResearchCount * 0.05;
        double historyBonus = totalSuccessCount * 0.05;

        double finalRate = baseSuccessRate + globalSuccessBonus + historyBonus - successPenalty;
        finalRate = Math.max(0.0, Math.min(1.0, finalRate));

        dailyResearchCount++;

        double rand = Math.random();
        if (rand < finalRate) {
            isDiscovered = true;
            totalSuccessCount++;
            return true;
        }
        return false;
    }

    /**
     * 💰 動態成本（僅看藥物自身的專利折讓）
     */
    public double getDynamicCost() {
        double historyCostDiscount = totalSuccessCount * 0.05;
        historyCostDiscount = Math.min(0.90, historyCostDiscount);
        return this.cost * (1.0 - historyCostDiscount);
    }

    /**
     * ⚡ 【核心重寫】：刷新機制（回補當天次數，但該藥物的刷新機率腰斬！）
     */
    public void refundDailyCount() {
        if (this.dailyResearchCount > 0) {
            this.dailyResearchCount--;       // 成功回補 1 次研發機會
            this.refreshRateMultiplier *= 0.5; // ⚡ 刷新機率直接腰斬！
        }
    }

    /**
     * 🌅 每日換日重置
     */
    public void resetDailyCount() {
        this.dailyResearchCount = 0;
        this.refreshRateMultiplier = 1.0; // 隔天重置，刷新機率恢復 100% 狀態
    }

    // ==========================================
    // Getters & Setters
    // ==========================================
    public String getName() { return name; }
    public DrugType getType() { return type; }
    public double getPrice() { return price; }
    public boolean isDiscovered() { return isDiscovered; }
    public int getDailyResearchCount() { return dailyResearchCount; }
    public int getTotalSuccessCount() { return totalSuccessCount; }
    public double getRefreshRateMultiplier() { return refreshRateMultiplier; } // 新增 Getter
    public double getBaseSuccessRate() { return baseSuccessRate; }
}