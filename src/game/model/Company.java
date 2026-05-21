package game.model;

import game.model.bank.bank_Customer;

import java.util.Random;

/**
 * 貸款申請案類別（檔名堅持不變：bank_LoanRequest）
 * 負責處理客戶的分期還款、每期違約機率抽籤，以及被拒絕後的劇本對接。
 */
public class Company {

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
    public Company(String applicantName, double amount, double interestRate, int creditScore, int totalTicks) {
        this.applicantName = applicantName;
        this.amount = amount;
        this.interestRate = interestRate;
        this.creditScore = Math.max(1, Math.min(100, creditScore));
        this.totalTicks = totalTicks;
        this.remainingTicks = totalTicks;
        this.rejectCount = 0; // 剛登場時被拒絕次數為 0
    }

    // ==========================================
    // 3. 核心邏輯方法 (Business Logic)
    // ==========================================

    /**
     * 【精準金融分級】計算這名客戶的「每期違約率」
     * 信用分數越高越安全；信用分數越低，越容易爆雷
     */
    public double getDefaultProbability() {
        if (this.creditScore >= 80) {
            return 0.005; // 🌟 信用優良 (80~100)：極度安全，違約率 0.5%
        } else if (this.creditScore >= 60) {
            return 0.02;  // 📈 信用一般 (60~79)：平穩，違約率 2%
        } else if (this.creditScore >= 40) {
            return 0.08;  // ⚠️ 信用瑕疵 (40~59)：高風險，違約率 8%
        } else {
            return 0.35;  // ❌ 信用極差 (1~39)：次級炸彈，違約率飆高到 35%
        }
    }

    /**
     * 【隨機未爆彈抽籤】在未來的每一個 Tick 判定這個客人這回合會不會突然跑路
     * @return true 代表客人違約跑路；false 代表這回合平安
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
    public game.model.bank.bank_LoanRequest processRejection() {
        this.rejectCount++;

        // 如果只被拒絕過 1 次，他就會變更條件重新回來敲門
        if (this.rejectCount == 1) {
            // 這裡會去呼叫妳等一下要設計的 bank_Customer 劇本
            game.model.bank.bank_LoanRequest loopRequest = bank_Customer.createRequestByName(this.applicantName, this.rejectCount);
            return loopRequest;
        }

        // 如果被拒絕 2 次以上，NPC 就會徹底放棄不回來了
        System.out.println("❌ " + this.applicantName + " 碎碎念：「這家銀行真難借，我去別家！」");
        return null;
    }

    // ==========================================
    // 4. Getter 與 Setter 方法 (供 UI 與 Engine 使用)
    // ==========================================
    public String getApplicantName() { return applicantName; }
    public double getAmount() { return amount; }
    public double getInterestRate() { return interestRate; }
    public int getCreditScore() { return creditScore; }
    public int getRemainingTicks() { return remainingTicks; }
    public int getTotalTicks() { return totalTicks; }
    public int getRejectCount() { return rejectCount; }

    public void setRejectCount(int rejectCount) { this.rejectCount = rejectCount; }
}