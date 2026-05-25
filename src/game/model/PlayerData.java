package game.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlayerData implements Serializable {

    private static final long serialVersionUID = 1L;

    // 玩家資料
    private String username;
    private String password;
    private Company company;
    // 遊戲進度
    private double money;
    private int reputation;
    private int companyLevel;
    private int day;

    // 歷史紀錄
    private List<String> history;

    // 建構子
    public PlayerData(String username, String password) {
        this.username = username;
        this.password = password;

        // 初始遊戲數值
        this.money = 100000;
        this.reputation = 0;
        this.companyLevel = 1;
        this.day = 1;

        this.history = new ArrayList<>();
    }

    // Getter / Setter

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public double getMoney() {
        return money;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public int getReputation() {
        return reputation;
    }

    public void setReputation(int reputation) {
        this.reputation = reputation;
    }

    public int getCompanyLevel() {
        return companyLevel;
    }

    public void setCompanyLevel(int companyLevel) {
        this.companyLevel = companyLevel;
    }

    public int getDay() {
        return day;
    }
    // 請在 PlayerData.java 裡面加上這幾行
    public void setMoney(double money) {
        this.money = money; // 這裡的變數名稱請對齊你子系統裡存錢的變數（例如 cash 或 balance）
    }
    public void setDay(int day) {
        this.day = day;
    }
    public void nextDay() {
        this.day++;
    }

    public List<String> getHistory() {
        return history;
    }

    public void addHistory(String record) {
        history.add(record);
    }
}