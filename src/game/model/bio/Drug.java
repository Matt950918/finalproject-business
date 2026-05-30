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
    private int totalSuccessCount = 0;

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
    }

    /**
     * 🎲 點擊當下立刻判定成功或失敗
     * 🎯【核心優化】：全面移除 dailyResearchCount 點擊次數懲罰，讓機率絕對正常且穩定地出現！
     */
    public boolean tryDevelop(double globalSuccessBonus) {
        // 歷史加成：每成功研發該類藥物一次，勝率永久加 5%
        double historyBonus = totalSuccessCount * 0.05;

        // 最終勝率 = 基礎勝率 + 科技樹加成 + 歷史成功回饋 (不再扣減任何點擊懲罰)
        double finalRate = baseSuccessRate + globalSuccessBonus + historyBonus;

        // 嚴格限制在 5% 保底 到 100% 封頂之間
        finalRate = Math.max(0.05, Math.min(1.0, finalRate));

        // 輸出除錯日誌，方便在主控台（Console）實時觀測最精準的正常勝率
        System.out.println(String.format("🔬 藥物【%s】解盲中 -> 基礎:%d%% | 歷史加成:+%d%% | 最終勝率:%.1f%%",
                this.name, (int)(baseSuccessRate * 100), (int)(historyBonus * 100), finalRate * 100));

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

    // ==========================================
    // 💡 為了防範外部類別（BioSystem / BioPanelController）呼叫舊方法導致編譯失敗，
    // 空殼保留以下三個方法，做完美相容性防呆，內部不執行任何懲罰累加
    // ==========================================
    public void refundDailyCount() {
        // 已移除次數懲罰與機率腰斬邏輯，此處留空做安全相容
    }

    public void resetDailyCount() {
        // 已移除次數懲罰，此處留空做安全相容
    }

    public int getDailyResearchCount() {
        return 0; // 永遠回傳 0，安全繞過外部 `executeResearch` 與 `researchDrug` 的次數上限檢查
    }

    public double getRefreshRateMultiplier() {
        return 1.0; // 永遠回傳不變的 1.0 倍率
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
    public int getTotalSuccessCount() { return totalSuccessCount; }
    public double getBaseSuccessRate() { return baseSuccessRate; }
    public int getRemainingCooldownDays() { return remainingCooldownDays; }
    public boolean isAvailable() { return remainingCooldownDays <= 0; }

    public boolean isLaunched() { return isLaunched; }
    public void setLaunched(boolean launched) { this.isLaunched = launched; }
}