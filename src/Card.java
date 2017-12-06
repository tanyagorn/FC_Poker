/**
 * Card represent one of 52 cards in the deck
 *
 * Created by TC group, 6 December 2017
 */
public class Card implements Comparable<Card>
{
    /** Letter or number of card ex. A,K,Q,J,10,9 */
    private String cardLetter;

    /** Type of card ex. clubs, hearts */
    private String cardType;

    /** order of card to calculate for winning chance */
    private int cardOrder = 0;

    /**
     * Constructor to create instance of card
     * @param cardLetter   string of letter or number
     * @param cardType     type of card
     * @param cardOrder    order of card
     */
    public Card(String cardLetter, String cardType, int cardOrder)
    {
        this.cardLetter = cardLetter;
        this.cardType = cardType;
        this.cardOrder = cardOrder;
    }

    /**
     * get card letter
     * @return card letter
     */
    public String getCardLetter()
    {
        return cardLetter;
    }

    /**
     * get card type
     * @return card type
     */
    public String getCardType()
    {
        return cardType;
    }

    /**
     * get card order
     * @return card order
     */
    public int getCardOrder()
    {
        return cardOrder;
    }

    /**
     * get readable string from card object
     * @return formatted string
     */
    @Override
    public String toString()
    {
        return "Letter " + cardLetter + " : Type " + cardType;
    }

    /**
     * sort card
     * @param card  card to be compared
     * @return always zero
     */
    public int compareTo(Card card)
    {
        return 0;
    }
}
