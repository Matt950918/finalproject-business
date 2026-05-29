package game.model.tech;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TechSystem implements Serializable {
    private static final long serialVersionUID = 1L;

    private double money = 50_000_000;
    private int aiResearchLevel = 0;
    private List<TechContract> activeContracts = new ArrayList<>();

    // 晶片設計系統等級（預設 0 代表未解鎖）
    private int designToolsLevel = 0;

    private int fireLockdownTurns = 0;

    public int getFireLockdownTurns() { return fireLockdownTurns; }
    public void triggerFireLockdown(int turns) { this.fireLockdownTurns = turns; }



    public boolean deductMoney(double amount) {
        if (money >= amount) {
            money -= amount;
            return true;
        }
        return false;
    }

    public void earnMoney(double amount) {
        money += amount;
    }

    public void signContract(TechContract contract) {
        activeContracts.add(contract);
    }

    public void tick() {
        // 🎯 【火災停工防呆】：如果遭遇火災停業，天數照減，但今天公司「完全沒有金流收益」！
        if (fireLockdownTurns > 0) {
            fireLockdownTurns--;
            System.out.println("🚨 科技工廠因火災事故清理停業中... 剩餘 " + fireLockdownTurns + " 天。");

            // 產線雖然停工，但執行中的合約工期天數依然會扣減消耗（合約有時效性）
            for (TechContract contract : activeContracts) {
                contract.processTick();
            }
            // 移除到期的合約
            List<TechContract> toRemove = new ArrayList<>();
            for (TechContract contract : activeContracts) {
                if (contract.getDurationTicks() <= 0) {
                    toRemove.add(contract);
                }
            }
            activeContracts.removeAll(toRemove);
            return; // 🛑 直接中斷，今天不加也不扣任何供應鏈的錢
        }
        List<TechContract> toRemove = new ArrayList<>();
        double totalTickProfit = 0;

        int upstreamCount = 0;
        for (TechContract contract : activeContracts) {
            if (contract.getRevenue() == 0 && contract.getCost() > 0) {
                upstreamCount++;
            }
        }

        for (TechContract contract : activeContracts) {
            double revenue = contract.getRevenue();
            double cost = contract.getCost();

            if (revenue > 0 && upstreamCount > 0) {
                revenue *= (1.0 + (upstreamCount * 0.50));
            }

            double margin = revenue - cost;

            if (margin < 0) {
                margin += Math.abs(margin) * (aiResearchLevel * 0.02);
            }

            totalTickProfit += margin;
            money += margin;

            contract.processTick();

            if (contract.getDurationTicks() <= 0) {
                toRemove.add(contract);
            }
        }

        activeContracts.removeAll(toRemove);
    }

    public boolean upgradeAIResearch() {
        double upgradeCost = 1_000_000 * Math.pow(2, aiResearchLevel);
        if (deductMoney(upgradeCost)) {
            aiResearchLevel++;
            return true;
        }
        return false;
    }

    // 獲取晶片設計系統當前等級
    public int getDesignToolsLevel() {
        return designToolsLevel;
    }

    // 計算下一次升級費用：800萬 -> 2000萬 -> 5000萬 -> 1.25億
    public double getDesignToolsUpgradeCost() {
        return 8_000_000 * Math.pow(2.5, designToolsLevel);
    }

    // 執行升級
    public boolean upgradeDesignTools() {
        double cost = getDesignToolsUpgradeCost();
        if (deductMoney(cost)) {
            designToolsLevel++;
            return true;
        }
        return false;
    }

    public double getMoney() { return money; }
    public int getAiResearchLevel() { return aiResearchLevel; }
    public int getActiveContractsCount() { return activeContracts.size(); }
    public void setMoney(double money) { this.money = money; }
}