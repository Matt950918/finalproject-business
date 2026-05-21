package core;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Mainapp extends Application {

    // 🌟 設為 static，這樣 Controller 就能隨時呼叫它來換畫面！
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        showHome(); // 啟動時先載入首頁
    }

    // ==========================================
    // 🏠 首頁 (載入 Switch 風格的 FXML)
    // ==========================================
    public static void showHome() {
        try {
            // 🌟 2. 因為是 static 方法，這裡必須改成 Mainapp.class
            Parent root = FXMLLoader.load(Mainapp.class.getResource("/game/view/MainMenu.fxml"));

            // 視窗設為 800x600，比較有大作遊戲感
            primaryStage.setScene(new Scene(root, 800, 600));
            primaryStage.setTitle("大老闆模擬器");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ 找不到 FXML 檔案，請確認檔案是否有放在 /game/view/ 裡面！");
        }
    }

    // ==========================================
    // 🏢 公司選擇畫面
    // ==========================================
    public static void showCompanySelect() {
        Text title = new Text("請選擇公司類型");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Button bankBtn = new Button("🏦 銀行業");
        Button techBtn = new Button("💻 科技業");
        Button bioBtn = new Button("🧬 生技業");

        Button backBtn = new Button("返回首頁");

        bankBtn.setOnAction(e -> enterCompany("BANK"));
        techBtn.setOnAction(e -> enterCompany("TECH"));
        bioBtn.setOnAction(e -> enterCompany("BIO"));

        backBtn.setOnAction(e -> showHome());

        VBox root = new VBox(15, title, bankBtn, techBtn, bioBtn, backBtn);
        root.setStyle("-fx-alignment: center; -fx-padding: 40;");

        primaryStage.setScene(new Scene(root, 800, 600)); // 統一尺寸
    }

    // ==========================================
    // 🎮 進入公司
    // ==========================================
    public static void enterCompany(String type) {
        switch (type) {
            case "BANK":
                primaryStage.setScene(createScene("🏦 銀行業"));
                break;
            case "TECH":
                primaryStage.setScene(createScene("💻 科技業"));
                break;
            case "BIO":
                primaryStage.setScene(createScene("🧬 生技業"));
                break;
        }
    }

    // ==========================================
    // 🎯 公司內部畫面測試用
    // ==========================================
    private static Scene createScene(String companyName) {
        Text title = new Text(companyName + " 運作中...");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Text desc = new Text("這裡之後會放遊戲主系統（研發 / 股價 / 市場）");

        Button backBtn = new Button("返回公司選單");
        backBtn.setOnAction(e -> showCompanySelect());

        VBox root = new VBox(20, title, desc, backBtn);
        root.setStyle("-fx-alignment: center; -fx-padding: 40;");

        return new Scene(root, 800, 600);
    }

    public static void main(String[] args) {
        launch(args);
    }
}