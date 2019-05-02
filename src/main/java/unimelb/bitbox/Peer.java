package unimelb.bitbox;

import java.io.*;
import java.nio.file.FileSystems;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.FileSystemManager;
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
            HostPort hostPort = new HostPort(Configuration.getConfigurationValue("peers"));
            int port = hostPort.port;
            String ip = hostPort.host;
            Socket socket = new Socket(ip, port);
            System.out.println("Connection established");
            ServerMain f = new ServerMain(socket);

            // Try if connectable
            client_T T3 = new client_T(socket, f);
            T3.start();
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
                    Server T2 = new Server("peer4 server",
                                            Integer.parseInt(Configuration.getConfigurationValue("port")),
                                            clientSocket, i, listeningSocket);
                    T2.start();
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
