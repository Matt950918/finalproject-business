package game.model;

public class MarketEvent {
    private String name;
    private String description;
    private double priceChange; // 固定金額變動 (相容舊系統)

    public MarketEvent(String name, double priceChange) {
        this.name = name;
        this.description = "市場發生了隨機變動。";
        this.priceChange = priceChange;
    }

    public MarketEvent(String name, String description, double priceChange) {
        this.name = name;
        this.description = description;
        this.priceChange = priceChange;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPriceChange() { return priceChange; }
    public double getEffect() { return priceChange; } // 相容你夥伴原本舊代碼的別名
}