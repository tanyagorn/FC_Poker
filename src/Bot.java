import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Random;

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

        balanceLabel.setText(Integer.toString(balance));
    }

    @Override
    public void addCardOnHand(Card card) {
        cardOnHand.addCard(card, position, false);
    }


    // bettingTurn only decide for which action this bot should take
    // but the amount of money will be specified by Game class
    // return string of action
    // TODO: calculating percent of winning then making a decision
    @Override
    public String bettingTurn(List<String> availableOption) {

//        int random = 0;
//        random = (int)(Math.random() * (availableOption.size()-1));
//        availableOption.get(random);
//        return availableOption.get(random);

        return "fold";
    }

}
