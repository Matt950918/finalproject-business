package game.controller;

import game.model.Company;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GachaController {

    @FXML private Button btnGachaAction;
    @FXML private Label lblResultDisplay; // 🎯 畫面中央唯一的文字大標籤

    private MainGameController mainController;
    private final Random random = new Random();
    private List<GachaEvent> eventPool = new ArrayList<>();

    // 🎯 精準紀錄當下這回合到底有沒有按下按鈕抽取過
    private boolean hasDrawnThisTurn = false;

    /**
     * 🎯 JavaFX 自動生命週期方法
     * 當 FXML 載入時會第一時間自動執行，確保 eventPool 絕對不會為空
     */
    @FXML
    public void initialize() {
        initializeEventPool();
    }

    public void initData(MainGameController mainController) {
        this.mainController = mainController;
        this.hasDrawnThisTurn = false; // 每次重新點進面板時，重置抽卡旗標

        // 🎯 防呆：如果因任何意外導致事件池空了，重新初始化
        if (eventPool == null || eventPool.isEmpty()) {
            initializeEventPool();
        }

        refreshGachaUI();
    }

    public void refreshGachaUI() {
        if (mainController == null || mainController.getPlayerCompany() == null) return;
        if (btnGachaAction == null) return;

        Company company = mainController.getPlayerCompany();
        double gachaCost = 1000000;

        // ==========================================
        // 💡【核心破產漏洞修正】只要現金 <= 0，即刻判定為破產逆天改命特殊狀態！
        // ==========================================
        if (company.getCash() <= 0) {
            btnGachaAction.setDisable(false); // 🔓 確保絕對不被卡住
            btnGachaAction.setVisible(true);
            btnGachaAction.setText(" 逆天改命！(免費機會)");
            btnGachaAction.setStyle("-fx-background-color: #E74A3B; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");

            if (lblResultDisplay != null) {
                lblResultDisplay.setText(" 孤注一擲！\n\n 這是你最後的機會，抽中加薪事件即可復活！");
                lblResultDisplay.setStyle("-fx-text-fill: #e74a3b; -fx-font-size: 15px; -fx-font-weight: bold;");
            }
            return; // 🎯 致命阻斷！防止走進下方常規檢查被二次鎖死
        }

        // ==========================================
        // 🎯 常規狀態語意判定（有錢時）
        // ==========================================
        if (hasDrawnThisTurn) {
            // 狀態 A：董事長「今天已經按過抽獎」
            btnGachaAction.setDisable(true);
            btnGachaAction.setText("🔒 今日契機已用盡 (等待換日)");
            btnGachaAction.setStyle("-fx-background-color: #BDC3C7; -fx-text-fill: #7F8C8D; -fx-font-weight: bold; -fx-background-radius: 10;");

            if (lblResultDisplay != null) {
                lblResultDisplay.setText("✨ 本日機會命運已抽取完畢。\n投資市場瞬息萬變，請期待明日的全新商業契機！");
                lblResultDisplay.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 15px; -fx-font-weight: bold;");
            }
        } else if (!mainController.isGachaAvailableToday()) {
            // 狀態 B：董事長「還沒抽」，但今天主金庫隨機判定為冷卻日（無契機）
            btnGachaAction.setDisable(true);
            btnGachaAction.setText("🔒 今日無法遊玩");
            btnGachaAction.setStyle("-fx-background-color: #BDC3C7; -fx-text-fill: #7F8C8D; -fx-font-weight: bold; -fx-background-radius: 10;");

            if (lblResultDisplay != null) {
                lblResultDisplay.setText("💤 商業契機尚未顯現...\n\n今日不宜抽卡。卡池關閉。\n請點擊「結束本日營業」進入下一天，刷新集團運勢！");
                lblResultDisplay.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 15px; -fx-font-weight: bold;");
            }
        } else if (company.getCash() < gachaCost) {
            // 狀態 C：今天有契機，但公司金庫錢不夠
            btnGachaAction.setDisable(true);
            btnGachaAction.setText("❌ 資金不足 ($100 萬)");
            btnGachaAction.setStyle("-fx-background-color: #E67E22; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-background-radius: 10;");

            if (lblResultDisplay != null) {
                lblResultDisplay.setText("🎲 機會與命運已就緒！\n\n⚠️ 警告：目前集團現金不足 $100 萬手續費，無法開啟契約。");
                lblResultDisplay.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 15px; -fx-font-weight: bold;");
            }
        } else {
            // 狀態 D：今天有契機、錢也夠，完美的準備開抽狀態！
            btnGachaAction.setDisable(false);
            btnGachaAction.setText("🎲 抽取機會命運 (花費 $100 萬)");
            btnGachaAction.setStyle("-fx-background-color: #2E59D9; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");

            if (lblResultDisplay != null) {
                lblResultDisplay.setText("🎲 命運與機會的牌卡已準備完畢！\n\n✨ 提示：今日幸運女神對集團露出了微笑，花費 $100 萬手續費即可遊玩一次機會命運！");
                lblResultDisplay.setStyle("-fx-text-fill: #2e59d9; -fx-font-size: 15px; -fx-font-weight: bold;");
            }
        }
    }

    @FXML
    private void handleDraw(ActionEvent event) {
        Company company = mainController.getPlayerCompany();
        if (company == null) return;

        // 💡 判斷當前是否處於「破產逆天改命」的特殊狀態
        boolean isBankruptcyDraw = (company.getCash() <= 0 && company.isHasBankrupted());

        if (!isBankruptcyDraw) {
            // 常規狀態：檢查今日是否可抽、是否這回合已抽、金額是否足夠
            if (!mainController.isGachaAvailableToday() || hasDrawnThisTurn) return;
            if (company.getCash() < 1000000) return;
        } else {
            // 破產狀態：防連點
            if (hasDrawnThisTurn) return;
        }

        // 🔒 按下瞬間立刻鎖死按鈕，防連點
        btnGachaAction.setDisable(true);
        this.hasDrawnThisTurn = true; // 標記當下這回合已經抽取

        // 1. 金額扣除與帳目紀錄
        if (!isBankruptcyDraw) {
            // 常規狀態：實質扣除 100 萬現金
            company.setCash(company.getCash() - 1000000);
            mainController.syncCashToAllIndustries();
            company.recordTransaction("↳ [第 " + mainController.getCurrentDay() + " 天] 🎲 繳納機會命運手續費：-$100.00 萬");
        } else {
            // 破產狀態：不扣錢，改為同步當前慘澹的財政，並紀錄逆天改命事件
            mainController.syncCashToAllIndustries();
            company.recordTransaction("↳ [第 " + mainController.getCurrentDay() + " 天] ⚠️ 觸發破產逆襲機制：【機會命運】孤注一擲！(免費)");
        }

        // 🌀 拉霸高速滾動動態字串
        String[] fakeRouletteTexts;
        if (!isBankruptcyDraw) {
            fakeRouletteTexts = new String[]{
                    "⚡ 正在對接華爾街核心創投群...",
                    "🧬 正在解密新型病毒基因核心...",
                    "💻 正在遠端破解矽谷防火牆...",
                    "📊 正在操縱市場大數據即時情緒...",
                    "🚀 正在秘密對接巨頭級晶圓供應鏈...",
                    "🏦 正在暗中清查海外匿名隱藏帳戶..."
            };
        } else {
            // 破產專屬的悲壯感跑馬燈
            fakeRouletteTexts = new String[]{
                    "🔥 正在變賣創辦人最後的法拉利...",
                    "📡 正在向地下錢莊發出靈魂求助...",
                    "🏛️ 正在死守法院傳票的最後防線...",
                    "📊 正在將公司最後的希望注入命運指針...",
                    "🚨 正在與破產清算小組進行極速賽跑...",
                    "💫 正在點燃公司僅存的星星之火..."
            };
        }

        Timeline rouletteTimeline = new Timeline();
        rouletteTimeline.setCycleCount(15);

        if (lblResultDisplay != null) {
            if (isBankruptcyDraw) {
                lblResultDisplay.setStyle("-fx-text-fill: #e74a3b; -fx-font-size: 15px; -fx-font-weight: bold;");
            } else {
                lblResultDisplay.setStyle("-fx-text-fill: #9b59b6; -fx-font-size: 15px; -fx-font-weight: bold;");
            }
        }

        KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.04), e -> {
            String fakeMsg = fakeRouletteTexts[random.nextInt(fakeRouletteTexts.length)];
            if (lblResultDisplay != null) {
                String prefix = isBankruptcyDraw ? "🚨 逆天改命生死一搏 ➔ " : "🌀 命運之輪高速運轉中 ➔ ";
                lblResultDisplay.setText(prefix + fakeMsg);
            }
        });
        rouletteTimeline.getKeyFrames().add(keyFrame);

        rouletteTimeline.setOnFinished(e -> {
            executeActualGachaLogic(company);
        });

        rouletteTimeline.play();
    }

    private void executeActualGachaLogic(Company company) {
        if (eventPool == null || eventPool.isEmpty()) {
            initializeEventPool();
        }
        if (eventPool.isEmpty()) {
            if (lblResultDisplay != null) {
                lblResultDisplay.setText("❌ 錯誤：無法載入機會命運事件池，請檢查後台數據。");
            }
            return;
        }

        // 2. 隨機抽選機會命運事件
        GachaEvent pulledEvent = eventPool.get(random.nextInt(eventPool.size()));

        // 💡 判斷這次抽取是不是「破產逆天改命」觸發的
        boolean isBankruptcyDraw = (company.getCash() <= 0 && company.isHasBankrupted());

        // 3. 實質結算後續的現金與股價獎懲
        company.setCash(company.getCash() + pulledEvent.cashImpact);
        mainController.syncCashToAllIndustries();

        if (pulledEvent.stockPricePercent != 0.0) {
            double oldPrice = company.getStockPrice();
            double newPrice = oldPrice * (1.0 + pulledEvent.stockPricePercent);
            company.setStockPrice(newPrice);
        }

        // 4. 寫入事件結果的流水帳
        company.recordTransaction(String.format("↳ 🎲 機會命運結果：【%s】%s",
                pulledEvent.title,
                pulledEvent.cashImpact >= 0 ? "+$" + mainController.formatMoney(pulledEvent.cashImpact) : "-$" + mainController.formatMoney(Math.abs(pulledEvent.cashImpact))
        ));

        // 5. 常規抽中之後，立刻去主控制器拔掉紅點、鎖上大金庫冷卻
        mainController.setGachaUsedToday();

        // 🎯 將 UI 阻斷型彈窗移出動畫渲染處理週期
        javafx.application.Platform.runLater(() -> {
            // 6. 渲染阻斷型炫彩對話盒
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            String dialogTitle = "🎲 機會命運揭曉";
            String dialogHeader = "✨ 集團運勢解析：【" + pulledEvent.title + "】";

            String originalContent = pulledEvent.description + "\n\n" +
                    "📊 集團財務部核算影響：\n" +
                    "↳ 現金資產變動: " + (pulledEvent.cashImpact >= 0 ? "+$" : "-$") + mainController.formatMoney(Math.abs(pulledEvent.cashImpact)) + "\n" +
                    "↳ 企業整體股價: " + (pulledEvent.stockPricePercent >= 0 ? "+" : "") + String.format("%.0f%%", pulledEvent.stockPricePercent * 100);

            String dialogContent = isBankruptcyDraw ? "⚠️【最後生還機會結算】\n" + originalContent : originalContent;

            if (pulledEvent.type.equals("傳說") && pulledEvent.cashImpact >= 10000000) {
                dialogTitle = "👑 SSR 商業神話降臨！";
                alert.getDialogPane().setStyle("-fx-border-color: #f1c40f; -fx-border-width: 4px; -fx-background-color: #fffde7; -fx-font-family: 'Microsoft JhengHei';");
            } else if (pulledEvent.type.equals("命運") && pulledEvent.cashImpact <= -5000000) {
                alert = new Alert(Alert.AlertType.ERROR);
                dialogTitle = "🚨 💥 SSR 毀滅級黑天鵝！";
                alert.getDialogPane().setStyle("-fx-border-color: #c0392b; -fx-border-width: 4px; -fx-background-color: #fdf2f2; -fx-font-family: 'Microsoft JhengHei';");
            } else if (pulledEvent.type.equals("機運")) {
                dialogTitle = "🔮 SR 業界創新奇蹟！";
                alert.getDialogPane().setStyle("-fx-border-color: #9b59b6; -fx-border-width: 3px; -fx-background-color: #faf5ff; -fx-font-family: 'Microsoft JhengHei';");
            } else if (pulledEvent.cashImpact < 0) {
                alert = new Alert(Alert.AlertType.WARNING);
                alert.getDialogPane().setStyle("-fx-font-family: 'Microsoft JhengHei';");
            } else {
                alert.getDialogPane().setStyle("-fx-font-family: 'Microsoft JhengHei';");
            }

            alert.setTitle(dialogTitle);
            alert.setHeaderText(dialogHeader);
            alert.setContentText(dialogContent);

            javafx.scene.Node contentLabel = alert.getDialogPane().lookup(".content.label");
            if (contentLabel instanceof javafx.scene.control.Label) {
                ((javafx.scene.control.Label) contentLabel).setWrapText(true);
            }

            alert.showAndWait();

            // 7. 視窗關閉後，全面重整 UI 狀態文字與主介面標籤
            refreshGachaUI();
            mainController.updateStatusLabels();

            // 💡【核心新增】判定是否破產逆襲成功
            if (isBankruptcyDraw) {
                if (company.getCash() > 0) {
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("🎉 逆天改命成功！");
                    successAlert.setHeaderText("✨ 奇蹟降臨！公司起死回生！");
                    successAlert.setContentText(String.format("不可思議！【%s】帶來的資金挹注，成功讓公司現金轉正為 $%s！\n董事會全體起立為您鼓掌，請珍惜這第二次機會，繼續帶領公司前進！",
                            pulledEvent.title, mainController.formatMoney(company.getCash())));
                    successAlert.getDialogPane().setStyle("-fx-border-color: #2ecc71; -fx-border-width: 4px; -fx-background-color: #f4fbf7; -fx-font-family: 'Microsoft JhengHei';");
                    successAlert.showAndWait();

                    mainController.handleReturnToGame();
                } else {
                    mainController.checkBankruptcy();
                }
            }
        });
    }

    @FXML
    private void handleBack(ActionEvent event) {
        if (mainController != null) {
            Company company = mainController.getPlayerCompany();
            if (company != null && company.getCash() <= 0 && company.isHasBankrupted()) {
                Alert warnAlert = new Alert(Alert.AlertType.ERROR);
                warnAlert.setTitle("🛑 逃避無效");
                warnAlert.setHeaderText("無法離開命運之輪");
                warnAlert.setContentText("公司財政已然破產，您必須按下「逆天改命」進行最後一搏，否則無法返回遊戲！");
                warnAlert.getDialogPane().setStyle("-fx-font-family: 'Microsoft JhengHei';");
                warnAlert.showAndWait();
                return;
            }
            mainController.handleReturnToGame();
        }
    }

    private void initializeEventPool() {
        eventPool.clear();
        // ==========================================
        // 💎 傳說級（10 筆）
        // ==========================================
        eventPool.add(new GachaEvent("傳說", "幸運踩中命運格子", "董事長今天手氣爆棚，骰子精準落到獎勵格，獲得全場稅收總分成！", 12000000, 0.20));
        eventPool.add(new GachaEvent("傳說", "集團二十週年慶大樂透", "福委會買的慶祝彩券竟然中了頭獎！全公司員工年終獎金直接加碼！", 15000000, 0.05));
        eventPool.add(new GachaEvent("傳說", "路過領取遺產", "海外神祕富豪指名你為唯一繼承人，大筆實體金條直接運到公司保險箱！", 25000000, 0.0));
        eventPool.add(new GachaEvent("傳說", "獲得「免罪卡」大讓利", "因對社會做出重大貢獻，法官判定集團免除本季所有爭議訴訟，並退回扣押保證金！", 10000000, 0.15));
        eventPool.add(new GachaEvent("傳說", "全天下起鈔票雨", "地圖上觸發了瘋狂撒錢活動，總務部全員出動拿水桶去頂樓接钱！", 18000000, 0.10));
        eventPool.add(new GachaEvent("傳說", "對手踩到你的帝王地標", "死對頭總裁開車路過你蓋滿研究所的黃金地段，被迫繳納天價過路費！", 20000000, 0.30));
        eventPool.add(new GachaEvent("傳說", "神秘財神爺附身", "財神爺在總部大樓上方停留整整三天，集團做任何商務決策都莫名其妙大賺特賺！", 30000000, 0.40));
        eventPool.add(new GachaEvent("傳說", "均富卡發威", "發動均富卡！強制將地圖上最富有NPC的流動現金平分給集團！", 11000000, 0.0));
        eventPool.add(new GachaEvent("命運", "誤入惡魔島監獄", "董事長因涉嫌壟斷市場，被法官判決「坐牢三天」，需繳納保釋金並暫停所有商務談判！", -15000000, -0.15));
        eventPool.add(new GachaEvent("命運", "踩到死神格子", "倒楣透頂！死神無預警纏上總部大樓，集團資產瞬間蒸發、元氣大傷！", -12000000, -0.20));

        // ==========================================
        // ✨ 機運利多（11 筆）
        // ==========================================
        eventPool.add(new GachaEvent("機運", "使用「轉向卡」成功避禍", "本來要踩到對手的豪宅，董事長機智打出轉向卡，反而順手抄底了路邊的無人空地！", 4000000, 0.05));
        eventPool.add(new GachaEvent("機運", "搶奪卡大成功", "工程師發動搶奪卡，成功從競爭對手那裡無償奪取了一項關鍵的半導體專利！", 3000000, 0.10));
        eventPool.add(new GachaEvent("機運", "請神符請到土地公", "辦公室門口請到土地公保佑，本地房產市價翻倍，連帶讓公司總部估值暴漲！", 5000000, 0.15));
        eventPool.add(new GachaEvent("機運", "對手使用了怪獸卡", "競爭對手的怪獸把另外一家大廠的工廠踩平了，市場少了一個勁敵，我方順勢承接訂單！", 2000000, 0.08));
        eventPool.add(new GachaEvent("機運", "工程師群體觸發機運", "研發部封閉開發時手氣大開，寫程式像有神助，晶片良率莫名其妙大幅提升！", 1500000, 0.05));
        eventPool.add(new GachaEvent("機運", "買地卡強制收購", "動用買地卡，以極低代價強行買下隔壁園區的精華土地，賺取大筆潛在增值！", 6000000, 0.0));
        eventPool.add(new GachaEvent("機運", "幸運之神發紅包", "路過起點，銀行行長心情好，額外多發給集團一筆豐厚的特別創業津貼！", 3500000, 0.0));
        eventPool.add(new GachaEvent("機運", "烏托邦格子一日遊", "全公司移師地圖上的度假天堂舉辦員工旅遊，員工士氣暴增，Debug 速度提升 200%！", 0, 0.12));
        eventPool.add(new GachaEvent("命運", "對手對你用烏龜卡", "被對手惡意施放烏龜卡！每步只能走一格，商務擴展速度嚴重受阻，錯失大好良機！", -2000000, -0.10));
        eventPool.add(new GachaEvent("命運", "踩到地雷炸飛醫院", "董事長開車不小心踩到地雷，轟的一聲被送進醫院躺了三天，醫藥費與公關費損失慘重！", -6000000, -0.05));
        eventPool.add(new GachaEvent("命運", "惡魔附身遭小偷", "被地圖上的窮神和衰神同時附身，又剛好遇到小偷偷走研發部最新晶片原型機！", -3000000, -0.02));

        // ==========================================
        // ☕ 日常/命運（11 筆）
        // ==========================================
        eventPool.add(new GachaEvent("日常", "撿到拾金不昧獎金", "董事長在總部後門散步時撿到皮夾送交警局，獲得失主親自登門致謝並奉上酬金。", 1000000, 0.0));
        eventPool.add(new GachaEvent("日常", "銀行利息發放", "大富翁銀行今日結算活期利息，雖然不多，但也是一筆主動收入！", 2500000, 0.0));
        eventPool.add(new GachaEvent("日常", "向所有玩家徵收物業稅", "發動稅收卡！向地圖上的其他所有大富翁強制徵收少額的地皮管理費。", 1800000, 0.01));
        eventPool.add(new GachaEvent("日常", "老家房子拆遷補償", "前任老總裁留在林口老街的破舊瓦房被政府強制徵收，補償款今天入帳。", 5000000, 0.0));
        eventPool.add(new GachaEvent("日常", "變賣過期卡片", "把道具欄裡用不到的冬眠卡、路障卡打包賣給黑市商人，不無小補。", 1200000, 0.0));
        eventPool.add(new GachaEvent("日常", "抽中免費麥當勞全餐", "福委會抽中麥當勞全體員工大禮包，今天午餐全公司免費吃大麥克，士氣大振！", 0, 0.02));
        eventPool.add(new GachaEvent("日常", "投擲骰子點數皆相同", "連續擲出三個 6！雖然沒有實質獎勵，但心情大好，研發效率微微上升。", 0, 0.01));
        eventPool.add(new GachaEvent("命運", "繳納地價稅與房屋稅", "一年一度的大富翁稅務總複查來臨，集團名下地皮太多，被迫繳納巨額綜合稅！", -4000000, 0.0));
        eventPool.add(new GachaEvent("命運", "踩到路障被迫停車", "不知道誰在路上亂放路障，物流車隊被迫在路邊苦等一天，繳納超時違約金。", -2500000, -0.02));
        eventPool.add(new GachaEvent("命運", "被狗咬進醫院", "在路邊招惹流浪狗結果被瘋狂追咬，董事長被迫住院打狂犬病疫苗，笑掉大眾大牙。", -1200000, 0.0));
        eventPool.add(new GachaEvent("命運", "對手發動冬眠卡", "全公司集體被對手催眠進入冬眠狀態！雖然只有短短幾天，但部分業務被迫停擺。", 0, -0.08));
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