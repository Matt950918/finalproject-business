package game.model;

import java.io.Serializable;

public class PlayerAccountSlots implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;

    // 陣列長度為 3，代表 Slot 0, Slot 1, Slot 2。裡面放的就是你的 PlayerData
    private PlayerData[] slots = new PlayerData[3];

    public PlayerAccountSlots(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }

    /**
     * 取得所有槽位列表（供 UI 檢查哪些槽位有存檔、哪些是空的）
     */
    public PlayerData[] getSlots() {
        return slots;
    }

    /**
     * 取得特定槽位的遊戲進度
     */
    public PlayerData getSlot(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < 3) {
            return slots[slotIndex];
        }
        return null;
    }

    /**
     * 儲存或更新特定槽位的遊戲進度
     */
    public void setSlot(int slotIndex, PlayerData data) {
        if (slotIndex >= 0 && slotIndex < 3) {
            this.slots[slotIndex] = data;
        }
    }
}