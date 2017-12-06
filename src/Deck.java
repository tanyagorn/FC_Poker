import java.util.ArrayList;
import java.util.Random;

/**
 *   Singleton of Deck, holding 52 cards to be used in poker game
 *
 *   Created by TC group, 6 December 2017
 */
public class Deck
{
    /** Instance of Deck */
    private static Deck instance = null;

    /** 52 cards within the deck */
    private ArrayList<Card> cards = new ArrayList<Card>();

    /**
     * Constructor creates 52 of cards
     * at the beginning of the game.
     */
    private Deck()
    {
        cards.add(new Card("2", "clubs",1));
        cards.add(new Card("3", "clubs",2));
        cards.add(new Card("4", "clubs",3));
        cards.add(new Card("5", "clubs",4));
        cards.add(new Card("6", "clubs",5));
        cards.add(new Card("7", "clubs",6));
        cards.add(new Card("8", "clubs",7));
        cards.add(new Card("9", "clubs",8));
        cards.add(new Card("10", "clubs",9));
        cards.add(new Card("J", "clubs",10));
        cards.add(new Card("Q", "clubs",11));
        cards.add(new Card("K", "clubs",12));
        cards.add(new Card("A", "clubs",13));

        cards.add(new Card("2", "diamonds",1));
        cards.add(new Card("3", "diamonds",2));
        cards.add(new Card("4", "diamonds",3));
        cards.add(new Card("5", "diamonds",4));
        cards.add(new Card("6", "diamonds",5));
        cards.add(new Card("7", "diamonds",6));
        cards.add(new Card("8", "diamonds",7));
        cards.add(new Card("9", "diamonds",8));
        cards.add(new Card("10", "diamonds",9));
        cards.add(new Card("J", "diamonds",10));
        cards.add(new Card("Q", "diamonds",11));
        cards.add(new Card("K", "diamonds",12));
        cards.add(new Card("A", "diamonds",13));

        cards.add(new Card("2", "hearts",1));
        cards.add(new Card("3", "hearts",2));
        cards.add(new Card("4", "hearts",3));
        cards.add(new Card("5", "hearts",4));
        cards.add(new Card("6", "hearts",5));
        cards.add(new Card("7", "hearts",6));
        cards.add(new Card("8", "hearts",7));
        cards.add(new Card("9", "hearts",8));
        cards.add(new Card("10", "hearts",9));
        cards.add(new Card("J", "hearts",10));
        cards.add(new Card("Q", "hearts",11));
        cards.add(new Card("K", "hearts",12));
        cards.add(new Card("A", "hearts",13));

        cards.add(new Card("2", "spades",1));
        cards.add(new Card("3", "spades",2));
        cards.add(new Card("4", "spades",3));
        cards.add(new Card("5", "spades",4));
        cards.add(new Card("6", "spades",5));
        cards.add(new Card("7", "spades",6));
        cards.add(new Card("8", "spades",7));
        cards.add(new Card("9", "spades",8));
        cards.add(new Card("10", "spades",9));
        cards.add(new Card("J", "spades",10));
        cards.add(new Card("Q", "spades",11));
        cards.add(new Card("K", "spades",12));
        cards.add(new Card("A", "spades",13));
    }

    /**
     * To get an instance for this Deck.
     * @return an instance for this Deck
     */
    public static Deck getInstance()
    {
        if (instance == null)
        {
            instance = new Deck();
        }

        return instance;
    }

    /**
     *  Re-initialize deck of card
     */
    public void reInitDeck()
    {
        cards.clear();
        cards.add(new Card("2", "clubs",1));
        cards.add(new Card("3", "clubs",2));
        cards.add(new Card("4", "clubs",3));
        cards.add(new Card("5", "clubs",4));
        cards.add(new Card("6", "clubs",5));
        cards.add(new Card("7", "clubs",6));
        cards.add(new Card("8", "clubs",7));
        cards.add(new Card("9", "clubs",8));
        cards.add(new Card("10", "clubs",9));
        cards.add(new Card("J", "clubs",10));
        cards.add(new Card("Q", "clubs",11));
        cards.add(new Card("K", "clubs",12));
        cards.add(new Card("A", "clubs",13));

        cards.add(new Card("2", "diamonds",1));
        cards.add(new Card("3", "diamonds",2));
        cards.add(new Card("4", "diamonds",3));
        cards.add(new Card("5", "diamonds",4));
        cards.add(new Card("6", "diamonds",5));
        cards.add(new Card("7", "diamonds",6));
        cards.add(new Card("8", "diamonds",7));
        cards.add(new Card("9", "diamonds",8));
        cards.add(new Card("10", "diamonds",9));
        cards.add(new Card("J", "diamonds",10));
        cards.add(new Card("Q", "diamonds",11));
        cards.add(new Card("K", "diamonds",12));
        cards.add(new Card("A", "diamonds",13));

        cards.add(new Card("2", "hearts",1));
        cards.add(new Card("3", "hearts",2));
        cards.add(new Card("4", "hearts",3));
        cards.add(new Card("5", "hearts",4));
        cards.add(new Card("6", "hearts",5));
        cards.add(new Card("7", "hearts",6));
        cards.add(new Card("8", "hearts",7));
        cards.add(new Card("9", "hearts",8));
        cards.add(new Card("10", "hearts",9));
        cards.add(new Card("J", "hearts",10));
        cards.add(new Card("Q", "hearts",11));
        cards.add(new Card("K", "hearts",12));
        cards.add(new Card("A", "hearts",13));

        cards.add(new Card("2", "spades",1));
        cards.add(new Card("3", "spades",2));
        cards.add(new Card("4", "spades",3));
        cards.add(new Card("5", "spades",4));
        cards.add(new Card("6", "spades",5));
        cards.add(new Card("7", "spades",6));
        cards.add(new Card("8", "spades",7));
        cards.add(new Card("9", "spades",8));
        cards.add(new Card("10", "spades",9));
        cards.add(new Card("J", "spades",10));
        cards.add(new Card("Q", "spades",11));
        cards.add(new Card("K", "spades",12));
        cards.add(new Card("A", "spades",13));
    }

    /**
     * remove card from deck
     * @param  card which card
     */
    public void removeCard(Card card)
    {
        cards.remove(card);
    }

    /**
     * random card for user, then automatically remove that card from Deck
     * @return random card from deck
     */
    public Card getRandomCard()
    {
        Random rand = new Random();
        int random = rand.nextInt((cards.size()-1));
        Card randomCard = cards.get(random);
        removeCard(randomCard);
        return randomCard;
    }
}
