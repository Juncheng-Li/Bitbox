package unimelb.bitbox;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ServerSocketFactory;
import java.io.*;
import java.net.*;
import java.nio.Buffer;
import java.util.Objects;


public class Server extends Thread
{
    private Thread t;
    private String threadName;
    private String ip;
    private int port;
    private int counter = 0;
    private int i = 0;
    private ServerSocket listeningSocket = null;
    private Socket clientSocket = null;
    private String clientMsg = null;


    Server(String threadname, int port, Socket clientSocket, int i)
    {
        this.threadName = threadname;
        this.port = port;
        this.clientSocket = clientSocket;
        this.i = i;
    }
    /*
    public void run()
    {
        System.out.println("Thread: " + threadName + " starting...");
        String clientMsg = null;

        try
        {
            while (true)
            {
                System.out.println("Server listening on port:" + 3000);
                //Accept an incoming client connection request
                clientSocket = listeningSocket.accept(); //This method will block until a connection request is received
                i++;
                System.out.println("Client number " + i + " accepted:");
                //System.out.println("Remote Port: " + clientSocket.getPort());
                //System.out.println("Remote Hostname: " + clientSocket.getInetAddress().getHostName());
                //System.out.println("Local Port: " + clientSocket.getLocalPort());

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));


                //Read the message from the client and reply
                //Notice that no other connection can be accepted and processed until the last line of
                //code of this loop is executed, incoming connections have to wait until the current
                //one is processed unless...we use threads!
                try
                {
                    while ((clientMsg = in.readLine()) != null)
                    {
                        System.out.println("Message from client " + i + ": " + clientMsg);
                        out.write("Server Ack " + clientMsg + "\n");
                        out.flush();
                        System.out.println("Response sent");
                    }
                } catch (SocketException e)
                {
                    System.out.println("closed...");
                }
                clientSocket.close();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }



    }

     */


    public void run()
    {
        System.out.println("Thread: " + threadName + " started...");
        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));


            //Read the message from the client and reply
            //Notice that no other connection can be accepted and processed until the last line of
            //code of this loop is executed, incoming connections have to wait until the current
            //one is processed unless...we use threads!
            try
            {
                while ((clientMsg = in.readLine()) != null)
                {
                    System.out.println("Message from client " + i + ": " + clientMsg);
                    out.write("Server Ack " + clientMsg + "\n");
                    out.flush();
                    System.out.println("Response sent");
                }
            } catch (SocketException e)
            {
                System.out.println("closed...");
            }
            clientSocket.close();
        }catch (IOException e)
        {
            e.printStackTrace();
        }
    }


}
