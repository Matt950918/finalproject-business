import game.model.bio.BioCompany;
import game.model.bio.Drug;
import game.model.bio.BioTechTree;

public class Main {

    public static void main(String[] args) {

        BioCompany company = new BioCompany("BioTech", 1000000);

        Drug d = new Drug("感冒藥A",
                Drug.DrugType.COLD,
                0.35,
                200000,
                50);

        company.addDrug(d);

        BioTechTree tech = new BioTechTree(company);

        tech.upgradeRnD();

        company.researchDrug(d);

        company.sellDrug(d, 10000);

        System.out.println(company.getMoney());
    }
}