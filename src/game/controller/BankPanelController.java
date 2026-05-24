package game.controller;

import game.model.bank.bank_LoanRequest;
import game.model.bank.bank_system;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class BankPanelController {

    @FXML private Label lblLoanCount;
    @FXML private VBox loanListContainer;

    private bank_system bankSystem;
    private MainGameController mainController; // 用來通知主畫面扣錢

    public void initData(bank_system bankSystem, MainGameController mainController) {
        this.bankSystem = bankSystem;
        this.mainController = mainController;
    }

    /**
     * 清空畫面，並把傳入的申請案畫成一張張的卡片
     */
    public void loadRequests(List<bank_LoanRequest> requests) {
        loanListContainer.getChildren().clear();
        lblLoanCount.setText("待審核案件：" + requests.size() + " 件");

        if (requests.isEmpty()) {
            Label emptyLabel = new Label("今天大廳空蕩蕩的，沒有客人上門。");
            emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #555555;");
            loanListContainer.getChildren().add(emptyLabel);
            return;
        }

        for (bank_LoanRequest req : requests) {
            loanListContainer.getChildren().add(createRequestCard(req));
        }
    }

    /**
     * 動態生成單張貸款申請書的 UI 卡片 (仙女系配色版)
     */
    /**
     * 動態生成單張貸款申請書的 UI 卡片 (包含客戶台詞顯示)
     */
    private VBox createRequestCard(bank_LoanRequest req) {
        VBox card = new VBox(12);
        // 卡片底色：灰色 #f7f7f7
        card.setStyle("-fx-background-color: #f7f7f7; -fx-background-radius: 15; -fx-padding: 20; -fx-border-color: #ebe9f4; -fx-border-width: 2; -fx-border-radius: 13; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 5, 0, 0, 2);");

        // 第一排：名字與信用分數
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        Label nameLbl = new Label("👤 " + req.getApplicantName());
        nameLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label creditLbl = new Label("信用評分: " + req.getCreditScore());
        String creditBgColor = req.getCreditScore() >= 80 ? "#b7e9ed" : (req.getCreditScore() >= 60 ? "#c4dcfa" : "#e3d8f9");
        creditLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333; -fx-background-color: " + creditBgColor + "; -fx-padding: 5 12; -fx-background-radius: 20;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(nameLbl, creditLbl, spacer);

        // 💬 關鍵新增：顯示客人的對話台詞！
        Label dialogLbl = new Label(req.getDialogue() != null ? req.getDialogue() : "「我想申請一筆貸款...」");
        dialogLbl.setStyle("-fx-font-size: 16px; -fx-text-fill: #555555; -fx-font-style: italic; -fx-padding: 5 0;");
        dialogLbl.setWrapText(true); // 避免長對話超出視窗

        // 第三排：貸款條件
        HBox details = new HBox(30);
        Label amountLbl = new Label(String.format("💰 申請金額: $%.0f 萬", req.getAmount() / 10000.0));
        Label rateLbl = new Label(String.format("📈 期望利率: %.1f%%", req.getInterestRate() * 100));
        Label tickLbl = new Label("⏳ 還款期數: " + req.getTotalTicks() + " 期");
        String detailStyle = "-fx-font-size: 15px; -fx-text-fill: #666666;";
        amountLbl.setStyle(detailStyle);
        rateLbl.setStyle(detailStyle);
        tickLbl.setStyle(detailStyle);
        details.getChildren().addAll(amountLbl, rateLbl, tickLbl);

        // 第四排：按鈕區
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnReject = new Button("❌ 委婉拒絕");
        btnReject.setStyle("-fx-background-color: #fac9de; -fx-text-fill: #333333; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");

        Button btnApprove = new Button("✅ 核准放款");
        btnApprove.setStyle("-fx-background-color: #b7e9ed; -fx-text-fill: #333333; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");

        // Hover 效果
        btnReject.setOnMouseEntered(e -> btnReject.setStyle("-fx-background-color: #f7b2cf; -fx-text-fill: #333333; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;"));
        btnReject.setOnMouseExited(e -> btnReject.setStyle("-fx-background-color: #fac9de; -fx-text-fill: #333333; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;"));
        btnApprove.setOnMouseEntered(e -> btnApprove.setStyle("-fx-background-color: #9cdadd; -fx-text-fill: #333333; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;"));
        btnApprove.setOnMouseExited(e -> btnApprove.setStyle("-fx-background-color: #b7e9ed; -fx-text-fill: #333333; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;"));

        btnApprove.setOnAction(e -> handleApprove(req, card));
        btnReject.setOnAction(e -> handleReject(req, card));

        actions.getChildren().addAll(btnReject, btnApprove);

        // 依序疊加
        card.getChildren().addAll(header, dialogLbl, details, actions);
        return card;
    }

    private void handleApprove(bank_LoanRequest req, VBox card) {
        // 檢查金庫的錢夠不夠放貸
        if (mainController.getPlayerCompany().getCash() < req.getAmount()) {
            // 💡 新增：如果錢不夠，跳出提示視窗，玩家才知道為什麼框框沒消失
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("庫存現金不足");
            alert.setHeaderText(null);
            alert.setContentText("公司目前可用資金不足，無法核准此筆 $" + (req.getAmount()/10000.0) + " 萬的放貸申請！");
            alert.showAndWait();
            return;
        }

        mainController.getPlayerCompany().spendCash(req.getAmount());
        bankSystem.addLoan(req);
        mainController.updateStatusLabels();
        mainController.getPlayerCompany().recordTransaction("[第" + mainController.getCurrentDay() + "天] 核准貸款 - 放款給 " + req.getApplicantName() + "：-$" + (req.getAmount()/10000.0) + " 萬");

        // 成功扣錢後，框框就會順利消失！
        loanListContainer.getChildren().remove(card);
        updateLoanCountLabel();
    }

    private void handleReject(bank_LoanRequest req, VBox card) {
        // 1. 執行貸款案的拒絕邏輯 (可能會產生降階的捲土重來案子)
        bank_LoanRequest newReq = req.processRejection();

        // 2. 先把原本的卡片移除
        int index = loanListContainer.getChildren().indexOf(card);
        loanListContainer.getChildren().remove(card);

        // 3. 如果客人願意降價重來，就在原地塞入新的卡片！
        if (newReq != null) {
            VBox newCard = createRequestCard(newReq);
            loanListContainer.getChildren().add(index, newCard);
        }

        updateLoanCountLabel();
    }

    private void updateLoanCountLabel() {
        lblLoanCount.setText("待審核案件：" + loanListContainer.getChildren().size() + " 件");
    }
}