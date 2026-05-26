package game.controller;

import game.model.*;
import game.model.bank.bank_Customer;
import game.model.bank.bank_LoanRequest;
import game.model.bank.bank_system;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

public class MainGameController {

    @FXML
    private Label lblDay;
    @FXML
    private Label lblTimer;
    @FXML
    private Button btnStockPrice;
    @FXML
    private Button btnCash;

    // 彈窗相關元件
    @FXML
    private VBox resultOverlay;
    @FXML
    private Label lblSettleReason;
    @FXML
    private Label lblPriceChange;
    @FXML
    private Label lblResultHeader;
    @FXML
    private VBox mainGameLayer;

    @FXML
    private VBox newsOverlay;
    @FXML
    private Label lblNewsTitle;
    @FXML
    private VBox optionsBox;
    @FXML
    private StackPane industryContentArea;

    // 底層系統
    private bank_system bankSystem = new bank_system();
    private game.model.bio.BioSystem bioSystem = new game.model.bio.BioSystem();
    private game.model.tech.TechSystem techSystem = new game.model.tech.TechSystem();

    private Company playerCompany;
    private int currentDay = 0;
    private Timeline timeline;
    private int timeLeft = 60;

    private BankPanelController currentBankController;

    // ==========================================
    // 🔗 開放給外部呼叫的接口
    // ==========================================
    public Company getPlayerCompany() {
        return playerCompany;
    }

    public int getCurrentDay() {
        return currentDay;
    }

    public void updateStatusLabels() {
        lblDay.setText("第 " + currentDay + " 天");
        btnCash.setText("資金：$" + formatMoney(playerCompany.getCash()));
        btnStockPrice.setText("股價：$" + String.format("%.2f", playerCompany.getStockPrice()));

        if (playerCompany != null && playerCompany.getIndustry() == IndustryType.BANK && currentBankController != null) {
            currentBankController.updateBankTitle();
        }
    }

    // ==========================================
    // 🚀 遊戲生命週期
    // ==========================================
    public void startGame(String customName, IndustryType selectedIndustry) {
        playerCompany = new Company(customName, selectedIndustry);

        // 初始化流水帳
        playerCompany.getLedger().clear();
        playerCompany.recordTransaction("🏢 [系統] " + playerCompany.getName() + " 正式創立！");
        playerCompany.recordTransaction("💰 [系統] 存入初始資本額：$5,000.00 萬");

        if (selectedIndustry == IndustryType.BANK) {
            bankSystem.setMoney(playerCompany.getCash());
        }

        // 💡 修正綁定：點擊資金看歷史明細、點擊股價看折線圖走勢。內部無法改名。
        btnCash.setOnAction(e -> showLedger());
        btnStockPrice.setOnAction(e -> showStockChart());

        NewsDatabase.resetDatabase();

        if (newsOverlay != null) newsOverlay.setVisible(false);
        if (resultOverlay != null) resultOverlay.setVisible(false);

        setupTimer();
        startNewDay();
    }

    private void setupTimer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            timeLeft--;
            lblTimer.setText("剩餘時間：" + timeLeft + " 秒");
            if (timeLeft <= 0) handleTimeout();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void startNewDay() {
        currentDay++;

        if (playerCompany.getIndustry() == IndustryType.BANK) {
            double beforeMoney = bankSystem.getMoney();
            List<String> bankReports = bankSystem.tick(); // 跑收息邏輯
            double income = bankSystem.getMoney() - beforeMoney; // 算出當日淨利息收入

            if (income > 0) {
                playerCompany.earnCash(income); // 確實加上當天利息
                playerCompany.recordTransaction("↳ [第 " + currentDay + " 天] 💰 收到放款本息：+$" + formatMoney(income));
            }

            // 💡 【重要】改用強力 setCash 覆蓋，子系統金庫與公司總資產維持絕對一體化同步
            bankSystem.setMoney(playerCompany.getCash());
            loadBankPanel();

            if (!bankReports.isEmpty()) {
                showBankReportPopup(bankReports);
            }

        } else if (playerCompany.getIndustry() == IndustryType.BIOTECH) {
            bioSystem.tick();
            playerCompany.setCash(bioSystem.getMoney());
            loadBioPanel();

        } else if (playerCompany.getIndustry() == IndustryType.TECH) {
            techSystem.tick();
            playerCompany.setCash(techSystem.getMoney());
            loadTechPanel();
        }

        updateStatusLabels();

        timeLeft = 60;
        timeline.play();

        if (newsOverlay != null) newsOverlay.setVisible(false);
        if (Math.random() < 0.25) {
            DailyNews todayNews = NewsDatabase.getRandomNewsFor(playerCompany.getIndustry());
            if (todayNews != null) {
                showNewsPopup(todayNews);
            }
        }

        if (currentDay > 0 && currentDay % 30 == 0) {
            int month = currentDay / 30;
            playerCompany.summarizeLedger(month);
            playerCompany.recordTransaction("🏦 [月結] 第 " + month + " 個月結算保留盈餘：$" + formatMoney(playerCompany.getCash()));
        }
    }

