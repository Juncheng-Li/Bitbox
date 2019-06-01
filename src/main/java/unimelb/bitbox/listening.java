package unimelb.bitbox;

import org.json.simple.JSONObject;
import unimelb.bitbox.util.Configuration;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class listening extends Thread
{
    private  ServerMain f;
    socketStorage ss;

    listening(socketStorage ss, ServerMain f)
    {
        this.f = f;
        this.ss = ss;
    }

    public void run()
    {
        ServerSocket listeningSocket = null;
        Socket clientSocket = null;
        ArrayList<JSONObject> peerList = new ArrayList<>();
        int i = 0; //counter to keep track of the number of clients
        try
        {
            listeningSocket = new ServerSocket(Integer.parseInt(Configuration.getConfigurationValue("port")));
            while (true)
            {
                System.out.println("listening on port " +
                        Integer.parseInt(Configuration.getConfigurationValue("port")));
                clientSocket = listeningSocket.accept();
                i++;
                // Check if already 10 clients?
                if (i <= Integer.parseInt(Configuration.getConfigurationValue("maximumIncommingConnections")))
                {
                    JSONObject peer = new JSONObject();
                    peer.put("host", clientSocket.getInetAddress().toString().replaceAll("/", ""));
                    peer.put("port", clientSocket.getLocalPort());
                    peerList.add(peer);
                    ss.add(clientSocket);
                    System.out.println("Peer_clientSide " + i + " accepted.");
                    Peer_serverSide T_server = new Peer_serverSide(clientSocket, i, f, ss);
                    T_server.start();
                }
                else
                {
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
                    JSONObject reply = new JSONObject();
                    reply.put("command", "CONNECTION_REFUSED");
                    reply.put("message", "connection limit reached");
                    reply.put("peers", peerList);
                    System.out.println("sent: " + reply.toJSONString());
                    out.write(reply.toJSONString() + "\n");
                    out.flush();
                }
            }
        } catch (SocketException ex)
        {
            //ex.printStackTrace();
        } catch (IOException e)
        {
            //e.printStackTrace();
        } finally
        {
            if (listeningSocket != null)
            {
                try
                {
                    listeningSocket.close();
                } catch (IOException e)
                {
                    //e.printStackTrace();
                }
            }
        }
    }
}
