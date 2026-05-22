package game.model;

public class StockRecord {

    private int time;
    private double price;

    public StockRecord(int time, double price) {
        this.time = time;
        this.price = price;
    }

    public int getTime() {
        return time;
    }

    public double getPrice() {
        return price;
    }
}