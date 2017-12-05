import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

public class Deck {

    /** Instance of TileManager */
    private static Deck instance = null;

    /** Initial a hundred tiles for Scrabble game */
    private ArrayList<Card> cards = new ArrayList<Card>();

    /**
     * Constructor creates 52 of cards
     * at the beginning of the game.
     */
    private Deck()
    {
        cards.add(new Card("2", "clubs"));
        cards.add(new Card("3", "clubs"));
        cards.add(new Card("4", "clubs"));
        cards.add(new Card("5", "clubs"));
        cards.add(new Card("6", "clubs"));
        cards.add(new Card("7", "clubs"));
        cards.add(new Card("8", "clubs"));
        cards.add(new Card("9", "clubs"));
        cards.add(new Card("10", "clubs"));
        cards.add(new Card("J", "clubs"));
        cards.add(new Card("Q", "clubs"));
        cards.add(new Card("K", "clubs"));
        cards.add(new Card("A", "clubs"));

        cards.add(new Card("2", "diamonds"));
        cards.add(new Card("3", "diamonds"));
        cards.add(new Card("4", "diamonds"));
        cards.add(new Card("5", "diamonds"));
        cards.add(new Card("6", "diamonds"));
        cards.add(new Card("7", "diamonds"));
        cards.add(new Card("8", "diamonds"));
        cards.add(new Card("9", "diamonds"));
        cards.add(new Card("10", "diamonds"));
        cards.add(new Card("J", "diamonds"));
        cards.add(new Card("Q", "diamonds"));
        cards.add(new Card("K", "diamonds"));
        cards.add(new Card("A", "diamonds"));

        cards.add(new Card("2", "hearts"));
        cards.add(new Card("3", "hearts"));
        cards.add(new Card("4", "hearts"));
        cards.add(new Card("5", "hearts"));
        cards.add(new Card("6", "hearts"));
        cards.add(new Card("7", "hearts"));
        cards.add(new Card("8", "hearts"));
        cards.add(new Card("9", "hearts"));
        cards.add(new Card("10", "hearts"));
        cards.add(new Card("J", "hearts"));
        cards.add(new Card("Q", "hearts"));
        cards.add(new Card("K", "hearts"));
        cards.add(new Card("A", "hearts"));

        cards.add(new Card("2", "spades"));
        cards.add(new Card("3", "spades"));
        cards.add(new Card("4", "spades"));
        cards.add(new Card("5", "spades"));
        cards.add(new Card("6", "spades"));
        cards.add(new Card("7", "spades"));
        cards.add(new Card("8", "spades"));
        cards.add(new Card("9", "spades"));
        cards.add(new Card("10", "spades"));
        cards.add(new Card("J", "spades"));
        cards.add(new Card("Q", "spades"));
        cards.add(new Card("K", "spades"));
        cards.add(new Card("A", "spades"));
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

    public void reInitDeck()
    {
        cards.clear();
        cards.add(new Card("2", "clubs"));
        cards.add(new Card("3", "clubs"));
        cards.add(new Card("4", "clubs"));
        cards.add(new Card("5", "clubs"));
        cards.add(new Card("6", "clubs"));
        cards.add(new Card("7", "clubs"));
        cards.add(new Card("8", "clubs"));
        cards.add(new Card("9", "clubs"));
        cards.add(new Card("10", "clubs"));
        cards.add(new Card("J", "clubs"));
        cards.add(new Card("Q", "clubs"));
        cards.add(new Card("K", "clubs"));
        cards.add(new Card("A", "clubs"));

        cards.add(new Card("2", "diamonds"));
        cards.add(new Card("3", "diamonds"));
        cards.add(new Card("4", "diamonds"));
        cards.add(new Card("5", "diamonds"));
        cards.add(new Card("6", "diamonds"));
        cards.add(new Card("7", "diamonds"));
        cards.add(new Card("8", "diamonds"));
        cards.add(new Card("9", "diamonds"));
        cards.add(new Card("10", "diamonds"));
        cards.add(new Card("J", "diamonds"));
        cards.add(new Card("Q", "diamonds"));
        cards.add(new Card("K", "diamonds"));
        cards.add(new Card("A", "diamonds"));

        cards.add(new Card("2", "hearts"));
        cards.add(new Card("3", "hearts"));
        cards.add(new Card("4", "hearts"));
        cards.add(new Card("5", "hearts"));
        cards.add(new Card("6", "hearts"));
        cards.add(new Card("7", "hearts"));
        cards.add(new Card("8", "hearts"));
        cards.add(new Card("9", "hearts"));
        cards.add(new Card("10", "hearts"));
        cards.add(new Card("J", "hearts"));
        cards.add(new Card("Q", "hearts"));
        cards.add(new Card("K", "hearts"));
        cards.add(new Card("A", "hearts"));

        cards.add(new Card("2", "spades"));
        cards.add(new Card("3", "spades"));
        cards.add(new Card("4", "spades"));
        cards.add(new Card("5", "spades"));
        cards.add(new Card("6", "spades"));
        cards.add(new Card("7", "spades"));
        cards.add(new Card("8", "spades"));
        cards.add(new Card("9", "spades"));
        cards.add(new Card("10", "spades"));
        cards.add(new Card("J", "spades"));
        cards.add(new Card("Q", "spades"));
        cards.add(new Card("K", "spades"));
        cards.add(new Card("A", "spades"));
    }

    public void removeCard(Card card) {
        cards.remove(card);
    }

    // Automatically remove that card from Deck
    public Card getRandomCard() {
        Random rand = new Random();
        int random = rand.nextInt((cards.size()-1) + 1);
        Card randomCard = cards.get(random);
        removeCard(randomCard);
        return randomCard;
    }
}
