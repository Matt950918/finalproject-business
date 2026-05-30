package game.model.bio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BioSystem implements Serializable {
    private static final long serialVersionUID = 1L;

    private double money = 50_000_000;
    private double successBonus = 0;  // 🎯 基礎成功率加成（會隨著科技一同步提升！）
    private double brandValue = 0;    // 品牌溢價報酬

    private double rndLevel = 0;       // 科技一：研發能力等級（控制天數，每次升級 +1.0）
    private double costDiscount = 0;  // 科技三：生產效率折讓（控制金錢，每次升級 +0.05）

    // 用來追蹤每筆利潤的工期倒數
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
    String systemMessage = "";

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
        // 🎯【核心漏洞修復】第一線防禦：如果這顆藥今天已經解盲上市成功了，直接阻斷，防止在換日前被任何連點或惡意巨集洗錢！
        if (drug.isLaunched()) {
            systemMessage = "❌ 【" + drug.getName() + "】今日已成功通過臨床解盲，正在排程生產中，換日前請勿重複投產！";
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

        // 🎲 當下即時判定結果 (完美傳入修正後的 successBonus)
        boolean success = drug.tryDevelop(successBonus);

        if (success) {
            double brandLevel = this.brandValue * 10;
            double finalRewardMultiplier = drug.getRewardMultiplier() + (0.1 * brandLevel);
            double rewardAmount = actualCost * finalRewardMultiplier;

            // 利潤加入等待隊列，等工期倒數完畢後入帳
            rewardList.add(new PendingRewardItem(rewardAmount, finalCooldownDays));

            // 🎯 【核心修正】成功上市後打上標記，且在畫面上將按鈕鎖死。
            drug.setLaunched(true);

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
     * 🎯【核心優化】：引入每日研發次數重置，徹底解放研發必定失敗的 0% 機率陷阱！
     */
    public void tick() {
        systemMessage = "";
        double totalSettledToday = 0;

        // 1. 結算排程利潤 - 📜 100% 完美保留妳的等待利潤隊列
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

        // 🚨 廠房遭查封停工狀態下的換日處理 - 📜 100% 完美保留妳的檢警查封停業劇本
        if (lockdownTurns > 0) {
            lockdownTurns--;
            for (Drug drug : drugs) {
                drug.advanceDay();
                drug.resetDailyCount(); // 🎯 修正：即使停工重整中，換日也要將昨日研發次數歸零，避免復工後勝率被鎖死！
            }
            return;
        }

        // 2. 推進所有正常藥物的工期與次數重置
        List<Drug> drugsToRemove = new ArrayList<>();
        for (Drug drug : drugs) {
            drug.advanceDay(); // 工期倒數減 1 天

            // 🎯【關鍵修正】：每日換日時，通知留在畫面上的藥物執行歸零！
            // 呼叫 Drug 內部的 resetDailyCount()，將 dailyResearchCount 清空為 0
            // 這樣新的一天開始後，玩家就能重新享受最乾淨、沒有累積懲罰的公正基礎成功率！
            drug.resetDailyCount();

            // 🎯 100% 完美保留妳的核心機制：只要是「已研發成功」的藥物，換日時就正式移出清單，隔天直接刷新下一個新藥品品！
            if (drug.isLaunched()) {
                drugsToRemove.add(drug);
            }
        }
        drugs.removeAll(drugsToRemove);
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

    // =======================================================
    // 🎯 核心重構區：直接將成功率合併進科技一的點擊接口！
    // =======================================================
    public void addEfficiency(double value) {
        this.rndLevel += value;        // 1. 增加原本的天數控制等級
        this.successBonus += 0.05;    // 2. 🎯 【科技合併】同步在後端讓解盲率永久加 5%！
    }

    public void addSuccessBonus(double value) {
        // 空殼保留以防其他關聯類別報錯
    }

    public void addCostDiscount(double value) {
        this.costDiscount += value;   // 科技三：生產開銷折讓（維持不變）
    }

    public void addBrandValue(double value) {
        this.brandValue += value;     // 科技二：品牌行銷溢價（維持不變）
    }

    // ==========================================
    // Getters & Setters
    // ==========================================
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