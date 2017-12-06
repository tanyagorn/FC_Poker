/**
 *   Pool holding bet balance and give to the player
 *   with best hand at the end of poker game
 *
 *   Created by TC group, 6 December 2017
 */
public class Pool
{
    /** holding bet balance for all */
    private int pool;

    /**
     * Constructor initialize pool with value of 0
     */
    public Pool()
    {
        this.pool = 0;
    }

    /**
     * get pool
     * @return pool
     */
    public int getPool()
    {
        return pool;
    }

    /**
     * set pool
     * @param pool
     */
    public void setPool(int pool)
    {
        this.pool = pool;
    }
}
