package game.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RankingSystem {
    private List<RankingEntry> leaderboard = new ArrayList<>();

    public void addScore(String name, double score) {
        leaderboard.add(new RankingEntry(name, score));
        // 排序：分數降序，若分數相同則時間升序
        leaderboard.sort(Comparator.comparingDouble(RankingEntry::getScore).reversed());

        // 只保留前 10 名
        if (leaderboard.size() > 10) {
            leaderboard = new ArrayList<>(leaderboard.subList(0, 10));
        }
    }

    public List<RankingEntry> getTopScores() {
        return new ArrayList<>(leaderboard);
    }
}