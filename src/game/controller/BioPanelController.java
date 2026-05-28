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

        // 🆕 核心優化：紀錄 tick 前的金庫餘額，用來捕捉生科「隔日讚助金入帳」的數值
        double beforeMoney = bioSystem.getMoney();

        // 1. 推進生技系統每日進程 (發放昨日收益、遞減全藥物工期、遞減全廠警方查封天數)
        if (bioSystem != null) {
            bioSystem.tick();
        }

        // 🆕 核心優化：如果換日結算後錢增加了，自動幫他們補上「研發讚助金入帳」明細
        double dailyIncome = bioSystem.getMoney() - beforeMoney;
        if (dailyIncome > 0) {
            mainController.getPlayerCompany().recordTransaction(
                    "↳ [第 " + mainController.getCurrentDay() + " 天] 💰 收到上市新藥之每日研發讚助營收：+$" + mainController.formatMoney(dailyIncome)
            );
        } else if (dailyIncome < 0) {
            mainController.getPlayerCompany().recordTransaction(
                    "↳ [第 " + mainController.getCurrentDay() + " 天] 🚨 扣除檢警全面查封之懲罰性罰金：-$" + mainController.formatMoney(Math.abs(dailyIncome))
            );
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
            showAlert(Alert.AlertType.WARNING, "產線排程中", "❌ 該藥物產線正忙！剩餘工期/設備調校天數: " + drug.getRemainingCooldownDays() + " 天。");
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
        lblBrandStatus.setText(String.format("📢 品牌營銷溢價: +%.0f%%", bioSystem.getBrandValue() * 100));

        if (lblEfficiencyStatus != null) {
            if (bioSystem.getLockdownTurns() > 0) {
                lblEfficiencyStatus.setText(String.format("廠房動態：🚨 遭檢警全面查封停工中！(剩餘 %d 天)", bioSystem.getLockdownTurns()));
                lblEfficiencyStatus.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
            } else {
                lblEfficiencyStatus.setText("廠房動態：🟢 運作正常");
                lblEfficiencyStatus.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
        }

        if (preventiveDrug != null) {
            lblCostPreventive.setText("研發成本: $" + String.format("%,.0f", preventiveDrug.getDynamicCost(bioSystem.getCostDiscount()) / 10000) + " 萬");
            updateDrugRowUI(preventiveDrug, btnResearchPreventive, lblStatusPreventive);
        }
        if (coldDrug != null) {
            lblCostCold.setText("研發成本: $" + String.format("%,.0f", coldDrug.getDynamicCost(bioSystem.getCostDiscount()) / 10000) + " 萬");
            updateDrugRowUI(coldDrug, btnResearchCold, lblStatusCold);
        }
        if (specialDrug != null) {
            lblCostSpecial.setText("研發成本: $" + String.format("%,.0f", specialDrug.getDynamicCost(bioSystem.getCostDiscount()) / 10000) + " 萬");
            updateDrugRowUI(specialDrug, btnResearchSpecial, lblStatusSpecial);
        }
        if (narcoticDrug != null) {
            lblCostNarcotic.setText("研發成本: $" + String.format("%,.0f", narcoticDrug.getDynamicCost(bioSystem.getCostDiscount()) / 10000) + " 萬");
            updateDrugRowUI(narcoticDrug, btnResearchNarcotic, lblStatusNarcotic);
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
            button.setText("明日請早");
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