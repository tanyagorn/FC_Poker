import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
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

        nameLabel.setText(name);
        balanceLabel.setText(Integer.toString(balance));
        betTag = new Label();
        betTag.setLayoutX(Game.getBettingTagPosition(this).getX());
        betTag.setLayoutY(Game.getBettingTagPosition(this).getY());
        amountTag = new Label();
        amountTag.setLayoutX(Game.getBettingAmountPosition(this).getX());
        amountTag.setLayoutY(Game.getBettingAmountPosition(this).getY());
        Game.getPane().getChildren().addAll(betTag, amountTag);
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
        patternOnhand();        // update score first
        System.out.println("====================================================");
        System.out.println(name + " has score = " + score);
        System.out.println("====================================================");

//        System.out.println("available opt = " + availableOption);
        int random = 0;
        random = (int)(Math.random() * (availableOption.size()-1));
        availableOption.get(random);
        return availableOption.get(random);
    }

    @Override
    public ArrayList<HBox> drawingTurn() {
        ArrayList<HBox> selectedCards = new ArrayList<>();
        ArrayList<Card> rmvcard = new ArrayList<Card>();
        int i = 0;
        if(score == 0)//Mean has nothing
        {
            for( i = 1;i<5;i++) {
                rmvcard.add(cardOnHand.getCard(i));
                System.out.println("+-+-+-+-+-+--+--+--++-+--+---++-+--+-+--+-+--+--+--+--+-");
                System.out.println("Remove card : "+cardOnHand.getCard(i));
                System.out.println("+-+-+-+-+-+--+--+--++-+--+---++-+--+-+--+-+--+--+--+--+-");
            }
        }
        if(score == 1 || score == 3)//Mean pair or 3 of kind
        {
            for(i = 0;i<5;i++)
            {
                if(cardOnHand.getCard(i).getCardOrder() != pair)
                {
                    rmvcard.add(cardOnHand.getCard(i));
                }
            }
        }
        if(score == 2)//mean two pair
        {
            for(i = 0;i<5;i++)
            {
                if(cardOnHand.getCard(i).getCardOrder() != pair1 ||cardOnHand.getCard(i).getCardOrder() != pair2 )
                {
                    rmvcard.add(cardOnHand.getCard(i));
                }
            }
        }

        String matchCardID = "";
        for (Card card : rmvcard)
        {
            matchCardID = card.getCardLetter() + "_" + card.getCardType();
            for (HBox hbox : imgCards)
            {
                if (hbox.getId().equals(matchCardID))
                    selectedCards.add(hbox);
            }
        }

        return selectedCards;
    }

}
