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

        // 🎯 核心終局攔截：如果 currentDay 超過 50（代表剛結束第 50 天營業）
        if (this.currentDay > 50) {
            // 1. 強制把背景倒數計時器關掉
            if (timeline != null) {
                timeline.stop();
            }

            // 2. 徹底關閉所有可能會殘留的滿版遮罩層（杜絕白色、黑色滿版發白問題）
            if (resultOverlay != null) resultOverlay.setVisible(false);
            if (newsOverlay != null) newsOverlay.setVisible(false);

            // 3. 確保大層完全解鎖，允許點擊
            if (mainGameLayer != null) mainGameLayer.setDisable(false);
            if (industryContentArea != null) industryContentArea.setDisable(false);

            // 4. 自動幫玩家把最終成績存檔
            saveCurrentProgress();

            // 5. 彈出大富翁風格的終局結算對話盒
            javafx.application.Platform.runLater(() -> {
                javafx.scene.control.Alert gameOverAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                gameOverAlert.setTitle("🏁 50 天商業帝國任期圓滿結束！");
                gameOverAlert.setHeaderText("👑 恭喜董事長完成 50 天的經營挑戰！");

                double finalCash = playerCompany.getCash();
                double finalStockPrice = playerCompany.getStockPrice();

                String comment;
                if (finalCash >= 100000000) {
                    comment = "🎉 簡真是商業神話！您成功打造了市值通天的超級企業巨頭！";
                } else if (finalCash >= 50000000) {
                    comment = "✨ 經營有方！您的企業已經成為園區內不容忽視的強大中流砥柱！";
                } else if (finalCash > 0) {
                    comment = "☕ 穩健經營！您成功帶領全體員工挺過了 50 天的商海風暴！";
                } else {
                    comment = "🚨 慘澹收場！看來大富翁的商場並不好混，下次再接再厲！";
                }

                gameOverAlert.setContentText(String.format(
                        "董事長，任期已滿！集團財務部與董事會已完成最終清算：\n\n" +
                                "💰 集團最終總資金: %s\n" +
                                "📈 集團最終總股價: $%.2f\n\n" +
                                "%s\n\n" +
                                "👉 提示：點擊確定後，畫面將自動為您切換至【🏆 企業排行榜】。您可以查看自己在所有存檔中的歷史排名，也可以隨時點擊右下角【返回主選單】開啟新局！",
                        formatMoney(finalCash), finalStockPrice, comment
                ));

                gameOverAlert.getDialogPane().setStyle("-fx-border-color: #3498db; -fx-border-width: 4px; -fx-background-color: #f7f9fa; -fx-font-family: 'Microsoft JhengHei';");
                // 🎯 1. 核心修正：將對話框面板拉寬到 550 像素（保證寬度足夠）
                gameOverAlert.getDialogPane().setPrefWidth(550);

                // 🎯 2. 正確換行修正：從對話框中抓出真正的文字 Label 節點，並對它設定自動換行
                javafx.scene.Node contentLabel = gameOverAlert.getDialogPane().lookup(".content.markdown-writer-node");
                if (contentLabel == null) {
                    // 如果用 class 找不到，就改用一般的文字區域 lookup 抓法
                    contentLabel = gameOverAlert.getDialogPane().lookup(".content.label");
                }
                if (contentLabel instanceof javafx.scene.control.Label) {
                    ((javafx.scene.control.Label) contentLabel).setWrapText(true);
                }
                gameOverAlert.showAndWait();

                // 6. 點擊確認後，精準切換至排行榜，並同步頂部真實資產數值
                handleLoadRanking(null);
                updateStatusLabels();

                // 🎯 核心修正：破關後強行把「返回公司經營」按鈕拔掉！
                if (industryContentArea != null) {
                    // 透過 CSS Selector 尋找排行榜面板內寫著「返回公司經營」或 fx:id 的按鈕
                    // 如果你在 FXML 裡有給它 id（例如 btnBackToGame），也可以直接去 RankingController 裡拔
                    // 這裡最安全、不用改 FXML 的暴力抓法是直接搜尋該區域內所有的 Button：
                    for (javafx.scene.Node node : industryContentArea.lookupAll(".button")) {
                        if (node instanceof javafx.scene.control.Button) {
                            javafx.scene.control.Button btn = (javafx.scene.control.Button) node;
                            if (btn.getText().contains("返回公司經營") || btn.getText().contains("BACK")) {
                                btn.setVisible(false); // 徹底隱藏，不留痕跡！
                                btn.setDisable(true);  // 雙重保險防點擊
                            }
                        }
                    }
                }
            });

            return; // 🎯 致命阻斷！絕對不讓程式碼往下跑隨機新聞與其他重新載入邏輯！
        }
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

        // 🆕 【精準攔截連動】：如果結果名稱包含「火災停業」，強迫科技系統啟動 3 天停業
        if (resultEvent != null && resultEvent.getName() != null && resultEvent.getName().contains("火災停業")) {
            if (techSystem != null) {
                techSystem.triggerFireLockdown(3);
                System.out.println("🚨 成功攔截火災事件！已強迫科技半導體系統停業 3 天。");
            }
        }

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