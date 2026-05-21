package game.controller;
import core.Mainapp;
import game.model.PlayerAccount;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class MainMenuController {

    // 綁定圖層
    @FXML private VBox mainMenuLayer;
    @FXML private VBox instructionLayer;
    @FXML private VBox loginLayer;

    // 綁定登入元件
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblLoginMessage;
    @FXML private Label lblWelcome;

    // 記錄目前誰登入了 (空值代表沒登入)
    public static String currentUser = null;

    // ==========================================
    // 遊戲主流程按鈕
    // ==========================================
    @FXML
    private void handleStartGame(ActionEvent event) {
        if (currentUser == null) {
            lblWelcome.setText("⚠️ 請先登入帳號才能開始遊戲！");
            lblWelcome.setStyle("-fx-text-fill: #e60012;"); // 變紅色警告
            showLoginLayer(null); // 強制彈出登入視窗
        } else {
            System.out.println(currentUser + " 準備跳轉到【選擇產業】畫面！");
            Mainapp.showCompanySelect();
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        System.exit(0);
    }

    // ==========================================
    // 帳號登入與註冊邏輯
    // ==========================================
    @FXML
    private void handleLogin(ActionEvent event) {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        if (PlayerAccount.login(user, pass)) {
            currentUser = user;
            lblWelcome.setText("目前登入：" + currentUser);
            lblWelcome.setStyle("-fx-text-fill: #00c3e3;"); // 變藍色
            hideLoginLayer(null);
        } else {
            lblLoginMessage.setText("❌ 帳號或密碼錯誤！");
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            lblLoginMessage.setText("⚠️ 帳號跟密碼不能為空！");
            return;
        }

        if (PlayerAccount.register(user, pass)) {
            lblLoginMessage.setText("✅ 註冊成功！請按左方登入");
            lblLoginMessage.setStyle("-fx-text-fill: #00FF00;"); // 綠色
        } else {
            lblLoginMessage.setText("❌ 此帳號已經有人使用了！");
            lblLoginMessage.setStyle("-fx-text-fill: #e60012;"); // 紅色
        }
    }

    // ==========================================
    // 圖層開關控制
    // ==========================================
    @FXML
    private void showLoginLayer(ActionEvent event) {
        lblLoginMessage.setText(""); // 清除錯誤訊息
        txtUsername.clear();
        txtPassword.clear();
        mainMenuLayer.setVisible(false);
        loginLayer.setVisible(true);
    }

    @FXML
    private void hideLoginLayer(ActionEvent event) {
        loginLayer.setVisible(false);
        mainMenuLayer.setVisible(true);
    }

    @FXML
    private void showInstructions(ActionEvent event) {
        mainMenuLayer.setVisible(false);
        instructionLayer.setVisible(true);
    }

    @FXML
    private void hideInstructions(ActionEvent event) {
        instructionLayer.setVisible(false);
        mainMenuLayer.setVisible(true);
    }
}