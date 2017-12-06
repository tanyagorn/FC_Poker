import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Player class keep information both model and view
 * and provide getter/setter method to keep
 * model and view consistent
 *
 * Created by TC group, 6 December 2017
 */
public class Player implements Comparable<Player>
{
    /** name of player */
    protected String name;

    /** seat number */
    protected int seat;

    /** seat position */
    protected Point position;

    /** cards belonging to this player */
    protected CardOnHand cardOnHand = new CardOnHand();

    /** holding total balance */
    protected int balance;

    /** holding betting balance for each betting turn */
    protected int betBalance;

    /** rate of card which will be used for choosing decision in betting turn */
    protected int score;

    /** holding status of this player */
    protected boolean active;

    /** GUI component; display which decision is taken */
    protected Label betTag;

    /** GUI component; display amount of money */
    protected Label amountTag;

    /** GUI components; holding GUI object represent each card
     * for this player, so we can remove it later */
    protected ArrayList<HBox> imgCards = new ArrayList<HBox>();

    /** GUI components loading from .FXML file */
    @FXML protected VBox nameTag;
    @FXML protected Label nameLabel;
    @FXML protected Label balanceLabel;

    /** temporary variable to be used in making decision for betting */
    protected int pair1 = 0;
    protected int pair2 = 0;
    protected int pair = 0;

    /**
     * Default constructor of player
     * */
    public Player()
    {

    }

    /**
     * Constructor to create instance of player
     * @param name      string of name
     * @param seat      seat number
     * @param position  position of seat number
     * @param balance   intial balance
     * @param active    status of player
     */
    public Player(String name, int seat, Point position, int balance, boolean active)
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
     * get player's name
     * @return name of player
     */
    public String getName()
    {
        return name;
    }

    /**
     * get seat number
     * @return seat number
     */
    public int getSeat()
    {
        return seat;
    }

    /**
     * get balance
     * @return current balance
     */
    public int getBalance()
    {
        return balance;
    }

    /**
     * update balance value
     * @param balance new balance value
     */
    public void setBalance(int balance)
    {
        this.balance = balance;
    }

    /**
     * get bet balance
     * @return bet balance
     */
    public int getBetBalance()
    {
        return betBalance;
    }

    /**
     * update bet balance value
     * @param betBalance new bet balance value
     */
    public void setBetBalance(int betBalance)
    {
        this.betBalance = betBalance;
    }

    /**
     * get score
     * @return rate of player's hand
     */
    public int getScore()
    {
        return score;
    }

    /**
     * get status of player
     * @return status of player
     */
    public boolean isActive()
    {
        return active;
    }

    /**
     * set status of player
     * @param active new status
     */
    public void setActive(boolean active)
    {
        this.active = active;
    }

    /**
     * get position of seat
     * @return seat's position
     */
    public Point getPosition()
    {
        return position;
    }

    /**
     * get belonging cards
     * @return cards own by this player
     */
    public CardOnHand getCardOnHand()
    {
        return cardOnHand;
    }

    /**
     * get spedific card in the collection
     * @param which  index of card
     * @return requested card
     */
    public Card getCard(int which)
    {
        return cardOnHand.getCard(which);
    }

    /**
     * add new card to player
     * @param card  new card
     */
    public void addCardOnHand(Card card)
    {
        cardOnHand.addCard(card);
    }

    /**
     * Change style of name tag when status of player is inactive
     */
    public void setNameTagInactive()
    {
        this.nameTag.setStyle("-fx-background-color: #373737;");
    }

    /**
     * update value in betTag
     * @param display string to be displayed
     */
    public void setBetTag(String display)
    {
        this.betTag.setText(display);
    }

    /**
     * get object of betTag
     * @return object of betTag
     */
    public Label getBetTag()
    {
        return this.betTag;
    }

    /**
     * get object of amountTag
     * @return object of amountTag
     */
    public Label getAmountTag()
    {
        return amountTag;
    }

    /**
     * update balance label
     */
    public void setBalanceLabel()
    {
        this.balanceLabel.setText(Integer.toString(balance));
    }

    /**
     * add GUI of this card, so we can refer to if need to remove
     * @param img GUI of card
     */
    public void addImgCards(HBox img)
    {
        imgCards.add(img);
    }

    /**
     * get GUI cards
     * @return
     */
    public ArrayList<HBox> getImgCards()
    {
        return imgCards;
    }

    /**
     * remove GUI card for specific card
     * @param card GUI card to be removed
     */
    public void removeImgCards(HBox card)
    {
        this.imgCards.remove(card);
    }


    /**
     * Betting turn of actual player will be determined in Game class
     * @param availableOption  selectable option
     * @return selected option
     */
    public String bettingTurn(List<String> availableOption)
    {
        return null;
    }

    /**
     * Drawing turn for actual player will be determined in Game class
     * which is hosting our GUI
     * @return list of GUI of card that must be remove
     */
    public ArrayList<HBox> drawingTurn()
    {
        return null;
    }


    /**
     * Compare player to sort seat position
     * @param compare instance of player to be compared
     * @return sorted value
     */
    @Override
    public int compareTo(Player compare)
    {
        int tempCompare = ((Player)compare).getSeat();

        /* For Ascending order*/
        return this.seat-tempCompare;
    }

