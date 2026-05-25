package game.model;

import java.io.Serializable;

public class StockRecord implements Serializable{

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