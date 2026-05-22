package game.service;

import game.model.Company;
import game.model.MarketEvent;

import java.util.Random;

public class StockService {

    private static final Random rand = new Random();

    // 基礎股價計算
    public static double calculateBasePrice(Company c) {

        double cashScore = Math.log(c.getCash() + 1) * 2;

        double repScore = c.getReputation() * 1.5;

        double levelScore = c.getLevel() * 10;

        return cashScore + repScore + levelScore;
    }

    // 套用市場事件
    public static double applyMarketEvent(double price, MarketEvent event) {
        return price * event.getEffect();
    }

    // 市場波動（隨機性）
    public static double applyVolatility(double price) {
        double noise = 0.9 + (rand.nextDouble() * 0.2);
        return price * noise;
    }
}