package game.controller;

import game.model.RankingEntry;
import game.model.RankingSystem;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;
import java.util.List;

public class RankingPanelController {

    @FXML private VBox rankContainer;

    private MainGameController mainController;
    private RankingSystem currentRankingSystem = new RankingSystem();
    private boolean isShowingCash = true;

    public void initData(MainGameController mainController) {
        this.mainController = mainController;
        refreshLeaderboard(currentRankingSystem);
    }

    public void showLeaderboard(RankingSystem rankingSystem) {
        if (rankingSystem != null) {
            this.currentRankingSystem = rankingSystem;
        }
        refreshLeaderboard(this.currentRankingSystem);
    }

    @FXML
    public void handleShowCashRank() {
        this.isShowingCash = true;
        refreshLeaderboard(this.currentRankingSystem);
    }

    @FXML
    public void handleShowStockRank() {
        this.isShowingCash = false;
        refreshLeaderboard(this.currentRankingSystem);
    }

    public void refreshLeaderboard(RankingSystem rankingSystem) {
        if (rankContainer == null) return;
        rankContainer.getChildren().clear();

        if (rankingSystem == null) {
            rankingSystem = this.currentRankingSystem;
        }

        List<RankingEntry> list = isShowingCash ?
                rankingSystem.getTopCashEntries() :
                rankingSystem.getTopStockPriceEntries();

        if (list.isEmpty()) {
            Label lblEmpty = new Label("目前系統中尚無玩家資料，虛位以待！");
            lblEmpty.setStyle("-fx-font-size: 15px; -fx-text-fill: #999999; -fx-padding: 20;");
            rankContainer.getChildren().add(lblEmpty);
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            RankingEntry entry = list.get(i);
            int rank = i + 1;

            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);
            row.setSpacing(20);
            row.setStyle("-fx-padding: 12 25; -fx-background-color: #ffffff; -fx-background-radius: 12; "
                    + "-fx-border-color: #ebe9f4; -fx-border-width: 1; "
                    + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.02), 5, 0, 0, 2);");

            String rankVisual = String.valueOf(rank);
            if (rank == 1) rankVisual = "🥇 1";
            else if (rank == 2) rankVisual = "🥈 2";
            else if (rank == 3) rankVisual = "🥉 3";

            Label lblRank = new Label(rankVisual);
            lblRank.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: "
                    + (rank <= 3 ? "#ffaa00" : "#666666") + "; -fx-min-width: 50;");

            String displayText = entry.getCompanyName() + " (" + entry.getUsername() + ")";
            Label lblName = new Label(displayText);
            lblName.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333333;");
            lblName.setMinWidth(220);

            Label lblIndustry = new Label("[" + entry.getIndustryChinese() + "]");
            lblIndustry.setStyle("-fx-font-size: 13px; -fx-text-fill: #858796; -fx-min-width: 100;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label lblData = new Label();
            if (isShowingCash) {
                lblData.setText("資產: $" + formatMoney(entry.getCash()));
                lblData.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2e59d9;");
            } else {
                lblData.setText(String.format("股價: $%.2f", entry.getStockPrice()));
                lblData.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e74a3b;");
            }

            row.getChildren().addAll(lblRank, lblName, lblIndustry, spacer, lblData);
            rankContainer.getChildren().add(row);
        }
    }

    @FXML
    private void handleBack() {
        if (mainController != null) {
            mainController.handleReturnToGame();
        }
    }

    public static void saveNewRecord(String name, double finalScore) {
        System.out.println("💡 [排行榜系統] 已全面改採全帳號即時連線排序。");
    }

    private String formatMoney(double amount) {
        boolean isNegative = amount < 0;
        double absAmount = Math.abs(amount);
        String formattedStr;
        if (absAmount >= 1_000_000_00) formattedStr = String.format("%.2f 億", absAmount / 1_000_000_00.0);
        else if (absAmount >= 10000) formattedStr = String.format("%.2f 萬", absAmount / 10000.0);
        else formattedStr = String.format("%.0f", absAmount);
        return (isNegative ? "-" : "") + formattedStr;
    }
}