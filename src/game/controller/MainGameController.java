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
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

public class MainGameController {

    @FXML private Label lblDay;
    @FXML private Label lblTimer;
    @FXML private Button btnStockPrice;
    @FXML private Button btnCash;

    @FXML private Button btnGacha;
    @FXML private Button btnRanking;

    @FXML private Circle gachaBadge;

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

    // 🎲 全新機會命運狀態機變數
    private boolean gachaUsedYesterday = false;
    private boolean gachaAvailableToday = false;

    // 底層系統
    private bank_system bankSystem = new bank_system();
    private game.model.bio.BioSystem bioSystem = new game.model.bio.BioSystem();
    private game.model.tech.TechSystem techSystem = new game.model.tech.TechSystem();

    private Company playerCompany;
    private int currentDay = 0;
    private Timeline timeline;
    private int timeLeft = 60;

    private BankPanelController currentBankController;
    private BioPanelController currentBioController;
    private TechPanelController currentTechController;

    private RankingSystem rankingSystem = new RankingSystem();

    // 🆕 新增：紀錄當前玩家遊玩的存檔槽位 (0, 1, 2)
    private int currentSlotIndex = 0;

    @FXML
    private void handleLoadGacha(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/game/view/GachaPanel.fxml"));
            VBox gachaPanel = loader.load();
            GachaController gachaController = loader.getController();

            gachaController.initData(this);
            this.currentBankController = null;

            industryContentArea.getChildren().clear();
            industryContentArea.getChildren().add(gachaPanel);
            System.out.println("🎲 已成功切換至抽卡系統介面");
        } catch (Exception e) {
            System.err.println("❌ 載入 GachaPanel.fxml 失敗！");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLoadRanking(ActionEvent event) {
        try {
            java.net.URL fxmlUrl = MainGameController.class.getResource("/game/view/RankingPanel.fxml");
            if (fxmlUrl == null) {
                System.err.println("❌ 嚴重錯誤：找不到 /game/view/RankingPanel.fxml 檔案！");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            VBox rankingPanel = loader.load();
            RankingPanelController rankingController = loader.getController();

            rankingController.initData(this);
            rankingController.showLeaderboard(rankingSystem);
            this.currentBankController = null;

            industryContentArea.getChildren().clear();
            industryContentArea.getChildren().add(rankingPanel);
            System.out.println("🏆 已成功切換至排行榜介面");
        } catch (Exception e) {
            System.err.println("❌ 載入 RankingPanel.fxml 失敗！");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleReturnToGame() {
        if (playerCompany == null) return;

        IndustryType activeType = playerCompany.getIndustry();
        System.out.println("🔄 玩家點擊返回鍵，正在退回當前產業主畫面: " + activeType);

        if (activeType == IndustryType.BANK) {
            loadBankPanel();
        } else if (activeType == IndustryType.BIOTECH) {
            loadBioPanel();
        } else if (activeType == IndustryType.TECH) {
            loadTechPanel();
        }
    }

    public Company getPlayerCompany() { return playerCompany; }
    public game.model.tech.TechSystem getTechSystem() { return techSystem; }
    public game.model.bio.BioSystem getBioSystem() { return bioSystem; }
    public int getCurrentDay() { return currentDay; }

    public void updateStatusLabels() {
        if (playerCompany == null) return;
        lblDay.setText("第 " + currentDay + " 天");
        btnCash.setText("資金：$" + formatMoney(playerCompany.getCash()));
        btnStockPrice.setText("股價：$" + String.format("%.2f", playerCompany.getStockPrice()));

        if (playerCompany.getIndustry() == IndustryType.BANK && currentBankController != null) {
            currentBankController.updateBankTitle();
        } else if (playerCompany.getIndustry() == IndustryType.BIOTECH && currentBioController != null) {
            currentBioController.updateBioTitle();
        } else if (playerCompany.getIndustry() == IndustryType.TECH && currentTechController != null) {
            currentTechController.updateTechTitle();
        }
    }

    /**
     * 🛠️ 修改：startGame 新增傳入選取的存檔槽位 slotIndex
     */
    public void startGame(String customName, IndustryType selectedIndustry, int slotIndex) {
        this.currentSlotIndex = slotIndex; // 鎖定槽位
        PlayerData sessionData = MainMenuController.activeProgress; // 這裡通常是玩家點選槽位後包好的對象

        if (sessionData != null && sessionData.getCompany() != null && sessionData.getDay() > 0) {
            System.out.println("📂 [進度載入] 成功載入 Slot [" + slotIndex + "] 歷史存檔，產業: " + sessionData.getCompany().getIndustry());
            this.playerCompany = sessionData.getCompany();
            this.currentDay = sessionData.getDay() - 1;

            if (playerCompany.getIndustry() == IndustryType.BANK) bankSystem.setMoney(playerCompany.getCash());
            else if (playerCompany.getIndustry() == IndustryType.BIOTECH) bioSystem.setMoney(playerCompany.getCash());
            else if (playerCompany.getIndustry() == IndustryType.TECH) techSystem.setMoney(playerCompany.getCash());
        } else {
            System.out.println("🏢 [進度建立] Slot [" + slotIndex + "] 為新局，新創產業: " + selectedIndustry);
            playerCompany = new Company(customName, selectedIndustry);
            playerCompany.getLedger().clear();
            playerCompany.recordTransaction("🏢 [系統] " + playerCompany.getName() + " 正式創立！");

            if (selectedIndustry == IndustryType.BANK) bankSystem.setMoney(playerCompany.getCash());
            else if (selectedIndustry == IndustryType.BIOTECH) bioSystem.setMoney(playerCompany.getCash());
            else if (selectedIndustry == IndustryType.TECH) techSystem.setMoney(playerCompany.getCash());

            if (sessionData != null) {
                sessionData.setCompany(playerCompany);
                sessionData.setDay(1);
                sessionData.setMoney(playerCompany.getCash());
            }
        }

        btnCash.setOnAction(e -> showLedger());
        btnStockPrice.setOnAction(e -> showStockChart());

        NewsDatabase.resetDatabase();
        if (newsOverlay != null) newsOverlay.setVisible(false);
        if (resultOverlay != null) resultOverlay.setVisible(false);

        setupTimer();
        startNewDay();
    }

    /**
     * 🛠️ 修改：配合多槽位核心存檔機制重構
     */
    private void saveCurrentProgress() {
        PlayerData sessionData = MainMenuController.activeProgress;
        if (sessionData != null && playerCompany != null) {
            // 同步最新的各產業資產到公司物件上
            if (playerCompany.getIndustry() == IndustryType.BANK) playerCompany.setCash(bankSystem.getMoney());
            else if (playerCompany.getIndustry() == IndustryType.BIOTECH) playerCompany.setCash(bioSystem.getMoney());
            else if (playerCompany.getIndustry() == IndustryType.TECH) playerCompany.setCash(techSystem.getMoney());

            sessionData.setCompany(this.playerCompany);
            sessionData.setMoney(this.playerCompany.getCash());
            sessionData.setDay(this.currentDay);

            // 🎯 重點修正：改為調用具備槽位參數的 saveSlotProgress 方法
            PlayerAccount.saveSlotProgress(sessionData.getUsername(), this.currentSlotIndex, sessionData);
        }
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
        if (playerCompany != null) {
            playerCompany.decrementBuffTurns();
        }

        if (gachaUsedYesterday) {
            gachaAvailableToday = false;
            gachaUsedYesterday = false;
            System.out.println("🎲 [機會命運] 由於昨日已抽卡，今日強制進入冷卻。");
        } else {
            gachaAvailableToday = Math.random() < 0.50;
            System.out.println("🎲 [機會命運] 隨機判定結果：今日契機開啟 = " + gachaAvailableToday);
        }

        updateGachaBadge();

        IndustryType activeType = playerCompany.getIndustry();
        System.out.println("🌅 第 " + currentDay + " 天開始。當前核心產業判定為: " + activeType);

        if (activeType == IndustryType.BANK) {
            double beforeMoney = bankSystem.getMoney();
            List<String> bankReports = bankSystem.tick();
            double income = bankSystem.getMoney() - beforeMoney;

            if (income > 0) {
                playerCompany.earnCash(income);
                playerCompany.recordTransaction("↳ [第 " + currentDay + " 天] 💰 收到放款本息：+$" + formatMoney(income));
            }
            bankSystem.setMoney(playerCompany.getCash());
            loadBankPanel();

            if (!bankReports.isEmpty()) {
                showBankReportPopup(bankReports);
            }
        }
        else if (activeType == IndustryType.BIOTECH) {
            double beforeMoney = bioSystem.getMoney();
            bioSystem.tick();
            double income = bioSystem.getMoney() - beforeMoney;

            if (income > 0) {
                playerCompany.recordTransaction("↳ [第 " + currentDay + " 天] 💰 收到上市新藥之每日研發讚助營收：+$" + formatMoney(income));
            }
            playerCompany.setCash(bioSystem.getMoney());
            loadBioPanel();
        }
        else if (activeType == IndustryType.TECH) {
            double beforeMoney = techSystem.getMoney();
            techSystem.tick();
            double income = techSystem.getMoney() - beforeMoney;

            if (income > 0) {
                playerCompany.recordTransaction("↳ [第 " + currentDay + " 天] 💰 收到供應鏈合約清算淨利潤：+$" + formatMoney(income));
            } else if (income < 0) {
                playerCompany.recordTransaction("↳ [第 " + currentDay + " 天] 📉 支付供應鏈合約與維護淨虧損：-$" + formatMoney(Math.abs(income)));
            }
            playerCompany.setCash(techSystem.getMoney());
            loadTechPanel();
        }

        updateStatusLabels();
        saveCurrentProgress();

        timeLeft = 60;
        timeline.play();

        if (newsOverlay != null) newsOverlay.setVisible(false);
        if (Math.random() < 0.25) {
            DailyNews todayNews = NewsDatabase.getRandomNewsFor(playerCompany.getIndustry());
            if (todayNews != null) showNewsPopup(todayNews);
        }


    }

    private void loadBankPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/game/view/BankPanel.fxml"));
            VBox bankPanel = loader.load();
            BankPanelController bankController = loader.getController();

            this.currentBankController = bankController;
            this.currentBioController = null;
            this.currentTechController = null;

            bankController.initData(bankSystem, this);
            List<bank_LoanRequest> todayRequests = new ArrayList<>();
            todayRequests.add(bank_Customer.createRandomRequest());
            bankController.loadRequests(todayRequests);

            industryContentArea.getChildren().clear();
            industryContentArea.getChildren().add(bankPanel);

            javafx.application.Platform.runLater(() -> {
                if (this.currentBankController != null) this.currentBankController.updateBankTitle();
            });
        } catch (Exception e) {
            System.err.println("❌ 載入 BankPanel.fxml 失敗！");
            e.printStackTrace();
        }
    }

    private void loadBioPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/game/view/BioPanel.fxml"));
            VBox bioPanel = loader.load();
            BioPanelController bioController = loader.getController();

            this.currentBioController = bioController;
            this.currentBankController = null;
            this.currentTechController = null;

            bioController.initData(bioSystem, this);

            industryContentArea.getChildren().clear();
            industryContentArea.getChildren().add(bioPanel);

            javafx.application.Platform.runLater(() -> {
                if (this.currentBioController != null) this.currentBioController.updateBioTitle();
            });
        } catch (Exception e) {
            System.err.println("❌ 載入 BioPanel.fxml 失敗！");
            e.printStackTrace();
        }
    }

