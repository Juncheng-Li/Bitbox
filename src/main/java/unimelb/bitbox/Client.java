package unimelb.bitbox;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import unimelb.bitbox.util.*;

import java.io.*;
import java.net.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Timer;


class Client extends Thread
{
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private ServerMain f;
    private String host;
    private int port;

    Client(String host, int port) throws IOException, NoSuchAlgorithmException
    {

        this.socket = new Socket(host, port);
        System.out.println("Connection established");
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        this.f = new ServerMain(socket);
    }

    public void run()
    {
        try
        {
            // Handshake - fixed!
            JSONObject hs = new JSONObject();
            JSONObject hostPort = new JSONObject();
            hostPort.put("host", Configuration.getConfigurationValue("advertisedName"));
            hostPort.put("port", Configuration.getConfigurationValue("port"));
            hs.put("command", "HANDSHAKE_REQUEST");
            hs.put("hostPort", hostPort);
            out.write(hs + "\n");
            out.flush();
            System.out.println(hs);


            // Receive incoming reply
            JSONParser parser = new JSONParser();
            String message = null;
            while ((message = in.readLine()) != null)
            {
                JSONObject command = (JSONObject) parser.parse(message);
                System.out.println("(Client)Message from peer server: " + command.toJSONString());

                //Copied from server
                if(command.getClass().getName().equals("org.json.simple.JSONObject"))
                {
                    if (command.get("command").toString().equals("HANDSHAKE_RESPONSE"))
                    {
                        // Synchronizing Events after Handshake!!!
                        //SyncEvents initSync = new SyncEvents(f);
                        //initSync.run();
                        Timer timer = new Timer();
                        timer.schedule(new SyncEvents(f), 0, Integer.parseInt(Configuration.getConfigurationValue("syncInterval")) * 1000);
                    }
                    else if (command.get("command").toString().equals("CONNECTION_REFUSED"))
                    {
                        System.out.println("Server maximum connection reached");
                        break;
                    }
                    else
                    {
                        commNProcess process_T = new commNProcess(command, socket, f);
                        process_T.start();
                    }
                }
                else
                {
                    // If not a JSONObject
                    JSONObject reply = new JSONObject();
                    reply.put("command", "INVALID_PROTOCOL");
                    reply.put("message", "message must contain a command field as string");
                    System.out.println(reply);
                    out.write(reply + "\n");
                    out.flush();
                }
            }
        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            if (e.toString().contains("ConnectException"))
            {
                System.out.println("Peer working as a server");
            } else
            {
                e.printStackTrace();
            }
        } catch (ParseException e)
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
                    System.out.println("server socket closed...");
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
