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
                // 🔥 只要選了需要花費的選項卻資金不足，一律觸發破產流程
                System.out.println("🚨 選項花費不足（需要 $" + cost + "），觸發破產檢查！");
                return new MarketEvent("__TRIGGER_BANKRUPTCY__", 0.5);
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
            // 🔥 進一步檢查：火災發生後，若公司已沒錢負擔損失，也觸發破產
            if (company.getCash() <= 0) {
                System.out.println("🚨 火災發生且公司資金歸零，觸發破產檢查！");
                return new MarketEvent("__TRIGGER_BANKRUPTCY__", 0.5);
            }
            System.out.println("🚨 偵測到火災新聞，觸發事件通知。");
        }

        return resultEvent;
    }
}