    private void showBankReportPopup(List<String> reports) {
        StringBuilder sb = new StringBuilder();
        for (String r : reports) {
            sb.append(r).append("\n\n");
        }
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("🏦 銀行每日帳務匯報");
        alert.setHeaderText("客戶結案與違約動態");
        alert.setContentText(sb.toString());
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
    }

    private void showNewsPopup(DailyNews news) {
        optionsBox.getChildren().clear();
        lblNewsTitle.setText(news.getTitle());

        for (NewsOption option : news.getOptions()) {
            Button btn = new Button(option.getDescription());
            btn.getStyleClass().add("switch-button");
            btn.setOnAction(e -> {
                newsOverlay.setVisible(false);
                handleOptionSelected(option);
            });
            optionsBox.getChildren().add(btn);
        }
        newsOverlay.setVisible(true);
        timeline.pause();
    }

    @FXML
    private void handleSkipDay(ActionEvent event) {
        if (newsOverlay != null) {
            newsOverlay.setVisible(false);
        }
        timeline.play();
    }

    private void handleOptionSelected(NewsOption selectedOption) {
        timeline.stop();

        double oldPrice = playerCompany.getStockPrice();

        // 1. 執行事件的影響 (如果有扣事件的公關費，會直接扣除 playerCompany 的現金)
        MarketEvent resultEvent = (selectedOption != null) ? selectedOption.execute(playerCompany) : null;
        playerCompany.updateStockPrice(currentDay, resultEvent);

        // 💡 2. 【核心修正】資產不要與股價大盤隨機波動連動。子金庫維持同步公司主體。
        // 💡 營業結束時，三大產業的子金庫「死死同步」目前公司的真實實時餘額！
        if (playerCompany.getIndustry() == IndustryType.BANK) {
            bankSystem.setMoney(playerCompany.getCash());
        } else if (playerCompany.getIndustry() == IndustryType.BIOTECH) {
            bioSystem.setMoney(playerCompany.getCash()); // 💡 剛才補上方法後，這裡就不會再噴紅字了！
        } else if (playerCompany.getIndustry() == IndustryType.TECH) {
            techSystem.setMoney(playerCompany.getCash()); // 💡 剛才補上方法後，這裡就不會再噴紅字了！
        }

        updateStatusLabels();

        String msg = (selectedOption == null)
                ? "今日營業時間結束，市場結算完畢。"
                : ((resultEvent != null) ? resultEvent.getName() : "公司維持穩定經營，市場無重大消息。");

        showResultPopup(msg, oldPrice, playerCompany.getStockPrice());
    }

    private void showResultPopup(String message, double oldPrice, double newPrice) {
        lblSettleReason.setText(message);
        double diff = newPrice - oldPrice;
        double percent = (oldPrice == 0) ? 0 : (diff / oldPrice) * 100;

        if (diff > 0.01) {
            lblResultHeader.setText("💰 經營獲利報告");
            lblPriceChange.setText(String.format("股價變動: +$%.2f (+%.2f%%)", diff, percent));
            lblPriceChange.setStyle("-fx-text-fill: #E74A3B;");
        } else if (diff < -0.01) {
            lblResultHeader.setText("📉 損失預警報告");
            lblPriceChange.setText(String.format("股價變動: -$%.2f (%.2f%%)", Math.abs(diff), percent));
            lblPriceChange.setStyle("-fx-text-fill: #1CC88A;");
        } else {
            lblResultHeader.setText("📊 市場觀察報告");
            lblPriceChange.setText("股價持平：無明顯波動");
            lblPriceChange.setStyle("-fx-text-fill: #5A5C69;");
        }

        mainGameLayer.setDisable(true);
        if (newsOverlay != null) newsOverlay.setVisible(false);
        resultOverlay.setVisible(true);
    }

    @FXML
    private void closeResultAndProceed(ActionEvent event) {
        resultOverlay.setVisible(false);
        mainGameLayer.setDisable(false);
        startNewDay();
    }

    private void handleTimeout() {
        handleOptionSelected(null);
    }

    public String formatMoney(double amount) {
        boolean isNegative = amount < 0;
        double absAmount = Math.abs(amount);
        String formattedStr;

        if (absAmount >= 1_000_000_00) {
            formattedStr = String.format("%.2f 億", absAmount / 1_000_000_00.0);
        } else if (absAmount >= 10000) {
            formattedStr = String.format("%.2f 萬", absAmount / 10000.0);
        } else {
            formattedStr = String.format("%.0f", absAmount);
        }
        return (isNegative ? "-" : "") + formattedStr;
    }

    private void loadBankPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/game/view/BankPanel.fxml"));
            VBox bankPanel = loader.load();
            BankPanelController bankController = loader.getController();

            this.currentBankController = bankController;

            industryContentArea.getChildren().clear();
            industryContentArea.getChildren().add(bankPanel);

            bankController.initData(bankSystem, this);

