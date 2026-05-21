package game.model.bank;

import java.util.Random;

/**
 * 貸款申請案類別（檔名：bank_LoanRequest）
 * 負責處理客戶的分期還款、階梯式違約機率抽籤，以及被拒絕後的劇本對接。
 */
public class bank_LoanRequest {

    // ==========================================
    // 1. 屬性 (Attributes)
    // ==========================================
    private String applicantName;   // 申請人姓名
    private double amount;          // 申請貸款金額
    private double interestRate;    // 房貸年利率 (例如：0.025 代表 2.5%)
    private int creditScore;        // 信用分數 (1 ~ 100)
    private int totalTicks;         // 總還款期數 (分多少個 Tick 還清)
    private int remainingTicks;     // 剩餘還款期數
    private int rejectCount;        // 紀錄被拒絕了幾次 (捲土重來機制的關鍵)

    private static final Random random = new Random();

    // ==========================================
    // 2. 建構子 (Constructor)
    // ==========================================
    public bank_LoanRequest(String applicantName, double amount, double interestRate, int creditScore, int totalTicks) {
        this.applicantName = applicantName;
        this.amount = amount;
        this.interestRate = interestRate;
        this.creditScore = Math.max(1, Math.min(100, creditScore));
        this.totalTicks = totalTicks;
        this.remainingTicks = totalTicks;
        this.rejectCount = 0;
    }

    // ==========================================
    // 3. 核心邏輯方法 (Business Logic)
    // ==========================================

    /**
     * 【精準階梯式平衡】依據信用分數，回傳該客戶每回合的「爆雷機率」
     * 既能給玩家安全感，又能帶來既期待又怕受傷害的賭博感
     */
    public double getDefaultProbability() {
        if (this.creditScore >= 80) {
            return 0.005; // 🌟 優質客戶 (80~100)：爆雷率 0.5% (幾乎不爆，給玩家安全感)
        } else if (this.creditScore >= 60) {
            return 0.02;  // 📈 普通客戶 (60~79)：爆雷率 2.0% (偶爾嚇一跳，增加真實感)
        } else if (this.creditScore >= 40) {
            return 0.08;  // ⚠️ 投機/瑕疵客戶 (40~59)：爆雷率 8.0% (高風險，考驗玩家心臟)
        } else {
            return 0.35;  // ❌ 極差/炸彈客戶 (1~39)：爆雷率 35.0% (超級次級炸彈，貪心極易破產)
        }
    }

    /**
     * 【每回合未爆彈抽籤】判定這個客人這回合會不會突然因為突發事件跑路
     * @return true 代表客人突發爆雷跑路；false 代表這回合平安
     */
    public boolean checkEventualDefault() {
        return random.nextDouble() < getDefaultProbability();
    }

    /**
     * 每回合還款邏輯（精準年利率轉月領息模型）
     * @return 銀行這回合可以收到的還款金額（本金 + 利息）
     */
    public double processTickPayment() {
        if (remainingTicks > 0) {
            remainingTicks--;

            // 每期應還本金 = 總金額 / 總期數
            double principalPerTick = amount / totalTicks;

            // 每期利息 = 剩餘本金 * (年利率 / 12) -> 完美符合每月領息與年利率邏輯
            double interestPerTick = (principalPerTick * (remainingTicks + 1)) * (interestRate / 12.0);

            return principalPerTick + interestPerTick;
        }
        return 0;
    }

    /**
     * 當玩家在介面按「拒絕」時觸發的邏輯
     * @return 重新包裝過後的全新貸款申請案 (也就是打折、換條件後捲土重來的他)
     */
    public bank_LoanRequest processRejection() {
        this.rejectCount++;

        // 如果只被拒絕過 1 次，他就會變更條件重新回來敲門
        if (this.rejectCount == 1) {
            // 呼叫 bank_Customer 的劇本庫生成二度挑戰的對話與數據
            bank_LoanRequest loopRequest = bank_Customer.createRequestByName(this.applicantName, this.rejectCount);
            return loopRequest;
        }

        // 如果被拒絕 2 次以上，NPC 就會徹底放棄不回來了
        System.out.println("❌ " + this.applicantName + " 碎碎念：「這家銀行真難借，我去別家！」");
        return null;
    }

    // ==========================================
    // 4. Getter 與 Setter 方法
    // ==========================================
    public String getApplicantName() { return applicantName; }
    public double getAmount() { return amount; }
    public double getInterestRate() { return interestRate; }
    public int getCreditScore() { return creditScore; }
    public int getRemainingTicks() { return remainingTicks; }
    public int getTotalTicks() { return totalTicks; }
    public int getRejectCount() { return rejectCount; }

    public void setRejectCount(int rejectCount) { this.rejectCount = rejectCount; }
    public boolean checkDefault() {
        return random.nextDouble() < getDefaultProbability();
    }
}