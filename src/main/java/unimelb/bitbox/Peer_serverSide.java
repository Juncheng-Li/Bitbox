package unimelb.bitbox;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ServerSocketFactory;
import java.io.*;
import java.net.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;

import unimelb.bitbox.util.*;

import java.nio.file.Files;

public class Peer_serverSide extends Thread
{
    private int i = 0;
    private Socket clientSocket = null;
    private String clientMsg = null;
    private JSONObject command = null;
    private ServerMain f;
    private Timer timer = new Timer();
    private socketStorage ss;

    Peer_serverSide(Socket clientSocket, int i, ServerMain f, socketStorage ss)
    {
        this.clientSocket = clientSocket;
        this.i = i;
        this.f = f;
        this.ss = ss;
    }


    public void run()
    {
        System.out.println("Thread: Peer_serverSide for Client-" + i + " started...");
        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
            JSONParser parser = new JSONParser();

            try
            {
                while ((clientMsg = in.readLine()) != null)
                {
                    System.out.println("Before parse: " + clientMsg);
                    command = (JSONObject) parser.parse(clientMsg);
                    System.out.println("(Peer_serverSide)Message from Client " + i + ": " + command.toJSONString());
                    if (command.getClass().getName().equals("org.json.simple.JSONObject"))
                    {
                        if (command.get("command").toString().equals("HANDSHAKE_REQUEST"))
                        {
                            JSONObject hs_res = new JSONObject();
                            JSONObject hostPort = new JSONObject();
                            String ip = Configuration.getConfigurationValue("advertisedName");
                            int port = Integer.parseInt(Configuration.getConfigurationValue("port"));
                            hs_res.put("command", "HANDSHAKE_RESPONSE");
                            hostPort.put("host", ip);
                            hostPort.put("port", port);
                            hs_res.put("hostPort", hostPort);
                            System.out.println("sent: " + hs_res.toJSONString());
                            out.write(hs_res.toJSONString() + "\n");
                            out.flush();

                            timer.schedule(new SyncEvents(f), 0,
                                        Integer.parseInt(Configuration.getConfigurationValue("syncInterval"))*1000);
                        }
                        else
                        {
                            commNProcess command_T = new commNProcess(command, clientSocket, f);
                            command_T.start();
                        }
                    }
                    else
                    {
                        // If not a JSONObject
                        JSONObject reply = new JSONObject();
                        reply.put("command", "INVALID_PROTOCOL");
                        reply.put("message", "message must contain a command field as string");
                        System.out.println("sent: " + reply);
                        out.write(reply.toJSONString() + "\n");
                        out.flush();
                    }
                }
            } catch (SocketException e)
            {
                e.printStackTrace();
                clientSocket.close();
                System.out.println("closed...");
            } catch (ParseException e)
            {
                System.out.println(e);
                e.printStackTrace();
            }
            clientSocket.close();
        } catch (IOException e)
        {
            System.out.println(e);
            e.printStackTrace();
        } finally
        {
            // kill timer
            timer.cancel();
            timer.purge();
            ss.remove(clientSocket);
            // kill serverMain
            System.out.println("Peer - " + i + " disconnected.");
        }
    }


}
