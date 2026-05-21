package game.model;

import java.util.Random;

/**
 * 銀行客戶劇本資料庫（檔名：bank_Customer）
 * 集中管理 9 位客人的初登場、捲土重來、成功還清、突發爆雷的所有數值與搞笑對話
 */
public class bank_Customer {

    private static final Random random = new Random();

    // 9位客戶名單
    private static final String[] CUSTOMER_NAMES = {
            "航海王", "玉珠姐", "角頭 龍哥", "草莓姐姐", "Selina", "純情阿達", "青溪姐", "竹科工程師 提姆", "公務員 陳小姐"
    };

    /**
     * 【遊戲引擎專用】隨機挑選一位客戶作為「新登場」的貸款申請案
     */
    public static bank_LoanRequest createRandomRequest() {
        String name = CUSTOMER_NAMES[random.nextInt(CUSTOMER_NAMES.length)];
        return createRequestByName(name, 0); // 0 代表初登場
    }

    /**
     * 核心劇本工廠：根據名字與狀態，生成對應的 bank_LoanRequest 數據與列印台詞
     */
    public static bank_LoanRequest createRequestByName(String name, int rejectCount) {
        switch (name) {
            // =================================================================
            // 📈 第一類：投機與投資客群
            // =================================================================
            case "航海王":
                if (rejectCount == 0) {
                    printDialog(name, "初登場", "「經理，現在台股都四萬點了啦！我跟你說，年底一定到五萬點！快借我 500 萬我去 ALL IN 台積電，財富自由就看這波了！」");
                    return new bank_LoanRequest(name, 5000000, 0.05, 45, 24);
                } else {
                    printDialog(name, "捲土重來", "「上次竟然拒絕我？還好我沒買，台積電拉回修正了啦！我改借 200 萬去買高股息 ETF 質押總行了吧？利率我多給妳 3% 啦！」");
                    bank_LoanRequest req = new bank_LoanRequest(name, 2000000, 0.08, 55, 12);
                    req.setRejectCount(1);
                    return req;
                }

            case "玉珠姐":
                if (rejectCount == 0) {
                    printDialog(name, "初登場", "「哎呀經理，阿姨我在中央大學旁邊那整棟學生套房要翻修啦，想借 3000 萬，學生租金收一收馬上就還你了，穩當當！」");
                    return new bank_LoanRequest(name, 30000000, 0.06, 40, 36);
                } else {
                    printDialog(name, "捲土重來", "「政府現在打房又限貸，套房不好做啦... 不然阿姨改借 1000 萬，把一樓改裝成店面租給超商，利息算你高一點嘛！」");
                    bank_LoanRequest req = new bank_LoanRequest(name, 10000000, 0.08, 50, 24);
                    req.setRejectCount(1);
                    return req;
                }

            case "角頭 龍哥":
                if (rejectCount == 0) {
                    printDialog(name, "初登場", "「經理，我堂口...呃不是，我公司最近需要 1500 萬週轉一下。你懂的，別問太多，順順利利最重要。」");
                    return new bank_LoanRequest(name, 15000000, 0.07, 20, 36);
                } else {
                    printDialog(name, "捲土重來", "「（帶兩個小弟走進來）聽說你上次不給我面子？算了，龍哥不刁難你，借 500 萬發薪水，這次別不識相了。」");
                    bank_LoanRequest req = new bank_LoanRequest(name, 5000000, 0.12, 25, 12);
                    req.setRejectCount(1);
                    return req;
                }

                // =================================================================
                // 👨‍🎤 第二類：網紅與網拍客群
                // =================================================================
            case "草莓姐姐":
                if (rejectCount == 0) {
                    printDialog(name, "初登場", "「嗨經理~ 我想借 800 萬買信義區豪宅當拍片背景，過氣了就還你，不可以拒絕美少女喔！」");
                    return new bank_LoanRequest(name, 8000000, 0.04, 30, 24);
                } else {
                    printDialog(name, "捲土重來", "「哼！上次竟然拒絕我！好啦那我委屈點，借 300 萬買特斯拉拍開箱總行了吧？利率隨你開啦！」");
                    bank_LoanRequest req = new bank_LoanRequest(name, 3000000, 0.09, 35, 12);
                    req.setRejectCount(1);
                    return req;
                }

            case "Selina":
                if (rejectCount == 0) {
                    printDialog(name, "初登場", "「經理好，我經營 Threads 團購與服飾網拍，因為換季要向韓國東大門大量進貨，需要 200 萬週轉。」");
                    return new bank_LoanRequest(name, 2000000, 0.04, 65, 12);
                } else {
                    printDialog(name, "捲土重來", "「換季進貨期快過了，我真的很急！只要借 100 萬就好，還款期數縮短，利息我願意多給一點！」");
                    bank_LoanRequest req = new bank_LoanRequest(name, 1000000, 0.06, 70, 6);
                    req.setRejectCount(1);
                    return req;
                }

                // =================================================================
                // 🧑‍🍳 第三類：夢想家與創業客群
                // =================================================================
            case "純情阿達":
                if (rejectCount == 0) {
                    printDialog(name, "初登場", "「那個...我想借 100 萬研發『自動幫你挑韭菜股票』的 App，這是我一生的夢想！」");
                    return new bank_LoanRequest(name, 1000000, 0.03, 60, 48);
                } else {
                    printDialog(name, "捲土重來", "「上次拒絕我後我想通了，那個App太難了。但我最近開發了『暈船計量器』App，想借 50 萬翻身！」");
                    bank_LoanRequest req = new bank_LoanRequest(name, 500000, 0.04, 50, 24);
                    req.setRejectCount(1);
                    return req;
                }

            case "青溪姐":
                if (rejectCount == 0) {
                    printDialog(name, "初登場", "「經理好，我想在大學旁邊開一家高端文青網美咖啡廳，需要 300 萬的頂讓與裝潢費。」");
                    return new bank_LoanRequest(name, 3000000, 0.03, 75, 36);
                } else {
                    printDialog(name, "捲土重來", "「被拒絕後我想通了，店面成本太高。我改去夜市擺攤賣炸雞排，只要借 80 萬，回本很快的！」");
                    bank_LoanRequest req = new bank_LoanRequest(name, 800000, 0.05, 80, 12);
                    req.setRejectCount(1);
                    return req;
                }

                // =================================================================
                // 💼 第四類：成家立業剛需客群（優質好人，拒絕後不吃回頭草）
                // =================================================================
            case "竹科工程師 提姆":
                printDialog(name, "初登場", "「經理您好，我在護國神山擔任高級工程師。因為今年要結婚，看中了竹北一間新成屋，想申請 1200 萬的青年首購房貸。」");
                return new bank_LoanRequest(name, 12000000, 0.02, 95, 60);

            case "公務員 陳小姐":
                printDialog(name, "初登場", "「經理您好，我是通過高考的正式公務員。為了成家立業，想要申請 600 萬的溫馨溫暖小宅房貸。」");
                return new bank_LoanRequest(name, 6000000, 0.02, 98, 48);

            default:
                return new bank_LoanRequest("無名氏", 1000000, 0.03, 60, 12);
        }
    }

