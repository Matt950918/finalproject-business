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

import java.io.File;

public class MainMenuController {

    // ==========================================
    // 🎵 BGM（避免被 GC 回收）
    // ==========================================
    private static MediaPlayer bgmPlayer;

    // ==========================================
    // 📦 UI Layer
    // ==========================================
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

    // ==========================================
    // 👤 登入狀態（重點）
    // ==========================================
    public static String currentUser = null;

    // ==========================================
    // 🌟 初始化（FXML載入時）
    // ==========================================
    @FXML
    public void initialize() {

        // ==============================
        // ✅ 修正重點：回復登入狀態UI
        // ==============================
        if (currentUser != null) {

            lblWelcome.setText("目前登入：" + currentUser);
            lblWelcome.setStyle(
                    "-fx-text-fill: #232946;" +
                            "-fx-font-size: 22px;" +
                            "-fx-font-weight: bold;"
            );

        } else {

            lblWelcome.setText("尚未登入");
            lblWelcome.setStyle(
                    "-fx-text-fill: #666666;" +
                            "-fx-font-size: 18px;"
            );
        }

        // ==============================
        // 🎵 BGM（只初始化一次）
        // ==============================
        if (bgmPlayer != null) return;

        try {

            File file = new File("src/resources.image/bgm.mp3");

            if (!file.exists()) {
                file = new File("resources.image/bgm.mp3");
            }

            if (!file.exists()) {
                file = new File("music/bgm.mp3");
            }

            if (file.exists()) {

                Media media = new Media(file.toURI().toString());
                bgmPlayer = new MediaPlayer(media);

                bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                bgmPlayer.setVolume(0.3);
                bgmPlayer.play();

                System.out.println("🎵 BGM播放成功: " + file.getAbsolutePath());

            } else {
                System.err.println("❌ 找不到 bgm.mp3");
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
    // 🔐 登入
    // ==========================================
    @FXML
    private void handleLogin(ActionEvent event) {

        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        if (PlayerAccount.login(user, pass)) {

            currentUser = user;

            lblWelcome.setText("目前登入：" + currentUser);
            lblWelcome.setStyle(
                    "-fx-text-fill: #232946;" +
                            "-fx-font-size: 22px;" +
                            "-fx-font-weight: bold;"
            );

            hideLoginLayer(null);

        } else {
            lblLoginMessage.setText("❌ 帳號或密碼錯誤！");
        }
    }

    // ==========================================
    // 📝 註冊
    // ==========================================
    @FXML
    private void handleRegister(ActionEvent event) {

        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            lblLoginMessage.setText("⚠️ 不可為空！");
            return;
        }

        if (PlayerAccount.register(user, pass)) {

            lblLoginMessage.setText("✅ 註冊成功！");
            lblLoginMessage.setStyle("-fx-text-fill: #00FF00;");

        } else {

            lblLoginMessage.setText("❌ 帳號已存在！");
            lblLoginMessage.setStyle("-fx-text-fill: #e60012;");
        }
    }

    // ==========================================
    // 🔲 UI切換：登入頁
    // ==========================================
    @FXML
    private void showLoginLayer(ActionEvent event) {

        lblLoginMessage.setText("");
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

    // ==========================================
    // 📖 說明頁
    // ==========================================
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