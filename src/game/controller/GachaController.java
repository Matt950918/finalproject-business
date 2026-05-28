package game.controller;

import game.model.Company;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GachaController {

    @FXML private Button btnGachaAction;
    @FXML private Label lblResultDisplay;

    private MainGameController mainController;

    private final Random random = new Random();
    private List<GachaEvent> eventPool = new ArrayList<>();

    public void initData(MainGameController mainController) {
        this.mainController = mainController;
        initializeEventPool();
        refreshGachaUI();
    }

    public void refreshGachaUI() {
        if (mainController == null || mainController.getPlayerCompany() == null) return;
        if (btnGachaAction == null) return;

        Company company = mainController.getPlayerCompany();
        double gachaCost = 1000000;

        if (!mainController.isGachaAvailableToday()) {
            btnGachaAction.setDisable(true);
            btnGachaAction.setText("🔒 今日無契機 (等待商業時機)");
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

    @FXML
    private void handleDraw(ActionEvent event) {
        if (!mainController.isGachaAvailableToday()) return;

        Company company = mainController.getPlayerCompany();
        if (company.getCash() < 1000000) return;

        // 1. 實質扣除 100 萬現金
        company.setCash(company.getCash() - 1000000);

        // ⚡ 核心修正：立即同步手續費扣款至大廠底層子金庫，防止數據被吃掉！
        mainController.syncCashToAllIndustries();

        // 2. 寫入手續費明細
        company.recordTransaction("↳ [第 " + mainController.getCurrentDay() + " 天] 🎲 繳納機會命運手續費：-$100.00 萬");

        // 3. 隨機抽選機會命運事件
        GachaEvent pulledEvent = eventPool.get(random.nextInt(eventPool.size()));

        // 4. 實質結算後續的現金與股價獎懲
        company.setCash(company.getCash() + pulledEvent.cashImpact);

        // ⚡ 核心修正：中獎加錢後，立刻通知底層子金庫將中獎金額同步寫入！
        mainController.syncCashToAllIndustries();

        if (pulledEvent.stockPricePercent != 0.0) {
            double oldPrice = company.getStockPrice();
            double newPrice = oldPrice * (1.0 + pulledEvent.stockPricePercent);
            company.setStockPrice(newPrice);
        }

        // 5. 寫入事件結果的流水帳
        company.recordTransaction(String.format("↳ 🎲 機會命運結果：【%s】%s",
                pulledEvent.title,
                pulledEvent.cashImpact >= 0 ? "+$" + mainController.formatMoney(pulledEvent.cashImpact) : "-$" + mainController.formatMoney(Math.abs(pulledEvent.cashImpact))
        ));

        // 6. 渲染結果到面板上
        StringBuilder sb = new StringBuilder();
        sb.append("✨ 【").append(pulledEvent.type).append(" · ").append(pulledEvent.title).append("】\n\n");
        sb.append(pulledEvent.description).append("\n\n");
        sb.append("💰 現金變動: ").append(pulledEvent.cashImpact >= 0 ? "+" : "").append(pulledEvent.cashImpact / 10000).append(" 萬\n");
        sb.append("📈 股價影響: ").append(pulledEvent.stockPricePercent >= 0 ? "+" : "").append((int)(pulledEvent.stockPricePercent * 100)).append("%");

        if (lblResultDisplay != null) {
            lblResultDisplay.setText(sb.toString());
            lblResultDisplay.setStyle("-fx-text-fill: " + (pulledEvent.cashImpact < 0 || pulledEvent.stockPricePercent < 0 ? "#E74A3B" : "#2E59D9") + "; -fx-font-size: 15px; -fx-font-weight: bold;");
        }

        // 7. 通知主控制器今天已抽，明天強制進入冷卻
        mainController.setGachaUsedToday();

        // 8. 刷新當前抽卡面板 UI
        refreshGachaUI();

        mainController.updateStatusLabels();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        if (mainController != null) {
            mainController.handleReturnToGame();
        }
    }

    private void initializeEventPool() {
        eventPool.clear();

        // ==========================================
        // 💎 傳說級（超賺區）
        // ==========================================
        eventPool.add(new GachaEvent("傳說", "AI 技術奇點", "你的 AI 成本降低 95%，震撼市場。", 12000000, 0.35));
        eventPool.add(new GachaEvent("傳說", "國家級補助", "政府宣布扶植你的產業。", 15000000, 0.0));
        eventPool.add(new GachaEvent("傳說", "海外病毒式爆紅", "產品在全球社群瘋傳。", 0, 0.40));
        eventPool.add(new GachaEvent("傳說", "成功 IPO", "公司正式上市。", 30000000, 0.50));
        eventPool.add(new GachaEvent("傳說", "拿下世界級企業訂單", "簽下超大型合約。", 20000000, 0.0));

        // ==========================================
        // ✨ 機運利多（小賺～中賺）
        // ==========================================
        eventPool.add(new GachaEvent("機運", "天使投資人", "有人看好你的公司。", 4000000, 0.0));
        eventPool.add(new GachaEvent("機運", "銀行低利貸款", "成功貸到低利資金。", 3000000, 0.0));
        eventPool.add(new GachaEvent("機運", "YouTuber 爆推", "網網紅推薦你的產品。", 0, 0.12));
        eventPool.add(new GachaEvent("機運", "競爭對手翻車", "對手產品出包。", 0, 0.15));
        eventPool.add(new GachaEvent("機運", "撿到便宜設備", "買到超便宜高級器材。", 1500000, 0.0));
        eventPool.add(new GachaEvent("機運", "展覽大爆單", "展場訂單接不完。", 6000000, 0.0));
        eventPool.add(new GachaEvent("機運", "免費媒體曝光", "新聞主動採訪。", 0, 0.08));

        // ==========================================
        // ☕ 社會平穩（搞笑區）
        // ==========================================
        eventPool.add(new GachaEvent("日常", "免費咖啡日", "員工士氣提升。", 0, 0.0));
        eventPool.add(new GachaEvent("日常", "老闆請吃火鍋", "公司氣氛融洽。", 0, 0.0));
        eventPool.add(new GachaEvent("日常", "開會三小時", "什麼都沒決定。", 0, 0.0));
        eventPool.add(new GachaEvent("日常", "工程師 debug 一整天", "最後發現少打分號。", 0, 0.0));
        eventPool.add(new GachaEvent("日常", "同事帶貓上班", "大家都沒心情工作。", 0, 0.0));
        eventPool.add(new GachaEvent("日常", "尾牙抽中氣炸鍋", "全公司羨慕。", 0, 0.0));

        // ==========================================
        // 💥 命運考驗（虧錢區）
        // ==========================================
        eventPool.add(new GachaEvent("命運", "遭駭客攻擊", "資料庫被入侵。", -5000000, -0.15));
        eventPool.add(new GachaEvent("命運", "勞檢開罰", "加班問題被抓。", -3000000, 0.0));
        eventPool.add(new GachaEvent("命運", "核心員工離職", "技術主管被挖角。", 0, -0.20));
        eventPool.add(new GachaEvent("命運", "伺服器全面當機", "網站癱瘓一天。", -4000000, -0.10));
        eventPool.add(new GachaEvent("命運", "社群炎上", "公關危機爆發。", 0, -0.25));
        eventPool.add(new GachaEvent("命運", "投資錯誤", "盲目擴張失敗。", -8000000, 0.0));
        eventPool.add(new GachaEvent("命運", "合作夥伴跑路", "貨款追不回來。", -6000000, 0.0));
        eventPool.add(new GachaEvent("命運", "AI 回答失控", "產品講出奇怪內容。", 0, -0.18));
        eventPool.add(new GachaEvent("命運", "专利訴訟", "被競爭對手告上法院。", -12000000, 0.0));
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