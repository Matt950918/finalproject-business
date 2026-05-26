package game.controller;

import game.model.bio.BioSystem;
import game.model.bio.BioTechTree;
import game.model.bio.Drug;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

public class BioPanelController {

    @FXML private Label lblRnDStatus;
    @FXML private Label lblSuccessBonusStatus;
    @FXML private Label lblBrandStatus;      // 必須與 FXML 的 fx:id 一致
    @FXML private Label lblEfficiencyStatus; // 必須與 FXML 的 fx:id 一致

    private BioSystem bioSystem;
    private BioTechTree techTree;
    private MainGameController mainController;

    public void initData(BioSystem bioSystem, MainGameController mainController) {
        this.bioSystem = bioSystem;
        this.mainController = mainController;
        this.techTree = new BioTechTree(bioSystem);
        updateStatusLabels();
    }

    // 必須補上這個方法，否則 FXML 的 onAction 會找不到它而崩潰
    @FXML
    private void handleResearchSpecial() {
        Drug specialDrug = new Drug("次世代抗癌特效藥", Drug.DrugType.SPECIAL, 0.40, 5000000, 15000000);
        if (mainController.getPlayerCompany().getCash() < specialDrug.getCost()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("資金不足！");
            alert.showAndWait();
            return;
        }
        boolean success = bioSystem.researchDrug(specialDrug);
        if (success) {
            bioSystem.sellDrug(specialDrug, 1.0);
        }
        syncMoneyToMain();
        updateStatusLabels();
    }

    @FXML
    private void handleUpgradeRnD() {
        if (mainController.getPlayerCompany().getCash() < 3000000) return;
        bioSystem.deductMoney(3000000);
        techTree.upgradeRnD();
        syncMoneyToMain();
        updateStatusLabels();
    }

    @FXML
    private void handleUpgradeBrand() {
        if (mainController.getPlayerCompany().getCash() < 2500000) return;
        bioSystem.deductMoney(2500000);
        techTree.upgradeBrand();
        syncMoneyToMain();
        updateStatusLabels();
    }

    @FXML
    private void handleUpgradeEfficiency() {
        if (mainController.getPlayerCompany().getCash() < 2000000) return;
        bioSystem.deductMoney(2000000);
        techTree.upgradeEfficiency();
        syncMoneyToMain();
        updateStatusLabels();
    }

    public void updateStatusLabels() {
        if (bioSystem == null) return;
        lblSuccessBonusStatus.setText("成功率加成: " + String.format("+%.0f%%", bioSystem.getSuccessBonus() * 100));
        lblBrandStatus.setText("品牌: " + String.format("%.1f", bioSystem.getBrandValue() * 100));
        lblEfficiencyStatus.setText("效率: " + String.format("%.0f%%", bioSystem.getEfficiencyRate() * 100));
    }

    private void syncMoneyToMain() {
        mainController.getPlayerCompany().spendCash(mainController.getPlayerCompany().getCash());
        mainController.getPlayerCompany().earnCash(bioSystem.getMoney());
        mainController.updateStatusLabels();
    }
}