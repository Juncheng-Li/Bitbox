package unimelb.bitbox;

public class ThreadDemo extends Thread
{
    private Thread t;
    private String threadName;

    public ThreadDemo (String name)
    {
        threadName = name;
        System.out.println("Creating " + threadName + "thread");
    }

    public void run()
    {
        System.out.println("Running " + threadName + "thread");

        try
        {
            for (int i = 4; i >= 0; i--)
            {
                System.out.println("Thread " + threadName + "," + i);
                Thread.sleep(2000);
            }
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        System.out.println(threadName + " exiting...");
    }

    public void start()
    {
        System.out.println(threadName + "Start running -----");
        if (t == null)
        {
            t = new Thread(this, threadName);
            t.start();
        }
    }

    public static void main(String[] args)
    {
        ThreadDemo iron = new ThreadDemo("Iron man");
        ThreadDemo cap = new ThreadDemo("Cap");

        iron.start();
        cap.start();
    }
}
