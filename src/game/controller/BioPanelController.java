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
    @FXML private Label lblRnDStatus;           // 用來顯示科技一：研發能力等級
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

    // 🎯 新增：管制毒品 FXML 元件
    @FXML private Label lblCostNarcotic;
    @FXML private Label lblStatusNarcotic;
    @FXML private Button btnResearchNarcotic;

    private BioSystem bioSystem;
    private BioTechTree techTree;
    private MainGameController mainController;

    private Drug preventiveDrug;
    private Drug coldDrug;
    private Drug specialDrug;
    private Drug narcoticDrug; // 🎯 新增：管制毒品實體物件

    public void initData(BioSystem bioSystem, MainGameController mainController) {
        this.bioSystem = bioSystem;
        this.mainController = mainController;
        this.techTree = new BioTechTree(bioSystem);

        // 傳入 7 個參數：名稱、枚舉、成功率、成本、售價、解盲報酬倍率、基礎工期天數
        if (preventiveDrug == null) {
            preventiveDrug = new Drug("廣效型流感疫苗", Drug.DrugType.PREVENTIVE, 0.70, 1000000, 2500000, 1.25, 2);
        }
        if (coldDrug == null) {
            coldDrug = new Drug("高效速效感冒膠囊", Drug.DrugType.COLD, 0.50, 500000, 1200000, 1.50, 4);
        }
        if (specialDrug == null) {
            specialDrug = new Drug("次世代抗癌特效藥", Drug.DrugType.SPECIAL, 0.20, 5000000, 15000000, 2.00, 7);
        }
        // 🎯 新增：初始化管制毒品（2% 超低成功率、高暴利、基礎 12 天工期）
        if (narcoticDrug == null) {
            narcoticDrug = new Drug("【管制】精神活性毒品", Drug.DrugType.NARCOTIC, 0.02, 15000000, 120000000, 10.00, 12);
        }

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

        // 🎯 【精確記帳優化】：檢查是否有剛剛滿期、正式到帳的營收
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

        // 2. 每日換日重置當日研發次數上限 (3/3)
        if (preventiveDrug != null) preventiveDrug.resetDailyCount();
        if (coldDrug != null) coldDrug.resetDailyCount();
        if (specialDrug != null) specialDrug.resetDailyCount();
        if (narcoticDrug != null) narcoticDrug.resetDailyCount();

        syncMoneyToMain();
        updateStatusLabels();
    }

    @FXML private void handleResearchPreventive() { executeResearch(preventiveDrug); }
    @FXML private void handleResearchCold() { executeResearch(coldDrug); }
    @FXML private void handleResearchSpecial() { executeResearch(specialDrug); }
    @FXML private void handleResearchNarcotic() { executeResearch(narcoticDrug); }

    /**
     * 🔬 核心研發控管與限制邏輯（即時判定結果 + 工期鎖定天數版）
     */
    private void executeResearch(Drug drug) {
        if (bioSystem.getLockdownTurns() > 0) {
            showAlert(Alert.AlertType.WARNING, "廠房封鎖", "❌ 廠房正遭勒令停工中！剩餘 " + bioSystem.getLockdownTurns() + " 天解封。");
            return;
        }

        if (!drug.isAvailable()) {
            showAlert(Alert.AlertType.WARNING, "產線排程中", "❌ 該藥物產線仍在使用中！剩餘工期/設備調校天數: " + drug.getRemainingCooldownDays() + " 天。");
            return;
        }

        if (drug.getDailyResearchCount() >= 3) {
            showAlert(Alert.AlertType.WARNING, "研發限制", drug.getName() + " 今日臨床實驗次數已達上限（3/3 次）。");
            return;
        }

        syncMainToBio();

        double actualCost = drug.getDynamicCost(bioSystem.getCostDiscount());
        if (mainController.getPlayerCompany().getCash() < actualCost) {
            showAlert(Alert.AlertType.WARNING, "資金不足", "集團資金不足！無法支付臨床實驗費用。");
            return;
        }

        // 🎯 核心修正一：在實質扣款前，正式將研發實驗的開銷成本寫入流水帳明細
        mainController.getPlayerCompany().recordTransaction(
                "↳ [第 " + mainController.getCurrentDay() + " 天] 🧪 啟動臨床實驗 - " + drug.getName() + "：-$" + mainController.formatMoney(actualCost)
        );

        // 🎲 執行研發核心
        boolean success = bioSystem.researchDrug(drug);

        String systemMsg = bioSystem.getSystemMessage();

        if (success) {
            // 🎯 核心修正二：補上研發解盲成功的明細紀錄
            mainController.getPlayerCompany().recordTransaction(
                    "↳ [第 " + mainController.getCurrentDay() + " 天] 🎉 臨床解盲大成功 - " + drug.getName() + " 通過三期上市評估！"
            );
            showAlert(Alert.AlertType.INFORMATION, "🎉 研發成功！", systemMsg);
        } else {
            // 🎯 核心修正三：補上研發解盲失敗的明細紀錄
            mainController.getPlayerCompany().recordTransaction(
                    "↳ [第 " + mainController.getCurrentDay() + " 天] ❌ 臨床解盲失敗 - " + drug.getName() + " 數據未達標，宣布報廢。"
            );
            showAlert(Alert.AlertType.ERROR, "❌ 臨床實驗失敗", systemMsg);
        }

        syncMoneyToMain();
        updateStatusLabels();
    }

    // ==========================================
    // 🌳 技術專利升級事件處理（全部補上 recordTransaction）
    // ==========================================
    @FXML private void handleUpgradeRnD() {
        syncMainToBio();
        if (mainController.getPlayerCompany().getCash() < 3000000) return;
        bioSystem.deductMoney(3000000);
        techTree.upgradeRnD();

        // 🎯 修正：寫入研發縮時設備升級明細
        mainController.getPlayerCompany().recordTransaction(
                "↳ [第 " + mainController.getCurrentDay() + " 天] 🔬 生科專利升級 - 購置研發縮時設備：-$300.00 萬"
        );

        syncMoneyToMain();
        updateStatusLabels();
    }

    @FXML private void handleUpgradeBrand() {
        syncMainToBio();
        if (mainController.getPlayerCompany().getCash() < 2500000) return;
        bioSystem.deductMoney(2500000);
        techTree.upgradeBrand();

        // 🎯 修正：寫入品牌形象推廣明細
        mainController.getPlayerCompany().recordTransaction(
                "↳ [第 " + mainController.getCurrentDay() + " 天] 📢 生科專利升級 - 投放全球醫學期刊形象：-$250.00 萬"
        );

        syncMoneyToMain();
        updateStatusLabels();
    }

    @FXML private void handleUpgradeEfficiency() {
        syncMainToBio();
        if (mainController.getPlayerCompany().getCash() < 2000000) return;
        bioSystem.deductMoney(2000000);
        techTree.upgradeEfficiency();

        // 🎯 修正：寫入生產開銷優化明細
        mainController.getPlayerCompany().recordTransaction(
                "↳ [第 " + mainController.getCurrentDay() + " 天] 💰 生科專利升級 - 精進自動化生產開銷：-$200.00 萬"
        );

        syncMoneyToMain();
        updateStatusLabels();
    }

    // ==========================================
