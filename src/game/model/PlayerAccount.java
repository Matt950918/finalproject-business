package game.model;

import java.io.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class PlayerAccount {
    // 存檔檔案名稱
    private static final String SAVE_FILE = "user_data.dat";

    // 模擬資料庫：用來儲存所有玩家的帳號(Key)與密碼(Value)
    private static HashMap<String, String> userDatabase = new HashMap<>();

    // 模擬資料庫：用來儲存每個玩家的歷史結算紀錄
    private static HashMap<String, List<String>> userHistory = new HashMap<>();

    // 🌟 靜態初始化區塊：當程式一啟動，自動讀取以前的存檔
    static {
        loadData();
    }

    // ==========================================
    // 帳號註冊與登入邏輯
    // ==========================================

    // 註冊新帳號
    public static boolean register(String username, String password) {
        if (userDatabase.containsKey(username)) {
            return false; // 帳號已被註冊過了
        }
        userDatabase.put(username, password);
        userHistory.put(username, new ArrayList<>()); // 給他一個空的歷史紀錄本
        System.out.println("✅ 註冊成功！歡迎新老闆：" + username);

        // 🌟 註冊成功，立刻存檔
        saveData();
        return true;
    }

    // 登入驗證
    public static boolean login(String username, String password) {
        if (userDatabase.containsKey(username) && userDatabase.get(username).equals(password)) {
            System.out.println("🔓 登入成功！歡迎回來：" + username);
            return true;
        }
        return false; // 帳號或密碼錯誤
    }

    // ==========================================
    // 歷史紀錄邏輯 (供未來遊戲結算時呼叫)
    // ==========================================
    public static void addMatchRecord(String username, String record) {
        if (userHistory.containsKey(username)) {
            userHistory.get(username).add(record);
            // 🌟 歷史紀錄有更新，立刻存檔
            saveData();
        }
    }

    // ==========================================
    // 🌟 核心功能：存檔與讀檔邏輯
    // ==========================================

    // 儲存資料到檔案
    @SuppressWarnings("unchecked")
    public static void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            // 將兩個 HashMap 包進一個大 List 裡面一起存，比較乾淨
            List<Object> allData = new ArrayList<>();
            allData.add(userDatabase);
            allData.add(userHistory);

            oos.writeObject(allData);
            System.out.println("💾 遊戲資料已自動保存至本地！");
        } catch (IOException e) {
            System.err.println("❌ 存檔失敗：" + e.getMessage());
        }
    }

    // 從檔案讀取資料
    @SuppressWarnings("unchecked")
    public static void loadData() {
        File file = new File(SAVE_FILE);
        // 如果檔案不存在，代表是第一次開遊戲，直接跳過讀取
        if (!file.exists()) {
            System.out.println("ℹ️ 未偵測到舊存檔，將建立全新資料庫。");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<Object> allData = (List<Object>) ois.readObject();

            userDatabase = (HashMap<String, String>) allData.get(0);
            userHistory = (HashMap<String, List<String>>) allData.get(1);

            System.out.println("📂 成功載入以前的玩家資料！目前總註冊人數：" + userDatabase.size());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ 讀取存檔失敗（可能檔案損壞），將初始化新資料：" + e.getMessage());
            userDatabase = new HashMap<>();
            userHistory = new HashMap<>();
        }
    }
}