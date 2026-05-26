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

    @FXML private Label lblBankTitle;
    @FXML private Label lblLoanCount;
    @FXML private VBox loanListContainer;

    private bank_system bankSystem;
    private MainGameController mainController;

    public void initData(bank_system bankSystem, MainGameController mainController) {
        this.bankSystem = bankSystem;
        this.mainController = mainController;

        // 💡 確保數據與 mainController 引用注入後，才刷新標題文字
        updateBankTitle();
    }

    public void updateBankTitle() {
        // 💡 修正 3：加入更嚴謹的保護，避免改名時 mainController 狀態還在同步中
        if (lblBankTitle != null && mainController != null && mainController.getPlayerCompany() != null) {
            String companyName = mainController.getPlayerCompany().getName();
            lblBankTitle.setText("🏦 " + companyName + "國際放貸審核中心");
        }
    }

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

    private VBox createRequestCard(bank_LoanRequest req) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #f7f7f7; -fx-background-radius: 15; -fx-padding: 20; -fx-border-color: #ebe9f4; -fx-border-width: 2; -fx-border-radius: 13; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 5, 0, 0, 2);");

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

        Label dialogLbl = new Label(req.getDialogue() != null ? req.getDialogue() : "「我想申請一筆貸款...」");
        dialogLbl.setStyle("-fx-font-size: 16px; -fx-text-fill: #555555; -fx-font-style: italic; -fx-padding: 5 0;");
        dialogLbl.setWrapText(true);

        HBox details = new HBox(30);
        Label amountLbl = new Label(String.format("💰 申請金額: $%.0f 萬", req.getAmount() / 10000.0));
        Label rateLbl = new Label(String.format("📈 期望利率: %.1f%%", req.getInterestRate() * 100));
        Label tickLbl = new Label("⏳ 還款期數: " + req.getTotalTicks() + " 期");
        String detailStyle = "-fx-font-size: 15px; -fx-text-fill: #666666;";
        amountLbl.setStyle(detailStyle);
        rateLbl.setStyle(detailStyle);
        tickLbl.setStyle(detailStyle);
        details.getChildren().addAll(amountLbl, rateLbl, tickLbl);

        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnReject = new Button("❌ 委婉拒絕");
        btnReject.setStyle("-fx-background-color: #fac9de; -fx-text-fill: #333333; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");

        Button btnApprove = new Button("✅ 核准放款");
        btnApprove.setStyle("-fx-background-color: #b7e9ed; -fx-text-fill: #333333; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");

        btnReject.setOnMouseEntered(e -> btnReject.setStyle("-fx-background-color: #f7b2cf; -fx-text-fill: #333333; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;"));
        btnReject.setOnMouseExited(e -> btnReject.setStyle("-fx-background-color: #fac9de; -fx-text-fill: #333333; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;"));
        btnApprove.setOnMouseEntered(e -> btnApprove.setStyle("-fx-background-color: #9cdadd; -fx-text-fill: #333333; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;"));
        btnApprove.setOnMouseExited(e -> btnApprove.setStyle("-fx-background-color: #b7e9ed; -fx-text-fill: #333333; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;"));

        btnApprove.setOnAction(e -> handleApprove(req, card));
        btnReject.setOnAction(e -> handleReject(req, card));

        actions.getChildren().addAll(btnReject, btnApprove);
        card.getChildren().addAll(header, dialogLbl, details, actions);
        return card;
    }

    private void handleApprove(bank_LoanRequest req, VBox card) {
        // 1. 安全檢查：改用主畫面的 formatMoney 來顯示更漂亮的金額
        if (mainController.getPlayerCompany().getCash() < req.getAmount()) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("庫存現金不足");
            alert.setHeaderText(null);
            alert.setContentText("公司目前可用資金不足，無法核准此筆 $" + mainController.formatMoney(req.getAmount()) + " 的放貸申請！");
            alert.showAndWait();
            return;
        }

        // 2. 【核心修正】實時雙重扣款：公司扣錢，同時銀行子系統金庫也要同步扣錢！
        mainController.getPlayerCompany().spendCash(req.getAmount());
        bankSystem.setMoney(mainController.getPlayerCompany().getCash()); // 💡 讓銀行子系統同步扣除 569 萬與 710 萬！

        // 3. 將貸款加入銀行運作計息名單
        bankSystem.addLoan(req);

        // 4. 精準記帳：將這筆明確的扣款記錄在流水帳中
        mainController.getPlayerCompany().recordTransaction("↳ 🏦 [放貸核准] 核發貸款給 " + req.getApplicantName() + "：-$" + mainController.formatMoney(req.getAmount()));

        // 5. 強制重刷主畫面頂部按鈕狀態（此時畫面的資金會立刻「啪」的一聲現扣）
        mainController.updateStatusLabels();

        // 6. 移除卡片與更新剩餘件數
        loanListContainer.getChildren().remove(card);
        updateLoanCountLabel();
    }

    private void handleReject(bank_LoanRequest req, VBox card) {
        bank_LoanRequest newReq = req.processRejection();

        int index = loanListContainer.getChildren().indexOf(card);
        loanListContainer.getChildren().remove(card);

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