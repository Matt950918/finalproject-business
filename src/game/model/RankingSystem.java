package game.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class RankingSystem {

    /**
     * 🔄 自動刷新機制：直接讀取 PlayerAccount 內所有註冊帳號，轉換為排行列表
     */
    private List<RankingEntry> refreshLeaderboard() {
        List<RankingEntry> currentList = new ArrayList<>();

        // 🎯 全域抓取：從我們前面修好的 PlayerAccount 中，拿到目前載入的所有帳號對應
        Map<String, PlayerData> allUsers = PlayerAccount.getUserRegistry();

        if (allUsers == null || allUsers.isEmpty()) {
            return currentList;
        }

        // 將所有帳號逐一打包轉換為 RankingEntry 物件
        for (Map.Entry<String, PlayerData> entry : allUsers.entrySet()) {
            String username = entry.getKey();
            PlayerData playerData = entry.getValue();

            // 如果玩家註冊了帳號，但還沒選產業進入遊戲，就給予初始值
            if (playerData.getCompany() == null) {
                currentList.add(new RankingEntry(username, "籌備中企業", null, 50000000.0, 10.0));
            } else {
                Company comp = playerData.getCompany();
                currentList.add(new RankingEntry(
                        username,
                        comp.getName(),
                        comp.getIndustry(),
                        comp.getCash(),
                        comp.getStockPrice()
                ));
            }
        }
        return currentList;
    }

    /**
     * 💰 取得【總資產】前 10 名排行榜（降序）
     */
    public List<RankingEntry> getTopCashEntries() {
        List<RankingEntry> list = refreshLeaderboard();

        // 使用 Java 8 Stream 或是 Comparator 進行現金降序排序
        list.sort(Comparator.comparingDouble(RankingEntry::getCash).reversed());

        // 限制只保留前 10 名
        if (list.size() > 10) {
            return new ArrayList<>(list.subList(0, 10));
        }
        return list;
    }

    /**
     * 📈 取得【企業股價】前 10 名排行榜（降序）
     */
    public List<RankingEntry> getTopStockPriceEntries() {
        List<RankingEntry> list = refreshLeaderboard();

        // 依據股價降序排序
        list.sort(Comparator.comparingDouble(RankingEntry::getStockPrice).reversed());

        // 限制只保留前 10 名
        if (list.size() > 10) {
            return new ArrayList<>(list.subList(0, 10));
        }
        return list;
    }
}