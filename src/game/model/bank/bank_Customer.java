package game.model.bank;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 👥 大富翁放貸系統：神祕訪客資料模型 (bank_Customer.java)
 * 🎯 精準鎖定 src/resources/data/bank_customers.csv 路徑與檔名，全面復活 57 位商業故事角色！
 */
public class bank_Customer implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String story;
    private String officialRating;
    private double hiddenRisk;
    private double baseInterest;
    private double requestAmount;
    private int terms;

    // 5 參數標準建構子 (對齊你們的原廠規格)
    public bank_Customer(String name, String story, String officialRating, double hiddenRisk, double baseInterest) {
        this.name = name;
        this.story = story;
        this.officialRating = officialRating;
        this.hiddenRisk = hiddenRisk;
        this.baseInterest = baseInterest;
    }

    /**
     * 💡 對齊 bank_system 換日呼叫的結局劇本
     */
    public static String getEndGameScript(String name, String status) {
        if ("BOOM".equals(status)) {
            return "【突發事件】" + name + " 的投資項目徹底宣告失敗，資金鏈無預警斷裂！人已潛逃海外，這筆貸款確定化為呆帳。";
        } else {
            return "【結案通知】" + name + " 商業眼光精準、經營大獲成功！本日已遵照合約連本帶利將尾款全數結清，感謝經理相助！";
        }
    }

    /**
     * 🎲 核心：從你們順利載入的 57 人大名單中隨機抽取申請書
     */
    public static bank_LoanRequest createRandomRequest() {
        List<bank_Customer> csvList = loadYourFiftyCustomers();
        Random random = new Random();

        bank_Customer target;
        if (!csvList.isEmpty()) {
            // 👍 成功讀取！隨機從你們的 57 人大軍中抽一個
            target = csvList.get(random.nextInt(csvList.size()));
        } else {
            // 🚨 超級極端防線：萬一真的還是讀不到，用你們名單前幾位當寫死的備用，絕不單獨塞 Andy
            String[] backupNames = {"Selina", "林董", "Emily", "阿土伯", "王博士", "小智"};
            String[] backupStories = {"網拍進貨周轉", "海外加密貨幣翻倍", "有機超市擴店", "航運股補保證金", "癌症早篩研發", "夜市擺娃娃機"};
            int idx = random.nextInt(backupNames.length);
            target = new bank_Customer(backupNames[idx], backupStories[idx], "B", 0.0, 0.06);
        }

        // 動態演算放貸數字
        double randomAmount = 2000000 + random.nextInt(8) * 1000000; // $200萬 ~ $900萬
        int randomTicks = 3 + random.nextInt(4);                     // 3 ~ 6 回合
        double finalInterest = target.getBaseInterest();             // 完美抓取 CSV 裡的專屬利息

        // 依據官方評等對齊信用分數
        int creditScore = 70;
        if (target.getOfficialRating().contains("S")) creditScore = 95;
        else if (target.getOfficialRating().contains("A")) creditScore = 85;
        else if (target.getOfficialRating().contains("C")) creditScore = 50;
        else if (target.getOfficialRating().contains("D")) creditScore = 30;

        // 建立正式申請書 (嚴格對齊 5 參數建構子)
        bank_LoanRequest req = new bank_LoanRequest(
                target.getName(),
                randomAmount,
                finalInterest,
                creditScore,
                randomTicks
        );

        req.setDialogue("「" + target.getStory() + "」");
        return req;
    }

    /**
     * 🔄 被拒絕後的降價談判機制
     */
    public static bank_LoanRequest createRequestByName(String name, int rejectCount) {
        Random random = new Random();
        double lowerAmount = 1000000 + random.nextInt(3) * 1000000;
        double higherInterest = 0.08 + (random.nextDouble() * 0.04);
        int currentCredit = 65;
        int ticks = 4;

        bank_LoanRequest req = new bank_LoanRequest(name, lowerAmount, higherInterest, currentCredit, ticks);
        req.setDialogue("「經理拜託啦...被拒絕了 " + rejectCount + " 次很沒面子欸，不然額度降到 $" + (int)(lowerAmount/10000) + "萬 可以嗎？」");
        return req;
    }

    /**
     * 📁 精準鎖定器：專門咬定你們在 src/resources/data/ 底下的小寫底線 CSV 檔案
     */
    private static List<bank_Customer> loadYourFiftyCustomers() {
        List<bank_Customer> list = new ArrayList<>();

        // 🎯 1. 優先使用你們在專案內的精準相對路徑（符合小寫底線檔名）
        File csvFile = new File("src/resources/data/bank_customers.csv");

        // 🎯 2. 如果因為執行工作目錄不同找不到，自動切換至絕對路徑兜底
        if (!csvFile.exists()) {
            csvFile = new File("/Users/xuan/java/finalproject-business/src/resources/data/bank_customers.csv");
        }

        // 🎯 3. 其他常見備用路徑防線
        if (!csvFile.exists()) csvFile = new File("src/resources/bank_customers.csv");
        if (!csvFile.exists()) csvFile = new File("bank_customers.csv");

        if (!csvFile.exists()) {
            System.err.println("❌ [客戶系統] 嚴重大 Bug：在該路徑下依然找不到 bank_customers.csv 檔案！");
            return list;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            br.readLine(); // 跳過第 1 行標頭

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                // 過濾掉名單中間夾雜的重複標頭欄位
                if (line.startsWith("ID") || line.contains("OfficialRating") || line.contains("Name")) continue;

                try {
                    String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                    if (tokens.length >= 6) {
                        String csvName = tokens[1].trim();
                        String csvDesc = tokens[2].replace("\"", "").trim();
                        String rating = tokens[3].trim();
                        double hiddenRisk = Double.parseDouble(tokens[4].trim());
                        double baseInterest = Double.parseDouble(tokens[5].trim());

                        list.add(new bank_Customer(csvName, csvDesc, rating, hiddenRisk, baseInterest));
                    }
                } catch (Exception lineEx) {
                    System.err.println("⚠️ [客戶系統] 略過瑕疵資料列: " + line + " -> 原因: " + lineEx.getMessage());
                }
            }
            System.out.println("👥 [客戶系統] 成功！已從實體檔案精準載入 " + list.size() + " 位大富翁故事客戶！");
        } catch (Exception e) {
            System.err.println("❌ [客戶系統] 讀取 CSV 發生嚴重中斷異常: " + e.getMessage());
        }
        return list;
    }

    // ==========================================
    // Getter 區
    // ==========================================
    public String getName() { return name; }
    public String getStory() { return story; }
    public String getOfficialRating() { return officialRating; }
    public double getHiddenRisk() { return hiddenRisk; }
    public double getBaseInterest() { return baseInterest; }
    public double getRequestAmount() { return requestAmount; }
    public int getTerms() { return terms; }
}