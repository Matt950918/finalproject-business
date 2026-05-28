package game.model.bio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BioSystem implements Serializable {
    private static final long serialVersionUID = 1L;

    private double money = 50_000_000;
    private double successBonus = 0;  // 基礎成功率加成
    private double brandValue = 0;    // 品牌溢價報酬

    // ⚙️ 核心變數語意重構
    private double rndLevel = 0;       // 科技一：研發能力等級（控制天數，每次升級 +1.0）
    private double costDiscount = 0;  // 科技三：生產效率折讓（控制金錢，每次升級 +0.05）

    private double pendingReward = 0;
    private List<Drug> drugs = new ArrayList<>();

    private int lockdownTurns = 0;
    private String systemMessage = "";

    public BioSystem() {
        generateFiftyDrugs();
    }

    private void generateFiftyDrugs() {
        Random rand = new Random();
        drugs.clear();

        String[] prePrefix = {"倍能", "安妥", "舒緩", "保益", "維他", "諾華", "快克", "百服", "斯達", "利必"};
        String[] midPrefix = {"康", "寧", "適", "痛", "平", "克", "優", "普", "息", "瑞"};
        String[] suffix = {"錠", "素", "膠囊", "滴劑", "注射液", "軟膏", "噴劑", "散劑"};
        String[] sciPrefix = {"基因", "免疫", "標靶", "核心", "分子", "量子", "終極", "突變", "幻影", "幽靈"};
        String[] chemSuffix = {"酚", "醚", "強鹼", "生物鹼", "化合物", "多肽", "血清", "抑素"};

        // 1. 預防藥 20 種 (工期: 2天)
        for (int i = 1; i <= 20; i++) {
            String name = prePrefix[rand.nextInt(prePrefix.length)] + midPrefix[rand.nextInt(midPrefix.length)] + suffix[rand.nextInt(suffix.length)] + " " + i + "號";
            double rate = 0.70 + (rand.nextDouble() * 0.10 - 0.05);
            double reward = 1.25 + (rand.nextDouble() * 0.10 - 0.05);
            double cost = 500_000 + rand.nextInt(300_000);
            drugs.add(new Drug(name, Drug.DrugType.PREVENTIVE, rate, cost, cost * 1.5, reward, 2));
        }

        // 2. 感冒藥 15 種 (工期: 3~5天)
        for (int i = 1; i <= 15; i++) {
            String name = "感冒" + midPrefix[rand.nextInt(midPrefix.length)] + suffix[rand.nextInt(suffix.length)] + " " + (char)('A' + (i % 26)) + "型";
            double rate = 0.50 + (rand.nextDouble() * 0.10 - 0.05);
            double reward = 1.50 + (rand.nextDouble() * 0.10 - 0.05);
            double cost = 1_000_000 + rand.nextInt(500_000);
            int days = 3 + rand.nextInt(3);
            drugs.add(new Drug(name, Drug.DrugType.COLD, rate, cost, cost * 1.8, reward, days));
        }

        // 3. 特效藥 10 種 (工期: 7天)
        for (int i = 1; i <= 10; i++) {
            String name = sciPrefix[rand.nextInt(sciPrefix.length)] + midPrefix[rand.nextInt(midPrefix.length)] + chemSuffix[rand.nextInt(chemSuffix.length)];
            double rate = 0.20 + (rand.nextDouble() * 0.10 - 0.05);
            double reward = 2.00 + (rand.nextDouble() * 0.10 - 0.05);
            double cost = 5_000_000 + rand.nextInt(2_000_000);
            drugs.add(new Drug(name, Drug.DrugType.SPECIAL, rate, cost, cost * 3.0, reward, 7));
        }

        // 4. 毒品 5 種 (工期: 12天)
        for (int i = 1; i <= 5; i++) {
            String name = "【管制】" + sciPrefix[rand.nextInt(sciPrefix.length)] + chemSuffix[rand.nextInt(chemSuffix.length)] + " X-" + i;
            double rate = 0.02 + (rand.nextDouble() * 0.002 - 0.001);
            double reward = 10.00 + (rand.nextDouble() * 1.00 - 0.50);
            double cost = 15_000_000 + rand.nextInt(5_000_000);
            drugs.add(new Drug(name, Drug.DrugType.NARCOTIC, rate, cost, cost * 8.0, reward, 12));
        }

        Collections.shuffle(drugs);
    }

    /**
     * 🔬 點擊研發核心
     */
    public boolean researchDrug(Drug drug) {
        if (lockdownTurns > 0) {
            systemMessage = "❌ 廠房正遭勒令停工中！剩餘 " + lockdownTurns + " 天解封。";
            return false;
        }
        if (!drug.isAvailable()) {
            systemMessage = "❌ 【" + drug.getName() + "】正處於工期鎖定中，剩餘 " + drug.getRemainingCooldownDays() + " 天。";
            return false;
        }
        if (drug.getDailyResearchCount() >= 3) {
            systemMessage = "❌ 該藥物今日研發次數已達上限 (3次)。";
            return false;
        }

        // 🎯 傳入科技樹第三項的 costDiscount 來動態降低扣款開銷
        double actualCost = drug.getDynamicCost(this.costDiscount);
        if (!deductMoney(actualCost)) {
            systemMessage = "❌ 資金不足！";
            return false;
        }

        // ⏱️ 套用科技樹第一項的 rndLevel 縮短排程工期天數
        drug.calculateAndSetCooldown(this.rndLevel);

        // 🎲 當下即時判定結果
        boolean success = drug.tryDevelop(successBonus);

        if (success) {
            double brandLevel = this.brandValue * 10;
            double finalRewardMultiplier = drug.getRewardMultiplier() + (0.1 * brandLevel);
            double rewardAmount = actualCost * finalRewardMultiplier;
            this.pendingReward += rewardAmount;

            systemMessage = String.format("🎉 解盲成功！【%s】研發完成，工期鎖定 %d 天，明日到帳 $%.0f 萬！",
                    drug.getName(), drug.getRemainingCooldownDays(), rewardAmount / 10000);
        } else {
            systemMessage = String.format("💥 實驗失敗！【%s】進入 %d 天設備調校冷卻期。",
                    drug.getName(), drug.getRemainingCooldownDays());

            // 🎯 【黑市毒品處罰核心重構】
            if (drug.getType() == Drug.DrugType.NARCOTIC) {
                if (Math.random() < 0.90) { // 90% 機率翻車遭檢警突擊搜查
                    Random rand = new Random();

                    // 1. 隨機停工天數：3 ~ 7 天 (nextInt(5) 產生 0~4，加 3 等於 3~7)
                    this.lockdownTurns = 3 + rand.nextInt(5);

                    // 2. 處以當前動態研發成本的 2 倍行政罰金
                    double penaltyFine = actualCost * 2;
                    this.money -= penaltyFine; // 司法行政處分強制扣款

                    // 3. 組裝高代入感的系統警報訊息
                    systemMessage += String.format(
                            "\n\n🚨 【法律制裁】地下秘密研發遭內線舉報！" +
                                    "\n⚠️ 檢警與衛生局發動聯合突擊搜查！" +
                                    "\n🛑 全廠房勒令全面停工封鎖：%d 天！" +
                                    "\n💸 並且依法對集團處以 2 倍天價罰金：$%,.0f 萬！",
                            lockdownTurns, penaltyFine / 10000
                    );
                }
            }
        }

        return success;
    }

    public void tick() {
        systemMessage = "";

        // 🎯 修正修正：即使在全面停工期間，昨日已經研發成功的常規藥利潤（pendingReward）依然要能入帳
        if (pendingReward > 0) {
            earnMoney(pendingReward);
            pendingReward = 0;
        }

        if (lockdownTurns > 0) {
            lockdownTurns--;
            // 注意：停工期間，其他藥物的產線工期也應該要一併每天消退，否則復工後常規藥會卡死
            for (Drug drug : drugs) {
                drug.advanceDay();
            }
            return;
        }

        // 每日推進所有藥物的工期倒數
        for (Drug drug : drugs) {
            drug.advanceDay();
        }
    }

    public void sellDrug(Drug drug, double demand) {
        double revenue = demand * drug.getPrice() * (1.0 + brandValue);
        earnMoney(revenue);
    }

    public boolean deductMoney(double amount) {
        if (money >= amount) { money -= amount; return true; }
        return false;
    }
    public void earnMoney(double amount) { money += amount; }
    public void earnCash(double amount) { this.money += amount; }

    // ==========================================
    // ⚙️ 科技樹對接專用方法 (重要)
    // ==========================================
    public void addEfficiency(double value) {
        this.rndLevel += value;
    }

    public void addSuccessBonus(double value) {
        this.costDiscount += value;
    }

    public void addBrandValue(double value) { this.brandValue += value; }

    // Getters & Setters
    public double getMoney() { return money; }
    public void setMoney(double money) { this.money = money; }
    public double getSuccessBonus() { return successBonus; }
    public double getBrandValue() { return brandValue; }
    public double getRndLevel() { return rndLevel; }
    public double getCostDiscount() { return costDiscount; }
    public List<Drug> getDrugs() { return drugs; }
    public int getLockdownTurns() { return lockdownTurns; }
    public String getSystemMessage() { return systemMessage; }
}