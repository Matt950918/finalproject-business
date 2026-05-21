package game.model.bio;

public class BioTechTree {

    private BioCompany company;

    public BioTechTree(BioCompany company) {
        this.company = company;
    }

    // 🔬 提升成功率
    public void upgradeRnD() {
        company.addSuccessBonus(0.05); // +5%
        System.out.println("研發能力提升！");
    }

    // 📢 社會形象
    public void upgradeBrand() {
        company.addBrandValue(0.1); // +10%需求
        System.out.println("品牌形象提升！");
    }

    // ⚙️ 效率提升
    public void upgradeEfficiency() {
        company.addEfficiency(0.05); // -5%成本
        System.out.println("生產效率提升！");
    }
}
