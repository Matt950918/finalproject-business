
package game.model.bio;

public class Drug {
    public enum DrugType {
        PREVENTIVE,   // 預防型
        COLD,         // 感冒藥
        SPECIAL       // 特效藥
    }

    private String name;
    private DrugType type;

    private double baseSuccessRate;
    private double cost;
    private double price;

    private boolean isDiscovered;

    public Drug(String name, DrugType type, double baseSuccessRate, double cost, double price) {
        this.name = name;
        this.type = type;
        this.baseSuccessRate = baseSuccessRate;
        this.cost = cost;
        this.price = price;
        this.isDiscovered = false;
    }

    // 研發成功判定
    public boolean tryDevelop(double successBonus) {
        double finalRate = baseSuccessRate + successBonus;

        double rand = Math.random();

        if (rand < finalRate) {
            isDiscovered = true;
        }

        return isDiscovered;
    }

    // getters
    public String getName() { return name; }
    public DrugType getType() { return type; }
    public double getCost() { return cost; }
    public double getPrice() { return price; }
    public boolean isDiscovered() { return isDiscovered; }
}