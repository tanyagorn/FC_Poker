import java.util.ArrayList;

public class WinnerFinder
{
    private static ArrayList<Player> winners = new ArrayList<Player>();

    public static ArrayList<Player> findWinner(ArrayList<Player> players)
    {
        winners.clear();
        /**
         * After all player select the card sort card on hand of each player
         */
        for (int i = 0; i < players.size(); i++)
        {
            players.get(i).cardOnHand.sortCard();
        }

        /**
         * Find the on hand pattern of each player
         */
        for (int i = 0; i < players.size(); i++)
        {
            players.get(i).patternOnhand();
        }

        int maxweight = -1;
        /**
         * Get the maximum weight of the pattern among all player
         */
        for (int i = 0; i < players.size(); i++)
        {
            if (players.get(i).getScore() > maxweight)
            {
                maxweight = players.get(i).getScore();
            }
        }
        System.out.println("weight : " + maxweight);
        /**
         * Add each player who have weight equal to maximum weight to winner player collection
         */
        for (int i = 0; i < players.size(); i++)
        {
            if (players.get(i).getScore() == maxweight)
            {
                winners.add(players.get(i));
            }
        }
        /**
         * Compare winner of each on hand patter for each player in winner collection to ger the real winner
         */
        if (maxweight == 10 || maxweight == 5)//Straight flush , Straight fixed
        {
            int Startcard = -1;
            for (int i = 0; i < winners.size(); i++)
            {
                if (winners.get(i).cardOnHand.getOrder(0) > Startcard)
                {
                    Startcard = winners.get(i).cardOnHand.getOrder(0);
                }
            }
            for (int i = winners.size() - 1; i >= 0; i--)
            {
                if (winners.get(i).cardOnHand.getOrder(0) < Startcard)
                {
                    winners.remove(i);
                }
            }
        }
        else if (maxweight == 8)//4 of kind fixed
        {
            int Startcard = -1;
            for (int i = 0; i < winners.size(); i++)
            {
                if (winners.get(i).cardOnHand.getOrder(1) > Startcard)
                {
                    Startcard = winners.get(i).cardOnHand.getOrder(1);
                }
            }
            for (int i = winners.size() - 1; i >= 0; i--)
            {
                if (winners.get(i).cardOnHand.getOrder(1) < Startcard)
                {
                    winners.remove(i);
                }
            }
        }
        else if (maxweight == 7 || maxweight == 3)//Full house & 3 of kind fixed
        {
            int Startcard = 0;
            for (int i = 0; i < winners.size(); i++)
            {
                if (winners.get(i).cardOnHand.getOrder(2) > Startcard)
                {
                    Startcard = winners.get(i).cardOnHand.getOrder(2);
                }
            }
            for (int i = winners.size() - 1; i >= 0; i--)
            {
                if (winners.get(i).cardOnHand.getOrder(2) < Startcard)
                {
                    winners.remove(i);
                }
            }
        }
        else if (maxweight == 0 || maxweight == 6)//flush or nothing fixed
        {

            for (int i = 0; i < 5; i++)
            {
                int card = -1;
                for (int j = winners.size() - 1; j >= 0; j--)
                {
                    if (winners.get(j).cardOnHand.getOrder(i) > card)
                    {
                        card = winners.get(j).cardOnHand.getOrder(i);
                    }
                }
                for (int j = winners.size() - 1; j >= 0; j--)
                {
                    if (winners.get(j).cardOnHand.getOrder(i) < card)
                    {
                        winners.remove(j);
                    }
                }
            }
        }
        else if (maxweight == 2)//two pair fixed
        {
            int firstpair = -1;
            int secondpair = -1;
            for (int i = 0; i < winners.size(); i++)
            {
                if (winners.get(i).getfirstpair() > firstpair)
                {
                    firstpair = winners.get(i).getfirstpair();
                }
            }
            for (int i = winners.size() - 1; i >= 0; i--)
            {
                if (winners.get(i).getfirstpair() < firstpair)
                {
                    winners.remove(i);
                }
            }
            if (winners.size() > 1)
            {
                for (int i = 0; i < winners.size(); i++)
                {
                    if (winners.get(i).getsecondpair() > secondpair)
                    {
                        secondpair = winners.get(i).getsecondpair();
                    }
                }
                for (int i = winners.size() - 1; i >= 0; i--)
                {
                    if (winners.get(i).getsecondpair() < secondpair)
                    {
                        winners.remove(i);
                    }
                }
                if (winners.size() > 1)
                {
                    int lastcard = -1;
                    for (int i = 0; i < 5; i++)
                    {
                        for (Player winner : winners) {
                            if (winner.cardOnHand.getOrder(i) != firstpair && winner.cardOnHand.getOrder(i) != secondpair) {
                                if (winner.cardOnHand.getOrder(i) > lastcard) {
                                    lastcard = winner.cardOnHand.getOrder(i);
                                }
                            }
                        }
                    }
                    for (int i = 0; i < 5; i++)
                    {
                        for (int j = winners.size() - 1; j >= 0; j--)
                        {
                            if (winners.get(j).cardOnHand.getOrder(i) != firstpair && winners.get(j).cardOnHand.getOrder(i) != secondpair)
                            {
                                if (winners.get(j).cardOnHand.getOrder(i) < lastcard)
                                {
                                    winners.remove(j);
                                }
                            }
                        }
                    }
                }
            }
        }
        else if (maxweight == 1)//pair fixed
        {

            //System.out.println("Winner size before is : "+winners.size());

            int pair = -1;
            for (Player winner : winners) {
                if (winner.getpair() > pair) {
                    pair = winner.getpair();
                }
            }
            for (int i = winners.size() - 1; i >= 0; i--)
            {
                if (winners.get(i).getpair() < pair)
                {
                    winners.remove(i);
                }
            }
            if (winners.size() > 1)
            {
                for (int i = 0; i < 5; i++)
                {
                    int lastcard = -1;
                    for (int j = winners.size() - 1; j >= 0; j--)
                    {
                        if (winners.get(j).cardOnHand.getOrder(i) != pair)
                        {
                            if (winners.get(j).cardOnHand.getOrder(i) > lastcard)
                            {
                                lastcard = winners.get(j).cardOnHand.getOrder(i);
                            }
                        }
                    }
                    for (int j = winners.size() - 1; j >= 0; j--)
                    {
                        if (winners.get(j).cardOnHand.getOrder(i) != pair)
                        {
                            if (winners.get(j).cardOnHand.getOrder(i) < lastcard)
                            {
                                winners.remove(j);
                            }
                        }
                    }
                }
            }
        }
        return winners;
    }
}
