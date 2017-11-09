import java.awt.*;

public class Card implements Comparable<Card>{
    private String cardLetter;
    private String cardType;
    private Point position;
//    private int cardValue;
//    private int cardOrder;

    public Card(String cardLetter, String cardType) {
        this.cardLetter = cardLetter;
        this.cardType = cardType;


    }

//    public Card(String cardLetter, String cardType, int cardValue, int cardOrder) {
//        this.cardLetter = cardLetter;
//        this.cardType = cardType;
//        this.cardValue = cardValue;
//        this.cardOrder = cardOrder;
//    }

    public String getCardLetter() {
        return cardLetter;
    }

    public String getCardType() {
        return cardType;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    //    public int getCardValue() {
//        return cardValue;
//    }
//
//    public int getCardOrder() {
//        return cardOrder;
//    }

    @Override
    public String toString() {
        return "Letter " + cardLetter + " : Type " + cardType;
    }

    public int compareTo(Card c) {
        return 0;
    }
}
