import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Player implements Comparable<Player> {
    protected String name;
    protected int seat;
    protected Point position;
    protected CardOnHand cardOnHand = new CardOnHand();
    protected int balance;
    protected int betBalance;
    protected int score;                // to choose decision
    protected boolean active;
    protected Label betTag;             // string
    protected Label amountTag;          // amount of betting
    protected ArrayList<HBox> imgCards = new ArrayList<HBox>();


    @FXML protected VBox nameTag;
    @FXML protected Label nameLabel;
    @FXML protected Label balanceLabel;

    protected int pair1 = 0;
    protected int pair2 = 0;
    protected int pair = 0;
    
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
        betTag = new Label();
        betTag.setLayoutX(Game.getBettingTagPosition(this).getX());
        betTag.setLayoutY(Game.getBettingTagPosition(this).getY());

        amountTag = new Label();
        amountTag.setLayoutX(Game.getBettingAmountPosition(this).getX());
        amountTag.setLayoutY(Game.getBettingAmountPosition(this).getY());
        Game.getPane().getChildren().addAll(betTag, amountTag);

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

    public CardOnHand getCardOnHand() {
        return cardOnHand;
    }

    public Card getCard(int which)
    {
        return cardOnHand.getCard(which);
    }

    public void addCardOnHand(Card card) {
        cardOnHand.addCard(card, position, true);
    }

    // bettingTurn of actual player will be determined in Game class
    public String bettingTurn(List<String> availableOption) {
        return null;
    }

    public void setNameTagInactive() {
        this.nameTag.setStyle("-fx-background-color: #373737;");
    }

    public void setBetTag(String display)
    {
        this.betTag.setText(display);
    }

    public Label getBetTag()
    {
        return this.betTag;
    }

    public Label getAmountTag() {
        return amountTag;
    }

    public void setBalanceLabel() {
        this.balanceLabel.setText(Integer.toString(balance));
        System.out.println("SET BALANCE LABEL for " + name + " TO: " + balance);
    }

    public void addImgCards(HBox img)
    {
        imgCards.add(img);
    }

    public ArrayList<HBox> getImgCards() {
        return imgCards;
    }

    public void removeImgCards(HBox card)
    {
        this.imgCards.remove(card);
    }

    // drawingTurn of actual player will be determined in Game class
    public ArrayList<HBox> drawingTurn()
    {
        return null;
    }

    @Override
    public int compareTo(Player comparestu) {
        int compareage=((Player)comparestu).getSeat();

        /* For Ascending order*/
        return this.seat-compareage;

        /* For Descending order do like this */
        //return compareage-this.studentage;
    }
    /**
     *  Add check pattern 200 line ggez
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
