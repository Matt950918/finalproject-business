package game.controller;

import game.model.tech.TechSystem;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class TechPanelController {

    @FXML private Label lblYield;
    @FXML private Label lblCost;
    @FXML private VBox partnerListContainer;

    private TechSystem techSystem;
    private MainGameController mainController;

    public void initData(TechSystem techSystem, MainGameController mainController) {
        this.techSystem = techSystem;
        this.mainController = mainController;

        loadPartnerUI();
        updateStatusLabels();
    }

    // 購買 EDA 授權工具的邏輯
    @FXML
    private void handleBuyEDA() {
        double edaCost = 8000000;
        if (mainController.getPlayerCompany().getCash() < edaCost) {
            showAlert("資金不足", "無法支付 $800 萬的 EDA 軟體企業授權費。");
            return;
        }

        // 假設 techSystem 裡面有扣錢或升級的方法 (你可以換成你實際寫的方法)
        if (techSystem.deductMoney(edaCost)) {
            mainController.getPlayerCompany().recordTransaction("↳ [第 " + mainController.getCurrentDay() + " 天] 💻 採購升級 - 頂級 EDA 軟體授權：-$800.00 萬");
            showAlert("授權成功", "EDA 工具已全面升級！硬體架構模擬效率大幅提升，錯誤率下降。");
            syncMoneyToMain();
            updateStatusLabels();
        }
    }

    // 動態生成右側的合作夥伴談判清單
    private void loadPartnerUI() {
        partnerListContainer.getChildren().clear();

        // 💡 這裡你可以串接你寫好的 Tech_Partner 迴圈，目前我先生成一個示範廠商
        VBox partnerCard = createPartnerCard("台GG 晶圓代工廠", "提供 3nm 製程，高良率但報價強硬。", 15000000);
        VBox partnerCard2 = createPartnerCard("二線封測大廠", "提供穩定的後段封裝測試服務。", 5000000);

        partnerListContainer.getChildren().addAll(partnerCard, partnerCard2);
    }

    private VBox createPartnerCard(String name, String desc, double contractCost) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-padding: 15; -fx-border-color: #ebe9f4;");

        Label nameLbl = new Label("🏭 " + name);
        nameLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-text-fill: #666666; -fx-font-size: 13px;");
        descLbl.setWrapText(true);

        HBox actions = new HBox();
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button btnSign = new Button("📝 簽署合約 ($" + (contractCost/10000.0) + "萬)");
        btnSign.setStyle("-fx-background-color: #c4dcfa; -fx-text-fill: #333333; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

        btnSign.setOnAction(e -> {
            if (mainController.getPlayerCompany().getCash() >= contractCost && techSystem.deductMoney(contractCost)) {
                mainController.getPlayerCompany().recordTransaction("↳ [第 " + mainController.getCurrentDay() + " 天] 🤝 簽署供應鏈合約 - " + name + "：-$" + mainController.formatMoney(contractCost));
                syncMoneyToMain();
                btnSign.setText("✅ 已簽署");
                btnSign.setDisable(true);
            } else {
                showAlert("資金不足", "公司現金不足以支付此合約的訂金！");
            }
        });

        actions.getChildren().add(btnSign);
        card.getChildren().addAll(nameLbl, descLbl, actions);
        return card;
    }

    private void syncMoneyToMain() {
        mainController.getPlayerCompany().spendCash(mainController.getPlayerCompany().getCash());
        mainController.getPlayerCompany().earnCash(techSystem.getMoney());
        mainController.updateStatusLabels();
    }

    private void updateStatusLabels() {
        // 更新上方狀態的文字，如果 techSystem 有 getYield() 可以接在這裡
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}