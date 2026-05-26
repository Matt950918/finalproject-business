package game.model.bio;

import java.util.ArrayList;
import java.util.List;

/**
 * 生技業核心邏輯系統
 * 負責處理新藥研發、成功機率運算與科技樹數值
 */
public class BioSystem {

    // 統一初始資金：5000 萬
    private double money = 50_000_000;

    private double successBonus = 0;   // 研發成功率加成
    private double brandValue = 0;     // 社會形象
    private double efficiency = 0;     // 成本降低率

    private List<Drug> drugs = new ArrayList<>();

    // ==========================================
    // 💸 核心金流：確實從金庫扣款的防呆機制
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
    // 🔬 藥物研發系統
    // ==========================================
    public void addDrug(Drug drug) {
        drugs.add(drug);
    }

    public boolean researchDrug(Drug drug) {
        double cost = drug.getCost() * (1 - efficiency);

        if (!deductMoney(cost)) {
            System.out.println("❌ 資金不足，無法啟動 " + drug.getName() + " 的研發專案！");
            return false;
        }

        boolean success = drug.tryDevelop(successBonus);

        if (success) {
            System.out.println("✅ " + drug.getName() + " 研發成功！取得專利！");
        } else {
            System.out.println("💥 " + drug.getName() + " 臨床實驗失敗，研發經費付諸東流...");
        }

        return success;
    }

    public void sellDrug(Drug drug, double demand) {
        if (!drug.isDiscovered()) return;

        double revenue = demand * drug.getPrice() * (1 + brandValue);
        earnMoney(revenue);
    }

    // ==========================================
    // ⏱ 每回合運作 (預留給未來推進研發進度條)
    // ==========================================
    public void tick() {
        // 未來如果有「持續性銷售的藥品」或「需要跑好幾天的研發專案」，可以寫在這邊結算
    }

    // ==========================================
    // ⚙️ 科技樹與屬性 Getters / Setters
    // ==========================================
    public double getMoney() { return money; }
    public double getSuccessBonus() { return successBonus; } // 👈 補上這個
    public double getBrandValue() { return brandValue; }     // 👈 補上這個
    public double getEfficiency() { return efficiency; }     // 👈 補上這個

    public void addSuccessBonus(double value) { this.successBonus += value; }
    public void addBrandValue(double value) { this.brandValue += value; }
    public void addEfficiency(double value) { this.efficiency += value; }
    // 💡 請在 BioSystem 類別中找到管理 money 的地方，並補上這個公開的 Setter
    public void setMoney(double money) {
        this.money = money; // 確保與外部 Company 的 cash 強制絕對同步！
    }
}