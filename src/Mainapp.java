import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Mainapp extends Application {

    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        showHome();
    }

    // 🏠 首頁
    private void showHome() {

        Text title = new Text("🏢 公司成長模擬系統");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        Button startBtn = new Button("進入遊戲");

        startBtn.setOnAction(e -> showCompanySelect());

        VBox root = new VBox(20, title, startBtn);
        root.setStyle("-fx-alignment: center; -fx-padding: 50;");

        stage.setScene(new Scene(root, 500, 400));
        stage.setTitle("Company Simulator");
        stage.show();
    }

    // 🏢 公司選擇畫面
    private void showCompanySelect() {

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

        stage.setScene(new Scene(root, 500, 400));
    }

    // 🎮 進入公司
    private void enterCompany(String type) {

        switch (type) {
            case "BANK":
                stage.setScene(createScene("🏦 銀行業"));
                break;

            case "TECH":
                stage.setScene(createScene("💻 科技業"));
                break;

            case "BIO":
                stage.setScene(createScene("🧬 生技業"));
                break;
        }
    }

    // 🎯 公司內部畫面（先做簡化版）
    private Scene createScene(String companyName) {

        Text title = new Text(companyName + " 運作中...");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Text desc = new Text("這裡之後會放遊戲主系統（研發 / 股價 / 市場）");

        Button backBtn = new Button("返回公司選單");
        backBtn.setOnAction(e -> showCompanySelect());

        VBox root = new VBox(20, title, desc, backBtn);
        root.setStyle("-fx-alignment: center; -fx-padding: 40;");

        return new Scene(root, 600, 400);
    }

    public static void main(String[] args) {
        launch(args);
    }
}