package game.model.bio;

import java.util.ArrayList;
import java.util.List;

public class BioSystem {

    private double money = 50_000_000;
    private double successBonus = 0;
    private double brandValue = 0;
    private double efficiency = 0;

    private List<Drug> drugs = new ArrayList<>();

    // ==========================================
    // 💸 核心金流管理（直接重構優化）
    // ==========================================
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

    /**
     * 💡 直接在他這裡新增 earnCash，讓他配合我們主系統的呼叫習慣！
     */
    public void earnCash(double amount) {
        this.money += amount;
    }

    // ==========================================
    // 🔬 藥物研發系統
    // ==========================================
    public boolean researchDrug(Drug drug) {
        double cost = drug.getCost() * (1.0 - efficiency);
        if (!deductMoney(cost)) {
            return false;
        }
        return drug.tryDevelop(successBonus);
    }

    public void sellDrug(Drug drug, double demand) {
        double revenue = demand * drug.getPrice() * (1.0 + brandValue);
        earnMoney(revenue);
    }

    // ==========================================
    // ⚙️ 屬性存取器
    // ==========================================
    public double getMoney() { return money; }

    /**
     * 💡 確保有強力的 setMoney 可以直接同步主公司的總資金
     */
    public void setMoney(double money) { this.money = money; }

    public double getSuccessBonus() { return successBonus; }
    public double getBrandValue() { return brandValue; }
    public double getEfficiencyRate() { return 1.0 + efficiency; }
    public double getEfficiency() { return efficiency; }

    // 科技樹或抽卡可以直接呼叫這些方法來更新狀態
    public void addSuccessBonus(double value) { this.successBonus += value; }
    public void addBrandValue(double value) { this.brandValue += value; }
    public void addEfficiency(double value) { this.efficiency += value; }

    public void tick() {
        System.out.println("BioSystem 執行每回合更新...");
    }
}