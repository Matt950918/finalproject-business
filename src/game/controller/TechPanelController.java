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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class TechPanelController {

    @FXML private Label lblYield;
    @FXML private Label lblCost;
    @FXML private Label lblTechTitle;
    @FXML private VBox partnerListContainer;

    // 綁定升級按鈕控制元件
    @FXML private Button btnBuyEDA;
    @FXML private Button btnUpgradeAI;

    private TechSystem techSystem;
    private MainGameController mainController;

    private static List<TechContract> availableContracts = new ArrayList<>();

    // 靜態變數持久化記錄玩家是否解鎖了「自研晶片高階設計系統」特權
    private static boolean hasDesignTools = false;

    public void initData(TechSystem techSystem, MainGameController mainController) {
        this.techSystem = techSystem;
        this.mainController = mainController;

        if (availableContracts.isEmpty()) {
            refreshAvailableContracts();
        }

        loadPartnerUI();
        updateStatusLabels();
        updateTechTitle();
    }

    public void updateTechTitle() {
        if (mainController != null && mainController.getPlayerCompany() != null && lblTechTitle != null) {
            String companyName = mainController.getPlayerCompany().getName();
            lblTechTitle.setText("🏢 " + companyName + " - 科技研發主控台");
        }
    }

    private void refreshAvailableContracts() {
        availableContracts.clear();
        for (int i = 0; i < 3; i++) {
            availableContracts.add(Tech_Partner.generateRandomContract());
        }
    }

    /**
     * 💻 購買自研高階晶片設計系統邏輯 (原 EDA 軟體授權)
     */
    @FXML
    private void handleBuyEDA() {
        if (hasDesignTools) return;

        double edaCost = 8000000; // $800 萬
        if (mainController.getPlayerCompany().getCash() < edaCost) {
            showAlert("資金不足", "無法支付 $800 萬的自研晶片高階設計系統開發費。");
            return;
        }

        if (techSystem.deductMoney(edaCost)) {
            hasDesignTools = true; // 永久啟動商務保底特權！
            mainController.getPlayerCompany().recordTransaction("↳ [第 " + mainController.getCurrentDay() + " 天] 💻 系統研發 - 自研高階晶片設計系統解鎖：-$800.00 萬");
            showAlert("研發成功", "🎉 高階晶片設計系統已佈署完成！\n👉 【解鎖特權】：未來左側商務談判如果失敗，大廠不會抽單，合約將安全保留原價！");
            syncMoneyToMain();
            updateStatusLabels();
            loadPartnerUI();
        }
    }

    /**
     * 🚀 升級 AI 實驗室
     */
    @FXML
    private void handleUpgradeAI() {
        double upgradeCost = 1000000 * Math.pow(2, techSystem.getAiResearchLevel());

        if (mainController.getPlayerCompany().getCash() < upgradeCost) {
            showAlert("資金不足", "無法升級 AI 實驗室！需要資金：$" + mainController.formatMoney(upgradeCost));
            return;
        }

        if (techSystem.upgradeAIResearch()) {
            mainController.getPlayerCompany().recordTransaction("↳ [第 " + mainController.getCurrentDay() + " 天] 🚀 科技樹升級 - AI 實驗室 Lv." + techSystem.getAiResearchLevel() + "：-$" + mainController.formatMoney(upgradeCost));
            showAlert("升級成功", "AI 研究度升級成功！當前等級：Lv." + techSystem.getAiResearchLevel());
            syncMoneyToMain();
            updateStatusLabels();
            loadPartnerUI();
        }
    }

    private void loadPartnerUI() {
        partnerListContainer.getChildren().clear();

        for (TechContract contract : new ArrayList<>(availableContracts)) {
            VBox card = createContractCard(contract);
            partnerListContainer.getChildren().add(card);
        }

        if (availableContracts.isEmpty()) {
            refreshAvailableContracts();
            loadPartnerUI();
        }
    }

    private VBox createContractCard(TechContract contract) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12; -fx-padding: 16; -fx-border-color: #ebe9f4; -fx-border-width: 1.5;");

        boolean isUpstream = contract.getRevenue() == 0;
        String typeTag = isUpstream ? "【⚙️ 上游採購】" : "【📈 下游供應】";

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

        // --- 按鈕 A：談判按鈕 ---
        Button btnNegotiate = new Button();
        btnNegotiate.setStyle("-fx-background-color: #f6f6f6; -fx-text-fill: #4e73df; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand; -fx-border-color: #4e73df; -fx-border-radius: 6;");

        double baseRate = isUpstream ? 0.35 : 0.25;
        int currentWinRate = (int)((baseRate + (techSystem.getAiResearchLevel() * 0.05)) * 100);

        if (isUpstream) {
            btnNegotiate.setText(String.format("💬 嘗試砍價 (成功率: %d%%)", currentWinRate));
        } else {
            btnNegotiate.setText(String.format("📈 要求抬價 (成功率: %d%%)", currentWinRate));
        }

        if (contract.isNegotiated()) {
            btnNegotiate.setText("🔒 已完成談判");
            btnNegotiate.setDisable(true);
        }

        btnNegotiate.setOnAction(e -> {
            boolean success;
            if (isUpstream) {
                success = contract.negotiateCostDown(techSystem.getAiResearchLevel());
                if (success) {
                    showAlert("談判大成功！", "🤝 " + contract.getPartnerName() + " 同意了你的方案！成本成功打 75 折！");
                } else {
                    if (hasDesignTools) {
                        showAlert("談判不理想...", "❌ " + contract.getPartnerName() + " 談判破裂拒絕降價。但因為我們擁有【研發晶片設計系統】，大廠不敢輕易撤資，合約安全保留原價（未抽單）！");
                    } else {
                        showAlert("談判破裂...", "❌ " + contract.getPartnerName() + " 覺得你太沒誠意，憤而撤回此項合作！");
                        availableContracts.remove(contract);
                    }
                }
            } else {
                success = contract.negotiateRevenueUp(techSystem.getAiResearchLevel());
                if (success) {
                    showAlert("談判大成功！", "💰 " + contract.getPartnerName() + " 認可你的晶片架構，同意提高採購報價 30%！");
                } else {
                    if (hasDesignTools) {
                        showAlert("談判不理想...", "❌ " + contract.getPartnerName() + " 抬價談崩。但因為我們擁有【研發晶片設計系統】，大廠不敢直接解除合約，得以安全保留原價（未抽單）！");
                    } else {
                        showAlert("談判破裂...", "❌ " + contract.getPartnerName() + " 轉頭去找別家供應商了，合約作廢！");
                        availableContracts.remove(contract);
                    }
                }
            }
            loadPartnerUI();
        });

        // --- 按鈕 B：簽署合約按鈕 ---
        Button btnSign = new Button("📝 簽署此合約");
        btnSign.setStyle("-fx-background-color: #4E73DF; -fx-text-fill: #FFFFFF; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

        btnSign.setOnAction(e -> {
            techSystem.signContract(contract);
            mainController.getPlayerCompany().recordTransaction(
                    "↳ [第 " + mainController.getCurrentDay() + " 天] 🤝 簽署供應鏈合約 - " + contract.getPartnerName()
            );
            availableContracts.remove(contract);
            showAlert("簽約成功", "合約正式生效！每日換日結束營業時，系統將自動為公司清算此供應鏈的利潤。");
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
                if (hasDesignTools) {
                    btnBuyEDA.setText("✅ 已全面升級自主設計核心");
                    btnBuyEDA.setDisable(true);
                } else {
                    btnBuyEDA.setText("💳 支付系統研發費 ($800萬)");
                    btnBuyEDA.setDisable(false);
                }
            }

            if (btnUpgradeAI != null) {
                double cost = 1000000 * Math.pow(2, techSystem.getAiResearchLevel());
                btnUpgradeAI.setText(String.format("⚡ 升級 AI 實驗室 (Lv.%d ➔ Lv.%d) | 費用: $%s",
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