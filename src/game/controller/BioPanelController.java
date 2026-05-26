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

    private BioSystem bioSystem;
    private BioTechTree techTree;
    private MainGameController mainController;

    public void initData(BioSystem bioSystem, MainGameController mainController) {
        this.bioSystem = bioSystem;
        this.mainController = mainController;
        this.techTree = new BioTechTree(bioSystem);
        updateStatusLabels();
    }

    @FXML
    private void handleResearchSpecial() {
        // 特效藥：基礎成功率 40%，成本 500 萬，成功基礎利潤 1500 萬
        Drug specialDrug = new Drug("次世代抗癌特效藥", Drug.DrugType.SPECIAL, 0.40, 5000000, 15000000);

        // 檢查資金
        if (mainController.getPlayerCompany().getCash() < specialDrug.getCost()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("研發資金不足");
            alert.setHeaderText(null);
            alert.setContentText("公司可用資金不足，無法啟動本筆 $" + (specialDrug.getCost()/10000.0) + " 萬的臨床新藥計畫！");
            alert.showAndWait();
            return;
        }

        // 寫入支出流水帳
        mainController.getPlayerCompany().recordTransaction("↳ [第 " + mainController.getCurrentDay() + " 天] 🔬 啟動新藥臨床實驗 - " + specialDrug.getName() + "：-$" + mainController.formatMoney(specialDrug.getCost()));

        // 執行底層研發機率擲骰子
        boolean success = bioSystem.researchDrug(specialDrug);

        if (success) {
            // 研發成功：立刻進行專利變現銷售
            bioSystem.sellDrug(specialDrug, 1.0);
            mainController.getPlayerCompany().recordTransaction("↳ [第 " + mainController.getCurrentDay() + " 天] 🎉 新藥解盲成功！上市專利分潤：+$" + mainController.formatMoney(specialDrug.getPrice()));
        }

        // 同步底層金庫與主畫面金額
        syncMoneyToMain();

        // 彈出解盲報告
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("臨床實驗結案報告");
        alert.setHeaderText(null);
        if (success) {
            alert.setContentText("🎉 【解盲成功】" + specialDrug.getName() + " 臨床數據極其驚艷！順利取得各國食藥署專利認證，全球訂單瘋湧而入！");
        } else {
            alert.setContentText("💥 【實驗失敗】" + specialDrug.getName() + " 三期臨床數據未達標。本次 $500 萬研發經費付諸東流，團隊將重新調整配方。");
        }
        alert.showAndWait();
    }

    @FXML
    private void handleUpgradeRnD() {
        if (mainController.getPlayerCompany().getCash() < 3000000) return;
        bioSystem.deductMoney(3000000);
        techTree.upgradeRnD();
        mainController.getPlayerCompany().recordTransaction("↳ [第 " + mainController.getCurrentDay() + " 天] 🌳 專利升級 - AI模擬設備：-$300.00 萬");
        syncMoneyToMain();
        updateStatusLabels();
    }

    @FXML
    private void handleUpgradeBrand() {
        if (mainController.getPlayerCompany().getCash() < 2500000) return;
        bioSystem.deductMoney(2500000);
        techTree.upgradeBrand();
        mainController.getPlayerCompany().recordTransaction("↳ [第 " + mainController.getCurrentDay() + " 天] 🌳 專利升級 - 醫學期刊投放：-$250.00 萬");
        syncMoneyToMain();
        updateStatusLabels();
    }

    @FXML
    private void handleUpgradeEfficiency() {
        if (mainController.getPlayerCompany().getCash() < 2000000) return;
        bioSystem.deductMoney(2000000);
        techTree.upgradeEfficiency();
        mainController.getPlayerCompany().recordTransaction("↳ [第 " + mainController.getCurrentDay() + " 天] 🌳 專利升級 - 低溫製程改良：-$200.00 萬");
        syncMoneyToMain();
        updateStatusLabels();
    }

    private void syncMoneyToMain() {
        mainController.getPlayerCompany().spendCash(mainController.getPlayerCompany().getCash());
        mainController.getPlayerCompany().earnCash(bioSystem.getMoney());
        mainController.updateStatusLabels();
    }

    private void updateStatusLabels() {
        // 1. 取得底層的加成數據
        double bonus = bioSystem.getSuccessBonus(); // 得到如 0.05, 0.10 等

        // 2. 將小數轉換為漂亮的百分比文字 (例如 0.05 -> "+5%")
        String bonusText = String.format("+%.0f%%", bonus * 100);

        // 3. 顯示在你的 Label 上
        lblSuccessBonusStatus.setText("研發成功率加成: " + bonusText);

        // 💡 提示：如果你還有其他 Label（例如顯示目前總機率、或品牌加成），也可以一併在這邊更新
        // 假設你未來有 lblBrandStatus，可以這樣寫：
        // lblBrandStatus.setText("品牌加成: " + String.format("+%.0f%%", bioSystem.getBrandValue() * 100));
    }
}