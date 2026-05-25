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
    private Label lblStockPrice;
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

    // 底層系統 (一啟動時會建立預設物件)
    private bank_system bankSystem = new bank_system();

    private Company playerCompany;
    private int currentDay = 1; // 修正：初始為第 1 天
    private Timeline timeline;
    private int timeLeft = 60;

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
        lblStockPrice.setText("股價：$" + String.format("%.2f", playerCompany.getStockPrice()));
    }

    // ==========================================
    // 🚀 遊戲生命週期
    // ==========================================
    public void startGame(IndustryType selectedIndustry) {
        PlayerData player = PlayerAccount.getPlayerData(MainMenuController.currentUser);

        if (player.getCompany() == null) {
            // 🆕 新遊戲建立
            playerCompany = new Company(selectedIndustry);
            playerCompany.spendCash(playerCompany.getCash()); // 清空預設，統一給 5000 萬
            playerCompany.earnCash(50_000_000);
            playerCompany.recordTransaction("🏢 [系統] 遠東集團創立，獲得初始資金：$5000 萬");

            this.currentDay = 1;
            player.setCompany(playerCompany);
            player.setDay(this.currentDay);
            PlayerAccount.saveData();

            System.out.println("🆕 新遊戲建立成功");

            // 新遊戲需要主動執行第一天
            setupTimer();
            startNewDay();
        } else {
            // 📂 載入舊遊戲
            this.playerCompany = player.getCompany();
            this.currentDay = player.getDay(); // 讀取上次存檔的天數

            System.out.println("📂 載入舊遊戲成功！目前天數：" + currentDay + "，資產：$" + formatMoney(playerCompany.getCash()));

            // 🌟 核心同步：把舊公司存下來的錢，強行灌進當前的 bankSystem
            if (playerCompany.getIndustry() == IndustryType.BANK) {
                if (bankSystem != null) {
                    bankSystem.setMoney(playerCompany.getCash());
                }
                loadBankPanel();
            }

            // 🌟 修正：讀檔時直接讓時鐘開始跑，不要重複呼叫 startNewDay() 避免多跳一天
            setupTimer();
            timeLeft = 60;
            timeline.play();
        }
        updateStatusLabels();
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
        // 1. 執行產業結算與記帳
        if (playerCompany.getIndustry() == IndustryType.BANK) {
            double beforeMoney = bankSystem.getMoney();
            List<String> bankReports = bankSystem.tick();
            double income = bankSystem.getMoney() - beforeMoney;

            if (income > 0) {
                playerCompany.recordTransaction("↳ [第 " + currentDay + " 天] 💰 收到放款本息：+$" + formatMoney(income));
            }

            // 同步子系統與公司的金額
            playerCompany.spendCash(playerCompany.getCash());
            playerCompany.earnCash(bankSystem.getMoney());
            loadBankPanel();

            if (!bankReports.isEmpty()) {
                showBankReportPopup(bankReports);
            }
        }

        // 2. 新聞彈窗
        if (newsOverlay != null) newsOverlay.setVisible(false);
        if (Math.random() < 0.25) {
            DailyNews todayNews = NewsDatabase.getRandomNewsFor(playerCompany.getIndustry());
            if (todayNews != null) {
                showNewsPopup(todayNews);
            }
        }

        // 3. 每 30 天結算產出月報
        if (currentDay > 0 && currentDay % 30 == 0) {
            int month = currentDay / 30;
            playerCompany.summarizeLedger(month);
            playerCompany.recordTransaction("🏦 [月結] 第 " + month + " 個月結算保留盈餘：$" + formatMoney(playerCompany.getCash()));
        }

        // 🌟 4. 換日結束，天數前進，並在這一刻強制執行「本地存檔」
        this.currentDay++;
        updateStatusLabels();

        PlayerData player = PlayerAccount.getPlayerData(MainMenuController.currentUser);
        if (player != null) {
            player.setCompany(this.playerCompany);
            player.setDay(this.currentDay);
            PlayerAccount.saveData(); // 儲存至本地 user_data.dat
            System.out.println("💾 [自動存檔] 第 " + (currentDay - 1) + " 天結束，最新進度已存入硬碟。");
        }

        timeLeft = 60;
        timeline.play();
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
        MarketEvent resultEvent = (selectedOption != null) ? selectedOption.execute(playerCompany) : null;
        playerCompany.updateStockPrice(currentDay, resultEvent);
        double newPrice = playerCompany.getStockPrice();

        // 🌟 修正：因為選完新聞可能會影響公司資金 (例如罰款、賺錢)，選完立刻更新同步
        if (playerCompany.getIndustry() == IndustryType.BANK) {
            bankSystem.setMoney(playerCompany.getCash());
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
            lblPriceChange.setStyle("-fx-text-fill: #1CC88A;");
        } else if (diff < -0.01) {
            lblResultHeader.setText("📉 損失預警報告");
            lblPriceChange.setText(String.format("股價變動: -$%.2f (%.2f%%)", Math.abs(diff), percent));
            lblPriceChange.setStyle("-fx-text-fill: #E74A3B;");
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
            bankController.initData(bankSystem, this);

            List<bank_LoanRequest> todayRequests = new ArrayList<>();
            todayRequests.add(bank_Customer.createRandomRequest());
            if (Math.random() > 0.5) {
                todayRequests.add(bank_Customer.createRandomRequest());
            }
            bankController.loadRequests(todayRequests);

            industryContentArea.getChildren().clear();
            industryContentArea.getChildren().add(bankPanel);
        } catch (Exception e) {
            System.err.println("❌ 載入 BankPanel.fxml 失敗！請檢查路徑與 Controller 設定。");
            e.printStackTrace();
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

    // 生技與科技大廳因為你尚未實作，先移除多餘呼叫以防編譯錯誤
    private void loadBioPanel() { System.out.println("🧬 生技系統尚未實作"); }
    private void loadTechPanel() { System.out.println("💻 科技系統尚未實作"); }
}