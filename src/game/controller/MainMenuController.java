package game.controller;

import core.Mainapp;
import game.model.PlayerAccount;
import game.model.PlayerData;
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

    private static MediaPlayer bgmPlayer;

    @FXML private TextField txtCompanyName;
    @FXML private VBox mainMenuLayer;
    @FXML private VBox instructionLayer;
    @FXML private VBox loginLayer;

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblLoginMessage;
    @FXML private Label lblWelcome;

    public static String companyName = "遠東集團";
    public static String currentUser = null;

    // 🎯 關鍵新增：全域靜態變數，用來裝載目前登入帳號的「完整進度」
    public static PlayerData activeProgress = null;

    @FXML
    public void initialize() {
        updateWelcomeUI();
        initBGM();
    }

    private void updateWelcomeUI() {
        if (currentUser != null) {
            lblWelcome.setText("目前登入：" + currentUser);
            lblWelcome.setStyle("-fx-text-fill: #232946; -fx-font-size: 22px; -fx-font-weight: bold;");
        } else {
            lblWelcome.setText("尚未登入");
            lblWelcome.setStyle("-fx-text-fill: #666666; -fx-font-size: 18px;");
        }
    }

    private void initBGM() {
        if (bgmPlayer != null) return;
        try {
            URL musicUrl = getClass().getResource("/resources/music/bgm.mp3");
            if (musicUrl != null) {
                Media media = new Media(musicUrl.toString());
                bgmPlayer = new MediaPlayer(media);
                bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                bgmPlayer.setVolume(0.3);
                bgmPlayer.setStartTime(Duration.seconds(2));
                bgmPlayer.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStartGame(ActionEvent event) {
        if (currentUser == null || activeProgress == null) {
            lblWelcome.setText("⚠️ 請先登入帳號！");
            lblWelcome.setStyle("-fx-text-fill: #e60012;");
            showLoginLayer(null);
        } else {
            System.out.println(currentUser + " 進入產業選擇，將沿用或建立專屬進度。");
            Mainapp.showCompanySelect();
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        if (bgmPlayer != null) bgmPlayer.stop();
        System.exit(0);
    }

    /**
     * 🔐 登入重構：直接撈取完整的進度物件
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        // 🎯 撈出該帳號完整的 PlayerData
        PlayerData loadedProgress = PlayerAccount.loginAndGetProgress(user, pass);

        if (loadedProgress != null) {
            currentUser = user;
            activeProgress = loadedProgress; // 裝進暫存區

            // 為了防錯，如果它是剛註冊的新帳號，底下的 Company 可能還是 null，主畫面會幫它 new 出來
            if (loadedProgress.getCompany() != null) {
                core.Mainapp.setGlobalCompanyName(loadedProgress.getCompany().getName());
            } else {
                core.Mainapp.setGlobalCompanyName("新創企業");
            }

            updateWelcomeUI();
            hideLoginLayer(null);
        } else {
            lblLoginMessage.setText("❌ 帳號或密碼錯誤！");
            lblLoginMessage.setStyle("-fx-text-fill: #e60012;");
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();
        String compName = txtCompanyName.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            lblLoginMessage.setText("⚠️ 帳號或密碼不可為空！");
            lblLoginMessage.setStyle("-fx-text-fill: #e60012;");
            return;
        }

        if (PlayerAccount.register(user, pass, compName)) {
            lblLoginMessage.setText("✅ 註冊成功！請直接點擊登入。");
            lblLoginMessage.setStyle("-fx-text-fill: #00FF00;");
        } else {
            lblLoginMessage.setText("❌ 帳號已存在！");
            lblLoginMessage.setStyle("-fx-text-fill: #e60012;");
        }
    }

    @FXML private void showLoginLayer(ActionEvent event) { lblLoginMessage.setText(""); txtUsername.clear(); txtPassword.clear(); txtCompanyName.clear(); mainMenuLayer.setVisible(false); loginLayer.setVisible(true); }
    @FXML private void hideLoginLayer(ActionEvent event) { loginLayer.setVisible(false); mainMenuLayer.setVisible(true); }
    @FXML private void showInstructions(ActionEvent event) { mainMenuLayer.setVisible(false); instructionLayer.setVisible(true); }
    @FXML private void hideInstructions(ActionEvent event) { instructionLayer.setVisible(false); mainMenuLayer.setVisible(true); }
}