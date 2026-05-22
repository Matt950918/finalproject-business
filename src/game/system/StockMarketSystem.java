package game.system;

import game.model.Company;
import game.model.MarketEvent;
import game.model.StockRecord;
import game.service.MarketService;
import game.service.StockService;

import java.util.ArrayList;
import java.util.List;

public class StockMarketSystem {

    private Company company;
    private MarketService marketService;

    private List<StockRecord> history = new ArrayList<>();

    private int time = 0;

    public StockMarketSystem(Company company) {
        this.company = company;
        this.marketService = new MarketService();
    }

    // 一回合（核心模擬）
    public void nextTurn() {

        double price = StockService.calculateBasePrice(company);

        MarketEvent event = marketService.getRandomEvent();

        price = StockService.applyMarketEvent(price, event);

        price = StockService.applyVolatility(price);

        history.add(new StockRecord(time++, price));

        // 🔥 事件影響公司本身
        if (event.getEffect() > 1) {
            company.addReputation(2);
        } else {
            company.addReputation(-2);
        }
    }

    public List<StockRecord> getHistory() {
        return history;
    }

    public double getLatestPrice() {
        if (history.isEmpty()) return 0;
        return history.get(history.size() - 1).getPrice();
    }

    public Company getCompany() {
        return company;
    }
}