package game.controller;

import game.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class RankingPanelController {
    @FXML private VBox rankContainer;

    public void showLeaderboard(RankingSystem system) {
        rankContainer.getChildren().clear();
        int rank = 1;
        for (RankingEntry entry : system.getTopScores()) {
            Label lbl = new Label(rank + ". " + entry.getPlayerName() + " - $" + String.format("%,.0f", entry.getScore()));
            lbl.setStyle("-fx-font-size: 18px; -fx-padding: 5;");
            rankContainer.getChildren().add(lbl);
            rank++;
        }
    }
}