package game.controller;

import core.Mainapp;
import game.model.PlayerAccount;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;
import javafx.util.Duration;

public class MainMenuController {

    // ==========================================
    // 🎵 BGM
    // ==========================================
    private static MediaPlayer bgmPlayer;

    // ==========================================
    // 📦 UI Layer
    // ==========================================
    @FXML private TextField txtCompanyName; // 畫面上原有的公司名稱輸入框
    @FXML private VBox mainMenuLayer;
    @FXML private VBox instructionLayer;
    @FXML private VBox loginLayer;

    // ==========================================
    // 🔐 Login UI
    // ==========================================
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblLoginMessage;
    @FXML private Label lblWelcome;

    public static String companyName = "遠東集團"; // 預設值

    // ==========================================
    // 👤 登入狀態
    // ==========================================
    public static String currentUser = null;

    // ==========================================
    // 🌟 初始化
    // ==========================================
    @FXML
    public void initialize() {
        updateWelcomeUI();
        initBGM();
    }

    // ==========================================
    // 🧹 模組化：統一管理登入 UI 狀態
    // ==========================================
    private void updateWelcomeUI() {
        if (currentUser != null) {
            lblWelcome.setText("目前登入：" + currentUser);
            lblWelcome.setStyle("-fx-text-fill: #232946; -fx-font-size: 22px; -fx-font-weight: bold;");
        } else {
            lblWelcome.setText("尚未登入");
            lblWelcome.setStyle("-fx-text-fill: #666666; -fx-font-size: 18px;");
        }
    }

    // ==========================================
    // 🧹 模組化：乾淨的 BGM 初始化邏輯
    // ==========================================
    private void initBGM() {
        if (bgmPlayer != null) return;

        try {
            URL musicUrl = getClass().getResource("/resources/music/bgm.mp3");

            if (musicUrl != null) {
                Media media = new Media(musicUrl.toString());
                bgmPlayer = new MediaPlayer(media);
                bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                bgmPlayer.setVolume(0.3);

                // 設定音樂永遠從第 2 秒開始播放
                bgmPlayer.setStartTime(Duration.seconds(2));

                bgmPlayer.play();
                System.out.println("🎵 BGM播放成功 (已跳過前2秒)");
            } else {
                System.err.println("❌ 找不到 bgm.mp3，請確認檔案已放入 resources 資料夾");
            }
        } catch (Exception e) {
            System.err.println("⚠️ BGM錯誤：" + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==========================================
    // 🚀 開始遊戲
    // ==========================================
    @FXML
    private void handleStartGame(ActionEvent event) {
        if (currentUser == null) {
            lblWelcome.setText("⚠️ 請先登入帳號！");
            lblWelcome.setStyle("-fx-text-fill: #e60012;");
            showLoginLayer(null);
        } else {
            System.out.println(currentUser + " 進入產業選擇");
            Mainapp.showCompanySelect();
        }
    }

    // ==========================================
    // ❌ 離開遊戲
    // ==========================================
    @FXML
    private void handleExit(ActionEvent event) {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
        }
        System.exit(0);
    }

    // ==========================================
    // 🔐 登入 (老玩家：直接撈取當初綁定的公司名，不需要再打一次名字)
    // ==========================================
    @FXML
    private void handleLogin(ActionEvent event) {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        // 直接呼叫我們寫好的擴充方法，登入成功就順便把資料庫裡綁定的公司名稱拿回來
        String boundCompanyName = game.model.PlayerAccount.loginAndGetCompany(user, pass);

        if (boundCompanyName != null) {
            currentUser = user;

            // 將這台帳號綁定的公司名稱，直接灌進 Mainapp 裡面
            core.Mainapp.setGlobalCompanyName(boundCompanyName);

            updateWelcomeUI();
            hideLoginLayer(null);
        } else {
            lblLoginMessage.setText("❌ 帳號或密碼錯誤！");
            lblLoginMessage.setStyle("-fx-text-fill: #e60012;");
        }
    }

    // ==========================================
    // 📝 註冊 (新玩家：在同一個畫面上輸入帳密與想取的名字，直接註冊綁定！)
    // ==========================================
    @FXML
    private void handleRegister(ActionEvent event) {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();
        String compName = txtCompanyName.getText(); // 💡 沿用畫面上的輸入框！

        if (user.isEmpty() || pass.isEmpty()) {
            lblLoginMessage.setText("⚠️ 帳號或密碼不可為空！");
            lblLoginMessage.setStyle("-fx-text-fill: #e60012;");
            return;
        }

        // 💡 呼叫我們擴充的 register 方法，把名字、帳號、密碼一次打包綁定存檔！
        if (PlayerAccount.register(user, pass, compName)) {
            lblLoginMessage.setText("✅ 註冊成功！請直接點擊登入。");
            lblLoginMessage.setStyle("-fx-text-fill: #00FF00;");
        } else {
            lblLoginMessage.setText("❌ 帳號已存在！");
            lblLoginMessage.setStyle("-fx-text-fill: #e60012;");
        }
    }

    // ==========================================
    // 🔲 UI切換
    // ==========================================
    @FXML
    private void showLoginLayer(ActionEvent event) {
        lblLoginMessage.setText("");
        txtUsername.clear();
        txtPassword.clear();
        txtCompanyName.clear(); // 切換時清空
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

    // 已經幫妳把舊的、會報錯的 handleRegisterSubmit 刪除了，保持程式碼最乾淨的狀態

    @FXML
    private void hideInstructions(ActionEvent event) {
        instructionLayer.setVisible(false);
        mainMenuLayer.setVisible(true);
    }
}