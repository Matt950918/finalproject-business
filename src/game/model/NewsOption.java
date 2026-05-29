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
        MarketEvent resultEvent = outcomes[outcomes.length - 1];

        for (int i = 0; i < probabilities.length; i++) {
            cumulative += probabilities[i];
            if (rand <= cumulative) {
                resultEvent = outcomes[i];
                break;
            }
        }

        // 🆕 【不透過 PlayerData 的連動】：
        // 如果結果名字是「火災停業」，代表現在是科技業公司，直接叫他扣下停業 3 天的天數！
        if (resultEvent != null && "火災停業".equals(resultEvent.getName())) {
            // 直接對傳進來的 company 物件呼叫（稍後我們在 TechSystem 的換日上做防呆）
            System.out.println("🚨 偵測到火災新聞，觸發事件通知。");
        }

        return resultEvent;
    }
}