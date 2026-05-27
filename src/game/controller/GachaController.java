package game.controller;

import game.model.Company;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 🎲 機會命運抽卡系統 (GachaController.java)
 * 精準對齊 FXML 的 #handleDraw 事件名稱，徹底解決載入崩潰 Bug
 */
public class GachaController {

    // 💡 提示：請確保你的 GachaPanel.fxml 裡面，按鈕的 fx:id 填寫的是 btnGachaAction
    // 如果你們 FXML 裡按鈕的 fx:id 是別的名字（例如 btnDraw），請把它改成對應的名字
    @FXML private Button btnGachaAction;
    @FXML private Label lblResultDisplay;

    private MainGameController mainController;

    // 全域靜態冷卻計數器
    public static int cooldownTurns = 0;

    private final Random random = new Random();
    private List<GachaEvent> eventPool = new ArrayList<>();

    public void initData(MainGameController mainController) {
        this.mainController = mainController;
        initializeEventPool(); // 載入機會命運平衡事件池
        refreshGachaUI();      // 刷新按鈕外觀狀態
    }

    /**
     * 📊 核心 UI/UX 狀態機：動態重塑按鈕文字與停用狀態
     */
    public void refreshGachaUI() {
        if (mainController == null || mainController.getPlayerCompany() == null) return;

        // 防禦機制：如果 FXML 綁定尚未成功，先跳出避免 NullPointerException
        if (btnGachaAction == null) return;

        Company company = mainController.getPlayerCompany();
        double gachaCost = 1000000; // 花費 100 萬

        if (cooldownTurns > 0) {
            btnGachaAction.setDisable(true);
            btnGachaAction.setText("🔒 冷卻中 (剩餘 " + cooldownTurns + " 回合)");
            btnGachaAction.setStyle("-fx-background-color: #BDC3C7; -fx-text-fill: #7F8C8D; -fx-font-weight: bold; -fx-background-radius: 10;");
        } else if (company.getCash() < gachaCost) {
            btnGachaAction.setDisable(true);
            btnGachaAction.setText("❌ 資金不足 ($100 萬)");
            btnGachaAction.setStyle("-fx-background-color: #E67E22; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-background-radius: 10;");
        } else {
            btnGachaAction.setDisable(false);
            btnGachaAction.setText("🎲 抽取機會命運 (花費 $100 萬)");
            btnGachaAction.setStyle("-fx-background-color: #2E59D9; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-background-radius: 10;");
        }
    }

    /**
     * 🎯 關鍵修正：將方法名稱精準改為 handleDraw！
     * 完美對齊 GachaPanel.fxml 第 32 行的 onAction="#handleDraw"
     */
    @FXML
    private void handleDraw(ActionEvent event) {
        if (cooldownTurns > 0) return;

        Company company = mainController.getPlayerCompany();
        if (company.getCash() < 1000000) return;

        // 1. 實質扣除 100 萬現金
        company.setCash(company.getCash() - 1000000);

        // 2. 隨機抽選機會命運事件
        GachaEvent pulledEvent = eventPool.get(random.nextInt(eventPool.size()));

        // 3. 實質結算現金與股價
        company.setCash(company.getCash() + pulledEvent.cashImpact);

        if (pulledEvent.stockPricePercent != 0.0) {
            double oldPrice = company.getStockPrice();
            double newPrice = oldPrice * (1.0 + pulledEvent.stockPricePercent);
            company.setStockPrice(newPrice);
        }

        // 4. 寫入流水帳
        company.recordTransaction(String.format("↳ 🎲 機會命運：【%s】%s",
                pulledEvent.title,
                pulledEvent.cashImpact >= 0 ? "+$" + mainController.formatMoney(pulledEvent.cashImpact) : "-$" + mainController.formatMoney(Math.abs(pulledEvent.cashImpact))
        ));

        // 5. 渲染結果到面板上
        StringBuilder sb = new StringBuilder();
        sb.append("✨ 【").append(pulledEvent.type).append(" · ").append(pulledEvent.title).append("】\n\n");
        sb.append(pulledEvent.description).append("\n\n");
        sb.append("💰 現金變動: ").append(pulledEvent.cashImpact >= 0 ? "+" : "").append(pulledEvent.cashImpact / 10000).append(" 萬\n");
        sb.append("📈 股價影響: ").append(pulledEvent.stockPricePercent >= 0 ? "+" : "").append((int)(pulledEvent.stockPricePercent * 100)).append("%");

        if (lblResultDisplay != null) {
            lblResultDisplay.setText(sb.toString());
            lblResultDisplay.setStyle("-fx-text-fill: " + (pulledEvent.cashImpact < 0 || pulledEvent.stockPricePercent < 0 ? "#E74A3B" : "#2E59D9") + "; -fx-font-size: 15px; -fx-font-weight: bold;");
        }

        // 6. 進入 3 回合冷卻並刷新 UI
        cooldownTurns = 3;
        refreshGachaUI();

        mainController.updateStatusLabels();
    }

