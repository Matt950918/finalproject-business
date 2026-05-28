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

    @FXML private Button btnGacha;
    @FXML private Button btnRanking;

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

    private BankPanelController currentBankController;
    private BioPanelController currentBioController;
    private TechPanelController currentTechController;

    private RankingSystem rankingSystem = new RankingSystem();

    // ==========================================
    // 🎲 介面載入觸發
    // ==========================================
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

    /**
     * 🔙 提供給子面板呼叫的返回鍵接口
     */
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
     * 🎮 啟動遊戲核心邏輯
     */
    public void startGame(String customName, IndustryType selectedIndustry) {
        PlayerData sessionData = MainMenuController.activeProgress;

        if (sessionData != null && sessionData.getCompany() != null && sessionData.getDay() > 0) {
            System.out.println("📂 [進度載入] 還原產業: " + sessionData.getCompany().getIndustry());
            this.playerCompany = sessionData.getCompany();
            this.currentDay = sessionData.getDay() - 1;

            if (playerCompany.getIndustry() == IndustryType.BANK) bankSystem.setMoney(playerCompany.getCash());
            else if (playerCompany.getIndustry() == IndustryType.BIOTECH) bioSystem.setMoney(playerCompany.getCash());
            else if (playerCompany.getIndustry() == IndustryType.TECH) techSystem.setMoney(playerCompany.getCash());
        } else {
            System.out.println("🏢 [進度建立] 完美指定選擇產業: " + selectedIndustry);
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

        // 💡 提示：此處已不允許任何 setOnAction 覆蓋事件，全權由 FXML 接管路由。
        System.out.println("🔥 [初始化完成] 當前核心公司物件 Hash: " + System.identityHashCode(playerCompany));

        NewsDatabase.resetDatabase();
        if (newsOverlay != null) newsOverlay.setVisible(false);
        if (resultOverlay != null) resultOverlay.setVisible(false);

        setupTimer();
        startNewDay();
    }

    private void saveCurrentProgress() {
        PlayerData sessionData = MainMenuController.activeProgress;
        if (sessionData != null && playerCompany != null) {
            if (playerCompany.getIndustry() == IndustryType.BANK) playerCompany.setCash(bankSystem.getMoney());
            else if (playerCompany.getIndustry() == IndustryType.BIOTECH) playerCompany.setCash(bioSystem.getMoney());
            else if (playerCompany.getIndustry() == IndustryType.TECH) playerCompany.setCash(techSystem.getMoney());

            sessionData.setCompany(this.playerCompany);
            sessionData.setMoney(this.playerCompany.getCash());
            sessionData.setDay(this.currentDay);
            PlayerAccount.saveProgress(sessionData);
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

    /**
     * 🌅 每日換日核心調度邏輯
     */
    private void startNewDay() {
        currentDay++;
        if (playerCompany != null) {
            playerCompany.decrementBuffTurns();
        }

        GachaController.decrementCooldown();

        IndustryType activeType = playerCompany.getIndustry();
        System.out.println("🌅 第 " + currentDay + " 天開始。當前核心產業判定為: " + activeType);

        if (activeType == IndustryType.BANK) bankSystem.setMoney(playerCompany.getCash());
        else if (activeType == IndustryType.BIOTECH) bioSystem.setMoney(playerCompany.getCash());
        else if (activeType == IndustryType.TECH) techSystem.setMoney(playerCompany.getCash());

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
            bioSystem.tick();
            playerCompany.setCash(bioSystem.getMoney());
            loadBioPanel();
        }
        else if (activeType == IndustryType.TECH) {
            techSystem.tick();
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

        if (currentDay > 0 && currentDay % 30 == 0) {
            int month = currentDay / 30;
            playerCompany.summarizeLedger(month);
            saveCurrentProgress();
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

        if (playerCompany.getIndustry() == IndustryType.BANK) playerCompany.setCash(bankSystem.getMoney());
        else if (playerCompany.getIndustry() == IndustryType.BIOTECH) playerCompany.setCash(bioSystem.getMoney());
        else if (playerCompany.getIndustry() == IndustryType.TECH) playerCompany.setCash(techSystem.getMoney());

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

    /**
     * 📊 秀出公司歷史資金明細（含保險防空補登與即時 Console 監控機制）
     */
    @FXML
    private void showLedger() {
        if (playerCompany == null) {
            System.err.println("❌ [Ledger Error] 點擊帳本時 playerCompany 為 null！");
            return;
        }

        System.out.println("📊 [Ledger Debug] 目前物件 Hash: " + System.identityHashCode(playerCompany)
                + " | 當前天數: " + currentDay + " | 真實紀錄筆數: " + playerCompany.getLedger().size());

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("財務報表系統");
        alert.setHeaderText("📊 " + playerCompany.getName() + " - 核心資金異動歷史明細");

        javafx.scene.control.ListView<String> listView = new javafx.scene.control.ListView<>();

        // 🎯 毒品突發停工懲罰之明細即時自動登錄機制
        if (playerCompany.getIndustry() == IndustryType.BIOTECH && bioSystem.getLockdownTurns() > 0) {
            String lastLog = playerCompany.getLedger().isEmpty() ? "" : playerCompany.getLedger().get(playerCompany.getLedger().size() - 1);
            if (!lastLog.contains("🚨 [突發制裁]")) {
                double penaltyFine = bioSystem.getDrugs().stream()
                        .filter(d -> d.getType() == game.model.bio.Drug.DrugType.NARCOTIC)
                        .findFirst().map(d -> d.getDynamicCost(bioSystem.getCostDiscount()) * 2).orElse(30000000.0);
                playerCompany.recordTransaction(String.format("↳ [第 %d 天] 🚨 [突發制裁] 地下研發遭檢警查禁，強制執行 2 倍行政罰鍰：-$%,.0f 萬", currentDay, penaltyFine / 10000));
            }
        }

        List<String> records = playerCompany.getLedger();

        // 🌟【防空保險機制】：如果發現記憶體內的陣列是空的，當場動態幫玩家補登一筆當前的經營現況！
        if (records == null || records.isEmpty()) {
            System.out.println("⚠️ [Ledger Warning] 偵測到 ledger 為空，啟動防空安全補登機制！");
            listView.getItems().add(String.format("🏢 [經營現況] 公司名：%s (記憶體異動紀錄重置中)", playerCompany.getName()));
            listView.getItems().add(String.format("↳ [第 %d 天] 💰 目前留存結餘現金額：$%s", currentDay, formatMoney(playerCompany.getCash())));
            listView.getItems().add("ℹ️ 提示：新一輪投資獲利或研發扣款發生時，帳單將會自動接續更新。");
        } else {
            // 🌟 倒序排列優化：最新的帳務記錄永遠排在最上面
            for (int i = records.size() - 1; i >= 0; i--) {
                listView.getItems().add(records.get(i));
            }
        }

        // 🌟 介面 CSS 精美化
        listView.setPrefSize(550, 380);
        listView.setStyle(
                "-fx-font-family: 'Microsoft JhengHei', 'Segoe UI'; " +
                        "-fx-font-size: 13px; " +
                        "-fx-background-color: #f8f9fc; " +
                        "-fx-border-color: #eaecf4; " +
                        "-fx-border-radius: 5px;"
        );

        // 🌟 動態高亮色彩工廠
        listView.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    setText(item);
                    if (item.contains("+$") || item.contains("獲利") || item.contains("到帳")) {
                        setStyle("-fx-text-fill: #1cc88a; -fx-font-weight: bold; -fx-padding: 6px;");
                    } else if (item.contains("-$") || item.contains("❌") || item.contains("🚨") || item.contains("罰鍰") || item.contains("支出")) {
                        setStyle("-fx-text-fill: #e74a3b; -fx-font-weight: bold; -fx-padding: 6px;");
                    } else {
                        setStyle("-fx-text-fill: #4e73df; -fx-padding: 6px;");
                    }
                }
            }
        });

        alert.getDialogPane().setContent(listView);
        alert.getDialogPane().setPrefWidth(580);
        alert.showAndWait();
    }

    /**
     * 📊 秀出公司歷史股價走勢圖 (XYChart 內部類別符號完美版)
     */
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

                    segment.getData().add(d1);
                    segment.getData().add(d2);
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
}