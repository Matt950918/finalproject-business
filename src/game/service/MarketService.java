package game.service;

import game.model.Company;
import game.model.MarketEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MarketService {

    private static final Random rand = new Random();

    private List<MarketEvent> events = new ArrayList<>();

    public MarketService() {

        // 預設事件
        events.add(new MarketEvent("科技爆發", 1.3));
        events.add(new MarketEvent("經濟衰退", 0.7));
        events.add(new MarketEvent("市場穩定", 1.0));
    }

    // 隨機選市場事件
    public MarketEvent getRandomEvent() {
        return events.get(rand.nextInt(events.size()));
    }

    // 影響公司
    public MarketEvent triggerEvent(Company c) {
        return getRandomEvent();
    }
}