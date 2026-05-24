package game.model.bank;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 銀行客戶劇本資料庫 (支援隨機金額範圍版)
 */
public class bank_Customer {

    private static final Random random = new Random();

    private static final Map<String, CustomerRecord> customerDatabase = new HashMap<>();
    private static final List<String> customerNames = new ArrayList<>();

    static {
        loadCustomersFromCSV();
    }

    // ==========================================
    // 📂 讀取 CSV 引擎 (支援金額範圍)
    // ==========================================
    private static void loadCustomersFromCSV() {
        try {
            InputStream is = bank_Customer.class.getResourceAsStream("/resources/data/bank_customers.csv");
            BufferedReader br;
            if (is != null) {
                br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            } else {
                br = Files.newBufferedReader(Paths.get("bank_customers.csv"), StandardCharsets.UTF_8);
            }

            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine || line.trim().isEmpty()) {
                    isFirstLine = false;
                    continue;
                }

                String[] cols = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (cols.length < 13) continue;

                for (int i = 0; i < cols.length; i++) {
                    cols[i] = cols[i].replace("\"", "").trim();
                }

                CustomerRecord record = new CustomerRecord();
                record.name = cols[0];

                // 初次設定
                record.firstDialog = cols[1];
                double[] firstRange = parseAmountRange(cols[2]); // 解析金額範圍
                record.firstMinAmount = firstRange[0];
                record.firstMaxAmount = firstRange[1];
                record.firstRate = Double.parseDouble(cols[3]);
                record.firstCredit = Integer.parseInt(cols[4]);
                record.firstTicks = Integer.parseInt(cols[5]);

                // 遭拒後設定
                record.secondDialog = cols[6];
                record.willReturn = !cols[6].isEmpty() && !cols[6].equals("無");
                if (record.willReturn) {
                    double[] secondRange = parseAmountRange(cols[7]); // 解析遭拒後金額範圍
                    record.secondMinAmount = secondRange[0];
                    record.secondMaxAmount = secondRange[1];
                    record.secondRate = Double.parseDouble(cols[8]);
                    record.secondCredit = Integer.parseInt(cols[9]);
                    record.secondTicks = Integer.parseInt(cols[10]);
                }

                // 結局對話
                record.successDialog = cols[11];
                record.boomDialog = cols[12];

                customerDatabase.put(record.name, record);
                customerNames.add(record.name);
            }
            br.close();
            System.out.println("✅ 成功從 CSV 載入 " + customerNames.size() + " 位銀行客戶資料！");

        } catch (Exception e) {
            System.err.println("❌ 銀行客戶 CSV 讀取失敗：" + e.getMessage());
        }
    }

    // 💰 解析金額範圍字串 (如 "300~500" 或 "500")，並轉換成實際遊戲數值
    private static double[] parseAmountRange(String str) {
        str = str.trim();
        double min, max;
        try {
            if (str.contains("~")) {
                String[] parts = str.split("~");
                min = Double.parseDouble(parts[0].trim());
                max = Double.parseDouble(parts[1].trim());
            } else if (str.contains("-")) {
                String[] parts = str.split("-");
                min = Double.parseDouble(parts[0].trim());
                max = Double.parseDouble(parts[1].trim());
            } else {
                min = max = Double.parseDouble(str); // 若只有單一數字
            }
        } catch (NumberFormatException e) {
            min = max = 0; // 若解析失敗預設為 0
        }
        // 回傳 {最小值 * 10000, 最大值 * 10000}
        return new double[]{min * 10000, max * 10000};
    }

    // ==========================================
    // 🧠 遊戲引擎接口 (建立隨機金額訂單)
    // ==========================================

    public static bank_LoanRequest createRandomRequest() {
        if (customerNames.isEmpty()) return null;
        String name = customerNames.get(random.nextInt(customerNames.size()));
        return createRequestByName(name, 0);
    }

    public static bank_LoanRequest createRequestByName(String name, int rejectCount) {
        CustomerRecord rec = customerDatabase.get(name);
        if (rec == null) return null;

        if (rejectCount == 0) {
            printDialog(name, "初登場", rec.firstDialog);
            double finalAmount = getRandomAmount(rec.firstMinAmount, rec.firstMaxAmount);
            bank_LoanRequest req = new bank_LoanRequest(name, finalAmount, rec.firstRate, rec.firstCredit, rec.firstTicks);
            req.setDialogue(rec.firstDialog); // 👈 關鍵：把初次對話塞進去
            return req;
        } else if (rejectCount == 1 && rec.willReturn) {
            printDialog(name, "捲土重來", rec.secondDialog);
            double finalAmount = getRandomAmount(rec.secondMinAmount, rec.secondMaxAmount);
            bank_LoanRequest req = new bank_LoanRequest(name, finalAmount, rec.secondRate, rec.secondCredit, rec.secondTicks);
            req.setDialogue(rec.secondDialog); // 👈 關鍵：把遭拒後對話塞進去
            req.setRejectCount(1);
            return req;
        }
        return null;
    }

    // 🎲 根據給定的最小值與最大值，產生一個以萬為單位的隨機金額
    private static double getRandomAmount(double min, double max) {
        if (min == max) return min;
        double randomized = min + random.nextDouble() * (max - min);
        // 四捨五入到最近的「萬」 (避免出現 $3145621.5 這種零頭)
        return Math.round(randomized / 10000.0) * 10000.0;
    }

    public static String getEndGameScript(String name, String status) {
        CustomerRecord rec = customerDatabase.get(name);
        if (rec == null) return "「...... (此人不讀不回)」";

        if ("SUCCESS".equalsIgnoreCase(status)) {
            return rec.successDialog;
        } else if ("BOOM".equalsIgnoreCase(status)) {
            return rec.boomDialog;
        }
        return "";
    }

    private static void printDialog(String name, String stage, String text) {
        System.out.println("\n📢 [" + stage + "] " + name + " : " + text);
    }

    // ==========================================
    // 🗃️ 內部資料結構
    // ==========================================
    private static class CustomerRecord {
        String name;
        String firstDialog;
        double firstMinAmount;
        double firstMaxAmount;
        double firstRate;
        int firstCredit;
        int firstTicks;

        boolean willReturn;
        String secondDialog;
        double secondMinAmount;
        double secondMaxAmount;
        double secondRate;
        int secondCredit;
        int secondTicks;

        String successDialog;
        String boomDialog;
    }
}