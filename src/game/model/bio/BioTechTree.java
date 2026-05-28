package game.model.bio;

import java.io.Serializable;

public class BioTechTree implements Serializable {
    private static final long serialVersionUID = 1L;

    private BioSystem system;

    public BioTechTree(BioSystem system) {
        this.system = system;
    }

    /**
     * 🔬 升級核心研發能力 (原本的臨床成功率 $\rightarrow$ 改為縮短工期天數)
     * 🎯 關鍵改動：每次升級將技術等級實質 +1（在 BioSystem 中會換算成工期縮短 5% 且向上取整）
     */
    public void upgradeRnD() {
        // 💡 這裡傳入 1.0 代表等級加 1，用來觸發新系統的天數公式計算
        system.addEfficiency(1.0);
        System.out.println("⏱️ [科技樹升級] 研發技術突破：全藥物研發工期縮減 5% (天數向上取整)！");
    }

    /**
     * 📢 提升品牌形象
     * 每次升級讓銷售藥物時的市場需求與報酬溢價 +10%
     */
    public void upgradeBrand() {
        system.addBrandValue(0.1); // +10% 溢價報酬
        System.out.println("📢 [科技樹升級] 品牌公關形象提升：藥物銷售溢價 +10%！");
    }

    /**
     * ⚙️ 生產效率提升
     * 每次升級讓研發藥物時的基礎金錢成本減免 5%
     */
    public void upgradeEfficiency() {
        // 💡 回歸你最原始的設定，傳入 0.05 進行純金錢成本折讓
        system.addSuccessBonus(0.05); // 如果你係統內是用這個加成，或是改用對應的降成本 method
        System.out.println("⚙️ [科技樹升級] 生產效率提升：研發與生產金錢開銷優化 5%！");
    }
}