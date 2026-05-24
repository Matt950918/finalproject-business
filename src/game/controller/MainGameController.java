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
    @FXML private Label lblStockPrice;
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

    // ==========================================
    // 🔗 開放給外部呼叫的接口
    // ==========================================
    public Company getPlayerCompany() {
        return playerCompany;
    }

    // 🌟 補上這個！讓 BankPanel 可以取得今天是第幾天
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
        playerCompany = new Company(selectedIndustry);

        playerCompany.spendCash(playerCompany.getCash());
        playerCompany.earnCash(50_000_000);
        playerCompany.recordTransaction("🏢 [系統] 遠東集團創立，獲得初始資金：$5000 萬");

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

        // 1. 執行產業結算與記帳
        if (playerCompany.getIndustry() == IndustryType.BANK) {
            double beforeMoney = bankSystem.getMoney();
            List<String> bankReports = bankSystem.tick();
            double income = bankSystem.getMoney() - beforeMoney;

            // 📝 記下每天收到的利息
            if (income > 0) {
                playerCompany.recordTransaction("↳ [第 " + currentDay + " 天] 💰 收到放款本息：+$" + formatMoney(income));
            }

            playerCompany.spendCash(playerCompany.getCash());
            playerCompany.earnCash(bankSystem.getMoney());
            loadBankPanel();

            // 🏦 如果有結清或違約，跳出匯報彈窗
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

        // 2. 新聞彈窗
        if (newsOverlay != null) newsOverlay.setVisible(false);
        if (Math.random() < 0.25) {
            DailyNews todayNews = NewsDatabase.getRandomNewsFor(playerCompany.getIndustry());
            if (todayNews != null) {
                showNewsPopup(todayNews);
            }
        }

        // 3. 🌟 每 30 天結算產出月報
        if (currentDay > 0 && currentDay % 30 == 0) {
            int month = currentDay / 30;
            playerCompany.summarizeLedger(month);
            playerCompany.recordTransaction("🏦 [月結] 第 " + month + " 個月結算保留盈餘：$" + formatMoney(playerCompany.getCash()));
        }
    }

    // ==========================================
    // 🏦 銀行專屬：每日帳務匯報彈窗
    // ==========================================
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

    // ==========================================
    // 📰 突發新聞彈窗處理
    // ==========================================
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

    // ==========================================
    // 🖱️ 決策與結算處理
    // ==========================================
    private void handleOptionSelected(NewsOption selectedOption) {
        timeline.stop();
        double oldPrice = playerCompany.getStockPrice();
        MarketEvent resultEvent = (selectedOption != null) ? selectedOption.execute(playerCompany) : null;
        playerCompany.updateStockPrice(currentDay, resultEvent);
        double newPrice = playerCompany.getStockPrice();

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

    // ==========================================
    // 💰 數字格式化與 UI 載入工具
    // ==========================================
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
            // 如果你的 FXML 是放在 resources/layout 下，請把上面那行改成：
            // FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/layout/BankPanel.fxml"));
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

    // ==========================================
    // 📖 顯示資金明細帳本
    // ==========================================
    @FXML
    private void showLedger() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("財務報表");
        alert.setHeaderText("公司歷史資金明細");

        javafx.scene.control.ListView<String> listView = new javafx.scene.control.ListView<>();
        List<String> records = playerCompany.getLedger();

        // 🛡️ 雙重防呆：如果 records 根本不存在 (null)，或者裡面沒東西 (isEmpty)
        if (records == null || records.isEmpty()) {
            listView.getItems().add("目前尚無資金異動紀錄。");
        } else {
            // 將最新的紀錄顯示在最上面
            for (int i = records.size() - 1; i >= 0; i--) {
                listView.getItems().add(records.get(i));
            }
        }

        listView.setPrefSize(400, 300);
        alert.getDialogPane().setContent(listView);
        alert.showAndWait();
    }

    // ==========================================
    // 🧬 載入生技專屬全螢幕儀表板
    // ==========================================
    private void loadBioPanel() {
        try {
            // 💡 已經幫你把路徑對齊到 /game/view/BioPanel.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/game/view/BioPanel.fxml"));
            VBox bioPanel = loader.load();

            // 取得控制器並注入底層數據
            BioPanelController bioController = loader.getController();
            bioController.initData(bioSystem, this);

            // 清空主畫面中央區塊，並把生技大廳擺上去
            industryContentArea.getChildren().clear();
            industryContentArea.getChildren().add(bioPanel);

        } catch (Exception e) {
            System.err.println("❌ 載入 BioPanel.fxml 失敗！請檢查路徑與 Controller 設定。");
            e.printStackTrace();
        }
    }

    // ==========================================
    // 💻 載入科技專屬全螢幕儀表板
    // ==========================================
    private void loadTechPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/game/view/TechPanel.fxml"));
            VBox techPanel = loader.load();

            TechPanelController techController = loader.getController();
            techController.initData(techSystem, this);

            industryContentArea.getChildren().clear();
            industryContentArea.getChildren().add(techPanel);

        } catch (Exception e) {
            System.err.println("❌ 載入 TechPanel.fxml 失敗！");
            e.printStackTrace();
        }
    }
}