    /**
     * 【UI與通知專用接口】當客人在遊戲中「順利還清」或「不幸爆雷跑路」時，呼叫此方法取得對話台詞
     * @param name 客人姓名
     * @param status 狀態傳入 "SUCCESS" (還清) 或 "BOOM" (爆雷)
     * @return 搞笑的對話字串
     */
    public static String getEndGameScript(String name, String status) {
        if ("SUCCESS".equalsIgnoreCase(status)) {
            switch (name) {
                case "航海王": return "「哈哈經理！感謝妳當初借我錢，我不只還清貸款，還賺到在青埔買了一間房，謝啦！」";
                case "玉珠姐": return "「哎唷經理，套房全部滿租啦！這是本金跟利息，一分不少，下次有空阿姨請你吃飯喔！」";
                case "角頭 龍哥": return "「兄弟們這次大撈一筆，錢還你啦！還多請你抽幾支菸，下次記得再互相照顧啊！」";
                case "草莓姐姐": return "「經理～特斯拉拍開箱效果超好，本利都還清囉！下次記得再借我，啾咪！」";
                case "Selina": return "「東大門這一批衣服全部秒殺爆單！錢已經全數自動扣款還清囉，謝謝經理的週轉金！」";
                case "純情阿達": return "「經理！我的暈船App在軟體商店下載量第一名！我把貸款全還清了，我要去跟Threads女神告白了！」";
                case "青溪姐": return "「經理，我們雞排攤現在天天排隊，這筆錢已經連本帶利還清了，真的太謝謝你了！」";
                case "竹科工程師 提姆": return "「房貸都扣款完畢結清了，謝謝銀行的協助，未來有其他理財需要再找你們。」";
                case "公務員 陳小姐": return "「感謝銀行的合約，房貸已經全數還清囉。祝你們業績蒸蒸日上！」";
            }
        } else if ("BOOM".equalsIgnoreCase(status)) {
            switch (name) {
                case "航海王": return "⚠️【突發事件】全球晶片大廠無預警失火，台股大崩盤！航海王傳簡訊：「經理拍謝...台積電跌停鎖死我被斷頭了，我先去跑外送，暫時沒錢還...」";
                case "玉珠姐": return "⚠️【突發事件】少子化衝擊加上周邊狂蓋社會住宅！玉珠姐傳LINE：「經理拍謝，套房全空租不出去，阿姨資金軋不過來，先去避避風頭了...」";
                case "角頭 龍哥": return "⚠️【突發事件】龍哥的地下堂口遭警方拂曉出擊全面破獲！資產已被扣押凍結，銀行的貸款變成了無頭呆帳。";
                case "草莓姐姐": return "⚠️【突發事件】草莓姐姐爆出大型人設翻車醜聞，粉絲集體退訂！她因為破產已經買機票跑去國外躲債了。";
                case "Selina": return "⚠️【突發事件】東大門海運貨船發生大火，整批衣服燒光！Selina哭訴：「貨全沒了，消費者都在退款，我真的沒錢了...」";
                case "純情阿達": return "⚠️【突發事件】新型AI工具將獨立App完全取代！阿達憔悴表示：「App被迫下架，資金全燒光，這期真的繳不出來...」";
                case "青溪姐": return "⚠️【突發事件】夜市爆發嚴重食物中毒風波遭勒令停業！青溪姐哭訴：「夜市完全沒人來，我連攤位租金都付不出來了...」";
                case "竹科工程師 提姆": return "⚠️【突發事件】提姆因長期加班住院辭職，又逢科技業寒冬！提姆憔悴地說：「我非自願離職了...目前沒有收入，必須延遲還款...」";
                case "公務員 陳小姐": return "⚠️【突發事件】陳小姐當保人結果被親戚詐騙！陳小姐哭訴：「我一輩子奉公守法沒想到被親戚害慘，我的帳戶跟薪水被法院凍結了...」";
            }
        }
        return "「...... (此人不讀不回)」";
    }

    // 輔助列印對話的工具方法
    private static void printDialog(String name, String stage, String text) {
        System.out.println("\n📢 [" + stage + "] " + name + " : " + text);
    }
}