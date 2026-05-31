package game.controller;

import game.model.tech.TechContract;
import game.model.tech.TechSystem;
import game.model.tech.Tech_Partner;
import javafx.application.Platform;
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

        // 🛠️ 【資金同步】：確保子系統即時同步主公司的最新資金
        if (mainController != null && mainController.getPlayerCompany() != null) {
            this.techSystem.setMoney(mainController.getPlayerCompany().getCash());
        }

        // 🎯 1. 標題火災狀態控制（按鈕狀態已移至底端 updateStatusLabels 統一管理）
        if (techSystem.getFireLockdownTurns() > 0) {
            if (lblTechTitle != null) {
                lblTechTitle.setText(mainController.getPlayerCompany().getName() + " - 🚨 廠房火災封鎖重整中！");
                lblTechTitle.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;"); // 火災紅
            }
        } else {
            if (lblTechTitle != null) {
                lblTechTitle.setText(mainController.getPlayerCompany().getName() + " - 晶片半導體主控台");
                lblTechTitle.setStyle("");
            }
        }

        // 🎯 2. 【市場刷新】：天數改變或合約庫空了，一律強制刷新
        if (lastRefreshDay != mainController.getCurrentDay() || availableContracts.isEmpty()) {
            refreshAvailableContracts();
            lastRefreshDay = mainController.getCurrentDay();
        }

        // 🔥 關鍵：確保 UI 渲染與標籤更新在最後執行
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
            // 這裡把 AI 等級傳進去！
            availableContracts.add(Tech_Partner.generateRandomContract(techSystem.getAiResearchLevel()));
        }
    }

    @FXML
    private void handleBuyEDA() {
        double cost = techSystem.getDesignToolsUpgradeCost();

        if (mainController.getPlayerCompany().getCash() < cost) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("資金不足");
                alert.setHeaderText(null);
                alert.setContentText("無法支付 $" + mainController.formatMoney(cost) + " 的高階設計系統研發費。");
                alert.showAndWait();
            });
            return;
        }

        int newLvl = techSystem.getDesignToolsLevel() + 1;
        if (techSystem.upgradeDesignTools()) {
            // 💡 【修復核心】：直接扣除主公司資金，並同步更新子系統內部存儲的金額，防止舊金額覆蓋洗掉款項
            double remainingCash = mainController.getPlayerCompany().getCash() - cost;
            mainController.getPlayerCompany().setCash(remainingCash);
            techSystem.setMoney(remainingCash);

            int nextProtectionRate = Math.min(100, newLvl * 25);

            mainController.getPlayerCompany().recordTransaction(
                    "↳ [第 " + mainController.getCurrentDay() + " 天] 系統研發 - 晶片設計系統升級至 Lv." + newLvl + "：-$" + mainController.formatMoney(cost)
            );

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("研發成功");
                alert.setHeaderText(null);
                alert.setContentText(String.format("高階晶片設計系統已升級至 Lv.%d！\n談判失敗時，大廠有 %d%% 的機率不會抽單，合約將安全保留原價！", newLvl, nextProtectionRate));
                alert.showAndWait();
            });

            mainController.updateStatusLabels(); // 即時重新整理頂部主要 NavBar 資金數據
            updateStatusLabels();
            loadPartnerUI();
        }
    }

    @FXML
    private void handleUpgradeAI() {
        double upgradeCost = 1000000 * Math.pow(2, techSystem.getAiResearchLevel());

        if (mainController.getPlayerCompany().getCash() < upgradeCost) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("資金不足");
                alert.setHeaderText(null);
                alert.setContentText("無法升級 AI 實驗室！需要資金：$" + mainController.formatMoney(upgradeCost));
                alert.showAndWait();
            });
            return;
        }

        if (techSystem.upgradeAIResearch()) {
            // 💡 【修復核心】：直接扣除主公司資金，並同步更新子系統內部存儲的金額，防止舊金額覆蓋洗掉款項
            double remainingCash = mainController.getPlayerCompany().getCash() - upgradeCost;
            mainController.getPlayerCompany().setCash(remainingCash);
            techSystem.setMoney(remainingCash);

            mainController.getPlayerCompany().recordTransaction("↳ [第 " + mainController.getCurrentDay() + " 天] 科技樹升級 - AI 實驗室 Lv." + techSystem.getAiResearchLevel() + "：-$" + mainController.formatMoney(upgradeCost));

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("升級成功");
                alert.setHeaderText(null);
                alert.setContentText("AI 研究度升級成功！當前等級：Lv." + techSystem.getAiResearchLevel());
                alert.showAndWait();
            });

            mainController.updateStatusLabels(); // 即時重新整理頂部主要 NavBar 資金數據
            updateStatusLabels();
            loadPartnerUI();
        }
    }

    private void loadPartnerUI() {
        partnerListContainer.getChildren().clear();

        if (techSystem != null && techSystem.getFireLockdownTurns() > 0) {
            Label lblAlert = new Label("🚨 廠房火災封鎖重整中！\n【 暫 停 一 切 商 務 談 判 3 天 】\n產線清理中，剩餘 " + techSystem.getFireLockdownTurns() + " 天。");
            lblAlert.setStyle("-fx-font-size: 16px; -fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-alignment: center; -fx-text-alignment: center;");

            lblAlert.setMaxWidth(Double.MAX_VALUE);
            lblAlert.setMaxHeight(Double.MAX_VALUE);
            javafx.scene.layout.VBox.setVgrow(lblAlert, javafx.scene.layout.Priority.ALWAYS);

            partnerListContainer.getChildren().add(lblAlert);
            return;
        }

        if (availableContracts.isEmpty()) {
            Label lblNone = new Label("目前沒有可用的商務合約，請等待市場刷新。");
            lblNone.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");
            partnerListContainer.getChildren().add(lblNone);
            return;
        }

        for (TechContract contract : availableContracts) {
            javafx.scene.layout.VBox contractCard = createContractCard(contract);
            partnerListContainer.getChildren().add(contractCard);
        }
    }

    private VBox createContractCard(TechContract contract) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12; -fx-padding: 16; -fx-border-color: #ebe9f4; -fx-border-width: 1.5;");

        boolean isUpstream = contract.getRevenue() == 0;
        String typeTag = isUpstream ? "【上游技術授權/採購】" : "【下游客戶供應】";

        Label nameLbl = new Label(typeTag + contract.getPartnerName());
        nameLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + (isUpstream ? "#4E73DF" : "#1CC88A") + ";");

        Label descLbl = new Label(contract.getDescription());
        descLbl.setStyle("-fx-text-fill: #5A5C69; -fx-font-size: 13px;");
        descLbl.setWrapText(true);

        StringBuilder financeInfo = new StringBuilder();
        if (isUpstream) {
            financeInfo.append("每期授權費用支出: $").append(mainController.formatMoney(contract.getCost())).append("\n");
            financeInfo.append("📊 預估每期淨流出: $").append(mainController.formatMoney(contract.getCost()))
                    .append(" (合約效期: ").append(contract.getDurationTicks()).append(" 期)");
        } else {
            if (contract.getRevenue() > 0) financeInfo.append("每期合約收入: $").append(mainController.formatMoney(contract.getRevenue())).append("  ");
            if (contract.getCost() > 0) financeInfo.append("每期製造成本: $").append(mainController.formatMoney(contract.getCost())).append("\n");
            financeInfo.append("📊 預估每期利潤: $").append(mainController.formatMoney(contract.getMargin()))
                    .append(" (合約效期: ").append(contract.getDurationTicks()).append(" 期)");
        }

        Label financeLbl = new Label(financeInfo.toString());
        financeLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #858796; -fx-font-family: 'Courier New';");

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnNegotiate = new Button();
        btnNegotiate.setStyle("-fx-background-color: #f6f6f6; -fx-text-fill: #4e73df; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-border-color: #4e73df; -fx-border-radius: 6;");

        double baseRate = isUpstream ? 0.35 : 0.25;
        int currentWinRate = Math.min(100, (int)((baseRate + (techSystem.getAiResearchLevel() * 0.05)) * 100));

        if (isUpstream) {
            btnNegotiate.setText(String.format("嘗試向大廠砍價 (成功率: %d%%)", currentWinRate));
        } else {
            btnNegotiate.setText(String.format("要求抬價 (成功率: %d%%)", currentWinRate));
        }

        if (contract.isNegotiated()) {
            btnNegotiate.setText("已完成談判");
            btnNegotiate.setDisable(true);
        }

        btnNegotiate.setOnAction(e -> {
            // 🚨 火災即時攔截：防止玩家在事件觸發後仍能操作
            if (techSystem.getFireLockdownTurns() > 0) {
                Alert fireAlert = new Alert(Alert.AlertType.WARNING);
                fireAlert.setTitle("廠房封鎖中");
                fireAlert.setHeaderText(null);
                fireAlert.setContentText("廠房火災封鎖重整中，無法進行任何商務談判！");
                fireAlert.showAndWait();
                loadPartnerUI();
                return;
            }
            boolean success;
            int toolsLevel = techSystem.getDesignToolsLevel();
            double protectionChance = toolsLevel * 0.25;
            boolean isProtected = Math.random() < protectionChance;

            if (isUpstream) {
                success = contract.negotiateCostDown(techSystem.getAiResearchLevel());
                String title = success ? "談判成功" : (toolsLevel > 0 && isProtected ? "談判失敗" : "談判破裂");
                String msg = success ? contract.getPartnerName() + " 同意了方案，授權費成本打 75 折。"
                        : (toolsLevel > 0 && isProtected ? contract.getPartnerName() + " 拒絕降價。但因為晶片設計系統發揮作用，大廠未採取抽單，合約保留原價。"
                        : contract.getPartnerName() + " 直接撤回了此項合作。");

                if (!success && !(toolsLevel > 0 && isProtected)) {
                    availableContracts.remove(contract);
                }

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle(title);
                    alert.setHeaderText(null);
                    alert.setContentText(msg);
                    alert.showAndWait();
                });
            } else {
                success = contract.negotiateRevenueUp(techSystem.getAiResearchLevel());
                String title = success ? "談判成功" : (toolsLevel > 0 && isProtected ? "談判失敗" : "談判破裂");
                String msg = success ? contract.getPartnerName() + " 同意提高採購報價 30%。"
                        : (toolsLevel > 0 && isProtected ? contract.getPartnerName() + " 拒絕抬價。但因為晶片設計系統發揮作用，大廠未採取抽單，合約保留原價。"
                        : contract.getPartnerName() + " 轉頭尋找其他供應商，合約作廢。");

                if (!success && !(toolsLevel > 0 && isProtected)) {
                    availableContracts.remove(contract);
                }

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle(title);
                    alert.setHeaderText(null);
                    alert.setContentText(msg);
                    alert.showAndWait();
                });
            }
            loadPartnerUI();
        });

        Button btnSign = new Button("簽署此合約");
        btnSign.setStyle("-fx-background-color: #4E73DF; -fx-text-fill: #FFFFFF; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

        btnSign.setOnAction(e -> {
            // 🚨 火災即時攔截：防止玩家在事件觸發後仍能簽約
            if (techSystem.getFireLockdownTurns() > 0) {
                Alert fireAlert = new Alert(Alert.AlertType.WARNING);
                fireAlert.setTitle("廠房封鎖中");
                fireAlert.setHeaderText(null);
                fireAlert.setContentText("廠房火災封鎖重整中，無法簽署任何新合約！");
                fireAlert.showAndWait();
                loadPartnerUI();
                return;
            }
            double upfrontCost = (contract.getRevenue() == 0) ? contract.getCost() : 0;
            if (mainController.getPlayerCompany().getCash() < upfrontCost) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("資金不足");
                    alert.setHeaderText(null);
                    alert.setContentText("公司可用資金不足，無法支付首期建置費 $" + mainController.formatMoney(upfrontCost) + "！");
                    alert.showAndWait();
                });
                return;
            }

            if (upfrontCost > 0) {
                mainController.getPlayerCompany().spendCash(upfrontCost);
                techSystem.setMoney(mainController.getPlayerCompany().getCash());
                mainController.getPlayerCompany().recordTransaction(
                        "↳ [第 " + mainController.getCurrentDay() + " 天] 支付供應鏈首期建置費 - " + contract.getPartnerName() + "：-$" + mainController.formatMoney(upfrontCost)
                );
            }
            techSystem.signContract(contract);
            mainController.getPlayerCompany().recordTransaction(
                    "↳ [第 " + mainController.getCurrentDay() + " 天] 簽署供應鏈合約 - " + contract.getPartnerName()
            );
            availableContracts.remove(contract);

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("簽約成功");
                alert.setHeaderText(null);
                alert.setContentText("合約已正式生效。");
                alert.showAndWait();
            });

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
            mainController.getPlayerCompany().setCash(techSystem.getMoney());
            mainController.updateStatusLabels();
        }
    }

    private void updateStatusLabels() {
        if (techSystem != null) {
            // 💡 【安全防護】：為 lblYield 與 lblCost 加上 null 判斷保護，徹底防範隨機產生的 NullPointerException
            if (lblYield != null) {
                lblYield.setText("AI 研究等級：Lv." + techSystem.getAiResearchLevel());
            }
            if (lblCost != null) {
                lblCost.setText("進行中合約：" + techSystem.getActiveContractsCount() + " 檔");
            }

            // 💡 核心改良：統一由這個變數來判定火災遮罩
            boolean isFire = techSystem.getFireLockdownTurns() > 0;

            // ==========================================
            // 上方：自研晶片設計系統按鈕
            // ==========================================
            if (btnBuyEDA != null) {
                int currentLvl = techSystem.getDesignToolsLevel();
                double nextCost = techSystem.getDesignToolsUpgradeCost();
                int currentRate = Math.min(100, currentLvl * 25);

                if (isFire) {
                    btnBuyEDA.setText("🚨 工廠重整中：暫停晶片系統升級");
                    btnBuyEDA.setDisable(true);
                } else if (currentLvl >= 4) {
                    btnBuyEDA.setText(String.format("設計系統已達頂級 Lv.%d (100%% 保底)", currentLvl));
                    btnBuyEDA.setDisable(true);
                } else {
                    btnBuyEDA.setText(String.format("升級晶片設計系統 (Lv.%d➔Lv.%d) | 費用: $%s (防抽單率: %d%%)",
                            currentLvl, currentLvl + 1, mainController.formatMoney(nextCost), currentRate));
                    btnBuyEDA.setDisable(false);
                }
            }

            // ==========================================
            // 下方：AI 實驗室按鈕
            // ==========================================
            if (btnUpgradeAI != null) {
                double cost = 1000000 * Math.pow(2, techSystem.getAiResearchLevel());

                if (isFire) {
                    btnUpgradeAI.setText("🚨 工廠重整中：暫停 AI 實驗室研發");
                    btnUpgradeAI.setDisable(true);
                } else {
                    btnUpgradeAI.setText(String.format("升級 AI 實驗室 (Lv.%d ➔ Lv.%d) | 費用: $%s",
                            techSystem.getAiResearchLevel(), techSystem.getAiResearchLevel() + 1, mainController.formatMoney(cost)));
                    btnUpgradeAI.setDisable(false);
                }
            }
        }
    }
}