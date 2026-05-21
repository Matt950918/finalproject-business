package game.model.bio;

import java.util.ArrayList;
import java.util.List;

public class BioCompany {

    private String name;
    private double money;

    private double successBonus;   // 研發成功率加成
    private double brandValue;     // 社會形象
    private double efficiency;     // 成本降低

    private List<Drug> drugs;

    public BioCompany(String name, double money) {
        this.name = name;
        this.money = money;
        this.drugs = new ArrayList<>();
    }

    public void addDrug(Drug drug) {
        drugs.add(drug);
    }

    // 研發藥物
    public boolean researchDrug(Drug drug) {

        double cost = drug.getCost() * (1 - efficiency);

        if (money < cost) {
            System.out.println("資金不足");
            return false;
        }

        money -= cost;

        boolean success = drug.tryDevelop(successBonus);

        if (success) {
            System.out.println(drug.getName() + " 研發成功！");
        } else {
            System.out.println(drug.getName() + " 研發失敗");
        }

        return success;
    }

    // 銷售收入
    public void sellDrug(Drug drug, double demand) {
        if (!drug.isDiscovered()) return;

        double revenue = demand * drug.getPrice() * (1 + brandValue);

        money += revenue;
    }

    // getters / setters
    public double getMoney() { return money; }

    public void addSuccessBonus(double value) {
        successBonus += value;
    }

    public void addBrandValue(double value) {
        brandValue += value;
    }

    public void addEfficiency(double value) {
        efficiency += value;
    }
}
