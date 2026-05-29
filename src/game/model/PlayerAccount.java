package game.model;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class PlayerAccount implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String DATA_FILE = "user_data.dat";

    // 資料庫結構：帳號名稱 -> 多槽位帳戶物件
    private static Map<String, PlayerAccountSlots> userRegistry = new HashMap<>();

    static {
        loadData();
    }

    public static Map<String, PlayerAccountSlots> getUserRegistry() {
        return userRegistry;
    }

    /**
     * 🔑 玩家登入
     * 驗證成功後，回傳整組帳號物件（裡面含有 3 個存檔槽位）
     */
    public static PlayerAccountSlots loginAndGetAccount(String username, String password) {
        if (userRegistry.containsKey(username)) {
            PlayerAccountSlots account = userRegistry.get(username);
            if (account != null && account.getPassword().equals(password)) {
                return account;
            }
        }
        return null;
    }

    /**
     * 📝 玩家註冊
     * 建立一個擁有 3 個空槽位的乾淨帳號
     */
    public static boolean register(String username, String password) {
        if (userRegistry.containsKey(username) || username.trim().isEmpty() || password.trim().isEmpty()) {
            return false;
        }

        PlayerAccountSlots newAccount = new PlayerAccountSlots(username, password);
        userRegistry.put(username, newAccount);
        saveData();
        System.out.println("✅ [系統註冊] 成功創立帳號: " + username + "，包含 3 個空存檔槽位。");
        return true;
    }

    /**
     * 💾 儲存指定帳號、指定槽位的 PlayerData 遊戲進度
     */
    public static void saveSlotProgress(String username, int slotIndex, PlayerData currentData) {
        if (userRegistry.containsKey(username) && slotIndex >= 0 && slotIndex < 3) {
            PlayerAccountSlots account = userRegistry.get(username);
            if (account != null) {
                account.setSlot(slotIndex, currentData);
                saveData();
                System.out.println("💾 [系統儲存] 帳號 " + username + " 的 Slot [" + slotIndex + "] 進度已成功寫入。");
            }
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
                // 防呆：如果原本有舊版的單一存檔，自動重置避免格式衝突崩潰
                if (!tempMap.isEmpty() && !(tempMap.values().iterator().next() instanceof PlayerAccountSlots)) {
                    System.err.println("⚠️ 偵測到舊版本單一存檔格式，自動重置為多槽位格式...");
                    userRegistry = new HashMap<>();
                } else {
                    userRegistry = (Map<String, PlayerAccountSlots>) savedData;
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