            List<bank_LoanRequest> todayRequests = new ArrayList<>();
            todayRequests.add(bank_Customer.createRandomRequest());
            if (Math.random() > 0.5) {
                todayRequests.add(bank_Customer.createRandomRequest());
            }
            bankController.loadRequests(todayRequests);

            javafx.application.Platform.runLater(() -> {
                if (this.currentBankController != null) {
                    this.currentBankController.updateBankTitle();
                }
            });

        } catch (Exception e) {
            System.err.println("❌ 載入 BankPanel.fxml 失敗！請檢查路徑。");
            e.printStackTrace();
        }
    }

    private void loadBioPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/game/view/BioPanel.fxml"));
            VBox bioPanel = loader.load();
            BioPanelController bioController = loader.getController();
            bioController.initData(bioSystem, this);
            this.currentBankController = null;

            industryContentArea.getChildren().clear();
            industryContentArea.getChildren().add(bioPanel);
        } catch (Exception e) {
            System.err.println("❌ 載入 BioPanel.fxml 失敗！");
        }
    }

    private void loadTechPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/game/view/TechPanel.fxml"));
            VBox techPanel = loader.load();
            TechPanelController techController = loader.getController();
            techController.initData(techSystem, this);
            this.currentBankController = null;

            industryContentArea.getChildren().clear();
            industryContentArea.getChildren().add(techPanel);
        } catch (Exception e) {
            System.err.println("❌ 載入 TechPanel.fxml 失敗！");
        }
    }

    @FXML
    private void showLedger() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("財務報表");
        alert.setHeaderText("公司歷史資金明細");

        javafx.scene.control.ListView<String> listView = new javafx.scene.control.ListView<>();
        List<String> records = playerCompany.getLedger();

        if (records == null || records.isEmpty()) {
            listView.getItems().add("目前尚無資金異動紀錄。");
        } else {
            for (int i = records.size() - 1; i >= 0; i--) {
                listView.getItems().add(records.get(i));
            }
        }
        listView.setPrefSize(400, 300);
        alert.getDialogPane().setContent(listView);
        alert.showAndWait();
    }

    @FXML
    private void handleEndDay(ActionEvent event) {
        handleOptionSelected(null);
    }

    @FXML
    private void showStockChart() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("股市觀測站");
        alert.setHeaderText("公司歷史股價走勢圖");

        javafx.scene.chart.NumberAxis xAxis = new javafx.scene.chart.NumberAxis();
        xAxis.setLabel("上市天數");
        xAxis.setForceZeroInRange(false);
        xAxis.setTickUnit(1);
        xAxis.setMinorTickVisible(false);

        javafx.scene.chart.NumberAxis yAxis = new javafx.scene.chart.NumberAxis();
        yAxis.setLabel("股價 (NTD)");
        yAxis.setForceZeroInRange(false);

        javafx.scene.chart.LineChart<Number, Number> lineChart = new javafx.scene.chart.LineChart<>(xAxis, yAxis);
        lineChart.setTitle(playerCompany.getName() + " 走勢");
        lineChart.setLegendVisible(false);

        List<StockRecord> history = playerCompany.getStockHistory();

        if (history != null && !history.isEmpty()) {
            if (history.size() == 1) {
                // 💡 修正：使用 XYChart.Series
                javafx.scene.chart.XYChart.Series<Number, Number> startPoint = new javafx.scene.chart.XYChart.Series<>();
                startPoint.getData().add(new javafx.scene.chart.XYChart.Data<>(0, history.get(0).getPrice()));
                lineChart.getData().add(startPoint);
            } else {
                for (int i = 1; i < history.size(); i++) {
                    double prevPrice = history.get(i - 1).getPrice();
                    double currPrice = history.get(i).getPrice();

                    // 💡 修正：使用 XYChart.Series
                    javafx.scene.chart.XYChart.Series<Number, Number> segment = new javafx.scene.chart.XYChart.Series<>();
                    javafx.scene.chart.XYChart.Data<Number, Number> d1 = new javafx.scene.chart.XYChart.Data<>(i - 1, prevPrice);
                    javafx.scene.chart.XYChart.Data<Number, Number> d2 = new javafx.scene.chart.XYChart.Data<>(i, currPrice);

                    segment.getData().add(d1);
                    segment.getData().add(d2);
                    lineChart.getData().add(segment);

                    String color = (currPrice >= prevPrice) ? "#E74A3B" : "#1CC88A";

                    javafx.scene.Node line = segment.getNode().lookup(".chart-series-line");
                    if (line != null) {
                        line.setStyle("-fx-stroke: " + color + "; -fx-stroke-width: 3px;");
                    }

                    if (d1.getNode() != null) d1.getNode().setStyle("-fx-background-color: " + color + ", white;");
                    if (d2.getNode() != null) d2.getNode().setStyle("-fx-background-color: " + color + ", white;");
                }
            }
        }
        lineChart.setPrefSize(550, 400);
        alert.getDialogPane().setContent(lineChart);
        alert.showAndWait();
    }
}