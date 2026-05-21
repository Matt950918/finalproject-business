package game.model;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class PlayerAccount {
    // 模擬資料庫：用來儲存所有玩家的帳號(Key)與密碼(Value)
    private static HashMap<String, String> userDatabase = new HashMap<>();

    // 模擬資料庫：用來儲存每個玩家的歷史結算紀錄
    private static HashMap<String, List<String>> userHistory = new HashMap<>();

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
        }
    }
}