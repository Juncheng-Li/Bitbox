package unimelb.bitbox;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.HostPort;

import java.net.*;

public class Peer
{
    private static Logger log = Logger.getLogger(Peer.class.getName());

    public static void main(String[] args) throws IOException, NumberFormatException, NoSuchAlgorithmException
    {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tc] %2$s %4$s: %5$s%n");
        log.info("BitBox Peer starting...");
        Configuration.getConfiguration();



        // Client Start
        try
        {
            String peers = Configuration.getConfigurationValue("peers");
            String[] peersArray = peers.split(" ");
            for (String element : peersArray)
            {
                HostPort peer_hp = new HostPort(element);
                Client T_client = new Client(peer_hp.host, peer_hp.port);
                T_client.start();
            }
        }
        catch (IOException e)
        {
            System.out.println("Peer working as a server..");
        }

        // Server Start
        ServerSocket listeningSocket = null;
        Socket clientSocket = null;
        ArrayList<JSONObject> peerList = new ArrayList<>();
        int i = 0; //counter to keep track of the number of clients
        try
        {
            listeningSocket = new ServerSocket(Integer.parseInt(Configuration.getConfigurationValue("port")));
            while (true)
            {
                System.out.println("listening on port 3000");
                clientSocket = listeningSocket.accept();
                i++;
                // Check if already 10 clients?
                if(i <= Integer.parseInt(Configuration.getConfigurationValue("maximumIncommingConnections")))
                {
                    JSONObject peer = new JSONObject();
                    peer.put("host", clientSocket.getInetAddress().toString().replaceAll("/", ""));
                    peer.put("port", clientSocket.getLocalPort());
                    peerList.add(peer);
                    System.out.println("Client " + i + " accepted.");
                    Server T_server = new Server(Integer.parseInt(Configuration.getConfigurationValue("port")),
                                                   clientSocket, i, listeningSocket);
                    T_server.start();
                }
                else
                {
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
                    JSONObject reply = new JSONObject();
                    reply.put("command","CONNECTION_REFUSED");
                    reply.put("message", "connection limit reached");
                    reply.put("peers", peerList);
                    System.out.println(reply.toJSONString());
                    out.write(reply.toJSONString() + "\n");
                    out.flush();
                }


                //ExecutorService pool = Executors.newFixedThreadPool(10);
                //pool.execute(T2);
            }
        } catch (SocketException ex)
        {
            ex.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            if (listeningSocket != null)
            {
                try
                {
                    listeningSocket.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }



}
