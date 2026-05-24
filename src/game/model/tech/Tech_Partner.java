package game.model.tech;

import java.util.Random;

/**
 * 科技業上下游廠商資料庫
 * 負責生成各種 B2B 商業合約與情境對話
 */
public class Tech_Partner {

    private static final Random random = new Random();

    // 知名上下游廠商名單
    private static final String[] UPSTREAM = {"台積電 (TSMC)", "ARM (安謀)", "ASML (艾司摩爾)", "輝達 (NVIDIA)"};
    private static final String[] DOWNSTREAM = {"水果公司 (Apple)", "微軟 (Microsoft)", "華碩 (ASUS)", "索尼 (Sony)"};

    /**
     * 隨機生成一份商業合約
     */
    public static TechContract generateRandomContract() {
        boolean isUpstream = random.nextBoolean();

        if (isUpstream) {
            String partner = UPSTREAM[random.nextInt(UPSTREAM.length)];
            return createUpstreamContract(partner);
        } else {
            String partner = DOWNSTREAM[random.nextInt(DOWNSTREAM.length)];
            return createDownstreamContract(partner);
        }
    }

    private static TechContract createUpstreamContract(String partner) {
        switch (partner) {
            case "台積電 (TSMC)":
                printDialog(partner, "「你們這批 AI 晶片要用我們的 3nm 製程，產能很滿，代工費不能少，一季算你 500 萬就好。」");
                // 參數：廠商名稱, 描述, 預期收入(無), 預期成本(高), 期數
                return new TechContract(partner, "3nm 晶圓代工合約", 0, 5000000, 12);
            case "ARM (安謀)":
                printDialog(partner, "「用我們的架構開發 CPU？沒問題，但架構授權費跟抽成我們算得很精，一季收你 200 萬。」");
                return new TechContract(partner, "底層架構 IP 授權", 0, 2000000, 24);
            case "輝達 (NVIDIA)":
                printDialog(partner, "「要買 H100 伺服器建置算力中心？現在要排隊喔，一口價 800 萬不二價，愛買不買。」");
                return new TechContract(partner, "高階算力設備採購", 0, 8000000, 6);
            default:
                return new TechContract(partner, "常規設備採購", 0, 1000000, 12);
        }
    }

    private static TechContract createDownstreamContract(String partner) {
        switch (partner) {
            case "水果公司 (Apple)":
                printDialog(partner, "「我們下一代 iPhone 考慮用你們的零組件。我們會付 800 萬的貨款，但供應鏈要求很嚴格，你們成本自己吸收。」");
                // 參數：廠商名稱, 描述, 預期收入(高), 預期成本(也高，毛利薄), 期數
                return new TechContract(partner, "iPhone 零組件供應", 8000000, 6500000, 12);
            case "微軟 (Microsoft)":
                printDialog(partner, "「我們想外包一部分 Azure 雲端後台的開發給你們，專案費 400 萬，但如果出 Bug 違約金很重喔。」");
                return new TechContract(partner, "雲端系統外包開發", 4000000, 2500000, 8);
            case "華碩 (ASUS)":
                printDialog(partner, "「ROG 新筆電想搭載你們的散熱演算法，我們開價 200 萬，大家台灣人算便宜一點啦。」");
                return new TechContract(partner, "軟體演算法授權", 2000000, 500000, 12);
            default:
                return new TechContract(partner, "一般軟體授權", 1000000, 300000, 12);
        }
    }

    private static void printDialog(String name, String text) {
        System.out.println("\n🤝 [商務信件] " + name + " : " + text);
    }
}