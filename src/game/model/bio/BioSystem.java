package game.model.bio;

import java.util.ArrayList;
import java.util.List;

/**
 * 生技業核心邏輯系統
 * 負責處理新藥研發、成功機率運算與科技樹數值
 */
public class BioSystem {

    private double money = 50_000_000;
    private double successBonus = 0;   // 研發成功率加成
    private double brandValue = 0;     // 社會形象
    private double efficiency = 0;     // 成本降低率 (例如 0.1 代表 10% 成本節省)

    private List<Drug> drugs = new ArrayList<>();

    // ==========================================
    // 💸 核心金流管理
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

    // ==========================================
    // 🔬 藥物研發系統 (核心邏輯：品牌與效率的應用)
    // ==========================================
    public boolean researchDrug(Drug drug) {
        // 使用 efficiency 計算實際研發成本
        double cost = drug.getCost() * (1.0 - efficiency);

        if (!deductMoney(cost)) {
            return false;
        }

        // 使用 successBonus 進行擲骰機率運算
        boolean success = drug.tryDevelop(successBonus);

        return success;
    }

    public void sellDrug(Drug drug, double demand) {
        // 品牌形象會加成銷售利潤
        double revenue = demand * drug.getPrice() * (1.0 + brandValue);
        earnMoney(revenue);
    }

    // ==========================================
    // ⚙️ 屬性存取器 (Getters & Setters)
    // ==========================================
    public double getMoney() { return money; }
    public void setMoney(double money) { this.money = money; }

    public double getSuccessBonus() { return successBonus; }
    public double getBrandValue() { return brandValue; }

    // 為了讓 Controller 能正確顯示「百分比」(如 100% 研發效率)，
    // 我們回傳 1.0 + efficiency (預設 efficiency 為 0，回傳 1.0)
    public double getEfficiencyRate() { return 1.0 + efficiency; }
    public double getEfficiency() { return efficiency; }

    // 科技樹呼叫這些方法來更新狀態
    public void addSuccessBonus(double value) { this.successBonus += value; }
    public void addBrandValue(double value) { this.brandValue += value; }
    public void addEfficiency(double value) { this.efficiency += value; }

    public void tick() {
        // 這裡是你原本預留的邏輯區塊
        // 如果未來有需要隨時間變化的數值（例如：廣告效果隨時間衰退），都可以在這處理
        System.out.println("BioSystem 執行每回合更新...");
    }
}