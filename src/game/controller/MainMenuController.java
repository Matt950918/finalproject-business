package game.controller;

import core.Mainapp;
import game.model.PlayerAccount;
import game.model.PlayerAccountSlots;
import game.model.PlayerData;
import game.model.Company;
import game.model.IndustryType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MainMenuController {

    private static MediaPlayer bgmPlayer;

    @FXML private VBox mainMenuLayer;
    @FXML private VBox instructionLayer;
    @FXML private VBox loginLayer;
    @FXML private VBox registerLayer;

    // 登入相關欄位
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblLoginMessage;

    // 註冊相關欄位
    @FXML private TextField txtRegUsername;
    @FXML private PasswordField txtRegPassword;
    @FXML private TextField txtBankCompanyName;
    @FXML private TextField txtTechCompanyName;
    @FXML private TextField txtBioCompanyName;
    @FXML private Label lblRegisterMessage;

    @FXML private Label lblWelcome;

    public static String currentUser = null;
    private static PlayerAccountSlots currentAccountSlots = null;
    public static PlayerData activeProgress = null;

    @FXML
    public void initialize() {
        updateWelcomeUI();
        initBGM();
    }

    private void updateWelcomeUI() {
        if (currentUser != null) {
            lblWelcome.setText("已登入玩家: " + currentUser);
            lblWelcome.setStyle("-fx-text-fill: #232946; -fx-font-size: 22px; -fx-font-weight: bold;");
        } else {
            lblWelcome.setText("未登入");
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
     * 點擊「開始遊戲」按鈕的處理邏輯
     */
    @FXML
    private void handleStartGameAction(ActionEvent event) {
        if (currentUser == null || currentAccountSlots == null) {
            lblWelcome.setText("請先登入帳號！");
            lblWelcome.setStyle("-fx-text-fill: #e60012;");
            showLoginLayer(null);
            return;
        }

        // 完美對應你的設計理念：直接讓玩家選擇要切換去哪一個分身產業面板
        List<String> choices = new ArrayList<>();
        String[] industries = {"1. 銀行業", "2. 科技業", "3. 生科業"};

        for (int i = 0; i < 3; i++) {
            PlayerData slotData = currentAccountSlots.getSlot(i);
            if (slotData != null && slotData.getCompany() != null) {
                choices.add(industries[i] + " [" + slotData.getCompany().getName() + " - 第 " + slotData.getDay() + " 天]");
            } else {
                choices.add(industries[i] + " [未建立]");
            }
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("選擇要經營的產業分身");
        dialog.setHeaderText("請選擇您今天要視察並經營的公司部門：");
        dialog.setContentText("產業清單:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            int selectedSlotIndex = choices.indexOf(result.get());
            PlayerData loadedProgress = currentAccountSlots.getSlot(selectedSlotIndex);

            if (loadedProgress != null && loadedProgress.getCompany() != null) {
                activeProgress = loadedProgress;
                Mainapp.setGlobalCompanyName(loadedProgress.getCompany().getName());
                Mainapp.enterCompany(loadedProgress.getCompany().getIndustry(), selectedSlotIndex);
            } else {
                lblWelcome.setText("錯誤：該產業分身資料異常，請重新註冊。");
                lblWelcome.setStyle("-fx-text-fill: #e60012;");
            }
        }
    }

    /**
     * 執行登入驗證
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String user = txtUsername.getText().trim();
        String pass = txtPassword.getText().trim();

        PlayerAccountSlots loadedAccount = PlayerAccount.loginAndGetAccount(user, pass);
        if (loadedAccount != null) {
            currentUser = user;
            currentAccountSlots = loadedAccount;
            lblLoginMessage.setText("登入成功！");
            lblLoginMessage.setStyle("-fx-text-fill: #00FF00;");
            updateWelcomeUI();
            hideLoginLayer(null);
        } else {
            lblLoginMessage.setText("帳號或密碼錯誤！");
            lblLoginMessage.setStyle("-fx-text-fill: #e60012;");
        }
    }

    /**
     * 執行註冊（在這裡把三個分身名字一併綁定創好）
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        String user = txtRegUsername.getText().trim();
        String pass = txtRegPassword.getText().trim();
        String bankName = txtBankCompanyName.getText().trim();
        String techName = txtTechCompanyName.getText().trim();
        String bioName = txtBioCompanyName.getText().trim();

        if (user.isEmpty() || pass.isEmpty() || bankName.isEmpty() || techName.isEmpty() || bioName.isEmpty()) {
            lblRegisterMessage.setText("所有欄位均為必填，請完整填寫！");
            lblRegisterMessage.setStyle("-fx-text-fill: #e60012;");
            return;
        }

        // 呼叫底層註冊帳號
        if (PlayerAccount.register(user, pass)) {
            // 註冊成功後，立刻取得該帳號的實體，把三個產業分身全部打包初始化進去！
            PlayerAccountSlots newAccountSlots = PlayerAccount.getUserRegistry().get(user);

            // Slot 0 = 銀行
            PlayerData bankData = new PlayerData(user, pass);
            bankData.setCompany(new Company(bankName, IndustryType.BANK));
            newAccountSlots.setSlot(0, bankData);

            // Slot 1 = 科技
            PlayerData techData = new PlayerData(user, pass);
            techData.setCompany(new Company(techName, IndustryType.TECH));
            newAccountSlots.setSlot(1, techData);

            // Slot 2 = 生科
            PlayerData bioData = new PlayerData(user, pass);
            bioData.setCompany(new Company(bioName, IndustryType.BIOTECH));
            newAccountSlots.setSlot(2, bioData);

            // 強制存檔保存初始化資料
            PlayerAccount.saveSlotProgress(user, 0, bankData);
            PlayerAccount.saveSlotProgress(user, 1, techData);
            PlayerAccount.saveSlotProgress(user, 2, bioData);

            lblRegisterMessage.setText("帳號與三產業分身公司創建成功！請返回登入。");
            lblRegisterMessage.setStyle("-fx-text-fill: #00FF00;");

            // 註冊完後清空欄位
            txtRegUsername.clear();
            txtRegPassword.clear();
            txtBankCompanyName.clear();
            txtTechCompanyName.clear();
            txtBioCompanyName.clear();
        } else {
            lblRegisterMessage.setText("帳號已存在或創建失敗！");
            lblRegisterMessage.setStyle("-fx-text-fill: #e60012;");
        }
    }

    @FXML private void switchToRegister() { loginLayer.setVisible(false); registerLayer.setVisible(true); lblRegisterMessage.setText(""); }
    @FXML private void switchToLogin() { registerLayer.setVisible(false); loginLayer.setVisible(true); lblLoginMessage.setText(""); }
    @FXML private void showLoginLayer(ActionEvent event) { lblLoginMessage.setText(""); txtUsername.clear(); txtPassword.clear(); mainMenuLayer.setVisible(false); loginLayer.setVisible(true); }
    @FXML private void hideLoginLayer(ActionEvent event) { loginLayer.setVisible(false); registerLayer.setVisible(false); mainMenuLayer.setVisible(true); }
    @FXML private void showInstructions(ActionEvent event) { mainMenuLayer.setVisible(false); instructionLayer.setVisible(true); }
    @FXML private void hideInstructions(ActionEvent event) { instructionLayer.setVisible(false); mainMenuLayer.setVisible(true); }
    @FXML private void handleExit(ActionEvent event) { if (bgmPlayer != null) bgmPlayer.stop(); System.exit(0); }
}