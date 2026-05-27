package game.controller;

import game.model.RankingEntry;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 🏆 單機高分榜面板控制器 (RankingPanelController.java)
 * 完美內建舊代碼相容轉接橋樑，維持不驚動其他夥伴檔案的最高指導原則
 */
public class RankingPanelController {

    @FXML private VBox rankContainer; // FXML 的垂直容器

    private static final String FILE_PATH = "ranking.dat"; // 本地存檔名稱
    private MainGameController mainController;

    public void initData(MainGameController mainController) {
        this.mainController = mainController;
        refreshLeaderboard();
    }

    // ==========================================
    // 🔀 舊系統相容轉接器
    // ==========================================

    /**
     * 💡 關鍵修正：補上這個舊方法簽章，當其他檔案帶入 RankingSystem 呼叫時，
     * 會自動分流導向我們全新做好的單機高分榜刷新機制，紅字當場解掉！
     */
    public void showLeaderboard(game.model.RankingSystem rankingSystem) {
        refreshLeaderboard();
    }

    // ==========================================
    // 🔄 核心高分榜業務邏輯
    // ==========================================

    /**
     * 從本地檔案讀取歷史高分榜，並渲染到介面上
     */
    public void refreshLeaderboard() {
        if (rankContainer == null) return;
        rankContainer.getChildren().clear(); // 清空舊畫面

        // 1. 讀取並排序
        List<RankingEntry> list = loadRankingFile();
        Collections.sort(list);

        // 2. 如果沒有任何紀錄，顯示提示文字
        if (list.isEmpty()) {
            Label lblEmpty = new Label("目前尚無歷史通關紀錄，前輩留名，虛位以待！");
            lblEmpty.setStyle("-fx-font-size: 15px; -fx-text-fill: #999999; -fx-padding: 20;");
            rankContainer.getChildren().add(lblEmpty);
            return;
        }

        // 3. 只取前 10 名渲染到 JavaFX 畫面上
        int limit = Math.min(10, list.size());
        for (int i = 0; i < limit; i++) {
            RankingEntry entry = list.get(i);
            int rank = i + 1;

            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);
            row.setSpacing(25);
            row.setStyle("-fx-padding: 15 25; -fx-background-color: #ffffff; -fx-background-radius: 12; -fx-border-color: #ebe9f4; -fx-border-width: 1; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.02), 5, 0, 0, 2);");

            // 名次花式 Emoji
            String rankVisual = String.valueOf(rank);
            if (rank == 1) rankVisual = "🥇 1";
            else if (rank == 2) rankVisual = "🥈 2";
            else if (rank == 3) rankVisual = "🥉 3";

            Label lblRank = new Label(rankVisual);
            lblRank.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + (rank <= 3 ? "#ffaa00" : "#666666") + "; -fx-min-width: 50;");

            // 玩家名稱
            Label lblName = new Label(entry.getName());
            lblName.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333333;");
            lblName.setMinWidth(200);

            // 彈性撐開間距
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // 通關/結算總資產分數
            Label lblScore = new Label(String.format("$%.2f 萬", entry.getScore() / 10000.0));
            lblScore.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2e59d9;");

            row.getChildren().addAll(lblRank, lblName, spacer, lblScore);
            rankContainer.getChildren().add(row);
        }
    }

    /**
     * 靜態開放接口：供遊戲結束（通關或破產）時，直接呼叫儲存新分數
     */
    public static void saveNewRecord(String name, double finalScore) {
        List<RankingEntry> list = loadRankingFile();
        list.add(new RankingEntry(name, finalScore));

        // 排序後只保留前 10 名
        Collections.sort(list);
        if (list.size() > 10) {
            list = new ArrayList<>(list.subList(0, 10));
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(list);
            System.out.println("💾 [高分榜] 成功將新紀錄儲存至本地檔案！");
        } catch (IOException e) {
            System.err.println("❌ 排行榜存檔失敗: " + e.getMessage());
        }
    }

    /**
     * 內部工具：讀取本地存檔
     */
    @SuppressWarnings("unchecked")
    private static List<RankingEntry> loadRankingFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<RankingEntry>) ois.readObject();
        } catch (Exception e) {
            System.err.println("⚠️ 讀取排行榜存檔異常，已自動初始化新榜單。");
            return new ArrayList<>();
        }
    }
}