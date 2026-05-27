package game.controller;

import game.model.GachaSystem;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

public class GachaController {
    @FXML private Label lblResult;
    @FXML private Button btnDraw;

    private GachaSystem gachaSystem = new GachaSystem();
    private MainGameController mainController;

    public void initData(MainGameController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleDraw() {
        double cost = 1000000; // 抽獎一次 $100 萬
        if (mainController.getPlayerCompany().getCash() >= cost) {
            mainController.getPlayerCompany().spendCash(cost);
            String result = gachaSystem.draw();
            lblResult.setText(result);
            mainController.updateStatusLabels();
        } else {
            lblResult.setText("資金不足，無法抽獎！");
        }
    }
}