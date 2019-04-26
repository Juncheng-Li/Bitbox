package unimelb.bitbox;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.*;


class Client extends Thread
{
    private Thread t;
    private String threadName;
    private String ip;
    private int port;


    Client(String threadName, String ip, int port)
    {
        this.threadName = threadName;
        this.ip = ip;
        this.port = port;
    }

    public void run()
    {
        Socket socket = null;
        try
        {
            socket = new Socket(ip, port);
            System.out.println("Connection established");
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

            String inputStr = "Hello from client";

            //While the user input differs from "exit"


            // Send the input string to the server by writing to the socket output stream
            out.write(inputStr + "\n");
            out.flush();
            System.out.println("Message sent");

            // Receive the reply from the server by reading from the socket input stream
            String received = in.readLine(); // This method blocks until there
            // is something to read from the
            // input stream
            System.out.println("Message received: " + received);



        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            // Close the socket
            if (socket != null)
            {
                try
                {
                    socket.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
