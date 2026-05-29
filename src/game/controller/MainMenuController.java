package game.controller;

import core.Mainapp;
import game.model.PlayerAccount;
import game.model.PlayerAccountSlots;
import game.model.PlayerData;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    // 🎯 暫存目前登入帳號的「多槽位物件」
    private static PlayerAccountSlots currentAccountSlots = null;

    // 🎯 全域靜態變數，維持給其他子系統讀取「當前遊玩中」的單一公司進度
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

    /**
     * 🛠️ 修改：點擊開始遊戲時，彈出「選擇存檔槽位」視窗
     */
    @FXML
    private void handleStartGame(ActionEvent event) {
        if (currentUser == null || currentAccountSlots == null) {
            lblWelcome.setText("⚠️ 請先登入帳號！");
            lblWelcome.setStyle("-fx-text-fill: #e60012;");
            showLoginLayer(null);
            return;
        }

        // 1. 準備下拉選單的選項文字
        List<String> choices = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            PlayerData slotData = currentAccountSlots.getSlot(i);
            if (slotData == null) {
                choices.add("存檔槽位 " + (i + 1) + " [ 空白新局 ]");
            } else {
                String compName = (slotData.getCompany() != null) ? slotData.getCompany().getName() : "籌備中企業";
                choices.add("存檔槽位 " + (i + 1) + " [ " + compName + " - 第 " + slotData.getDay() + " 天 ]");
            }
        }

        // 2. 彈出 ChoiceDialog 讓玩家選槽位
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("選擇存檔槽位");
        dialog.setHeaderText("請選擇您要載入或新創的企業存檔：");
        dialog.setContentText("可選槽位：");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            // 算出玩家選了哪一個索引 (0, 1, 2)
            int selectedSlotIndex = choices.indexOf(result.get());

            // 3. 取得該槽位的資料
            PlayerData loadedProgress = currentAccountSlots.getSlot(selectedSlotIndex);

            if (loadedProgress != null) {
                // ➔ 舊存檔：直接載入，繞過選產業畫面
                activeProgress = loadedProgress;
                if (loadedProgress.getCompany() != null) {
                    Mainapp.setGlobalCompanyName(loadedProgress.getCompany().getName());
                    Mainapp.enterCompany(loadedProgress.getCompany().getIndustry(), selectedSlotIndex);
                } else {
                    // 如果有存檔但沒公司（籌備中），帶去選產業
                    Mainapp.setGlobalCompanyName("新創企業");
                    Mainapp.showCompanySelect(selectedSlotIndex);
                }
            } else {
                // ➔ 新遊戲：開啟新局，彈窗詢問公司名稱
                TextInputDialog nameDialog = new TextInputDialog("遠東生技");
                nameDialog.setTitle("新創企業命名");
                nameDialog.setHeaderText("建立新公司");
                nameDialog.setContentText("請輸入您的公司名稱：");

                Optional<String> nameResult = nameDialog.showAndWait();
                String finalCompanyName = nameResult.isPresent() && !nameResult.get().trim().isEmpty()
                        ? nameResult.get().trim() : "新創集團";

                // 實例化全新 PlayerData 並綁定到該槽位
                activeProgress = new PlayerData(currentUser, txtPassword.getText());
                currentAccountSlots.setSlot(selectedSlotIndex, activeProgress); // 塞入槽位管理器

                // 帶入 global 名稱並切換至選產業畫面（傳入 slotIndex）
                Mainapp.setGlobalCompanyName(finalCompanyName);
                Mainapp.showCompanySelect(selectedSlotIndex);
            }
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        if (bgmPlayer != null) bgmPlayer.stop();
        System.exit(0);
    }

    /**
     * 🛠️ 修改：登入改撈取多槽位的 PlayerAccountSlots 物件
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        // 🎯 撈出該帳號的「多槽位控制器」
        PlayerAccountSlots loadedAccount = PlayerAccount.loginAndGetAccount(user, pass);

        if (loadedAccount != null) {
            currentUser = user;
            currentAccountSlots = loadedAccount; // 裝進暫存區

            lblLoginMessage.setText("✅ 登入成功！請關閉此視窗並點擊開始遊戲。");
            lblLoginMessage.setStyle("-fx-text-fill: #00FF00;");

            updateWelcomeUI();
            hideLoginLayer(null);
        } else {
            lblLoginMessage.setText("❌ 帳號或密碼錯誤！");
            lblLoginMessage.setStyle("-fx-text-fill: #e60012;");
        }
    }

    /**
     * 🛠️ 修改：註冊配合新版 PlayerAccount 的純淨註冊接口 (移除舊版多餘的 compName 參數)
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            lblLoginMessage.setText("⚠️ 帳號或密碼不可為空！");
            lblLoginMessage.setStyle("-fx-text-fill: #e60012;");
            return;
        }

        // 調用我們在 PlayerAccount 寫好的多槽位純淨註冊方法
        if (PlayerAccount.register(user, pass)) {
            lblLoginMessage.setText("✅ 帳號創立成功！請點擊登入後，再按開始遊戲創立公司。");
            lblLoginMessage.setStyle("-fx-text-fill: #00FF00;");
        } else {
            lblLoginMessage.setText("❌ 帳號已存在或格式錯誤！");
            lblLoginMessage.setStyle("-fx-text-fill: #e60012;");
        }
    }

    @FXML private void showLoginLayer(ActionEvent event) { lblLoginMessage.setText(""); txtUsername.clear(); txtPassword.clear(); txtCompanyName.clear(); mainMenuLayer.setVisible(false); loginLayer.setVisible(true); }
    @FXML private void hideLoginLayer(ActionEvent event) { loginLayer.setVisible(false); mainMenuLayer.setVisible(true); }
    @FXML private void showInstructions(ActionEvent event) { mainMenuLayer.setVisible(false); instructionLayer.setVisible(true); }
    @FXML private void hideInstructions(ActionEvent event) { instructionLayer.setVisible(false); mainMenuLayer.setVisible(true); }
}