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

    @FXML private Label lblDay;
    @FXML private Label lblTimer;
    @FXML private Button btnStockPrice;
    @FXML private Button btnCash;

    // 彈窗相關元件
    @FXML private VBox resultOverlay;
    @FXML private Label lblSettleReason;
    @FXML private Label lblPriceChange;
    @FXML private Label lblResultHeader;
    @FXML private VBox mainGameLayer;

    @FXML private VBox newsOverlay;
    @FXML private Label lblNewsTitle;
    @FXML private VBox optionsBox;
    @FXML private StackPane industryContentArea;

    // 底層系統
    private bank_system bankSystem = new bank_system();
    private game.model.bio.BioSystem bioSystem = new game.model.bio.BioSystem();
    private game.model.tech.TechSystem techSystem = new game.model.tech.TechSystem();

    private Company playerCompany;
    private int currentDay = 0;
    private Timeline timeline;
    private int timeLeft = 60;

    // 💡 關鍵：確保當前顯示的控制器全域可見
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

        // 💡 關鍵保險：只要狀態欄更新，就強迫當前的銀行面板標題一起同步刷新！
        if (playerCompany != null && playerCompany.getIndustry() == IndustryType.BANK && currentBankController != null) {
            currentBankController.updateBankTitle();
        }
    }
    // ==========================================
    // 🚀 遊戲生命週期
    // ==========================================
    public void startGame(String customName, IndustryType selectedIndustry) {
        playerCompany = new Company(customName, selectedIndustry);

        playerCompany.spendCash(playerCompany.getCash());
        playerCompany.earnCash(50_000_000);
        playerCompany.recordTransaction("🏢 [系統] " + playerCompany.getName() + " 創立，獲得初始資金：$5000 萬");

        if (selectedIndustry == IndustryType.BANK) {
            playerCompany.spendCash(playerCompany.getCash());
            playerCompany.earnCash(bankSystem.getMoney());
        } else if (selectedIndustry == IndustryType.BIOTECH) {
            playerCompany.spendCash(playerCompany.getCash());
            playerCompany.earnCash(bioSystem.getMoney());
        } else if (selectedIndustry == IndustryType.TECH) {
            playerCompany.spendCash(playerCompany.getCash());
            playerCompany.earnCash(techSystem.getMoney());
        }

        btnCash.setOnAction(e -> handleRenameCompany());
        btnStockPrice.setOnAction(e -> handleRenameCompany());

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
            List<String> bankReports = bankSystem.tick();
            double income = bankSystem.getMoney() - beforeMoney;

            if (income > 0) {
                playerCompany.recordTransaction("↳ [第 " + currentDay + " 天] 💰 收到放款本息：+$" + formatMoney(income));
            }

            playerCompany.spendCash(playerCompany.getCash());
            playerCompany.earnCash(bankSystem.getMoney());
            loadBankPanel();

            if (!bankReports.isEmpty()) {
                showBankReportPopup(bankReports);
            }

        } else if (playerCompany.getIndustry() == IndustryType.BIOTECH) {
            bioSystem.tick();
            playerCompany.spendCash(playerCompany.getCash());
            playerCompany.earnCash(bioSystem.getMoney());
            loadBioPanel();

        } else if (playerCompany.getIndustry() == IndustryType.TECH) {
            techSystem.tick();
            playerCompany.spendCash(playerCompany.getCash());
            playerCompany.earnCash(techSystem.getMoney());
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
        double oldCash = playerCompany.getCash();

        MarketEvent resultEvent = (selectedOption != null) ? selectedOption.execute(playerCompany) : null;
        playerCompany.updateStockPrice(currentDay, resultEvent);

        double newPrice = playerCompany.getStockPrice();
        double newCash = playerCompany.getCash();
        double cost = oldCash - newCash;

        if (cost > 0) {
            if (playerCompany.getIndustry() == IndustryType.BANK) {
                bankSystem.deductMoney(cost);
            } else if (playerCompany.getIndustry() == IndustryType.BIOTECH) {
                bioSystem.deductMoney(cost);
            } else if (playerCompany.getIndustry() == IndustryType.TECH) {
                techSystem.deductMoney(cost);
            }
            playerCompany.recordTransaction("↳ [第 " + currentDay + " 天] 📰 突發事件/危機公關支出：-$" + formatMoney(cost));
            updateStatusLabels();
        }

        String msg = (selectedOption == null)
                ? "今日營業時間結束，市場結算完畢。"
                : ((resultEvent != null) ? resultEvent.getName() : "公司維持穩定經營，市場無重大消息。");

        showResultPopup(msg, oldPrice, newPrice);
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

            // 1. 先記錄控制器實體
            this.currentBankController = bankController;

            // 2. 先把面板塞進主畫面的 ContentArea，讓 JavaFX 底層完成場景樹的掛載與渲染準備！
            industryContentArea.getChildren().clear();
            industryContentArea.getChildren().add(bankPanel);

            // 3. 面板確實進入畫面後，再注入數據並觸發 UI 變更！
            bankController.initData(bankSystem, this);

            // 4. 加載當天的貸款案件
            List<bank_LoanRequest> todayRequests = new ArrayList<>();
            todayRequests.add(bank_Customer.createRandomRequest());
            if (Math.random() > 0.5) {
                todayRequests.add(bank_Customer.createRandomRequest());
            }
            bankController.loadRequests(todayRequests);

            // 💡 5. 保險大絕招：用 Platform.runLater 確保在 JavaFX 渲染執行緒的下一幀，強制重刷一次大標題，絕對不被預設值蓋掉！
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

    private void handleRenameCompany() {
        if (timeline != null) timeline.pause();

        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog(playerCompany.getName());
        dialog.setTitle("變更公司名稱");
        dialog.setHeaderText("📊 進行企業更名/品牌重塑");
        dialog.setContentText("請輸入新的公司名稱：");

        java.util.Optional<String> result = dialog.showAndWait();

        result.ifPresent(newName -> {
            String trimmedName = newName.trim();
            if (!trimmedName.isEmpty()) {
                String oldName = playerCompany.getName();
                playerCompany.setName(trimmedName); // 確實改寫 Model 內部的 name
                playerCompany.recordTransaction("📝 [系統] 公司更名：【" + oldName + "】正式更名為【" + trimmedName + "】");

                updateStatusLabels();

                // 💡 用 runLater 強制通知 UI 執行緒去更換子面板大標題
                if (playerCompany.getIndustry() == IndustryType.BANK && currentBankController != null) {
                    javafx.application.Platform.runLater(() -> currentBankController.updateBankTitle());
                }

                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("更名成功");
                alert.setHeaderText(null);
                alert.setContentText("公司名稱已成功變更為：" + trimmedName);
                alert.showAndWait();
            }
        });

        if (timeline != null) timeline.play();
    }

    @FXML
    private void showStockChart() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("股市觀測站");
        alert.setHeaderText("公司歷史股價走勢圖");

        // 💡 修正：改成正确的 new NumberAxis()
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
                javafx.scene.chart.XYChart.Series<Number, Number> startPoint = new javafx.scene.chart.XYChart.Series<>();
                startPoint.getData().add(new javafx.scene.chart.XYChart.Data<>(0, history.get(0).getPrice()));
                lineChart.getData().add(startPoint);
            } else {
                for (int i = 1; i < history.size(); i++) {
                    double prevPrice = history.get(i - 1).getPrice();
                    double currPrice = history.get(i).getPrice();

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