package game.model;

import game.model.bio.BioSystem;
import game.model.tech.TechSystem;
import java.util.Random;

public class GachaSystem {
    private Random random = new Random();

    public String draw(Company company, TechSystem techSystem, BioSystem bioSystem, int currentTurn) {
        // 🔒 1. 核心冷卻限制：5天（5回合）之內禁止重複抽卡
        int daysPassed = currentTurn - company.getLastGachaTurn();
        if (daysPassed < 5) {
            int daysLeft = 5 - daysPassed;
            return "⏳ 【董事會攔截】戰略決策冷卻中！\n還要再過 " + daysLeft + " 天才能再次啟動商業抽卡。";
        }

        double cost = 1000000; // 抽卡報名花費 $100 萬
        if (company.getCash() < cost) {
            return "❌ 資金不足！公司金庫至少需要 $100 萬現金。";
        }

        // 扣除本體抽卡成本，並鎖定本次抽卡天數
        company.spendCash(cost);
        company.setLastGachaTurn(currentTurn);

        // 🎲 2. 機率判定：12%傳說、38%機運利多、25%社會平穩、25%命運考驗
        int roll = random.nextInt(100);

        if (roll < 12) {
            // ==========================================
            // 💎 【傳說級 - 世紀機遇】 (12% 機率)
            // ==========================================
            int eventId = random.nextInt(5);
            switch (eventId) {
                case 0:
                    company.applyStockPercentChange(0.35);
                    company.earnCash(12000000);
                    company.recordTransaction("🎲 [抽卡] 💎【傳說】AI技術奇點！AI成本降低95%：+$1200.00萬，股價飆升！");
                    return "【傳說卡 ✨ AI技術奇點】\n研發取得跨世紀突破！你的 AI 開發成本降低 95%，震撼全球市場！\n👉 效果：現金流實質增加 $1200 萬，公司股價大漲 +35%！";
                case 1:
                    company.earnCash(15000000);
                    // 套用狀態：未來 3 回合收益增加 10%
                    company.setRevenueBuff(3, 0.10);
                    company.recordTransaction("🎲 [抽卡] 💎【傳說】國家級戰略補助引進：+$1500.00萬，且未來3回合收益+10%！");
                    return "【傳說卡 ✨ 國家級補助】\n政府宣布你所屬的產業為國家核心扶植對象！\n👉 效果：直接獲撥 $1500 萬發展專金！【狀態Buff】未來 3 回合公司所有收益提升 10%！";
                case 2:
                    company.applyStockPercentChange(0.40);
                    company.recordTransaction("🎲 [抽卡] 💎【傳說】海外病毒式爆紅！產品在國際社群引起狂熱風潮，股價大漲！");
                    return "【傳說卡 ✨ 海外病毒式爆紅】\n公司主打產品在歐美社群被好萊塢巨星轉發，全球徹底陷入狂熱！\n👉 效果：品牌估值狂飆，公司股價當場暴漲 +40%！";
                case 3:
                    company.earnCash(30000000);
                    company.applyStockPercentChange(0.50);
                    company.recordTransaction("🎲 [抽卡] 💎【傳說】公司正式通過審查成功 IPO 上市！募集鉅額核心資金：+$3000.00萬！");
                    return "【傳說卡 ✨ 公司成功 IPO】\n公司正式敲鐘上市！公開發行股票引來市場法人瘋狂搶購！\n👉 效果：實質注入大筆募資現金 $3000 萬！股價瘋狂拉升 +50%！";
                default:
                    company.earnCash(20000000);
                    company.recordTransaction("🎲 [抽卡] 💎【傳說】拿下世界級企業超級訂單：+$2000.00萬！營收寫下歷史新頁！");
                    return "【傳說卡 ✨ 世界級企業訂單】\n與全球五百強榜首巨擘簽下超大型長期排他合約，訂單接不完！\n👉 效果：商務定金與首期款項現拿到手 $2000 萬現金！";
            }

        } else if (roll < 50) {
            // ==========================================
            // ✨ 【機運利多 - 商業小賺】 (38% 機率)
            // ==========================================
            int eventId = random.nextInt(8);
            switch (eventId) {
                case 0:
                    company.earnCash(4000000);
                    company.recordTransaction("🎲 [抽卡] ✨【利多】矽谷天使投資人看好前景，超額挹注專案資金：+$400.00萬");
                    return "【機運利多 📈 天使投資人】\n知名早期風投看中你們的商業策略，簽署股權協議！\n👉 效果：公司金庫直接增加 $400 萬現金！";
                case 1:
                    company.earnCash(3000000);
                    company.recordTransaction("🎲 [抽卡] ✨【利多】取得銀行低利過渡性轉結周轉貸款：+$300.00萬");
                    return "【機運利多 📈 銀行低利貸款】\n信用評等極佳，順利向合庫與台銀借到極低利周轉資金！\n👉 效果：取得流動現金 $300 萬！";
                case 2:
                    // 工程師神優化：未來 2 回合支出降低 30% (透過狀態記錄)
                    company.setDamageReduction(2, 0.30);
                    company.recordTransaction("🎲 [抽卡] ✨【利多】工程師完成底層架構神優化，大幅節省雲端伺服器固定成本支出！");
                    return "【機運利多 📈 工程師神優化】\n首席技術團隊重新架構底層程式碼！\n👉 效果：【狀態Buff】伺服器開銷與固定成本支出在未來 2 回合內大降 30%！";
                case 3:
                    company.applyStockPercentChange(0.12);
                    company.recordTransaction("🎲 [抽卡] ✨【利多】百萬級科技 YouTuber 發片強力推薦，市場詢問度破表！");
                    return "【機運利多 📈 YouTuber爆推】\n百萬知名科技網紅主動拍攝專題影片，大讚你們的產品是年度救星！\n👉 效果：大眾知名度引爆，股價應聲上漲 +12%！";
                case 4:
                    company.applyStockPercentChange(0.15);
                    company.recordTransaction("🎲 [抽卡] ✨【利多】頭號競爭對手發表會全面翻車，客戶訂單出現外溢效應！");
                    return "【機運利多 📈 競爭對手翻車】\n宿敵公司新系統上線爆發重大災情，客戶紛紛解約轉向你們投靠！\n👉 效果：市場份額擴大，公司股價上漲 +15%！";
                case 5:
                    company.earnCash(1500000);
                    company.recordTransaction("🎲 [抽卡] ✨【利多】撿到破產清算的科技廠高級實驗室淘汰耗材與平價二手高級設備：省下+$150.00萬");
                    return "【機運利多 📈 撿到便宜設備】\n剛好有同業宣布破產進行清算，你們低價買入大批近全新的高階高精密器材！\n👉 效果：大省一筆常規預算，變相拿回 $150 萬現金！";
                case 6:
                    company.earnCash(6000000);
                    company.recordTransaction("🎲 [抽卡] ✨【利多】國際世貿展覽獲得現象級大爆單，現場預收貨款湧入：+$600.00萬");
                    return "【機運利多 📈 展覽大爆單】\n在年度世界商務大展上大放異彩，全球代理商排隊簽約搶著下單！\n👉 效果：金庫當場進帳預收現款 $600 萬！";
                default:
                    company.applyStockPercentChange(0.08);
                    company.recordTransaction("🎲 [抽卡] ✨【利多】主流電視新聞與財經周刊主動免費專訪報導，股價微幅拉升。");
                    return "【機運利多 📈 免費媒體曝光】\n總經理登上財經主播面對面專訪，公司形象大加分！\n👉 效果：市場信心上揚，股價穩健上升 +8%！";
            }

        } else if (roll < 75) {
            // ==========================================
            // ☕ 【社會平穩 - 偏搞笑無事發生】 (25% 機率)
            // ==========================================
            int eventId = random.nextInt(6);
            String[] commonMessages = {
                    "【社會平穩 ☕ 免費咖啡日】\n今天老闆自掏腰包舉辦免費星巴克星冰樂咖啡日，全體員工士氣爆表！\n👉 效果：精神百倍，但對資產無事發生。",
                    "【社會平穩 🍲 老闆請吃火鍋】\n主管帶隊去吃海底撈火鍋，全公司歡樂撈麵，團隊氣氛融洽融洽！\n👉 效果：肚子很飽，資產無事發生。",
                    "【社會平穩 ⏳ 開會三小時】\n經理召集各部門開了冗長的跨部門核心週會，講了三個小時最後什麼都沒決定。\n👉 效果：虛度光陰，無事發生。",
                    "【社會平穩 💻 工程師 Debug 一整天】\n首席架構師抱著鍵盤痛苦死磕了 24 小時，最後發現只是少打一個分號 ( ; )。\n👉 效果：血壓上升，無事發生。",
                    "【社會平穩 🐱 同事帶貓上班】\n產品經理把貓咪帶到公司上班，全辦公室都聚在一起吸貓，根本沒人有心情工作。\n👉 效果：生產力短暫變為貓吸吸，無事發生。",
                    "【社會平穩 🎁 尾牙抽中氣炸鍋】\n助理在公司福利抽獎裡一抽抽中高級氣炸鍋，引來全公司同事的羨慕嫉妒恨！\n👉 效果：全公司歡呼，無事發生。"
            };
            company.recordTransaction("🎲 [抽卡] ☕【普通】日常茶水間趣聞，公司平穩度過一天。");
            return commonMessages[eventId];

        } else {
            // ==========================================
            // 💥 【命運考驗 - 黑天鵝虧錢區】 (25% 機率)
            // ==========================================
            // 💡 檢查是否有來自抽卡的傷害減免 Buff (如制度改革)
            double dmgFactor = 1.0;
            if (company.getDamageReductionTurns() > 0) {
                dmgFactor = 1.0 - company.getDamageReductionRate();
            }

            int eventId = random.nextInt(10);
            switch (eventId) {
                case 0:
                    double baseLoss0 = 5000000;
                    double finalLoss0 = baseLoss0 * dmgFactor;
                    company.spendCash(finalLoss0);
                    company.applyStockPercentChange(-0.15);
                    company.recordTransaction("🎲 [抽卡] 💥【命運】伺服器遭境外骇客APT勒索攻擊：-$" + (finalLoss0/10000) + "萬，股價暴跌！");
                    return "【💥 命運黑天鵝 · 遭駭客攻擊】\n核心產品資料庫遭到境外未知駭客組織植入勒索病毒入侵！\n👉 效果：緊急支付防禦費用 $" + (finalLoss0/10000) + " 萬，公司股價慘跌 -15%！";
                case 1:
                    double baseLoss1 = 3000000;
                    double finalLoss1 = baseLoss1 * dmgFactor;
                    company.spendCash(finalLoss1);
                    company.recordTransaction("🎲 [抽卡] 💥【命運】遭遇勞檢大突襲，加班超時開罰：-$" + (finalLoss1/10000) + "萬");
                    return "【💥 命運黑天鵝 · 勞檢開罰】\n突遭勞動部大陣仗勞檢突擊，被檢舉員工超時加班、少發加班費事實明確！\n👉 效果：遭官方重罰並勒令補繳，損失現款 $" + (finalLoss1/10000) + " 萬！";
                case 2:
                    company.applyStockPercentChange(-0.20);
                    company.recordTransaction("🎲 [抽卡] 💥【命運】核心研發技術主管被對手挖角，市場爆發經營信心動搖。");
                    return "【💥 命運黑天鵝 · 核心員工離職】\n你們的靈魂研發總監被競爭對手開出三倍薪資與股票期權殘忍挖角！\n👉 效果：技術開發進度受阻，公司股價重挫 -20%！";
                case 3:
                    double baseLoss3 = 4000000;
                    double finalLoss3 = baseLoss3 * dmgFactor;
                    company.spendCash(finalLoss3);
                    company.applyStockPercentChange(-0.10);
                    company.recordTransaction("🎲 [抽卡] 💥【命運】雲端機房全面癱瘓全面當機一天：-$" + (finalLoss3/10000) + "萬");
                    return "【💥 命運黑天鵝 · 伺服器全面當機】\n因雲端機房主機發生火災，導致公司線上網站與產品服務全面癱瘓一整天！\n👉 效果：賠償客戶商譽損失 $" + (finalLoss3/10000) + " 萬，股價下跌 -10%！";
                case 4:
                    company.applyStockPercentChange(-0.25);
                    company.recordTransaction("🎲 [抽卡] 💥【命運】不當發言爆發社群炎上風波，品牌形象重創，股價大跌！");
                    return "【💥 命運黑天鵝 · 社群炎上】\n行銷部門在網路上講錯話，引爆排山倒海的社群抵制與炎上風波！\n👉 效果：品牌形象大跌，股價狂瀉 -25%！";
                case 5:
                    double baseLoss5 = 8000000;
                    double finalLoss5 = baseLoss5 * dmgFactor;
                    company.spendCash(finalLoss5);
                    company.recordTransaction("🎲 [抽卡] 💥【命運】盲目涉足非核心領域，盲目擴張落入失敗：-$" + (finalLoss5/10000) + "萬");
                    return "【💥 命運黑天鵝 · 投資錯誤】\n董事會盲目追逐不熟悉的海外新創項目，最後血本無歸宣告失敗！\n👉 效果：直接導致清算虧損 $" + (finalLoss5/10000) + " 萬現金！";
                case 6:
                    double baseLoss6 = 6000000;
                    double finalLoss6 = baseLoss6 * dmgFactor;
                    company.spendCash(finalLoss6);
                    company.recordTransaction("🎲 [抽卡] 💥【命運】主力供應商與合作夥伴惡性倒閉破產跑路，壞帳無法追回：-$" + (finalLoss6/10000) + "萬");
                    return "【💥 命運黑天鵝 · 合作夥伴跑路】\n長期合作的上游代工廠老闆捲款潛逃海外，你們預付的大筆貨款直接變壞帳！\n👉 效果：流動現金當場慘賠 $" + (finalLoss6/10000) + " 萬！";
                case 7:
                    company.applyStockPercentChange(-0.18);
                    company.recordTransaction("🎲 [抽卡] 💥【命運】產品內建AI回答失控講出奇怪內容，遭遇炎上投訴。");
                    return "【💥 命運黑天鵝 · AI回答失控】\n核心產品內置的智慧語音助理，在回答客戶問題時失控爆粗口、給出錯誤指令！\n👉 效果：遭遇海量消費者客訴，股價暴跌 -18%！";
                case 8:
                    double baseLoss8 = 12000000;
                    double finalLoss8 = baseLoss8 * dmgFactor;
                    company.spendCash(finalLoss8);
                    company.recordTransaction("🎲 [抽卡] 💥【命運】遭惡意同業控告侵犯商業專利權：訴訟費支出-$" + (finalLoss8/10000) + "萬");
                    return "【💥 命運黑天鵝 · 專利訴訟】\n最大競爭對手聯合業界蟑螂，突然對公司發起龐大的商業專利侵權法律訴訟！\n👉 效果：被迫提撥天價律師團與訴訟擔保金 $" + (finalLoss8/10000) + " 萬！";
                default:
                    double baseLoss9 = 3500000;
                    double finalLoss9 = baseLoss9 * dmgFactor;
                    company.spendCash(finalLoss9);
                    company.recordTransaction("🎲 [抽卡] 💥【命運】雲端服務伺服器API大規模爆炸故障：-$" + (finalLoss9/10000) + "萬");
                    return "【💥 命運黑天鵝 · 雲端服務爆炸】\n第三方雲端廠商基礎基礎設施大斷線，連帶導致全公司的核心 API 服務全數掛點！\n👉 效果：緊急採購應急通道，折損資金 $" + (finalLoss9/10000) + " 萬！";
            }
        }
    }
}