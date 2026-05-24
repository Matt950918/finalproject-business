package game.model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NewsDatabase {
    private static List<DailyNews> allNews = new ArrayList<>();
    private static List<DailyNews> availableNews = new ArrayList<>();
    private static Random random = new Random();

    // 啟動時自動讀取 CSV
    static {
        loadNewsFromCSV();
    }

    // ==========================================
    // 📂 讀取 CSV 並自動轉換為遊戲物件
    // ==========================================
    private static void loadNewsFromCSV() {
        try {
            // 嘗試從資源資料夾讀取 (請確保路徑正確，這裡預設為 /resources/data/news_events.csv)
            InputStream is = NewsDatabase.class.getResourceAsStream("/resources/data/news_events.csv");

            BufferedReader br;
            if (is != null) {
                br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            } else {
                // 如果資源檔找不到，嘗試從專案根目錄直接讀取 (防呆機制)
                br = Files.newBufferedReader(Paths.get("news_events.csv"), StandardCharsets.UTF_8);
            }

            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                // 略過第一行的標題列與空白行
                if (isFirstLine || line.trim().isEmpty()) {
                    isFirstLine = false;
                    continue;
                }
                parseAndAddNews(line);
            }
            br.close();
            System.out.println("✅ 成功從 CSV 載入 " + allNews.size() + " 筆事件資料！");

        } catch (Exception e) {
            System.err.println("❌ CSV 讀取失敗，請確認 news_events.csv 是否存在！錯誤訊息：" + e.getMessage());
        }
    }

    // ==========================================
    // 🧠 核心解析器：看懂你的 Excel 濃縮語法
    // ==========================================
    private static void parseAndAddNews(String line) {
        // 使用正則切割逗號，但忽略引號內的逗號 (標準 CSV 處理)
        String[] cols = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        if (cols.length < 3) return;

        String industryStr = cols[0].replace("\"", "").trim();
        String title = cols[1].replace("\"", "").trim();

        IndustryType type = null;
        if (industryStr.contains("科技")) type = IndustryType.TECH;
        else if (industryStr.contains("銀行")) type = IndustryType.BANK;
        else if (industryStr.contains("生技")) type = IndustryType.BIOTECH;

        DailyNews news = new DailyNews(title, type);

        // 讀取 選項A, 選項B, 選項C...
        for (int i = 2; i < cols.length; i++) {
            String optStr = cols[i].replace("\"", "").trim();
            if (optStr.isEmpty() || optStr.equals("(無)")) continue;

            NewsOption option = parseOption(optStr);
            if (option != null) {
                news.addOption(option);
            }
        }

        // 確保至少有一個選項才加入資料庫
        if (!news.getOptions().isEmpty()) {
            allNews.add(news);
        }
    }

    // 解析單一選項的字串 (例如：[全面檢修機房](花費:$120萬)•85%:因為「成功恢復穩定」股價上升(x1.05)...)
    private static NewsOption parseOption(String optStr) {
        String title = "未知選項";
        double cost = 0;

        // 1. 抓取標題與花費
        Pattern headerPattern = Pattern.compile("\\[(.*?)\\]\\(花費:\\$?([0-9\\.]+)萬?\\)");
        Matcher hm = headerPattern.matcher(optStr);
        if (hm.find()) {
            title = hm.group(1);
            String costStr = hm.group(2);
            if (!costStr.equals("0")) {
                cost = Double.parseDouble(costStr) * 10000; // 自動把萬變成數字
            }
        } else {
            return null; // 格式不符直接跳過
        }

        List<Double> probs = new ArrayList<>();
        List<MarketEvent> events = new ArrayList<>();

        // 2. 抓取所有機率與結果 (支援無限多個結果)
        Pattern outcomePattern = Pattern.compile("(\\d+)[%％][:：]因為「(.*?)」.*?\\(x([0-9\\.]+)\\)");
        Matcher om = outcomePattern.matcher(optStr);

        while (om.find()) {
            double p = Double.parseDouble(om.group(1)) / 100.0;
            double mult = Double.parseDouble(om.group(3));
            String trend = (mult >= 1) ? "股價上升" : "股價下跌";
            String msg = "因為「" + om.group(2) + "」，" + trend;

            probs.add(p);
            events.add(new MarketEvent(msg, mult));
        }

        if (probs.isEmpty()) return null;

        // 將 List 轉回 Array 給原本的建構子
        double[] pArray = probs.stream().mapToDouble(Double::doubleValue).toArray();
        MarketEvent[] eArray = events.toArray(new MarketEvent[0]);

        return new NewsOption(title, cost, pArray, eArray);
    }

    // ==========================================
    // 🎲 原本的抽籤與重置邏輯
    // ==========================================
    public static void resetDatabase() {
        availableNews.clear();
        availableNews.addAll(allNews);
    }

    public static DailyNews getRandomNewsFor(IndustryType industry) {
        if (random.nextDouble() < 0.15) {
            return null;
        }

        List<DailyNews> validNews = availableNews.stream()
                .filter(n -> n.getTargetIndustry() == null || n.getTargetIndustry() == industry)
                .collect(Collectors.toList());

        if (validNews.isEmpty()) {
            resetDatabase();
            validNews = availableNews.stream()
                    .filter(n -> n.getTargetIndustry() == null || n.getTargetIndustry() == industry)
                    .collect(Collectors.toList());
        }

        DailyNews selectedNews = validNews.get(random.nextInt(validNews.size()));
        availableNews.remove(selectedNews);
        return selectedNews;
    }
}