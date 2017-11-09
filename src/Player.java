import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;


import java.awt.*;
import java.io.IOException;

public class Player implements Comparable<Player> {
    protected String name;
    protected int seat;
    protected Point position;
    protected CardOnHand cardOnHand = new CardOnHand();
    protected int balance;
    protected int betBalance;
    protected int score;
    protected boolean active;

    @FXML
    protected Label nameLabel;
    @FXML
    protected Label balanceLabel;

    public Player() {
    }

    public Player(String name, int seat, Point position, int balance, boolean active) {
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

    public String getName() {
        return name;
    }

    public int getSeat() {
        return seat;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public int getBetBalance() {
        return betBalance;
    }

    public void setBetBalance(int betBalance) {
        this.betBalance = betBalance;
    }

    public int getScore() {
        return score;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Point getPosition() {
        return position;
    }

    public void addCardOnHand(Card card) {
        cardOnHand.addCard(card, position, true);
    }

    public void bettingTurn() {
        // enable all button, so that player can't choose anything


//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException ie)
//        {
//            System.out.println("Scanning...");
//        }

    }

    @Override
    public int compareTo(Player comparestu) {
        int compareage=((Player)comparestu).getSeat();

        /* For Ascending order*/
        return this.seat-compareage;

        /* For Descending order do like this */
        //return compareage-this.studentage;
    }
}
