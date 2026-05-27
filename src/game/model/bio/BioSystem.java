package game.model.bio;

import java.util.ArrayList;
import java.util.List;

public class BioSystem {

    private double money = 50_000_000;
    private double successBonus = 0;
    private double brandValue = 0;
    private double efficiency = 0; // 💡 在這裡，這個值純粹代表「刷新機率」（例如 0.15 = 15% 機率）

    private double pendingReward = 0;
    private List<Drug> drugs = new ArrayList<>();

    public boolean deductMoney(double amount) {
        if (money >= amount) {
            money -= amount;
            return true;
        }
        return false;
    }

    public void earnMoney(double amount) { money += amount; }
    public void earnCash(double amount) { this.money += amount; }

    // ==========================================
    // 🔬 藥物研發系統（已移除效率降成本）
    // ==========================================
    public boolean researchDrug(Drug drug) {
        // 💡 修正：不再傳入 efficiency，成本純粹看藥物自身的專利折讓
        double actualCost = drug.getDynamicCost();

        if (!deductMoney(actualCost)) {
            return false;
        }

        boolean success = drug.tryDevelop(successBonus);

        if (success) {
            double brandLevel = this.brandValue * 10;
            double rewardMultiplier = 1.5 + (0.1 * brandLevel);
            double rewardAmount = actualCost * rewardMultiplier;
            this.pendingReward += rewardAmount;
        }

        return success;
    }

    public void sellDrug(Drug drug, double demand) {
        double revenue = demand * drug.getPrice() * (1.0 + brandValue);
        earnMoney(revenue);
    }

    public void tick() {
        if (pendingReward > 0) {
            earnMoney(pendingReward);
            pendingReward = 0;
        }
    }

    // ==========================================
    // ⚙️ Getters & Setters
    // ==========================================
    public double getMoney() { return money; }
    public void setMoney(double money) { this.money = money; }
    public double getSuccessBonus() { return successBonus; }
    public double getBrandValue() { return brandValue; }
    public double getEfficiencyRate() { return efficiency; } // UI 顯示改為純機率
    public double getEfficiency() { return efficiency; }
    public double getPendingReward() { return pendingReward; }

    public void addSuccessBonus(double value) { this.successBonus += value; }
    public void addBrandValue(double value) { this.brandValue += value; }
    public void addEfficiency(double value) { this.efficiency += value; }
}