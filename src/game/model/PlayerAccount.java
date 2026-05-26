package game.model;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class PlayerAccount implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String DATA_FILE = "user_data.dat";

    // 💡 核心修改：將儲存結構改為儲存一個包含密碼與公司名的封裝物件或直接用 Map 巢狀儲存
    // 這裡示範最快、最不破壞妳們原本架構的做法：用另一個 Map 存 帳號 -> 公司名
    private static Map<String, String> userPasswords = new HashMap<>();
    private static Map<String, String> userCompanies = new HashMap<>(); // 💡 新增：綁定帳號與公司名

    static {
        loadData();
    }

    // 💡 修改登入方法：登入成功時，回傳該帳號綁定的公司名稱（若無則回傳預設值）
    public static String loginAndGetCompany(String username, String password) {
        if (userPasswords.containsKey(username) && userPasswords.get(username).equals(password)) {
            // 抓取綁定的公司名稱，如果舊帳號沒有，就給預設值 "遠東集團"
            return userCompanies.getOrDefault(username, "遠東集團");
        }
        return null; // 登入失敗
    }

    // 💡 修改註冊方法：讓玩家在註冊時就把公司名稱綁定進去
    public static boolean register(String username, String password, String companyName) {
        if (userPasswords.containsKey(username) || username.trim().isEmpty() || password.trim().isEmpty()) {
            return false;
        }
        userPasswords.put(username, password);

        // 如果註冊時沒填公司名，就給預設值
        String finalCompName = (companyName == null || companyName.trim().isEmpty()) ? "遠東集團" : companyName.trim();
        userCompanies.put(username, finalCompName);

        saveData();
        return true;
    }

    // 💾 以下為資料讀寫讀取（確保 userCompanies 也有被一起序列化存檔）
    @SuppressWarnings("unchecked")
    private static void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            userPasswords = (Map<String, String>) ois.readObject();
            // 💡 讀取公司名稱綁定資料
            try {
                userCompanies = (Map<String, String>) ois.readObject();
            } catch (Exception e) {
                userCompanies = new HashMap<>(); // 隊友舊的存檔可能沒有，補空防錯
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(userPasswords);
            oos.writeObject(userCompanies); // 💡 寫入公司名稱綁定資料
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}