    private void loadTechPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/game/view/TechPanel.fxml"));
            VBox techPanel = loader.load();
            TechPanelController techController = loader.getController();

            this.currentTechController = techController;
            this.currentBankController = null;
            this.currentBioController = null;

            techController.initData(techSystem, this);

            industryContentArea.getChildren().clear();
            industryContentArea.getChildren().add(techPanel);

            javafx.application.Platform.runLater(() -> {
                if (this.currentTechController != null) this.currentTechController.updateTechTitle();
            });
        } catch (Exception e) {
            System.err.println("❌ 載入 TechPanel.fxml 失敗！");
            e.printStackTrace();
        }
    }

    private void showBankReportPopup(List<String> reports) {
        StringBuilder sb = new StringBuilder();
        for (String r : reports) sb.append(r).append("\n\n");
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

    @FXML private void handleSkipDay(ActionEvent event) { if (newsOverlay != null) newsOverlay.setVisible(false); timeline.play(); }
    private void handleTimeout() { handleOptionSelected(null); }
    @FXML private void handleEndDay(ActionEvent event) { handleOptionSelected(null); }

    private void handleOptionSelected(NewsOption selectedOption) {
        timeline.stop();
        double oldPrice = playerCompany.getStockPrice();
        MarketEvent resultEvent = (selectedOption != null) ? selectedOption.execute(playerCompany) : null;
        playerCompany.updateStockPrice(currentDay, resultEvent);

        if (playerCompany.getIndustry() == IndustryType.BANK) bankSystem.setMoney(playerCompany.getCash());
        else if (playerCompany.getIndustry() == IndustryType.BIOTECH) bioSystem.setMoney(playerCompany.getCash());
        else if (playerCompany.getIndustry() == IndustryType.TECH) techSystem.setMoney(playerCompany.getCash());

        updateStatusLabels();
        String msg = (selectedOption == null) ? "今日營業時間結束，市場結算完畢。" : ((resultEvent != null) ? resultEvent.getName() : "公司維持穩定經營，市場無重大消息。");
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
        saveCurrentProgress();
        startNewDay();
    }

    public String formatMoney(double amount) {
        boolean isNegative = amount < 0;
        double absAmount = Math.abs(amount);
        String formattedStr;
        if (absAmount >= 1_000_000_00) formattedStr = String.format("%.2f 億", absAmount / 1_000_000_00.0);
        else if (absAmount >= 10000) formattedStr = String.format("%.2f 萬", absAmount / 10000.0);
        else formattedStr = String.format("%.0f", absAmount);
        return (isNegative ? "-" : "") + formattedStr;
    }

    @FXML
    private void showLedger() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("財務報表");
        alert.setHeaderText("公司歷史資金明細");
        javafx.scene.control.ListView<String> listView = new javafx.scene.control.ListView<>();
        List<String> records = playerCompany.getLedger();
        if (records == null || records.isEmpty()) listView.getItems().add("目前尚無資金異動紀錄。");
        else { for (int i = records.size() - 1; i >= 0; i--) listView.getItems().add(records.get(i)); }
        listView.setPrefSize(400, 300);
        alert.getDialogPane().setContent(listView);
        alert.showAndWait();
    }

    @FXML
    private void showStockChart() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("股市觀測站");
        alert.setHeaderText("公司歷史股價走勢圖");
        javafx.scene.chart.NumberAxis xAxis = new javafx.scene.chart.NumberAxis();
        javafx.scene.chart.NumberAxis yAxis = new javafx.scene.chart.NumberAxis();
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
                    segment.getData().add(d1); segment.getData().add(d2);
                    lineChart.getData().add(segment);
                    String color = (currPrice >= prevPrice) ? "#E74A3B" : "#1CC88A";
                    javafx.scene.Node line = segment.getNode().lookup(".chart-series-line");
                    if (line != null) line.setStyle("-fx-stroke: " + color + "; -fx-stroke-width: 3px;");
                }
            }
        }
        lineChart.setPrefSize(550, 400);
        alert.getDialogPane().setContent(lineChart);
        alert.showAndWait();
    }

    public boolean isGachaAvailableToday() {
        return gachaAvailableToday;
    }

    public void setGachaUsedToday() {
        this.gachaAvailableToday = false;
        this.gachaUsedYesterday = true;
        updateGachaBadge();
    }

    public void updateGachaBadge() {
        if (gachaBadge != null) {
            gachaBadge.setVisible(gachaAvailableToday);
        }
    }

    public void syncCashToAllIndustries() {
        if (playerCompany == null) return;
        double currentCash = playerCompany.getCash();
        if (bankSystem != null) bankSystem.setMoney(currentCash);
        if (bioSystem != null) bioSystem.setMoney(currentCash);
        if (techSystem != null) techSystem.setMoney(currentCash);
    }

    /**
     * 🆕 新增：點擊「返回主選單」按鈕時的處理邏輯
     * 退出目前玩家的經營畫面，退回到最開頭的主選單
     */
    @FXML
    private void handleReturnToMainMenu(javafx.event.ActionEvent event) {
        // 1. 停止當前天數的倒數計時器，避免背景繼續跑
        if (timeline != null) {
            timeline.stop();
        }

        // 2. 自動幫玩家把目前的產業進度存檔，防止資料遺失
        saveCurrentProgress();
        System.out.println("💾 已自動保存當前產業進度，準備返回主選單。");

        try {
            // 3. 重新載入 MainMenu.fxml 畫面
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/game/view/MainMenu.fxml"));
            javafx.scene.Parent root = loader.load();

            // 4. 切換 Scene
            javafx.stage.Stage stage = (Stage) lblDay.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();

            System.out.println("🚪 成功退出遊戲，已返回主選單介面。");
        } catch (Exception e) {
            System.err.println("❌ 載入 MainMenu.fxml 失敗！");
            e.printStackTrace();
        }
    }
}