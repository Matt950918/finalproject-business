package game.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class RankingSystem {

    /**
     * 🔄 自動刷新機制：遍歷所有帳號、所有槽位，將所有「已創立的公司」加入排行榜
     */
    private List<RankingEntry> refreshLeaderboard() {
        List<RankingEntry> currentList = new ArrayList<>();

        // 🎯 抓取修改後的資料結構：Map<String, PlayerAccountSlots>
        Map<String, PlayerAccountSlots> allUsers = PlayerAccount.getUserRegistry();

        if (allUsers == null || allUsers.isEmpty()) {
            return currentList;
        }

        // 第一層迴圈：逐一檢查每個「玩家帳號」
        for (Map.Entry<String, PlayerAccountSlots> entry : allUsers.entrySet()) {
            String username = entry.getKey();
            PlayerAccountSlots accountSlots = entry.getValue();

            if (accountSlots == null) continue;

            // 第二層迴圈：檢查該帳號底下的 3 個存檔槽位 (Slot 0, 1, 2)
            for (int i = 0; i < 3; i++) {
                PlayerData playerData = accountSlots.getSlot(i);

                // 如果這個槽位是空的，代表還沒在這個槽位創立公司，直接跳過不列入排行
                if (playerData == null) {
                    continue;
                }

                // 檢查該槽位內是否有公司資料
                if (playerData.getCompany() == null) {
                    // 如果玩家開啟了這個槽位，但可能還在命名或籌備階段，給予預設值（加上槽位編號以利辨識）
                    currentList.add(new RankingEntry(
                            username + " (槽位" + (i + 1) + ")",
                            "籌備中企業",
                            null,
                            playerData.getMoney(), // 使用 PlayerData 內的初始資金
                            10.0
                    ));
                } else {
                    // 正常抓取公司資料列入排行
                    Company comp = playerData.getCompany();
                    currentList.add(new RankingEntry(
                            username + " (槽位" + (i + 1) + ")", // 名字後方標註槽位，避免同一玩家多間公司搞混
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

        // 使用 Comparator 進行現金降序排序
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