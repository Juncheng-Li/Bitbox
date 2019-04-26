package unimelb.bitbox;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ServerSocketFactory;
import java.io.*;
import java.net.*;
import java.nio.Buffer;
import java.util.Objects;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import javax.net.ServerSocketFactory;

import unimelb.bitbox.util.Document;

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
    private JSONObject command = null;
    private Document Msg = null;


    Server(String threadname, int port, Socket clientSocket, int i)
    {
        this.threadName = threadname;
        this.port = port;
        this.clientSocket = clientSocket;
        this.i = i;
    }


    public void run()
    {
        System.out.println("Thread: " + threadName + "-" + i + " started...");
        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
            JSONParser parser = new JSONParser();
            //Read the message from the client and reply
            //Notice that no other connection can be accepted and processed until the last line of
            //code of this loop is executed, incoming connections have to wait until the current
            //one is processed unless...we use threads!
            try
            {
                while ((clientMsg = in.readLine()) != null)
                {
                    command = (JSONObject) parser.parse(clientMsg);
                    System.out.println("Message from client " + i + ": " + command.toJSONString());
                    //out.write("Server Ack " + command.toJSONString() + "\n");
                    //out.flush();
                    //System.out.println("Reply sent");
                    //Execute command
                    doCommand(command, out);
                }
            } catch (SocketException e)
            {
                System.out.println("closed...");
            } catch (ParseException e)
            {
                e.printStackTrace();
            }
            clientSocket.close();
        }catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void doCommand(JSONObject command, BufferedWriter out)
    {
        if (command.get("command").equals("HANDSHAKE_REQUEST"))
        {
            JSONObject hs_res = new JSONObject();
            hs_res.put("command","HANDSHAKE_RESPONSE");
            JSONObject hostPort = new JSONObject();
            hostPort.put("host","localhost");
            hostPort.put("port",port);
            hs_res.put("hostPort",hostPort);
            try
            {
                out.write(hs_res.toJSONString());
                out.flush();
                System.out.println("Respond flushed");
            }catch (IOException e)
            {
                e.printStackTrace();
            }

        }
    }


}