// 🔄 UI 狀態刷新
// ==========================================
    public void updateStatusLabels() {
        if (bioSystem == null) return;

        lblRnDStatus.setText(String.format("🔬 研發縮時等級: LV.%.0f (工期減免 %.0f%%)", bioSystem.getRndLevel(), bioSystem.getRndLevel() * 5));
        lblSuccessBonusStatus.setText(String.format("💰 生產開銷折讓: -%.0f%%", bioSystem.getCostDiscount() * 100));
        lblBrandStatus.setText(String.format("📢 品牌行銷成功使商品漲價: +%.0f%%", bioSystem.getBrandValue() * 100));

        if (lblEfficiencyStatus != null) {
            if (bioSystem.getLockdownTurns() > 0) {
                lblEfficiencyStatus.setText(String.format("廠房動態：🚨 遭檢警全面查封停工中！(剩餘 %d 天)", bioSystem.getLockdownTurns()));
                lblEfficiencyStatus.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
            } else {
                lblEfficiencyStatus.setText("廠房動態：🟢 運作正常");
                lblEfficiencyStatus.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
        }

        // 1. 預防類藥物：判斷是否已成功研發（從底層清單被移除）
        if (preventiveDrug != null && !bioSystem.getDrugs().contains(preventiveDrug)) {
            preventiveDrug = null;
        }
        if (preventiveDrug != null) {
            lblCostPreventive.setText("研發成本: $" + String.format("%,.0f", preventiveDrug.getDynamicCost(bioSystem.getCostDiscount()) / 10000) + " 萬");
            updateDrugRowUI(preventiveDrug, btnResearchPreventive, lblStatusPreventive);
        } else {
            lblStatusPreventive.setText("狀態：已成功上市");
            lblStatusPreventive.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            btnResearchPreventive.setText("已上市");
            btnResearchPreventive.setDisable(true);
        }

        // 2. 感冒類藥物：判斷是否已成功研發
        if (coldDrug != null && !bioSystem.getDrugs().contains(coldDrug)) {
            coldDrug = null;
        }
        if (coldDrug != null) {
            lblCostCold.setText("研發成本: $" + String.format("%,.0f", coldDrug.getDynamicCost(bioSystem.getCostDiscount()) / 10000) + " 萬");
            updateDrugRowUI(coldDrug, btnResearchCold, lblStatusCold);
        } else {
            lblStatusCold.setText("狀態：已成功上市");
            lblStatusCold.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            btnResearchCold.setText("已上市");
            btnResearchCold.setDisable(true);
        }

        // 3. 特效類藥物：判斷是否已成功研發
        if (specialDrug != null && !bioSystem.getDrugs().contains(specialDrug)) {
            specialDrug = null;
        }
        if (specialDrug != null) {
            lblCostSpecial.setText("研發成本: $" + String.format("%,.0f", specialDrug.getDynamicCost(bioSystem.getCostDiscount()) / 10000) + " 萬");
            updateDrugRowUI(specialDrug, btnResearchSpecial, lblStatusSpecial);
        } else {
            lblStatusSpecial.setText("狀態：已成功上市");
            lblStatusSpecial.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            btnResearchSpecial.setText("已上市");
            btnResearchSpecial.setDisable(true);
        }

        // 4. 管制類藥物（毒藥）：判斷是否已成功研發
        if (narcoticDrug != null && !bioSystem.getDrugs().contains(narcoticDrug)) {
            narcoticDrug = null;
        }
        if (narcoticDrug != null) {
            lblCostNarcotic.setText("研發成本: $" + String.format("%,.0f", narcoticDrug.getDynamicCost(bioSystem.getCostDiscount()) / 10000) + " 萬");
            updateDrugRowUI(narcoticDrug, btnResearchNarcotic, lblStatusNarcotic);
        } else {
            lblStatusNarcotic.setText("狀態：已成功上市");
            lblStatusNarcotic.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            btnResearchNarcotic.setText("已上市");
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

        if (!drug.isAvailable()) {
            statusLabel.setText("🧪 產線工期鎖定中... 剩餘 " + drug.getRemainingCooldownDays() + " 天");
            statusLabel.setStyle("-fx-text-fill: #9b59b6; -fx-font-weight: bold;");
            button.setDisable(true);
            button.setText("研發排程中");
            return;
        }

        int remaining = 3 - drug.getDailyResearchCount();

        if (remaining <= 0) {
            statusLabel.setText("今日次數已耗盡 (0/3)");
            statusLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
            button.setDisable(true);
        } else {
            String statusText = String.format("今日剩餘次數: %d/3 次", remaining);
            statusLabel.setText(statusText);

            if (drug.isDiscovered()) {
                statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                button.setText("再次研發上市 ➔");
            } else {
                if (drug.getType() == Drug.DrugType.NARCOTIC) {
                    statusLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                    button.setText("啟動秘密研發 ➔");
                } else {
                    statusLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                    button.setText("啟動臨床實驗 ➔");
                }
            }
            button.setDisable(false);
        }
    }

    private void syncMainToBio() { if (mainController != null && mainController.getPlayerCompany() != null && bioSystem != null) { bioSystem.setMoney(mainController.getPlayerCompany().getCash()); } }
    private void syncMoneyToMain() { if (mainController != null && mainController.getPlayerCompany() != null && bioSystem != null) { mainController.getPlayerCompany().spendCash(mainController.getPlayerCompany().getCash()); mainController.getPlayerCompany().earnCash(bioSystem.getMoney()); mainController.updateStatusLabels(); } }
    public void updateBioTitle() { if (mainController != null && mainController.getPlayerCompany() != null && lblBioTitle != null) { lblBioTitle.setText("🧬 " + mainController.getPlayerCompany().getName() + " - 生物科技研發中心"); } }
    private void showAlert(Alert.AlertType type, String title, String content) { Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait(); }
}