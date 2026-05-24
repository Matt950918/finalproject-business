package game.model.tech;

import java.util.ArrayList;
import java.util.List;

public class TechSystem {

    // 💰 統一初始資金為 5000 萬
    private double money = 50_000_000;

    // 🤖 科技業專屬科技樹：AI 研究度
    private int aiResearchLevel = 0;

    // 正在執行中的商務合約
    private List<TechContract> activeContracts = new ArrayList<>();

    // ==========================================
    // 💰 科技業專屬金庫與金流控制 (防呆機制)
    // ==========================================

    // 確實從金庫扣款的防呆機制
    public boolean deductMoney(double amount) {
        if (money >= amount) {
            money -= amount;
            return true;
        }
        return false;
    }

    // 賺取收入
    public void earnMoney(double amount) {
        money += amount;
    }

    // ==========================================
    // 🤝 商務合約管理
    // ==========================================
    public void signContract(TechContract contract) {
        activeContracts.add(contract);
        System.out.println("📝 成功與 " + contract.getPartnerName() + " 簽署合約！");
    }

    // ==========================================
    // ⏱ 每回合運作 (Tick)
    // ==========================================
    public void tick() {
        List<TechContract> toRemove = new ArrayList<>();
        double totalTickProfit = 0;

        for (TechContract contract : activeContracts) {
            // 結算這份合約本期的毛利 (可能賺錢也可能燒錢)
            double margin = contract.processTick();

            // 🤖 AI 科技樹加成：自動化降低常規營運成本 (每級額外省 2% 總成本)
            if (margin < 0) {
                margin += Math.abs(margin) * (aiResearchLevel * 0.02);
            }

            totalTickProfit += margin;
            money += margin;

            // 合約到期，移除
            if (contract.getDurationTicks() <= 0) {
                System.out.println("📜 來自 " + contract.getPartnerName() + " 的合約已履行完畢。");
                toRemove.add(contract);
            }
        }

        activeContracts.removeAll(toRemove);
        System.out.println("📊 本期科技業供應鏈總毛利結算: $" + String.format("%.0f", totalTickProfit));
    }

    // ==========================================
    // 🤖 AI 科技樹升級系統
    // ==========================================
    public boolean upgradeAIResearch() {
        // 升級費用隨等級指數成長：100萬, 200萬, 400萬...
        double upgradeCost = 1_000_000 * Math.pow(2, aiResearchLevel);

        if (deductMoney(upgradeCost)) {
            aiResearchLevel++;
            System.out.println("🚀 AI 研究度升級成功！當前等級：" + aiResearchLevel);
            System.out.println("   (談判成功率提升！自動化降本能力增強！)");
            return true;
        } else {
            System.out.println("❌ 資金不足，無法升級 AI 實驗室。需要資金：" + upgradeCost);
            return false;
        }
    }

    // ==========================================
    // 🔍 Getters
    // ==========================================
    public double getMoney() { return money; }
    public int getAiResearchLevel() { return aiResearchLevel; }
    public int getActiveContractsCount() { return activeContracts.size(); }
}