package game.model;

import java.util.Random;

public class NewsOption {
    private String description;
    private double cost;          // 選擇此方案要花的錢
    private double[] probabilities; // 機率陣列 (例如 {0.6, 0.4} 代表 60% 與 40%)
    private MarketEvent[] outcomes; // 對應機率的結果陣列

    public NewsOption(String description, double cost, double[] probabilities, MarketEvent[] outcomes) {
        this.description = description;
        this.cost = cost;
        this.probabilities = probabilities;
        this.outcomes = outcomes;
    }

    public String getDescription() {
        return description + (cost > 0 ? " (花費: $" + cost + ")" : "");
    }

    // 🎯 核心邏輯：玩家點擊這個選項後，扣錢並擲骰子決定最終結果
    public MarketEvent execute(Company company) {
        // 1. 檢查並扣款
        if (cost > 0) {
            if (!company.spendCash(cost)) {
                return new MarketEvent("資金不足，無法應對", 0.85); // 沒錢硬選的懲罰：股價跌
            }
        }

        // 2. 擲骰子決定結果 (0.0 ~ 1.0)
        double rand = new Random().nextDouble();
        double cumulative = 0.0;

        for (int i = 0; i < probabilities.length; i++) {
            cumulative += probabilities[i];
            if (rand <= cumulative) {
                return outcomes[i]; // 回傳抽到的結果
            }
        }

        return outcomes[outcomes.length - 1]; // 防呆，預設回傳最後一個
    }
}
