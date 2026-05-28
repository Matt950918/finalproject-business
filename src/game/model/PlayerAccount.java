package game.model;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class PlayerAccount implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String DATA_FILE = "user_data.dat";

    private static Map<String, PlayerData> userRegistry = new HashMap<>();

    static {
        loadData();
    }

    /**
     * 🔓 專門提供給排行榜系統（RankingSystem）的公開數據接口
     */
    public static Map<String, PlayerData> getUserRegistry() {
        return userRegistry;
    }

    public static PlayerData loginAndGetProgress(String username, String password) {
        if (userRegistry.containsKey(username)) {
            PlayerData data = userRegistry.get(username);
            if (data != null && data.getPassword().equals(password)) {
                return data;
            }
        }
        return null;
    }

    /**
     * 📝 新增玩家註冊（已修正：拒絕在註冊時塞入預設 BANK 公司）
     */
    public static boolean register(String username, String password, String companyName) {
        if (userRegistry.containsKey(username) || username.trim().isEmpty() || password.trim().isEmpty()) {
            return false;
        }

        // 🆕 建立純淨的玩家帳號物件，此時內部 company 必須為 null！
        PlayerData newProgress = new PlayerData(username, password);

        userRegistry.put(username, newProgress);
        saveData(); // 儲存乾淨的空帳號
        System.out.println("✅ [系統註冊] 成功創立純淨帳號: " + username + "，等待玩家選擇產業。");
        return true;
    }

    public static void saveProgress(PlayerData currentData) {
        if (currentData != null && currentData.getUsername() != null) {
            userRegistry.put(currentData.getUsername(), currentData);
            saveData();
            System.out.println("💾 [系統儲存] 帳號 " + currentData.getUsername() + " 的進度已成功寫入檔案。");
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object savedData = ois.readObject();
            if (savedData instanceof Map) {
                Map<?, ?> tempMap = (Map<?, ?>) savedData;
                if (!tempMap.isEmpty() && tempMap.values().iterator().next() instanceof String) {
                    System.err.println("⚠️ 偵測到舊版本存檔格式，正在自動重置...");
                    userRegistry = new HashMap<>();
                } else {
                    userRegistry = (Map<String, PlayerData>) savedData;
                }
            }
        } catch (Exception e) {
            userRegistry = new HashMap<>();
        }
    }

    private static void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(userRegistry);
        } catch (Exception e) {
            System.err.println("❌ 寫入存檔失敗！");
            e.printStackTrace();
        }
    }
}