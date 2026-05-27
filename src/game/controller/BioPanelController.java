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
    @FXML private Label lblRnDStatus;
    @FXML private Label lblSuccessBonusStatus;
    @FXML private Label lblBrandStatus;
    @FXML private Label lblEfficiencyStatus; // 顯示全局基礎刷新率

    // 藥物組件
    @FXML private Label lblCostPreventive;
    @FXML private Label lblStatusPreventive;
    @FXML private Button btnResearchPreventive;

    @FXML private Label lblCostCold;
    @FXML private Label lblStatusCold;
    @FXML private Button btnResearchCold;

    @FXML private Label lblCostSpecial;
    @FXML private Label lblStatusSpecial;
    @FXML private Button btnResearchSpecial;

    private BioSystem bioSystem;
    private BioTechTree techTree;
    private MainGameController mainController;

    private Drug preventiveDrug;
    private Drug coldDrug;
    private Drug specialDrug;

    public void initData(BioSystem bioSystem, MainGameController mainController) {
        this.bioSystem = bioSystem;
        this.mainController = mainController;
        this.techTree = new BioTechTree(bioSystem);

        if (preventiveDrug == null) {
            preventiveDrug = new Drug("廣效型流感疫苗", Drug.DrugType.PREVENTIVE, 0.70, 1000000, 2500000);
        }
        if (coldDrug == null) {
            coldDrug = new Drug("高效速效感冒膠囊", Drug.DrugType.COLD, 0.85, 500000, 1200000);
        }
        if (specialDrug == null) {
            specialDrug = new Drug("次世代抗癌特效藥", Drug.DrugType.SPECIAL, 0.40, 5000000, 15000000);
        }

        syncMainToBio();
        updateStatusLabels();
        updateBioTitle();
    }

    public void onNextDay() {
        syncMainToBio();
        if (bioSystem != null) { bioSystem.tick(); }
        if (preventiveDrug != null) preventiveDrug.resetDailyCount();
        if (coldDrug != null) coldDrug.resetDailyCount();
        if (specialDrug != null) specialDrug.resetDailyCount();
        syncMoneyToMain();
        updateStatusLabels();
    }

    @FXML private void handleResearchPreventive() { executeResearch(preventiveDrug, btnResearchPreventive, lblStatusPreventive); }
    @FXML private void handleResearchCold() { executeResearch(coldDrug, btnResearchCold, lblStatusCold); }
    @FXML private void handleResearchSpecial() { executeResearch(specialDrug, btnResearchSpecial, lblStatusSpecial); }

    /**
     * 核心研發控管與限制邏輯（刷新機率動態腰斬版）
     */
    private void executeResearch(Drug drug, Button button, Label statusLabel) {
        if (drug.getDailyResearchCount() >= 3) {
            showAlert(Alert.AlertType.WARNING, "研發限制", drug.getName() + " 今日臨床實驗次數已達上限（3/3 次）。");
            return;
        }

        syncMainToBio();

        double actualCost = drug.getDynamicCost();
        if (mainController.getPlayerCompany().getCash() < actualCost) {
            showAlert(Alert.AlertType.WARNING, "資金不足", "集團資金不足！無法支付臨床實驗費用。");
            return;
        }

        // 🎯 1. 計算本次按下時真正的面板成功率 (不吃腰斬，回歸正常)
        double currentRate = drug.getBaseSuccessRate()
                + bioSystem.getSuccessBonus()
                + (drug.getTotalSuccessCount() * 0.05)
                - (drug.getDailyResearchCount() * 0.05);
        currentRate = Math.max(0.0, Math.min(1.0, currentRate)) * 100;

        // 🎯 2. 計算「本次判定時」該藥物實際的刷新機率 (基礎效率 * 藥物目前的刷新乘數)
        double baseChance = bioSystem.getEfficiency();
        double currentRefundChance = baseChance * drug.getRefreshRateMultiplier();

        // 🎲 3. 執行研發
        boolean success = bioSystem.researchDrug(drug);

        // ⚡ 4. 依據計算出的「當前刷新率」進行抽獎
        boolean isRefreshed = Math.random() < currentRefundChance;

        if (isRefreshed) {
            drug.refundDailyCount(); // 內部會回補次數，並讓該藥物的 refreshRateMultiplier 減半！
        }

        // 🎯 5. 計算「下一次點擊」將會套用的新刷新率，用來在警告中精準顯示
        double nextRefundChance = baseChance * drug.getRefreshRateMultiplier();

        // 建立刷新的警示文字
        String refreshMessage = isRefreshed
                ? String.format("\n\n⚡ 【過載運轉】：觸發了 %.0f%% 刷新機率！本次研發不消耗今日次數！\n⚠️ 【產線疲勞警告】：因設備過熱，當天該藥物的「次數刷新率」已被腰斬！下次刷新機率降為 %.1f%%！",
                currentRefundChance * 100, nextRefundChance * 100)
                : "";

        if (success) {
            bioSystem.sellDrug(drug, 1.0);
            double brandLevel = bioSystem.getBrandValue() * 10;
            double multiplier = 1.5 + (0.1 * brandLevel);
            double estimatedReward = actualCost * multiplier;

            showAlert(Alert.AlertType.INFORMATION, "🎉 研發成功！",
                    drug.getName() + " 成功上市！(本次成功率: " + String.format("%.0f%%", currentRate) + ")" + refreshMessage + "\n\n" +
                            "💰 【研發讚助】：明晨入帳: $" + String.format("%,.0f", estimatedReward)
            );
        } else {
            showAlert(Alert.AlertType.ERROR, "❌ 臨床實驗失敗",
                    drug.getName() + " 三期數據未達標。(本次成功率: " + String.format("%.0f%%", currentRate) + ")" + refreshMessage
            );
        }

        syncMoneyToMain();
        updateStatusLabels();
    }

    // ==========================================
    // 🌳 技術專利升級事件處理
    // ==========================================
    @FXML private void handleUpgradeRnD() { syncMainToBio(); if (mainController.getPlayerCompany().getCash() < 3000000) return; bioSystem.deductMoney(3000000); techTree.upgradeRnD(); syncMoneyToMain(); updateStatusLabels(); }
    @FXML private void handleUpgradeBrand() { syncMainToBio(); if (mainController.getPlayerCompany().getCash() < 2500000) return; bioSystem.deductMoney(2500000); techTree.upgradeBrand(); syncMoneyToMain(); updateStatusLabels(); }
    @FXML private void handleUpgradeEfficiency() { syncMainToBio(); if (mainController.getPlayerCompany().getCash() < 2000000) return; bioSystem.deductMoney(2000000); techTree.upgradeEfficiency(); syncMoneyToMain(); updateStatusLabels(); }

    // ==========================================
    // 🔄 UI 狀態刷新
    // ==========================================
    public void updateStatusLabels() {
        if (bioSystem == null) return;

        lblSuccessBonusStatus.setText("成功率加成: " + String.format("+%.0f%%", bioSystem.getSuccessBonus() * 100));
        lblBrandStatus.setText("品牌: " + String.format("%.1f", bioSystem.getBrandValue() * 100));

        // 顯示科技樹升級上去的「基礎刷新率」
        lblEfficiencyStatus.setText("基礎刷新率: " + String.format("%.0f%%", bioSystem.getEfficiency() * 100));
        if (lblRnDStatus != null) {
            lblRnDStatus.setText("🔬 基礎刷新率: " + String.format("%.0f%%", bioSystem.getEfficiency() * 100));
        }

        if (preventiveDrug != null) {
            lblCostPreventive.setText("研發成本: $" + String.format("%,.0f", preventiveDrug.getDynamicCost() / 10000) + " 萬");
            updateDrugRowUI(preventiveDrug, btnResearchPreventive, lblStatusPreventive);
        }
        if (coldDrug != null) {
            lblCostCold.setText("研發成本: $" + String.format("%,.0f", coldDrug.getDynamicCost() / 10000) + " 萬");
            updateDrugRowUI(coldDrug, btnResearchCold, lblStatusCold);
        }
        if (specialDrug != null) {
            lblCostSpecial.setText("研發成本: $" + String.format("%,.0f", specialDrug.getDynamicCost() / 10000) + " 萬");
            updateDrugRowUI(specialDrug, btnResearchSpecial, lblStatusSpecial);
        }
    }

    /**
     * 更新卡片 UI (包含動態顯示當前被腰斬後的刷新機率)
     */
    private void updateDrugRowUI(Drug drug, Button button, Label statusLabel) {
        if (drug == null || button == null || statusLabel == null) return;

        int remaining = 3 - drug.getDailyResearchCount();

        double nextRate = drug.getBaseSuccessRate()
                + bioSystem.getSuccessBonus()
                + (drug.getTotalSuccessCount() * 0.05)
                - (drug.getDailyResearchCount() * 0.05);
        nextRate = Math.max(0.0, Math.min(1.0, nextRate)) * 100;

        // 🎯 計算目前這款藥物當下真正的動態刷新率
        double currentDrugRefreshChance = bioSystem.getEfficiency() * drug.getRefreshRateMultiplier() * 100;

        if (remaining <= 0) {
            statusLabel.setText("今日次數已耗盡 (0/3)");
            statusLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
            button.setDisable(true);
            button.setText("明日請早");
        } else {
            String bonusTag = drug.getTotalSuccessCount() > 0 ? " (已享紅利)" : "";

            // 💡 修正：在面板上清晰印出這款藥物目前的【動態刷新率】，讓玩家一眼看出被腰斬了
            String refreshTag = String.format(" [當前刷新率: %.0f%%]", currentDrugRefreshChance);

            statusLabel.setText(String.format("今日剩餘: %d 次 | 下次機率: %.0f%%%s\n%s",
                    remaining, nextRate, bonusTag, refreshTag));

            if (drug.isDiscovered()) {
                statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                button.setText("再次研發上市 ➔");
            } else {
                statusLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                button.setText("啟動臨床實驗 ➔");
            }
            button.setDisable(false);
        }
    }

    private void syncMainToBio() { if (mainController != null && mainController.getPlayerCompany() != null && bioSystem != null) { bioSystem.setMoney(mainController.getPlayerCompany().getCash()); } }
    private void syncMoneyToMain() { if (mainController != null && mainController.getPlayerCompany() != null && bioSystem != null) { mainController.getPlayerCompany().spendCash(mainController.getPlayerCompany().getCash()); mainController.getPlayerCompany().earnCash(bioSystem.getMoney()); mainController.updateStatusLabels(); } }
    public void updateBioTitle() { if (mainController != null && mainController.getPlayerCompany() != null && lblBioTitle != null) { lblBioTitle.setText("🧬 " + mainController.getPlayerCompany().getName() + " - 生物科技研發中心"); } }
    private void showAlert(Alert.AlertType type, String title, String content) { Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait(); }
}