    /**
     *  Add check pattern
     */
    public int getpair()
    {
        return pair;
    }
    public int getfirstpair()
    {
        return pair1;
    }
    public int getsecondpair()
    {
        return pair2;
    }
    public int checkStraight()
    {
        int i = 0;
        int countcheck = 0;
        for(i = 1; i<=4; i++)
        {
            if(cardOnHand.cards.get(i-1).getCardOrder() - cardOnHand.cards.get(i).getCardOrder() == 1)
            {
                countcheck++;
            }
        }
        if(countcheck == 4)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    /**
     *   Check flush on hand if all the card on hand has the same suit
     */
    public int checkflush()
    {
        int i = 0;
        int check = 0;
        String firstsuit = cardOnHand.cards.get(0).getCardType();
        for(i = 1; i<=4; i++)
        {
            if(cardOnHand.cards.get(i).getCardType() == firstsuit)
            {
                check++;
            }
        }
        if(check == 4)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }
    /**
     *   Check fullhouse if has 3 kind and one pair
     */
    public int checkfullhouse()
    {
        int countnumcard = 0;
        int countnumcard2 = 0;
        int i = 0;
        int j = 0;
        for(i = 1; i<=4; i++)
        {
            if(cardOnHand.cards.get(i-1).getCardOrder() == cardOnHand.cards.get(i).getCardOrder())
            {
                countnumcard++;
            }
            else
            {

                break;
            }
        }
        if(countnumcard > 0)
        {
            for(j = i; j<=4; j++)
            {
                if(cardOnHand.cards.get(j-1).getCardOrder() == cardOnHand.cards.get(j).getCardOrder())
                {
                    countnumcard2++;
                }

            }
        }
        if(countnumcard == 2 && countnumcard2 == 1)
        {
            return 1;
        }
        else if(countnumcard == 1 && countnumcard2 == 2)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    /**
     *   Check card on hand if has the same order for many time
     */
    public int checknofkind()
    {
        int i = 0;
        int countnumcard = 0;
        int sameorder = 0;
        for(i = 1; i<=4; i++)
        {
            if(countnumcard == 0)
            {
                if(cardOnHand.cards.get(i-1).getCardOrder() == cardOnHand.cards.get(i).getCardOrder())
                {
                    sameorder = cardOnHand.cards.get(i).getCardOrder();
                    pair = sameorder;
                    countnumcard++;
                }
            }
            else
            {
                if(cardOnHand.cards.get(i).getCardOrder() == sameorder)
                {
                    countnumcard++;
                }
            }


        }
        return countnumcard;
    }

    /**
     *   Check two pair
     */
    public int checktwopair()
    {
        int i = 0;
        int j = 0;
        int sameorder = 0;
        int sameorder2 = 0;
        int countnumcard = 0;
        int countnumcard2 = 0;
        for(i = 1; i<=4; i++)
        {
            if(countnumcard == 0)
            {
                if(cardOnHand.cards.get(i-1).getCardOrder() == cardOnHand.cards.get(i).getCardOrder())
                {

                    sameorder = cardOnHand.cards.get(i).getCardOrder();
                    countnumcard++;
                }
            }
            else
            {
                if(cardOnHand.cards.get(i).getCardOrder() == sameorder)
                {
                    countnumcard++;
                }
                else
                {
                    break;
                }
            }
        }
        if(countnumcard > 0)
        {
            for(j = i; j<=4; j++)
            {
                if(countnumcard2 == 0)
                {
                    if(cardOnHand.cards.get(j-1).getCardOrder() == cardOnHand.cards.get(j).getCardOrder())
                    {
                        sameorder2 = cardOnHand.cards.get(j).getCardOrder();
                        countnumcard2++;
                    }
                }
                else
                {
                    if(cardOnHand.cards.get(j).getCardOrder() == sameorder2)
                    {
                        countnumcard2++;
                    }
                }
            }
        }
        if(countnumcard == 1 && countnumcard2 == 1)
        {
            pair1 = sameorder;
            pair2 = sameorder2;
            return 1;
        }
        else
        {
            return 0;
        }
    }

    /**
     *   Check lowStraight
     */
    public int checklowStraight()
    {
        int i = 0;
        int countcheck = 0;
        for(i = 2; i<=4; i++)
        {
            if(cardOnHand.cards.get(i-1).getCardOrder() - cardOnHand.cards.get(i).getCardOrder() == 1)
            {
                countcheck++;
            }
        }
        if(countcheck == 3)
        {
            if(cardOnHand.cards.get(0).getCardOrder() == 13)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
        else
        {
            return 0;
        }
    }

    /**
     * Check pattern of the card on player hand and update score for each player
     */
    public void patternOnhand()
    {
        int straight = 0;
        int flush = 0;
        int fullhouse = 0;
        int Nkind = 0;
        int lowstraight = 0;
        int twopair = 0;
        straight = checkStraight();
        flush = checkflush();
        fullhouse = checkfullhouse();
        Nkind = checknofkind();
        lowstraight = checklowStraight();
        twopair = checktwopair();
        if(straight == 1 && flush == 1)
        {
            score = 10;
        }
        else if(lowstraight == 1 && flush == 1)
        {
            score = 9;
        }
        else if(Nkind+1 == 4)
        {
            score = 8;
        }
        else if(fullhouse == 1)
        {
            score = 7;
        }
        else if(flush == 1)
        {
            score = 6;
        }
        else if(straight == 1)
        {
            score = 5;
        }
        else if(lowstraight == 1)
        {
            score = 4;
        }
        else if(Nkind+1 == 3)
        {
            score = 3;
        }
        else if(twopair == 1)
        {
            score = 2;
        }
        else if(Nkind+1 == 2)
        {
            score = 1;
        }
        else
        {
            score = 0;
        }

    }
}
