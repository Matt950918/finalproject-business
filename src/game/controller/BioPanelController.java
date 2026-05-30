package game.controller;

import game.model.bio.BioSystem;
import game.model.bio.BioTechTree;
import game.model.bio.Drug;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class BioPanelController {

    // ==========================================
    // 🏷️ FXML 元件綁定
    // ==========================================
    @FXML private Label lblBioTitle;
    @FXML private Label lblRnDStatus;           // 用來顯示科技一：研發能力與解盲機率等級
    @FXML private Label lblSuccessBonusStatus;   // 用來顯示科技三：生產開銷優化折讓
    @FXML private Label lblBrandStatus;          // 用來顯示科技二：品牌價值
    @FXML private Label lblEfficiencyStatus;     // 廠房動態標籤（正常 / 遭查封）

    // 藥物組件：預防藥
    @FXML private Label lblCostPreventive;
    @FXML private Label lblStatusPreventive;
    @FXML private Button btnResearchPreventive;

    // 藥物組件：感冒藥
    @FXML private Label lblCostCold;
    @FXML private Label lblStatusCold;
    @FXML private Button btnResearchCold;

    // 藥物組件：特效藥
    @FXML private Label lblCostSpecial;
    @FXML private Label lblStatusSpecial;
    @FXML private Button btnResearchSpecial;

    // 🎯 管制毒品 FXML 元件
    @FXML private Label lblCostNarcotic;
    @FXML private Label lblStatusNarcotic;
    @FXML private Button btnResearchNarcotic;

    // 🎯 核心修正：綁定 FXML 的四大藥物區塊大主標 Label
    @FXML private Label lblTitlePreventive;
    @FXML private Label lblTitleCold;
    @FXML private Label lblTitleSpecial;
    @FXML private Label lblTitleNarcotic;

    private BioSystem bioSystem;
    private BioTechTree techTree;
    private MainGameController mainController;

    public void initData(BioSystem bioSystem, MainGameController mainController) {
        this.bioSystem = bioSystem;
        this.mainController = mainController;
        this.techTree = new BioTechTree(bioSystem);

        syncMainToBio();
        updateStatusLabels();
        updateBioTitle();
    }

    /**
     * 🌅 每日換日事件處理
     */
    public void onNextDay() {
        syncMainToBio();

        // 1. 推進生技系統每日進程 (遞減全藥物工期、遞減全廠警方查封天數、處理滿期利潤)
        if (bioSystem != null) {
            bioSystem.tick();
        }

        // 【精確記帳優化】：檢查是否有剛剛滿期、正式到帳的營收
        String sysMsg = bioSystem.getSystemMessage();
        if (sysMsg != null && sysMsg.startsWith("INCOME:")) {
            try {
                double dailyIncome = Double.parseDouble(sysMsg.split(":")[1]);
                if (dailyIncome > 0) {
                    mainController.getPlayerCompany().recordTransaction(
                            "↳ [第 " + mainController.getCurrentDay() + " 天] 💰 產線工期結束！新藥利潤今日正式到帳：+$" + mainController.formatMoney(dailyIncome)
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        syncMoneyToMain();
        updateStatusLabels();
    }

    @FXML private void handleResearchPreventive() {
        Drug currentDrug = (Drug) btnResearchPreventive.getUserData();
        if (currentDrug != null) executeResearch(currentDrug);
    }

    @FXML private void handleResearchCold() {
        Drug currentDrug = (Drug) btnResearchCold.getUserData();
        if (currentDrug != null) executeResearch(currentDrug);
    }

    @FXML private void handleResearchSpecial() {
        Drug currentDrug = (Drug) btnResearchSpecial.getUserData();
        if (currentDrug != null) executeResearch(currentDrug);
    }

    @FXML private void handleResearchNarcotic() {
        Drug currentDrug = (Drug) btnResearchNarcotic.getUserData();
        if (currentDrug != null) executeResearch(currentDrug);
    }

    /**
     * 🔬 核心研發控管與限制邏輯（防連點與洗錢安全加強版）
     */
    private void executeResearch(Drug drug) {
        // 1. 第一線防線：提早檢查狀態，不合規就提早彈出提示
        if (bioSystem.getLockdownTurns() > 0) {
            showAlert(Alert.AlertType.WARNING, "廠房封鎖", "❌ 廠房正遭勒令停工中！剩餘 " + bioSystem.getLockdownTurns() + " 天解封。");
            return;
        }

        if (!drug.isAvailable()) {
            showAlert(Alert.AlertType.WARNING, "產線排程中", "❌ 【" + drug.getName() + "】正處於工期鎖定中！\n剩餘工期/設備調校天數: " + drug.getRemainingCooldownDays() + " 天。");
            return;
        }

        if (drug.isLaunched()) {
            showAlert(Alert.AlertType.WARNING, "請勿重複投產", "❌ 該藥物今日已成功解盲，請等待明日換日刷新！");
            return;
        }

        syncMainToBio();

        double actualCost = drug.getDynamicCost(bioSystem.getCostDiscount());
        if (mainController.getPlayerCompany().getCash() < actualCost) {
            showAlert(Alert.AlertType.WARNING, "資金不足", "集團資金不足！無法支付臨床實驗費用.");
            return;
        }

        // 📝 記帳流水
        mainController.getPlayerCompany().recordTransaction(
                "↳ [第 " + mainController.getCurrentDay() + " 天] 🧪 啟動臨床實驗 - " + drug.getName() + "：-$" + mainController.formatMoney(actualCost)
        );

        int beforeLockdown = bioSystem.getLockdownTurns();

        // 🎲 2. 真正交由後端擲骰子判定 (🎯 移除原本前端的越權鎖定，改由後端完全控管)
        boolean success = bioSystem.researchDrug(drug);
        String systemMsg = bioSystem.getSystemMessage();

        // 3. 瞬間重刷 UI，讓按鈕即時變更狀態
        updateStatusLabels();

        // 4. 根據「真實的骰子結果」精確彈窗
        if (success) {
            mainController.getPlayerCompany().recordTransaction(
                    "↳ [第 " + mainController.getCurrentDay() + " 天] 🎉 臨床解盲大成功 - " + drug.getName() + " 通過三期上市評估！"
            );
            showAlert(Alert.AlertType.INFORMATION, "🎉 研發成功！", systemMsg);
        } else {
            mainController.getPlayerCompany().recordTransaction(
                    "↳ [第 " + mainController.getCurrentDay() + " 天] ❌ 臨床解盲失敗 - " + drug.getName() + " 數據未達標，宣布報廢。"
            );

            if (drug.getType() == Drug.DrugType.NARCOTIC && bioSystem.getLockdownTurns() > beforeLockdown) {
                double penaltyFine = actualCost * 2;
                mainController.getPlayerCompany().recordTransaction(
                        "↳ [第 " + mainController.getCurrentDay() + " 天] 🚨 💥 東窗事發！研發毒品遭檢警查獲，追加勒令停業行政罰鍰：-$" + mainController.formatMoney(penaltyFine)
                );
            }
            showAlert(Alert.AlertType.ERROR, "❌ 臨床實驗失敗", systemMsg);
        }

        syncMoneyToMain();
        updateStatusLabels();
    }

    // ==========================================
    // 🌳 技術專利升級事件處理 (統一修正為 200 萬)
    // ==========================================
    @FXML private void handleUpgradeRnD() {
        syncMainToBio();
        if (mainController.getPlayerCompany().getCash() < 2000000) {
            showAlert(Alert.AlertType.WARNING, "資金不足", "集團資金不足！無法支付專利升級費用。");
            return;
        }

        mainController.getPlayerCompany().spendCash(2000000);
        bioSystem.setMoney(mainController.getPlayerCompany().getCash());

        techTree.upgradeRnD(); // 💡 內部會自動同步加強成功率與縮減天數
        mainController.getPlayerCompany().recordTransaction("↳ [第 " + mainController.getCurrentDay() + " 天] 🔬 生科專利升級 - 購置研發縮時與 AI 機率雙加強設備：-$200.00 萬");

        mainController.updateStatusLabels();
        updateStatusLabels();
    }

    @FXML private void handleUpgradeBrand() {
        syncMainToBio();
        if (mainController.getPlayerCompany().getCash() < 2000000) {
            showAlert(Alert.AlertType.WARNING, "資金不足", "集團資金不足！無法支付專利升級費用. ");
            return;
        }

        mainController.getPlayerCompany().spendCash(2000000);
        bioSystem.setMoney(mainController.getPlayerCompany().getCash());

        techTree.upgradeBrand();
        mainController.getPlayerCompany().recordTransaction("↳ [第 " + mainController.getCurrentDay() + " 天] 📢 專利升級 - 投放全球醫學期刊形象：-$200.00 萬");

        mainController.updateStatusLabels();
        updateStatusLabels();
    }

    @FXML private void handleUpgradeEfficiency() {
        syncMainToBio();
        if (mainController.getPlayerCompany().getCash() < 2000000) {
            showAlert(Alert.AlertType.WARNING, "資金不足", "集團資金不足！無法支付專利升級費用。");
            return;
        }

        mainController.getPlayerCompany().spendCash(2000000);
        bioSystem.setMoney(mainController.getPlayerCompany().getCash());

        techTree.upgradeEfficiency();
        mainController.getPlayerCompany().recordTransaction("↳ [第 " + mainController.getCurrentDay() + " 天] ⚙️ 專利升級 - 精進自動化生產開銷：-$200.00 萬");

        mainController.updateStatusLabels();
        updateStatusLabels();
    }

    // ==========================================
    // 🔄 UI 狀態刷新與同步
    // ==========================================
    public void updateStatusLabels() {
        if (bioSystem == null) return;

        // 🎯【完美相容】：在不改動 FXML 的前提下，第一個標籤同時動態呈現「工期減免」與實質疊加的「解盲成功率」！
        lblRnDStatus.setText(String.format("🔬 研發縮時: LV.%.0f (天數減免 %.0f%% / 解盲率 +%.0f%%)",
                bioSystem.getRndLevel(), bioSystem.getRndLevel() * 5, bioSystem.getSuccessBonus() * 100));

        lblSuccessBonusStatus.setText(String.format("💰 生產開銷折讓: -%.0f%%", bioSystem.getCostDiscount() * 100));
        lblBrandStatus.setText(String.format("📢 品牌行銷成功使商品漲價: +%.0f%%", bioSystem.getBrandValue() * 100));

        // 更新廠房查封狀態
        if (lblEfficiencyStatus != null) {
            if (bioSystem.getLockdownTurns() > 0) {
                lblEfficiencyStatus.setText(String.format("廠房動態：🚨 遭檢警全面查封停工中！(剩餘 %d 天)", bioSystem.getLockdownTurns()));
                lblEfficiencyStatus.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
            } else {
                lblEfficiencyStatus.setText("廠房動態：🟢 運作正常");
                lblEfficiencyStatus.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
        }

        // 從後端的隨機大清單中，動態篩選出每一種型態目前還存在、排序第一順位的隨機藥物
        Drug currentPreventive = bioSystem.getDrugs().stream().filter(d -> d.getType() == Drug.DrugType.PREVENTIVE).findFirst().orElse(null);
        Drug currentCold = bioSystem.getDrugs().stream().filter(d -> d.getType() == Drug.DrugType.COLD).findFirst().orElse(null);
        Drug currentSpecial = bioSystem.getDrugs().stream().filter(d -> d.getType() == Drug.DrugType.SPECIAL).findFirst().orElse(null);
        Drug currentNarcotic = bioSystem.getDrugs().stream().filter(d -> d.getType() == Drug.DrugType.NARCOTIC).findFirst().orElse(null);

        // 【1. 預防類藥物】
        if (currentPreventive != null) {
            if (lblTitlePreventive != null) {
                lblTitlePreventive.setText(currentPreventive.getName() + " (Preventive)");
            }
            lblCostPreventive.setText("研發成本: $" + String.format("%,.0f", currentPreventive.getDynamicCost(bioSystem.getCostDiscount()) / 10000) + " 萬");
            updateDrugRowUI(currentPreventive, btnResearchPreventive, lblStatusPreventive);
            btnResearchPreventive.setUserData(currentPreventive);
        } else {
            if (lblTitlePreventive != null) lblTitlePreventive.setText("預防類藥物 (Preventive)");
            lblStatusPreventive.setText("狀態：🎉 預防類所有隨機新藥皆已成功上市！");
            lblStatusPreventive.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            btnResearchPreventive.setText("研發圓滿");
            btnResearchPreventive.setDisable(true);
        }

        // 【2. 感冒類藥物】
        if (currentCold != null) {
            if (lblTitleCold != null) {
                lblTitleCold.setText(currentCold.getName() + " (Cold)");
            }
            lblCostCold.setText("研發成本: $" + String.format("%,.0f", currentCold.getDynamicCost(bioSystem.getCostDiscount()) / 10000) + " 萬");
            updateDrugRowUI(currentCold, btnResearchCold, lblStatusCold);
            btnResearchCold.setUserData(currentCold);
        } else {
            if (lblTitleCold != null) lblTitleCold.setText("感冒類藥物 (Cold)");
            lblStatusCold.setText("狀態：🎉 感冒類所有隨機新藥皆已成功上市！");
            lblStatusCold.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            btnResearchCold.setText("研發圓滿");
            btnResearchCold.setDisable(true);
        }

        // 【3. 特效類藥物】
        if (currentSpecial != null) {
            if (lblTitleSpecial != null) {
                lblTitleSpecial.setText(currentSpecial.getName() + " (Special)");
            }
            lblCostSpecial.setText("研發成本: $" + String.format("%,.0f", currentSpecial.getDynamicCost(bioSystem.getCostDiscount()) / 10000) + " 萬");
            updateDrugRowUI(currentSpecial, btnResearchSpecial, lblStatusSpecial);
            btnResearchSpecial.setUserData(currentSpecial);
        } else {
            if (lblTitleSpecial != null) lblTitleSpecial.setText("特效標靶類藥物 (Special)");
            lblStatusSpecial.setText("狀態：🎉 特效標靶類所有隨機新藥皆已成功上市！");
            lblStatusSpecial.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            btnResearchSpecial.setText("研發圓滿");
            btnResearchSpecial.setDisable(true);
        }

        // 【4. 管制類藥物】
        if (currentNarcotic != null) {
            if (lblTitleNarcotic != null) {
                lblTitleNarcotic.setText(currentNarcotic.getName() + " (Narcotic)");
            }
            lblCostNarcotic.setText("研發成本: $" + String.format("%,.0f", currentNarcotic.getDynamicCost(bioSystem.getCostDiscount()) / 10000) + " 萬");
            updateDrugRowUI(currentNarcotic, btnResearchNarcotic, lblStatusNarcotic);
            btnResearchNarcotic.setUserData(currentNarcotic);
        } else {
            if (lblTitleNarcotic != null) lblTitleNarcotic.setText("管制類藥物 (Narcotic)");
            lblStatusNarcotic.setText("狀態：🎉 管制地下毒品系列已全部攻克完畢！");
            lblStatusNarcotic.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            btnResearchNarcotic.setText("研發圓滿");
            btnResearchNarcotic.setDisable(true);
        }
    }

    private void updateDrugRowUI(Drug drug, Button button, Label statusLabel) {
        if (drug == null || button == null || statusLabel == null) return;

        if (bioSystem.getLockdownTurns() > 0) {
            statusLabel.setText("🚨 廠房封鎖中！暫停一切常規運作");
            statusLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
            button.setDisable(true);
            button.setText("暫停開工");
            return;
        }

        if (drug.isLaunched()) {
            statusLabel.setText("🎉 研發成功！生產排程中...");
            statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            button.setDisable(true);
            button.setText("已排程");
            return;
        }

        if (!drug.isAvailable()) {
            statusLabel.setText("💥 研發失敗！設備調校中... 剩餘 " + drug.getRemainingCooldownDays() + " 天");
            statusLabel.setStyle("-fx-text-fill: #e74a3b; -fx-font-weight: bold;");
            button.setDisable(true);
            button.setText("調校中");
            return;
        }

        statusLabel.setText("🟢 產線設備已就緒，可隨時投產");
        statusLabel.setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold;");

        if (drug.getType() == Drug.DrugType.NARCOTIC) {
            button.setText("啟動秘密研發 ➔");
        } else {
            button.setText("啟動臨床實驗 ➔");
        }
        button.setDisable(false);
    }

    private void syncMainToBio() {
        if (mainController != null && mainController.getPlayerCompany() != null && bioSystem != null) {
            bioSystem.setMoney(mainController.getPlayerCompany().getCash());
        }
    }

    private void syncMoneyToMain() {
        if (mainController != null && mainController.getPlayerCompany() != null && bioSystem != null) {
            mainController.getPlayerCompany().setCash(bioSystem.getMoney());
            mainController.updateStatusLabels();
        }
    }

    public void updateBioTitle() {
        if (mainController != null && mainController.getPlayerCompany() != null && lblBioTitle != null) {
            lblBioTitle.setText("🧬 " + mainController.getPlayerCompany().getName() + " - 生物科技研發中心");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        // 確保彈窗在多平台或不同 JavaFX 視窗層級下都能正確顯示
        alert.showAndWait();
    }
}