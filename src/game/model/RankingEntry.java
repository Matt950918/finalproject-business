package game.model;

import java.io.Serializable;

public class RankingEntry implements Serializable {
    private String playerName;
    private double score;
    private long timestamp;

    public RankingEntry(String playerName, double score) {
        this.playerName = playerName;
        this.score = score;
        this.timestamp = System.currentTimeMillis();
    }

    public String getPlayerName() { return playerName; }
    public double getScore() { return score; }
}