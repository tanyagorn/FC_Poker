import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class CardOnHand {
    protected ArrayList<Card> cards = new ArrayList<Card>();

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
    public void sortCard()
    {
        Collections.sort(this.cards, new Comparator<Card>(){
            public int compare(Card c1, Card c2)
            {
                return c2.getCardOrder() - c1.getCardOrder();
            }
        });
    }
    public int getorder(int card)
    {
        return cards.get(card).getCardOrder();
    }
}
