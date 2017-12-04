import java.awt.*;
import java.util.ArrayList;


public class CardOnHand {
    private ArrayList<Card> cards = new ArrayList<Card>();

    public void addCard(Card card, Point position, boolean visibility) {
        cards.add(card);
    }

    public void removeCard(Card card) {
        cards.remove(card);
    }

    public void printAll() {
        for (Card card : cards) {
            System.out.println(card.toString());
        }
    }

    public Card getCard(int index) {
        return cards.get(index);
    }

    public ArrayList<Card> getCards() {
        return cards;
    }
}
