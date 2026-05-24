package core;

import game.controller.MainGameController;
import game.model.IndustryType;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Mainapp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        showHome(); // 啟動時先載入首頁
    }

    // ==========================================
    // 🏠 首頁 (載入主選單)
    // ==========================================
    public static void showHome() {
        try {
            Parent root = FXMLLoader.load(Mainapp.class.getResource("/game/view/MainMenu.fxml"));
            primaryStage.setScene(new Scene(root, 900, 650));
            primaryStage.setTitle("大老闆模擬器");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ 找不到 MainMenu.fxml，請確認路徑是否正確！");
        }
    }

    // ==========================================
    // 🏢 公司選擇畫面
    // ==========================================
    public static void showCompanySelect() {
        Text title = new Text("請選擇公司類型");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-fill: #5A5C69;");

        Button bankBtn = new Button("🏦 銀行業");
        Button techBtn = new Button("💻 科技業");
        Button bioBtn = new Button("🧬 生技業");
        Button backBtn = new Button("返回首頁");

        // 套用樣式類別（與 global_theme.css 對齊）
        bankBtn.getStyleClass().add("switch-button");
        techBtn.getStyleClass().add("switch-button");
        bioBtn.getStyleClass().add("switch-button");
        backBtn.getStyleClass().add("switch-button");
        backBtn.setStyle("-fx-text-fill: #E74A3B;"); // 設定返回按鈕為醒目紅色

        // 點擊後直接傳遞 IndustryType Enum 進入遊戲
        bankBtn.setOnAction(e -> enterCompany(IndustryType.BANK));
        techBtn.setOnAction(e -> enterCompany(IndustryType.TECH));
        bioBtn.setOnAction(e -> enterCompany(IndustryType.BIOTECH));
        backBtn.setOnAction(e -> showHome());

        VBox root = new VBox(20, title, bankBtn, techBtn, bioBtn, backBtn);
        root.setStyle("-fx-alignment: center; -fx-padding: 40; -fx-background-color: #F4F6F9;");

        Scene scene = new Scene(root, 900, 650);
        try {
            scene.getStylesheets().add(Mainapp.class.getResource("/game/view/global_theme.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("⚠️ 載入選單樣式表失敗，請確認 global_theme.css 位置");
        }

        primaryStage.setScene(scene);
    }

    // ==========================================
    // 🎮 進入遊戲主畫面
    // ==========================================
    public static void enterCompany(IndustryType type) {
        try {
            FXMLLoader loader = new FXMLLoader(Mainapp.class.getResource("/game/view/MainGame.fxml"));
            Parent root = loader.load();

            // 取得對應畫面的 Controller，將選擇的產業傳遞過去
            MainGameController controller = loader.getController();
            controller.startGame(type);

            primaryStage.setScene(new Scene(root, 900, 650));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ 找不到 MainGame.fxml，請確認檔案是否有放在 /game/view/ 裡面！");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}