package game.model;

import java.io.Serializable;

/**
 * 🏆 單機高分榜：單筆歷史紀錄模型
 */
public class RankingEntry implements Serializable, Comparable<RankingEntry> {
    private static final long serialVersionUID = 1L;

    private String name;
    private double score;

    public RankingEntry(String name, double score) {
        this.name = name;
        this.score = score;
    }

    public String getName() { return name; }
    public double getScore() { return score; }

    @Override
    public int compareTo(RankingEntry o) {
        // 依照分數由高到低排序（降序）
        return Double.compare(o.score, this.score);
    }
}