package unimelb.bitbox;

public class ThreadDemo extends Thread
{
    private Thread t;
    private String threadName;


    public void run()
    {
        //System.out.println("Running " + threadName + "thread");

        for (int i = 44; i >= 0; i--)
        {
            System.out.println("Thread print" + "," + i);
            try
            {
                Thread.sleep(1000);

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        System.out.println(" exiting...");
    }

    public static void main(String[] args)
    {
        ThreadDemo iron = new ThreadDemo();
        ThreadDemo cap = new ThreadDemo();

        iron.start();

        try
        {
            iron.join();

        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        cap.start();

    }
}
