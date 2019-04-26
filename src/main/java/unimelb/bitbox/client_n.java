package unimelb.bitbox;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class client_n
{
    Socket socket = null;
    private String ip;
    private int port;
    private String peerName = null;

    client_n(String peerName, String ip, int port)
    {
        this.peerName = peerName;
        this.ip = ip;
        this.port = port;
    }

    public void clientRun()
    {
        try
        {
            socket = new Socket(ip, port);
            System.out.println("Connection established");
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            JSONParser parser = new JSONParser();

            JSONObject hs_r = new JSONObject();
            hs_r.put("command","HANDSHAKE_REQUEST");
            JSONObject hostPort = new JSONObject();
            hostPort.put("host",ip);
            hostPort.put("port",port);
            hs_r.put("hostport",hostPort);
            System.out.println(hs_r.toJSONString());

            // Send the input string to the server by writing to the socket output stream
            out.write(hs_r + "\n");
            out.flush();
            System.out.println("JSONObject sent");

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
