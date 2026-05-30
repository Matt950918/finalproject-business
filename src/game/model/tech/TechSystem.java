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

    // 🆕 新增：自研晶片設計系統等級（預設 0 級，代表未解鎖）
    private int designToolsLevel = 0;

    // 🆕 新增：晶片工廠火災停業剩餘天數（預設 0 天）
    private int fireLockdownTurns = 0;

    public int getDesignToolsLevel() {
        return designToolsLevel;
    }

    /**
     * 🆕 新增：計算自研晶片設計系統的升級費用（隨等級指數成長）
     * 基礎 800 萬，之後每次升級乘 2.5 倍：800萬 -> 2000萬 -> 5000萬 -> 1.25億...
     */
    public double getDesignToolsUpgradeCost() {
        return 8_000_000 * Math.pow(2.5, designToolsLevel);
    }

    /**
     * 🆕 新增：升級晶片設計系統邏輯
     */
    public boolean upgradeDesignTools() {
        double cost = getDesignToolsUpgradeCost();
        if (deductMoney(cost)) {
            designToolsLevel++;
            System.out.println("💻 晶片設計系統升級成功！當前等級：Lv." + designToolsLevel);
            return true;
        }
        return false;
    }

    // ==========================================
    // 🆕 新增：火災停業控制方法 (供外部 Controller 呼叫)
    // ==========================================
    public int getFireLockdownTurns() {
        return fireLockdownTurns;
    }

    public void triggerFireLockdown(int turns) {
        this.fireLockdownTurns = turns;
    }

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
    // ⏱ 每回合運作 (Tick) - 採購與供應鏈連動優化版
    // ==========================================
    public void tick() {
        // 🎯 【生科級停業機制】：如果還在火災停工天數內，天數減 1，且今天公司「完全不結算任何收益金流」！
        if (fireLockdownTurns > 0) {
            fireLockdownTurns--;
            System.out.println("🚨 晶片工廠火災清理中，今日產線完全停擺。剩餘天數：" + fireLockdownTurns);

            // 產線雖然停工，但執行中的舊合約工期天數依然要倒數消耗（合約具有時效性）
            for (TechContract contract : activeContracts) {
                contract.processTick();
            }
            // 移除到期的合約
            activeContracts.removeIf(contract -> contract.getDurationTicks() <= 0);
            return; // 🛑 核心中斷：直接彈出，今天晶片半導體部門不赚也不扣任何供應鏈的錢！
        }

        List<TechContract> toRemove = new ArrayList<>();
        double totalTickProfit = 0;

        // 1. 先計算玩家目前手上有幾檔「上游採購」合約正在執行
        int upstreamCount = 0;
        for (TechContract contract : activeContracts) {
            if (contract.getRevenue() == 0 && contract.getCost() > 0) {
                upstreamCount++;
            }
        }

        // 2. 開始結算每檔合約的利潤
        for (TechContract contract : activeContracts) {
            double revenue = contract.getRevenue();
            double cost = contract.getCost();

            // 🎯 【核心好處連動】：如果玩家有簽上游採購，下游供應合約的營收大幅加成！
            // 每擁有一檔上游採購，下游營收暴增 20% (可依平衡性自行調整)
            if (revenue > 0 && upstreamCount > 0) {
                revenue *= (1.0 + (upstreamCount * 0.20));
            }

            // 結算本期淨利 (收入 - 成本)
            double margin = revenue - cost;

            // 🤖 AI 科技樹加成：自動化降低常規營運成本
            if (margin < 0) {
                margin += Math.abs(margin) * (aiResearchLevel * 0.02);
            }

            totalTickProfit += margin;
            money += margin;

            // 合約到期控制
            // 由於 TechContract 的 processTick 會自動減天數並回傳舊的相減值，
            // 為了不弄髒原本的時程，我們手動在這邊呼叫前進
            contract.processTick();

            if (contract.getDurationTicks() <= 0) {
                System.out.println("📜 來自 " + contract.getPartnerName() + " 的合約已履行完畢。");
                toRemove.add(contract);
            }
        }

        activeContracts.removeAll(toRemove);
        System.out.println("📊 本期晶片半導體業供應鏈總毛利結算: $" + String.format("%.0f", totalTickProfit));
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
    // 🔍 Getters & Setters
    // ==========================================
    public double getMoney() { return money; }
    public int getAiResearchLevel() { return aiResearchLevel; }
    public int getActiveContractsCount() { return activeContracts.size(); }

    public void setMoney(double money) {
        this.money = money; // 確保與外部 Company 的 cash 強制絕對同步！
    }
}