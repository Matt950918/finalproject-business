package game.model.bio;

public class BioTechTree {

    private BioSystem system; // 👈 改為綁定 BioSystem

    public BioTechTree(BioSystem system) {
        this.system = system;
    }

    // 🔬 提升成功率
    public void upgradeRnD() {
        system.addSuccessBonus(0.05); // +5%
        System.out.println("研發能力提升！");
    }

    // 📢 社會形象
    public void upgradeBrand() {
        system.addBrandValue(0.1); // +10%需求
        System.out.println("品牌形象提升！");
    }

    // ⚙️ 效率提升
    public void upgradeEfficiency() {
        system.addEfficiency(0.05); // -5%成本
        System.out.println("生產效率提升！");
    }
}