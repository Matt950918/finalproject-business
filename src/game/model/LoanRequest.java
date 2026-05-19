package game.model;

import java.util.Random;

/**
 * 貸款申請案類別（銀行業核心機制）
 * 負責儲存單一客戶的貸款規格，並以現實金融機率模型推演還款與違約行為。
 */
public class LoanRequest {

    // ==========================================
    // 1. 屬性 (Attributes)：客人的申請資料
    // ==========================================
    private String applicantName;   // 申請人姓名
    private double amount;          // 申請貸款金額
    private double interestRate;    // 房貸年利率 (例如：0.025 代表 2.5%)
    private int creditScore;        // 信用分數 (範圍 1 ~ 100，越高代表信用越好)
    private int totalTicks;         // 總還款期數 (這個貸款要分多少個 Tick 還清)
    private int remainingTicks;     // 剩餘還款期數

    // 引入 Java 內建的亂數產生器，用來判定機率
    private static final Random random = new Random();

    // ==========================================
    // 2. 建構子 (Constructor)
    // ==========================================
    public LoanRequest(String applicantName, double amount, double interestRate, int creditScore, int totalTicks) {
        this.applicantName = applicantName;
        this.amount = amount;
        this.interestRate = interestRate;
        this.creditScore = Math.max(1, Math.min(100, creditScore)); // 限制在 1~100 之間
        this.totalTicks = totalTicks;
        this.remainingTicks = totalTicks;
    }

    // ==========================================
    // 3. 核心邏輯方法 (Business Logic)
    // ==========================================

    /**
     * 【現實優化版】計算這名客戶的違約率
     * 結合現實世界金融數值，確保高信用者極度安全，低信用者具備危險性
     */
    public double getDefaultProbability() {
        if (this.creditScore >= 80) {
            // 🌟 信用優良 (80~100)：現實房貸違約率極低，約 0.5%
            return 0.005;
        } else if (this.creditScore >= 60) {
            // 📈 信用一般 (60~79)：稍微有風險，約 2%
            return 0.02;
        } else if (this.creditScore >= 40) {
            // ⚠️ 信用瑕疵 (40~59)：高風險客戶，約 8%
            return 0.08;
        } else {
            // ❌ 信用極差 (1~39)：現實中的次級貸款、極高風險，違約率飆高到 35%
            // 給玩家帶來「想賺暴利卻可能直接踩雷」的驚險遊戲感
            return 0.35;
        }
    }

    /**
     * 【隨機抽籤】判定這名客戶在「這一個 Tick」會不會突然倒帳（違約）
     * @return true 代表客人違約，捲款潛逃；false 代表這回合平安無事
     */
    public boolean checkEventualDefault() {
        double chance = getDefaultProbability();
        // random.nextDouble() 會隨機產生一個 0.0 到 1.0 之間的小數
        // 如果隨機數小於違約率，就代表慘遭倒帳！
        return random.nextDouble() < chance;
    }

    /**
     * 每回合還款邏輯：如果沒倒帳，客人會還一部分的本金和利息
     * @return 銀行這回合可以收到的還款金額
     */
    public double processTickPayment() {
        if (remainingTicks > 0) {
            remainingTicks--;

            // 每期應還本金 = 總金額 / 總期數
            double principalPerTick = amount / totalTicks;
            // 每期利息 = 剩餘本金 * 利率 (這裡做簡化計算)
            double interestPerTick = (principalPerTick * remainingTicks) * (interestRate / 12.0);

            return principalPerTick + interestPerTick;
        }
        return 0;
    }

    // ==========================================
    // 4. Getter 方法
    // ==========================================
    public String getApplicantName() { return applicantName; }
    public double getAmount() { return amount; }
    public double getInterestRate() { return interestRate; }
    public int getCreditScore() { return creditScore; }
    public int getRemainingTicks() { return remainingTicks; }
}