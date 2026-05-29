package game.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class RankingSystem {

    /**
     * 🔄 自動刷新機制：遍歷所有帳號、所有固定產業，將所有「已創立的公司」加入排行榜
     */
    private List<RankingEntry> refreshLeaderboard() {
        List<RankingEntry> currentList = new ArrayList<>();

        Map<String, PlayerAccountSlots> allUsers = PlayerAccount.getUserRegistry();

        if (allUsers == null || allUsers.isEmpty()) {
            return currentList;
        }

        // 第一層迴圈：逐一檢查每個「玩家帳號」
        for (Map.Entry<String, PlayerAccountSlots> entry : allUsers.entrySet()) {
            String username = entry.getKey();
            PlayerAccountSlots accountSlots = entry.getValue();

            if (accountSlots == null) continue;

            // 第二層迴圈：檢查固定好的 3 個產業位置
            for (int i = 0; i < 3; i++) {
                PlayerData playerData = accountSlots.getSlot(i);

                if (playerData == null) {
                    continue;
                }

                // 檢查該產業內是否有公司資料
                if (playerData.getCompany() == null) {
                    // 🎯 拔除「部門、槽位」字眼，只留下最乾淨的玩家帳號
                    currentList.add(new RankingEntry(
                            username,
                            "籌備中企業",
                            null,
                            playerData.getMoney(),
                            10.0
                    ));
                } else {
                    // 正常抓取公司資料列入排行
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
        }
        return currentList;
    }

    /**
     * 💰 取得【總資產】前 10 名排行榜（降序）
     */
    public List<RankingEntry> getTopCashEntries() {
        List<RankingEntry> list = refreshLeaderboard();
        list.sort(Comparator.comparingDouble(RankingEntry::getCash).reversed());
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
        list.sort(Comparator.comparingDouble(RankingEntry::getStockPrice).reversed());
        if (list.size() > 10) {
            return new ArrayList<>(list.subList(0, 10));
        }
        return list;
    }
}