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

    private double rndLevel = 0;       // 科技一：研發能力等級（控制天數，每次升級 +1.0）
    private double costDiscount = 0;  // 科技三：生產效率折讓（控制金錢，每次升級 +0.05）

    // 🆕 移除原本的 pendingReward，改用一個內部類別與清單來追蹤每筆利潤的工期倒數
    private static class PendingRewardItem implements Serializable {
        private static final long serialVersionUID = 1L;
        double amount;
        int remainingDays; // 紀錄該利潤要等幾天

        PendingRewardItem(double amount, int remainingDays) {
            this.amount = amount;
            this.remainingDays = remainingDays;
        }
    }
    private List<PendingRewardItem> rewardList = new ArrayList<>();

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

        for (int i = 1; i <= 20; i++) {
            String name = prePrefix[rand.nextInt(prePrefix.length)] + midPrefix[rand.nextInt(midPrefix.length)] + suffix[rand.nextInt(suffix.length)] + " " + i + "號";
            double rate = 0.70 + (rand.nextDouble() * 0.10 - 0.05);
            double reward = 1.25 + (rand.nextDouble() * 0.10 - 0.05);
            double cost = 500_000 + rand.nextInt(300_000);
            drugs.add(new Drug(name, Drug.DrugType.PREVENTIVE, rate, cost, cost * 1.5, reward, 2));
        }

        for (int i = 1; i <= 15; i++) {
            String name = "感冒" + midPrefix[rand.nextInt(midPrefix.length)] + suffix[rand.nextInt(suffix.length)] + " " + (char)('A' + (i % 26)) + "型";
            double rate = 0.50 + (rand.nextDouble() * 0.10 - 0.05);
            double reward = 1.50 + (rand.nextDouble() * 0.10 - 0.05);
            double cost = 1_000_000 + rand.nextInt(500_000);
            int days = 3 + rand.nextInt(3);
            drugs.add(new Drug(name, Drug.DrugType.COLD, rate, cost, cost * 1.8, reward, days));
        }

        for (int i = 1; i <= 10; i++) {
            String name = sciPrefix[rand.nextInt(sciPrefix.length)] + midPrefix[rand.nextInt(midPrefix.length)] + chemSuffix[rand.nextInt(chemSuffix.length)];
            double rate = 0.20 + (rand.nextDouble() * 0.10 - 0.05);
            double reward = 2.00 + (rand.nextDouble() * 0.10 - 0.05);
            double cost = 5_000_000 + rand.nextInt(2_000_000);
            drugs.add(new Drug(name, Drug.DrugType.SPECIAL, rate, cost, cost * 3.0, reward, 7));
        }

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

        double actualCost = drug.getDynamicCost(this.costDiscount);
        if (!deductMoney(actualCost)) {
            systemMessage = "❌ 資金不足！";
            return false;
        }

        // ⏱️ 套用技術減免，計算並得到實質工期天數
        drug.calculateAndSetCooldown(this.rndLevel);
        int finalCooldownDays = drug.getRemainingCooldownDays();

        // 🎲 當下即時判定結果
        boolean success = drug.tryDevelop(successBonus);

        if (success) {
            double brandLevel = this.brandValue * 10;
            double finalRewardMultiplier = drug.getRewardMultiplier() + (0.1 * brandLevel);
            double rewardAmount = actualCost * finalRewardMultiplier;

            // 利潤加入等待隊列，等工期倒數完畢後入帳
            rewardList.add(new PendingRewardItem(rewardAmount, finalCooldownDays));

            // 🎯 【核心修正】成功上市後打上標記，再移出清單防止讀檔死鎖
            drug.setLaunched(true);
            this.drugs.remove(drug);

            systemMessage = String.format("🎉 解盲成功！【%s】進入生產排程。工期為 %d 天，工期結束隔日到帳 $%.0f 萬！",
                    drug.getName(), finalCooldownDays, rewardAmount / 10000);
        } else {
            // ❌ 研發失敗：不移除藥物，藥物會保留在清單中
            systemMessage = String.format("💥 實驗失敗！【%s】進入 %d 天設備調校冷卻期。",
                    drug.getName(), finalCooldownDays);

            if (drug.getType() == Drug.DrugType.NARCOTIC) {
                if (Math.random() < 0.90) {
                    Random rand = new Random();
                    this.lockdownTurns = 3 + rand.nextInt(5);
                    double penaltyFine = actualCost * 2;
                    this.money -= penaltyFine;

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

    /**
     * ⏱️ 每日換日
     */
    public void tick() {
        systemMessage = "";
        double totalSettledToday = 0;

        List<PendingRewardItem> toRemove = new ArrayList<>();
        for (PendingRewardItem item : rewardList) {
            if (item.remainingDays <= 0) {
                totalSettledToday += item.amount;
                toRemove.add(item);
            } else {
                item.remainingDays--;
            }
        }
        rewardList.removeAll(toRemove);

        if (totalSettledToday > 0) {
            earnMoney(totalSettledToday);
            systemMessage = "INCOME:" + totalSettledToday;
        }

        if (lockdownTurns > 0) {
            lockdownTurns--;
            for (Drug drug : drugs) {
                drug.advanceDay();
            }
            return;
        }

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

    public void addEfficiency(double value) { this.rndLevel += value; }
    public void addSuccessBonus(double value) { this.costDiscount += value; }
    public void addBrandValue(double value) { this.brandValue += value; }

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