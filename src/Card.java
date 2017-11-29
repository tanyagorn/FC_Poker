import java.awt.*;

public class Card implements Comparable<Card>{
    private String cardLetter;
    private String cardType;
    private Point position;

    public Card(String cardLetter, String cardType) {
        this.cardLetter = cardLetter;
        this.cardType = cardType;
    }

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

    @Override
    public String toString() {
        return "Letter " + cardLetter + " : Type " + cardType;
    }

    public int compareTo(Card c) {
        return 0;
    }
}
