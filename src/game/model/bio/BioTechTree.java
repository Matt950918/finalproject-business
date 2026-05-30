package game.model.bio;

import java.io.Serializable;

public class BioTechTree implements Serializable {
    private static final long serialVersionUID = 1L;

    private BioSystem system;

    public BioTechTree(BioSystem system) {
        this.system = system;
    }

    /**
     * 🔬 科技一：升級自動化排程 (天數縮減 5% ＋ 核心解盲成功率永久 +5% 兩大科技合併！)
     */
    public void upgradeRnD() {
        system.addEfficiency(1.0);       // 1. 減少排程工期天數
        system.addSuccessBonus(0.05);     // 2. 🎯 同步提升全藥物臨床成功率 5%！
        System.out.println("⏱️ [科技合併] 研發技術突破：全藥物工期縮減 5%，且解盲率永久 +5%！");
    }

    /**
     * 📢 科技二：提升品牌形象
     */
    public void upgradeBrand() {
        system.addBrandValue(0.1); // 漲價 +10%
    }

    /**
     * ⚙️ 科技三：生產效率提升 (研發開銷優化 -5%)
     */
    public void upgradeEfficiency() {
        system.addCostDiscount(0.05); // 金錢省 5%
    }
}