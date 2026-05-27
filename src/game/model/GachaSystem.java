package game.model;

import java.util.Random;

public class GachaSystem {
    private Random random = new Random();

    public String draw() {
        int chance = random.nextInt(100); // 0-99

        if (chance < 10) {
            return "【傳說】獲得研發核心組件！";
        } else if (chance < 30) {
            return "【稀有】獲得天使投資人挹注 $500 萬！";
        } else {
            return "【普通】獲得免費咖啡券，提升員工效率。";
        }
    }
}