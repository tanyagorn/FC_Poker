import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * CardOnHand represent collection of cards which
 * belonged to the player
 *
 * Created by TC group, 6 December 2017
 */
public class CardOnHand
{
    /** all belonging cards */
    protected ArrayList<Card> cards = new ArrayList<Card>();

    /**
     * add card to collection
     * @param card  card to be added
     */
    public void addCard(Card card)
    {
        cards.add(card);
    }

    /**
     * remove card from collection
     * @param card  card to be removed
     */
    public void removeCard(Card card)
    {
        cards.remove(card);
    }

    /**
     * get object of card from specified index
     * @param index    index of requested card
     * @return requested card
     */
    public Card getCard(int index)
    {
        return cards.get(index);
    }

    /**
     * get all belonging cards in collection
     * @return collection of cards
     */
    public ArrayList<Card> getCards()
    {
        return cards;
    }

    /**
     * sort card by order
     */
    public void sortCard()
    {
        Collections.sort(this.cards, new Comparator<Card>()
        {
            public int compare(Card c1, Card c2)
            {
                return c2.getCardOrder() - c1.getCardOrder();
            }
        });
    }

    /**
     * get order of requested card
     * @param card   index of requested card
     * @return order of card
     */
    public int getOrder(int card)
    {
        return cards.get(card).getCardOrder();
    }
}
