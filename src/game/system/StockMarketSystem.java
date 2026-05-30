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

    // 🎯 核心重構：統一由入口更新股價，此處僅做歷史追蹤與聲譽同步
    public void nextTurn() {
        // 1. 直接同步玩家公司在 MainGameController 已經計算完成的最新股價
        double currentPrice = company.getStockPrice();

        // 2. 將這筆乾淨的資料同步進 StockMarketSystem 的歷史紀錄
        history.add(new StockRecord(time++, currentPrice));

        // 3. 根據當前股價與初始股價(10元)的相對表現，調整公司聲譽
        if (currentPrice > 10.0) {
            company.addReputation(1); // 股價高於發行價，微幅增加商譽
        } else if (currentPrice < 10.0) {
            company.addReputation(-1); // 跌破發行價，商譽受損
        }
    }

    public List<StockRecord> getHistory() {
        // 為了防止圖表抓錯，直接與 Company 的歷史紀錄保持絕對同步
        return company.getStockHistory();
    }

    public double getLatestPrice() {
        return company.getStockPrice();
    }

    public Company getCompany() {
        return company;
    }
}