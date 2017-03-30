package gitcommit.org;

/**
 * Created by mikehoughton on 29/03/2017.
 */
public class StatsDemo {
    private int num = 0;
    @Monitor(writePeriod = 5)
    public void incNum(){

        num++;
        try {
            Thread.sleep((long)(Math.random() * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Monitor(writePeriod = 3)
    public void decNum(){
        num++;
        try {
            Thread.sleep((long)(Math.random() * 2000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    

}
