import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.io.IOException;

public class Bot extends Player {

    public Bot(String name, int seat, Point position, int balance, boolean active) {
        this.name = name;
        this.seat = seat;
        this.position = position;
        this.balance = balance;
        this.active = active;

        VBox vBox = new VBox();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/Player.fxml"));
        fxmlLoader.setRoot(vBox);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        Game.getPane().getChildren().add(vBox);

        vBox.setLayoutX(position.getX() - (180/2));
        vBox.setLayoutY(position.getY());

        nameLabel.setText(name);
        balanceLabel.setText(Integer.toString(balance));
    }

    @Override
    public void addCardOnHand(Card card) {
        cardOnHand.addCard(card, position, false);
    }

    // TODO: calculating percent of winning then making a decision
    @Override
    public void bettingTurn() {
        // disable all button, so that player can't choose anything
        System.out.println("eiei");

    }

}
