import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Bot class extends from Player class to implement
 * it own bettingTurn and drawingTurn algorithm
 *
 * Created by TC group, 6 December 2017
 */
public class Bot extends Player
{
    /**
     * Constructor to create instance of player
     * @param name      string of name
     * @param seat      seat number
     * @param position  position of seat number
     * @param balance   intial balance
     * @param active    status of player
     */
    public Bot(String name, int seat, Point position, int balance, boolean active)
    {
        // Set initial value
        this.name = name;
        this.seat = seat;
        this.position = position;
        this.balance = balance;
        this.active = active;

        // Load .fxml file to create GUI represents this player
        VBox vBox = new VBox();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/Player.fxml"));
        fxmlLoader.setRoot(vBox);
        fxmlLoader.setController(this);

        try
        {
            fxmlLoader.load();
        }
        catch (IOException exception)
        {
            throw new RuntimeException(exception);
        }

        Game.getPane().getChildren().add(vBox);
        vBox.setLayoutX(position.getX() - (180/2));
        vBox.setLayoutY(position.getY());

        // Set GUI component's value
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

    /**
     * bettingTurn only decide for which action this bot should take
     * but the amount of money will be specified by Game class
     *
     * @param availableOption  selectable option
     * @return string of action taken
     */
    @Override
    public String bettingTurn(List<String> availableOption)
    {
        // update score first
        patternOnhand();

        Random rn = new Random();

        // random between 0 and 1, be an index of return string
        int randomNum =  rn.nextInt(2);
        if (score > 0)
        {
            // Decide to choose between two options
            if (availableOption.contains("check") && (availableOption.contains("bet")))
            {
                randomNum = rn.nextInt(11) + 1;     // random between 1 and 10
                if (randomNum < score + 5)                // add possibility of choosing bet
                    return "bet";
                else
                    return "check";
            }
            else if (availableOption.contains("check") && (availableOption.contains("raise")))
            {
                randomNum = rn.nextInt(11) + 1;     // random between 1 and 10
                if (randomNum < score)
                    return "raise";
                else
                    return "check";
            }
            else if (availableOption.contains("call") && (availableOption.contains("raise")))
            {
                randomNum = rn.nextInt(11) + 1;     // random between 1 and 10
                if (randomNum < score)
                    return "raise";
                else
                    return "call";
            }
        }

        // getting here means score equal to zero, return random without third option
        return availableOption.get(randomNum);
    }

    /**
     * Decide to remove which card in player's hand
     *
     * @return list of GUI components which will be discarded
     */
    @Override
    public ArrayList<HBox> drawingTurn()
    {
        ArrayList<HBox> selectedCards = new ArrayList<>();
        ArrayList<Card> rmvcard = new ArrayList<Card>();
        int i = 0;
        if (score == 0)                  // has nothing
        {
            for (i = 1; i < 5; i++)
            {
                rmvcard.add(cardOnHand.getCard(i));
            }
        }
        if (score == 1 || score == 3)    // pair or 3 of kind
        {
            for (i = 0; i < 5; i++)
            {
                if(cardOnHand.getCard(i).getCardOrder() != pair)
                {
                    rmvcard.add(cardOnHand.getCard(i));
                }
            }
        }
        if (score == 2)                 //two pairs
        {
            for (i = 0; i < 5; i++)
            {
                if(cardOnHand.getCard(i).getCardOrder() != pair1 ||cardOnHand.getCard(i).getCardOrder() != pair2 )
                {
                    rmvcard.add(cardOnHand.getCard(i));
                }
            }
        }

        // set return value
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