    public static void decrementCooldown() {
        if (cooldownTurns > 0) cooldownTurns--;
    }

    private void initializeEventPool() {
        eventPool.clear();
        // 傳說級（超賺區）
        eventPool.add(new GachaEvent("傳說", "AI 技術奇點", "你的 AI 成本降低 95%，震撼市場。", 12000000, 0.35));
        eventPool.add(new GachaEvent("傳說", "國家級補助", "政府宣布扶植你的產業。", 15000000, 0.0));
        eventPool.add(new GachaEvent("傳說", "海外病毒式爆紅", "產品在全球社群瘋傳。", 0, 0.40));
        eventPool.add(new GachaEvent("傳說", "成功 IPO", "公司正式上市。", 30000000, 0.50));
        eventPool.add(new GachaEvent("傳說", "拿下世界級企業訂單", "簽下超大型合約。", 20000000, 0.0));

        // 機運利多（小賺～中賺）
        eventPool.add(new GachaEvent("機運", "天使投資人", "有人看好你的公司。", 4000000, 0.0));
        eventPool.add(new GachaEvent("機運", "銀行低利貸款", "成功貸到低利資金。", 3000000, 0.0));
        eventPool.add(new GachaEvent("機運", "YouTuber 爆推", "網紅推薦你的產品。", 0, 0.12));
        eventPool.add(new GachaEvent("機運", "競爭對手翻車", "對手產品出包。", 0, 0.15));
        eventPool.add(new GachaEvent("機運", "撿到便宜設備", "買到超便宜高級器材。", 1500000, 0.0));
        eventPool.add(new GachaEvent("機運", "展覽大爆單", "展場訂單接不完。", 6000000, 0.0));
        eventPool.add(new GachaEvent("機運", "免費媒體曝光", "新聞主動採訪。", 0, 0.08));

        // 社會平穩（搞笑區）
        eventPool.add(new GachaEvent("日常", "免費咖啡日", "員工士氣提升。", 0, 0.0));
        eventPool.add(new GachaEvent("日常", "老闆請吃火鍋", "公司氣氛融洽。", 0, 0.0));
        eventPool.add(new GachaEvent("日常", "開會三小時", "什麼都沒決定。", 0, 0.0));
        eventPool.add(new GachaEvent("日常", "工程師 debug 一整天", "最後發現少打分號。", 0, 0.0));
        eventPool.add(new GachaEvent("日常", "同事帶貓上班", "大家都沒心情工作。", 0, 0.0));
        eventPool.add(new GachaEvent("日常", "尾牙抽中氣炸鍋", "全公司羨慕。", 0, 0.0));

        // 命運考驗（虧錢區）
        eventPool.add(new GachaEvent("命運", "遭駭客攻擊", "資料庫被入侵。", -5000000, -0.15));
        eventPool.add(new GachaEvent("命運", "勞檢開罰", "加班問題被抓。", -3000000, 0.0));
        eventPool.add(new GachaEvent("命運", "核心員工離職", "技術主管被挖角。", 0, -0.20));
        eventPool.add(new GachaEvent("命運", "伺服器全面當機", "網站癱瘓一天。", -4000000, -0.10));
        eventPool.add(new GachaEvent("命運", "社群炎上", "公關危機爆發。", 0, -0.25));
        eventPool.add(new GachaEvent("命運", "投資錯誤", "盲目擴張失敗。", -8000000, 0.0));
        eventPool.add(new GachaEvent("命運", "合作夥伴跑路", "貨款追不回來。", -6000000, 0.0));
        eventPool.add(new GachaEvent("命運", "AI 回答失控", "產品講出奇怪內容。", 0, -0.18));
        eventPool.add(new GachaEvent("命運", "專利訴訟", "被競爭對手告上法院。", -12000000, 0.0));
        eventPool.add(new GachaEvent("命運", "雲端服務爆炸", "API 全掛。", -3500000, 0.0));
    }

    private static class GachaEvent {
        String type;
        String title;
        String description;
        double cashImpact;
        double stockPricePercent;

        GachaEvent(String type, String title, String description, double cashImpact, double stockPricePercent) {
            this.type = type;
            this.title = title;
            this.description = description;
            this.cashImpact = cashImpact;
            this.stockPricePercent = stockPricePercent;
        }
    }
}