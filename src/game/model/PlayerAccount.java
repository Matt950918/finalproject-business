package game.model;

import java.io.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class PlayerAccount {
    // 存檔檔案名稱
    private static final String SAVE_FILE = "user_data.dat";

    // 模擬資料庫：用來儲存所有玩家的帳號(Key)與密碼(Value)
    private static HashMap<String, PlayerData> userDatabase = new HashMap<>();

    // 模擬資料庫：用來儲存每個玩家的歷史結算紀錄

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
            return false;
        }

        PlayerData newPlayer = new PlayerData(username, password);

        userDatabase.put(username, newPlayer);

        saveData();

        return true;
    }

    // 登入驗證
    public static boolean login(String username, String password) {

        if (userDatabase.containsKey(username)) {

            PlayerData player = userDatabase.get(username);

            if (player.getPassword().equals(password)) {
                return true;
            }
        }

        return false;
    }
    // ==========================================
    // 歷史紀錄邏輯 (供未來遊戲結算時呼叫)
    // ==========================================
    public static void addMatchRecord(String username, String record) {

        if (userDatabase.containsKey(username)) {

            PlayerData player = userDatabase.get(username);

            player.addHistory(record);

            saveData();
        }
    }

    // ==========================================
    // 🌟 核心功能：存檔與讀檔邏輯
    // ==========================================

    // 儲存資料到檔案
    // 儲存資料到檔案 (修改 PlayerAccount.java)
    @SuppressWarnings("unchecked")
    public static void saveData() {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {

            oos.reset(); // 🌟 關鍵：強制清空序列化快取，避免 Java 偷懶拿舊資料來存！

            // 直接存整個玩家資料庫
            oos.writeObject(userDatabase);

            System.out.println("💾 遊戲資料已自動保存至本地！");

        } catch (IOException e) {
            System.err.println("❌ 存檔失敗：" + e.getMessage());
            e.printStackTrace(); // 建議加上這行，可以看到更詳細的背後錯誤
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

            userDatabase = (HashMap<String, PlayerData>) ois.readObject();

            System.out.println("📂 成功載入以前的玩家資料！目前總註冊人數：" + userDatabase.size());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ 讀取存檔失敗（可能檔案損壞），將初始化新資料：" + e.getMessage());
            userDatabase = new HashMap<>();
        }
    }
    public static PlayerData getPlayerData(String username) {
        return userDatabase.get(username);
    }
}