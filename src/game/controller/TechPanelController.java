package game.controller;

import game.model.tech.TechContract;
import game.model.tech.TechSystem;
import game.model.tech.Tech_Partner;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class TechPanelController {

    @FXML private Label lblYield;
    @FXML private Label lblCost;
    @FXML private Label lblTechTitle;
    @FXML private VBox partnerListContainer;

    @FXML private Button btnBuyEDA; // 畫面對應的升級按鈕
    @FXML private Button btnUpgradeAI;

    private TechSystem techSystem;
    private MainGameController mainController;

    private static List<TechContract> availableContracts = new ArrayList<>();
    private static int lastRefreshDay = -1;

    public void initData(TechSystem techSystem, MainGameController mainController) {
        this.techSystem = techSystem;
        this.mainController = mainController;

        // 🎯 1. 核心修復：如果工廠火災停業中，強制把右側所有研發、系統升級按鈕全部關閉！
        if (techSystem.getFireLockdownTurns() > 0) {
            if (lblTechTitle != null) {
                lblTechTitle.setText(mainController.getPlayerCompany().getName() + " - 🚨 廠房火災封鎖重整中！");
                lblTechTitle.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;"); // 變成火災紅
            }

            if (btnBuyEDA != null) {
                btnBuyEDA.setDisable(true);
                btnBuyEDA.setText("🚨 工廠重整中：暫停晶片系統升級");
            }
            if (btnUpgradeAI != null) {
                btnUpgradeAI.setDisable(true);
                btnUpgradeAI.setText("🚨 工廠重整中：暫停 AI 實驗室研發");
            }
        } else {
            // 火災過去了，恢復原本正常的標題與按鈕狀態
            if (lblTechTitle != null) {
                lblTechTitle.setText(mainController.getPlayerCompany().getName() + " - 晶片半導體主控台");
                lblTechTitle.setStyle("");
            }
        }

        // 🎯 2. 核心修復：不論是不是今天刷新的合約，一律在這裡強迫重新載入一次左邊的 UI 列表！
        // 這樣只要一進來這個畫面，它就會立刻去判斷要顯示「警告文字」還是「合約卡片」
        if (availableContracts.isEmpty() && lastRefreshDay != mainController.getCurrentDay()) {
            refreshAvailableContracts();
            lastRefreshDay = mainController.getCurrentDay();
        }

        // 🔥 關鍵：確保這行在 initData 的最下面被呼叫，它才能精準抓到最新天數並清空列表！
        loadPartnerUI();
        updateStatusLabels();
    }

    public void updateTechTitle() {
        if (mainController != null && mainController.getPlayerCompany() != null && lblTechTitle != null) {
            String companyName = mainController.getPlayerCompany().getName();
            lblTechTitle.setText(companyName + " - 晶片研發主控台");
        }
    }

    private void refreshAvailableContracts() {
        availableContracts.clear();
        for (int i = 0; i < 3; i++) {
            availableContracts.add(Tech_Partner.generateRandomContract());
        }
    }

    @FXML
    private void handleBuyEDA() {
        double cost = techSystem.getDesignToolsUpgradeCost();

        if (mainController.getPlayerCompany().getCash() < cost) {
            showAlert("資金不足", "無法支付 $" + mainController.formatMoney(cost) + " 的高階設計系統研發費。");
            return;
        }

        int newLvl = techSystem.getDesignToolsLevel() + 1;
        if (techSystem.upgradeDesignTools()) {
            int nextProtectionRate = Math.min(100, newLvl * 25);

            mainController.getPlayerCompany().recordTransaction(
                    "↳ [第 " + mainController.getCurrentDay() + " 天] 系統研發 - 晶片設計系統升級至 Lv." + newLvl + "：-$" + mainController.formatMoney(cost)
            );

            showAlert("研發成功", String.format("高階晶片設計系統已升級至 Lv.%d！\n談判失敗時，大廠有 %d%% 的機率不會抽單，合約將安全保留原價！", newLvl, nextProtectionRate));
            syncMoneyToMain();
            updateStatusLabels();
            loadPartnerUI();
        }
    }

    @FXML
    private void handleUpgradeAI() {
        double upgradeCost = 1000000 * Math.pow(2, techSystem.getAiResearchLevel());

        if (mainController.getPlayerCompany().getCash() < upgradeCost) {
            showAlert("資金不足", "無法升級 AI 實驗室！需要資金：$" + mainController.formatMoney(upgradeCost));
            return;
        }

        if (techSystem.upgradeAIResearch()) {
            mainController.getPlayerCompany().recordTransaction("↳ [第 " + mainController.getCurrentDay() + " 天] 科技樹升級 - AI 實驗室 Lv." + techSystem.getAiResearchLevel() + "：-$" + mainController.formatMoney(upgradeCost));
            showAlert("升級成功", "AI 研究度升級成功！當前等級：Lv." + techSystem.getAiResearchLevel());
            syncMoneyToMain();
            updateStatusLabels();
            loadPartnerUI();
        }
    }

    private void loadPartnerUI() {
        // 1. 每次進來，先清空原本的合約卡片列表
        partnerListContainer.getChildren().clear();

        // 🎯 2. 【生科級清空列表提示】：如果工廠正處於火災停業中
        if (techSystem != null && techSystem.getFireLockdownTurns() > 0) {
            // 建立一個漂亮的警告 Label 塞在原本的列表位置
            Label lblAlert = new Label("🚨 廠房火災封鎖重整中！\n【 暫 停 一 切 商 務 談 判 3 天 】\n產線清理中，剩餘 " + techSystem.getFireLockdownTurns() + " 天。");
            lblAlert.setStyle("-fx-font-size: 16px; -fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-alignment: center; -fx-text-alignment: center;");

            // 讓警告文字撐滿整個容器並置中
            lblAlert.setMaxWidth(Double.MAX_VALUE);
            lblAlert.setMaxHeight(Double.MAX_VALUE);
            javafx.scene.layout.VBox.setVgrow(lblAlert, javafx.scene.layout.Priority.ALWAYS);

            // 把警告訊息加進清空後的列表，然後直接結束，不載入任何合約！
            partnerListContainer.getChildren().add(lblAlert);
            return;
        }

        // =======================================================
        // 3. 以下為你原本正常的合約載入邏輯（火災過去後自動恢復）
        // =======================================================
        if (availableContracts.isEmpty()) {
            Label lblNone = new Label("目前沒有可用的商務合約，請等待市場刷新。");
            lblNone.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");
            partnerListContainer.getChildren().add(lblNone);
            return;
        }

        // 遍歷目前的合約，並把卡片加進去
        for (TechContract contract : availableContracts) {
            javafx.scene.layout.VBox contractCard = createContractCard(contract);
            partnerListContainer.getChildren().add(contractCard);
        }
    }

    private VBox createContractCard(TechContract contract) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12; -fx-padding: 16; -fx-border-color: #ebe9f4; -fx-border-width: 1.5;");

        boolean isUpstream = contract.getRevenue() == 0;
        String typeTag = isUpstream ? "【上游採購】" : "【下游供應】";

        Label nameLbl = new Label(typeTag + contract.getPartnerName());
        nameLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + (isUpstream ? "#4E73DF" : "#1CC88A") + ";");

        Label descLbl = new Label(contract.getDescription());
        descLbl.setStyle("-fx-text-fill: #5A5C69; -fx-font-size: 13px;");
        descLbl.setWrapText(true);

        StringBuilder financeInfo = new StringBuilder();
        if (contract.getRevenue() > 0) financeInfo.append("每期收入: $").append(mainController.formatMoney(contract.getRevenue())).append("  ");
        if (contract.getCost() > 0) financeInfo.append("每期成本: $").append(mainController.formatMoney(contract.getCost())).append("\n");
        financeInfo.append("預估每期淨利: $").append(mainController.formatMoney(contract.getMargin())).append(" (合約期數: ").append(contract.getDurationTicks()).append(" 期)");

        Label financeLbl = new Label(financeInfo.toString());
        financeLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #858796; -fx-font-family: 'Courier New';");

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnNegotiate = new Button();
        btnNegotiate.setStyle("-fx-background-color: #f6f6f6; -fx-text-fill: #4e73df; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-border-color: #4e73df; -fx-border-radius: 6;");

        double baseRate = isUpstream ? 0.35 : 0.25;
        int currentWinRate = (int)((baseRate + (techSystem.getAiResearchLevel() * 0.05)) * 100);

        if (isUpstream) {
            btnNegotiate.setText(String.format("嘗試砍價 (成功率: %d%%)", currentWinRate));
        } else {
            btnNegotiate.setText(String.format("要求抬價 (成功率: %d%%)", currentWinRate));
        }

        if (contract.isNegotiated()) {
            btnNegotiate.setText("已完成談判");
            btnNegotiate.setDisable(true);
        }

        btnNegotiate.setOnAction(e -> {
            boolean success;
            int toolsLevel = techSystem.getDesignToolsLevel();
            double protectionChance = toolsLevel * 0.25;
            boolean isProtected = Math.random() < protectionChance;

            if (isUpstream) {
                success = contract.negotiateCostDown(techSystem.getAiResearchLevel());
                if (success) {
                    showAlert("談判成功", contract.getPartnerName() + " 同意了方案，成本打 75 折。");
                } else {
                    if (toolsLevel > 0 && isProtected) {
                        showAlert("談判失敗", contract.getPartnerName() + " 拒絕降價。但因為晶片設計系統發揮作用，大廠未採取抽單，合約保留原價。");
                    } else {
                        showAlert("談判破裂", contract.getPartnerName() + " 直接撤回了此項合作。");
                        availableContracts.remove(contract);
                    }
                }
            } else {
                success = contract.negotiateRevenueUp(techSystem.getAiResearchLevel());
                if (success) {
                    showAlert("談判成功", contract.getPartnerName() + " 同意提高採購報價 30%。");
                } else {
                    if (toolsLevel > 0 && isProtected) {
                        showAlert("談判失敗", contract.getPartnerName() + " 拒絕抬價。但因為晶片設計系統發揮作用，大廠未採取抽單，合約保留原價。");
                    } else {
                        showAlert("談判破裂", contract.getPartnerName() + " 轉頭尋找其他供應商，合約作廢。");
                        availableContracts.remove(contract);
                    }
                }
            }
            loadPartnerUI();
        });

        Button btnSign = new Button("簽署此合約");
        btnSign.setStyle("-fx-background-color: #4E73DF; -fx-text-fill: #FFFFFF; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

        btnSign.setOnAction(e -> {
            techSystem.signContract(contract);
            mainController.getPlayerCompany().recordTransaction(
                    "↳ [第 " + mainController.getCurrentDay() + " 天] 簽署供應鏈合約 - " + contract.getPartnerName()
            );
            availableContracts.remove(contract);
            showAlert("簽約成功", "合約已正式生效。");
            syncMoneyToMain();
            updateStatusLabels();
            loadPartnerUI();
        });

        actions.getChildren().addAll(btnNegotiate, btnSign);
        card.getChildren().addAll(nameLbl, descLbl, financeLbl, actions);
        return card;
    }

    private void syncMoneyToMain() {
        if (mainController != null && mainController.getPlayerCompany() != null) {
            mainController.getPlayerCompany().spendCash(mainController.getPlayerCompany().getCash());
            mainController.getPlayerCompany().earnCash(techSystem.getMoney());
            mainController.updateStatusLabels();
        }
    }

    private void updateStatusLabels() {
        if (techSystem != null) {
            lblYield.setText("AI 研究等級：Lv." + techSystem.getAiResearchLevel());
            lblCost.setText("進行中合約：" + techSystem.getActiveContractsCount() + " 檔");

            if (btnBuyEDA != null) {
                int currentLvl = techSystem.getDesignToolsLevel();
                double nextCost = techSystem.getDesignToolsUpgradeCost();
                int currentRate = Math.min(100, currentLvl * 25);

                if (currentLvl >= 4) {
                    btnBuyEDA.setText(String.format("設計系統已達頂級 Lv.%d (100%% 保底)", currentLvl));
                    btnBuyEDA.setDisable(true);
                } else {
                    btnBuyEDA.setText(String.format("升級晶片設計系統 (Lv.%d➔Lv.%d) | 費用: $%s (防抽單率: %d%%)",
                            currentLvl, currentLvl + 1, mainController.formatMoney(nextCost), currentRate));
                    btnBuyEDA.setDisable(false);
                }
            }

            if (btnUpgradeAI != null) {
                double cost = 1000000 * Math.pow(2, techSystem.getAiResearchLevel());
                btnUpgradeAI.setText(String.format("升級 AI 實驗室 (Lv.%d ➔ Lv.%d) | 費用: $%s",
                        techSystem.getAiResearchLevel(), techSystem.getAiResearchLevel() + 1, mainController.formatMoney(cost)));
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}