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

    // 💡 確保是 public static，讓全案都能合法存取
    public static String globalCompanyName = "遠東集團";

    // 💡 提供公開方法讓 MainMenuController 寫入名字
    public static void setGlobalCompanyName(String name) {
        globalCompanyName = name;
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        showHome();
    }

    public static void showHome() {
        try {
            Parent root = FXMLLoader.load(Mainapp.class.getResource("/game/view/MainMenu.fxml"));
            primaryStage.setScene(new Scene(root, 900, 650));
            primaryStage.setTitle("大老闆模擬器");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 🛠️ 修改：選擇公司類型的畫面，必須知道現在是要把新創的公司塞進哪一個槽位 (slotIndex)
     */
    public static void showCompanySelect(int slotIndex) {
        Text title = new Text("請選擇公司類型");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-fill: #5A5C69;");

        Button bankBtn = new Button("🏦 銀行業");
        Button techBtn = new Button("💻 科技業");
        Button bioBtn = new Button("🧬 生技業");
        Button backBtn = new Button("返回首頁");

        bankBtn.getStyleClass().add("switch-button");
        techBtn.getStyleClass().add("switch-button");
        bioBtn.getStyleClass().add("switch-button");
        backBtn.getStyleClass().add("switch-button");
        backBtn.setStyle("-fx-text-fill: #E74A3B;");

        // 🎯 精準綁定：點擊按鈕時，除了傳入產業類型 (type)，也把指定好的 slotIndex 一併傳下去
        bankBtn.setOnAction(e -> enterCompany(IndustryType.BANK, slotIndex));
        techBtn.setOnAction(e -> enterCompany(IndustryType.TECH, slotIndex));
        bioBtn.setOnAction(e -> enterCompany(IndustryType.BIOTECH, slotIndex));
        backBtn.setOnAction(e -> showHome());

        VBox root = new VBox(20, title, bankBtn, techBtn, bioBtn, backBtn);
        root.setStyle("-fx-alignment: center; -fx-padding: 40; -fx-background-color: #F4F6F9;");

        Scene scene = new Scene(root, 900, 650);
        try {
            scene.getStylesheets().add(Mainapp.class.getResource("/game/view/global_theme.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("⚠️ 載入選單樣式表失敗");
        }

        primaryStage.setScene(scene);
    }

    /**
     * 🛠️ 修改：進入遊戲畫面方法，新增接收 slotIndex 參數
     */
    public static void enterCompany(IndustryType type, int slotIndex) {
        try {
            FXMLLoader loader = new FXMLLoader(Mainapp.class.getResource("/game/view/MainGame.fxml"));
            Parent root = loader.load();

            // 💡 讀取取好的公司名稱
            String userCompanyName = globalCompanyName;

            // 取得主畫面的 Controller
            MainGameController controller = loader.getController();

            // 🎯 重點修正：將槽位編號 slotIndex 灌入 startGame 方法，完成多存檔對接
            controller.startGame(userCompanyName, type, slotIndex);

            primaryStage.setScene(new Scene(root, 900, 650));
        } catch (Exception e) {
            System.err.println("❌ 載入 MainGame.fxml 或初始化遊戲主畫面失敗！");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}