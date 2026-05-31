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

    @FXML private Button btnEndDay; // 給 Bug 3 用的
    private List<bank_LoanRequest> currentBankRequests = null; // 給 Bug 1 用的：暫存今日銀行客人

    // 🎲 全新機會命運狀態機變數
    private boolean gachaUsedYesterday = false;
    private boolean gachaAvailableToday = false;

    // 🎯 用來判斷當前彈窗是「隨機突發消息」還是「真正的日終結算」
    private boolean isEndOfDaySettlement = false;

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

    public void startGame(String customName, IndustryType selectedIndustry, int slotIndex) {
        this.currentSlotIndex = slotIndex;
        PlayerData sessionData = MainMenuController.activeProgress;

        if (sessionData != null && sessionData.getCompany() != null && sessionData.getDay() > 0) {
            System.out.println("📂 [進度載入] 成功載入 Slot [" + slotIndex + "] 歷史存檔...");
            this.playerCompany = sessionData.getCompany();
            this.currentDay = sessionData.getDay() - 1;

            if (playerCompany.getIndustry() == IndustryType.BANK) {
                bankSystem.setMoney(playerCompany.getCash());

                // 💡 關鍵修復：不管是哪個時空背景的存檔，只要是銀行產業，
                // 讀檔進來的第一件事，就是強迫 bank_Customer 去把 57 人名單載入記憶體！
                // 這樣等一下 loadBankPanel 抽卡時，csvList 就絕對不會是空的！
                bank_Customer.createRandomRequest();
            }
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

    private void saveCurrentProgress() {
        PlayerData sessionData = MainMenuController.activeProgress;
        if (sessionData != null && playerCompany != null) {
            if (playerCompany.getIndustry() == IndustryType.BANK) playerCompany.setCash(bankSystem.getMoney());
            else if (playerCompany.getIndustry() == IndustryType.BIOTECH) playerCompany.setCash(bioSystem.getMoney());
            else if (playerCompany.getIndustry() == IndustryType.TECH) playerCompany.setCash(techSystem.getMoney());

            sessionData.setCompany(this.playerCompany);
            sessionData.setMoney(this.playerCompany.getCash());
            sessionData.setDay(this.currentDay);

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

        // 🌅 進入新的一天，重置日終結算旗標
        this.isEndOfDaySettlement = false;

        if (this.currentDay > 50) {
            if (timeline != null) {
                timeline.stop();
            }

            if (resultOverlay != null) resultOverlay.setVisible(false);
            if (newsOverlay != null) newsOverlay.setVisible(false);

            if (mainGameLayer != null) mainGameLayer.setDisable(false);
            if (industryContentArea != null) industryContentArea.setDisable(false);

            saveCurrentProgress();

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
                    comment = "✨ 經營有方！您的企業已經成為園園區內不容忽視的強大中流佇柱！";
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
                gameOverAlert.getDialogPane().setPrefWidth(550);

                javafx.scene.Node contentLabel = gameOverAlert.getDialogPane().lookup(".content.markdown-writer-node");
                if (contentLabel == null) {
                    contentLabel = gameOverAlert.getDialogPane().lookup(".content.label");
                }
                if (contentLabel instanceof javafx.scene.control.Label) {
                    ((javafx.scene.control.Label) contentLabel).setWrapText(true);
                }
                gameOverAlert.showAndWait();

                handleLoadRanking(null);
                updateStatusLabels();

                if (industryContentArea != null) {
                    for (javafx.scene.Node node : industryContentArea.lookupAll(".button")) {
                        if (node instanceof javafx.scene.control.Button) {
                            javafx.scene.control.Button btn = (javafx.scene.control.Button) node;
                            if (btn.getText().contains("返回公司經營") || btn.getText().contains("BACK")) {
                                btn.setVisible(false);
                                btn.setDisable(true);
                            }
                        }
                    }
                }
            });

            return;
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
            currentBankRequests = new ArrayList<>();
            currentBankRequests.add(bank_Customer.createRandomRequest());
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
            if (currentBankRequests == null) {
                currentBankRequests = new ArrayList<>();
                currentBankRequests.add(bank_Customer.createRandomRequest());
            }
            bankController.loadRequests(currentBankRequests);

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
                handleOptionSelected(option); // 🎯 點選新聞選項
            });
            optionsBox.getChildren().add(btn);
        }
        newsOverlay.setVisible(true);
        timeline.pause(); // 突發消息跳出時，先暫停白天的倒數
    }

    @FXML
    private void handleSkipDay(ActionEvent event) {
        if (newsOverlay != null) newsOverlay.setVisible(false);
        timeline.play();
    }

    // 倒數時間到換日、或者主動點擊換日，明確標記為「日終大結算」
    private void handleTimeout() {
        this.isEndOfDaySettlement = true;
        handleOptionSelected(null);
    }

    @FXML
    private void handleEndDay(ActionEvent event) {
        this.isEndOfDaySettlement = true;
        handleOptionSelected(null);
    }

    private void handleOptionSelected(NewsOption selectedOption) {
        // 如果 selectedOption 存在，表示這是白天的隨機新聞抉難，isEndOfDaySettlement 依然是 false
        timeline.stop();
        double oldPrice = playerCompany.getStockPrice();
        MarketEvent resultEvent = (selectedOption != null) ? selectedOption.execute(playerCompany) : null;

        if (resultEvent != null && resultEvent.getName() != null && resultEvent.getName().contains("火災停業")) {
            if (techSystem != null) {
                techSystem.triggerFireLockdown(3);
                System.out.println("🚨 成功攔截火災事件！已強迫科技半導體系統停業 3 天。");
                // 🔥 立即重載 TechPanel，讓封鎖畫面即時取代合約卡片，不等玩家點按鈕
                if (playerCompany.getIndustry() == IndustryType.TECH && currentTechController != null) {
                    javafx.application.Platform.runLater(this::loadTechPanel);
                }
            }
        }

        playerCompany.updateStockPrice(currentDay, resultEvent);

        if (playerCompany.getIndustry() == IndustryType.BANK) bankSystem.setMoney(playerCompany.getCash());
        else if (playerCompany.getIndustry() == IndustryType.BIOTECH) bioSystem.setMoney(playerCompany.getCash());
        else if (playerCompany.getIndustry() == IndustryType.TECH) techSystem.setMoney(playerCompany.getCash());

        // 🎯 核心修復：把 updateStatusLabels() 從這裡移除！
        // 絕對不要在隨機新聞剛點完時就提早去刷面板，避免火災時 TechPanelController 提早加載渲染出簽約卡片

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

        if (playerCompany.getCash() <= 0) {
            checkBankruptcy();
            return;
        }

        // 🎯 核心修復：等玩家真正把結果視窗（如火災損失報告）點擊確定關掉了，此時才安全地去刷新 UI！
        // 這時如果處於火災期間，TechPanelController 就會精確讀到重整狀態，並完美封鎖談判與簽約介面
        updateStatusLabels();

        // 🎯 核心控制分流
        if (this.isEndOfDaySettlement) {
            // 情況一：如果真的是時間到、或手動點擊結束本日營業，才推進到下一天
            System.out.println("⏳ 今日營業大結算完成，推進天數。");
            startNewDay();
        } else {
            // 情況二：這只是白天的突發隨機事件處理完畢，釋放 UI 面板，讓玩家繼續留在當天操作
            System.out.println("📰 突發事件處理完畢，返回當前第 " + currentDay + " 天主畫面。");
            timeline.play(); // 恢復時間倒數，繼續過完今天
        }
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

    @FXML
    private void handleReturnToMainMenu(javafx.event.ActionEvent event) {
        if (timeline != null) {
            timeline.stop();
        }

        saveCurrentProgress();
        System.out.println("💾 已自動保存當前產業進度，準備返回主選單。");

        core.Mainapp.showHome(); // ✅ 直接呼叫 Mainapp，滿版自動處理好

        System.out.println("🚪 成功退出遊戲，已返回主選單介面。");
    }

    /**
     * 🏁 宣告遊戲結束（比照 51 天規格）
     */
    private void triggerGameOver(String reason) {
        if (timeline != null) {
            timeline.stop();
        }
        if (resultOverlay != null) resultOverlay.setVisible(false);
        if (newsOverlay != null) newsOverlay.setVisible(false);
        if (mainGameLayer != null) mainGameLayer.setDisable(false);
        if (industryContentArea != null) industryContentArea.setDisable(false);

        if (btnEndDay != null) {
            btnEndDay.setDisable(true);
            btnEndDay.setVisible(false);
        }

        saveCurrentProgress();

        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert gameOverAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            gameOverAlert.setTitle(" 遊戲結束 ");
            gameOverAlert.setHeaderText(" 公司宣告破產，遊戲結束 ");

            double finalCash = playerCompany.getCash();
            double finalStockPrice = playerCompany.getStockPrice();

            gameOverAlert.setContentText(String.format(
                    " 結算原因: %s\n\n" +
                            " 最終資產: $%s\n" +
                            " 最終股價: $%.2f\n\n" +
                            " 遺憾！您未能挽回公司的頹勢，最終走向破產一途。歡迎再次挑戰！",
                    reason, formatMoney(finalCash), finalStockPrice
            ));

            gameOverAlert.getDialogPane().setStyle("-fx-border-color: #e74a3b; -fx-border-width: 4px; -fx-background-color: #fdf2f2; -fx-font-family: 'Microsoft JhengHei';");
            gameOverAlert.getDialogPane().setPrefWidth(550);

            // 確保文字自動換行
            javafx.scene.Node contentLabel = gameOverAlert.getDialogPane().lookup(".content.label");
            if (contentLabel instanceof javafx.scene.control.Label) {
                ((javafx.scene.control.Label) contentLabel).setWrapText(true);
            }

            gameOverAlert.showAndWait();

            // 導向排行榜並停用返回按鈕
            handleLoadRanking(null);
            updateStatusLabels();
            if (industryContentArea != null) {
                for (javafx.scene.Node node : industryContentArea.lookupAll(".button")) {
                    if (node instanceof javafx.scene.control.Button) {
                        javafx.scene.control.Button btn = (javafx.scene.control.Button) node;
                        if (btn.getText().contains("返回") || btn.getText().contains("BACK")) {
                            btn.setVisible(false);
                            btn.setDisable(true);
                        }
                    }
                }
            }
        });
    }

    /**
     * ⚠️ 檢查是否破產與執行逆天改命機制
     */
    public void checkBankruptcy() {
        if (playerCompany.getCash() <= 0) {
            // 檢查是否已經使用過逆天改命機會
            if (!playerCompany.isHasBankrupted()) {
                playerCompany.setHasBankrupted(true);

                // 彈出提示，告知玩家將強制執行一次機會命運
                javafx.application.Platform.runLater(() -> {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                    alert.setTitle(" 破產危機！");
                    alert.setHeaderText(" 公司資金已然見底！");
                    alert.setContentText(" 觸發特別機制：系統將強制為您執行一次【機會命運】！\n 這是您最後的翻盤機會，如果依然無法讓資金轉正，遊戲將直接結束！");
                    alert.getDialogPane().setStyle("-fx-font-family: 'Microsoft JhengHei';");
                    alert.showAndWait();

                    // 強制切換到 Gacha 畫面並自動執行
                    handleLoadGacha(null);
                });
            } else {
                // 已經破產過，且這次還是沒錢 -> 宣告遊戲結束
                triggerGameOver(" 機會命運未能救回公司財政，資金依舊為負。");
            }
        }
    }
}