package game.model;

import java.io.Serializable; // 如果你要儲存進度，建議實作 Serializable

public class LevelSystem implements Serializable {
    private int level = 1;
    private double exp = 0;
    private double expToNextLevel = 1000;

    public void addExp(double amount) {
        this.exp += amount;
        // 檢查是否升級，用 while 確保如果一次獲得大量經驗可以連升多級
        while (this.exp >= this.expToNextLevel) {
            this.exp -= this.expToNextLevel;
            this.level++;
            this.expToNextLevel *= 1.2; // 調整難度係數，例如 1.2 倍
        }
    }

    // Getters
    public int getLevel() { return level; }
    public double getExp() { return exp; }
    public double getExpToNextLevel() { return expToNextLevel; }
    public double getProgress() { return exp / expToNextLevel; }
}