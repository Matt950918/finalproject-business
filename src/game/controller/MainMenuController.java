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

    // 音樂播放器 (宣告為 static 防止被垃圾回收機制自動中斷)
    private static MediaPlayer bgmPlayer;

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
    // 🌟 初始化方法：畫面加載時「物理強行啟動」BGM
    // ==========================================
    @FXML
    public void initialize() {
        // 如果已經有音樂在播了，就不要重複建立，直接跳出
        if (bgmPlayer != null) {
            return;
        }

        try {
            // 💡 物理路徑策略 A：去硬碟的專案原始碼路徑找檔案
            File file = new File("src/resources.image/bgm.mp3");

            // 💡 物理路徑策略 B：如果是在編譯出的 out 目錄跑，去根目錄可能的位置撈
            if (!file.exists()) {
                file = new File("resources.image/bgm.mp3");
            }

            // 💡 物理路徑策略 C：如果還是找不到，去 target/out 輸出的根目錄找看看
            if (!file.exists()) {
                file = new File("music/bgm.mp3");
            }

            if (file.exists()) {
                // 轉化為標準的本地 URI 字串（格式會是 file:/C:/...）
                String source = file.toURI().toString();
                Media media = new Media(source);
                bgmPlayer = new MediaPlayer(media);

                // 設定無限循環播放
                bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                // 設定音量 (0.3 代表 30% 音量，剛好當背景音)
                bgmPlayer.setVolume(0.3);
                // 啟動播放
                bgmPlayer.play();
                System.out.println("🎵 【系統強行播放成功】實體檔案路徑: " + file.getAbsolutePath());
            } else {
                System.err.println("❌ 【物理路徑也找不到檔案】請確認你的 mp3 檔名是不是叫 bgm.mp3 且放在 src/resources.image 下！");
                System.err.println("🔍 程式預期它必須存在於: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("⚠️ 音樂播放器啟動失敗（請確認 VM Options 的 javafx.media 是否生效）：" + e.getMessage());
            e.printStackTrace(); // 把最深層的報錯堆疊印出來
        }

    }

    // ==========================================
    // 遊戲主流程按鈕
    // ==========================================
    @FXML
    private void handleStartGame(ActionEvent event) {
        if (currentUser == null) {
            lblWelcome.setText("⚠️ 請先登入帳號才能開始遊戲！");
            lblWelcome.setStyle("-fx-text-fill: #e60012;");
            showLoginLayer(null);
        } else {
            System.out.println(currentUser + " 準備跳轉到【選擇產業】畫面！");
            Mainapp.showCompanySelect();
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
        }
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

            // 🌟 在這裡加上 -fx-font-size 把字放大，再加粗體讓它更清楚！
            lblWelcome.setStyle("-fx-text-fill: #232946; -fx-font-size: 22px; -fx-font-weight: bold;");

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
            lblLoginMessage.setStyle("-fx-text-fill: #00FF00;");
        } else {
            lblLoginMessage.setText("❌ 此帳號已經有人使用了！");
            lblLoginMessage.setStyle("-fx-text-fill: #e60012;");
        }
    }

    // ==========================================
    // 圖層開關控制
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
    // 遊戲說明圖層控制
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