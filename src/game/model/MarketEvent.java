package game.model;

public class MarketEvent {

    private String name;
    private double effect; // 影響倍率

    public MarketEvent(String name, double effect) {
        this.name = name;
        this.effect = effect;
    }

    public String getName() {
        return name;
    }

    public double getEffect() {
        return effect;
    }
}