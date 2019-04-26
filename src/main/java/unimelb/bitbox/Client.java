package unimelb.bitbox;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import unimelb.bitbox.util.Document;
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
            JSONParser parser = new JSONParser();

            JSONObject hs_r = new JSONObject();
            hs_r.put("command","HANDSHAKE_REQUEST");
            JSONObject hostPort = new JSONObject();
            hostPort.put("host",ip);
            hostPort.put("port",port);
            hs_r.put("hostPort",hostPort);
            System.out.println(hs_r.toJSONString());


            // Send the input string to the server by writing to the socket output stream
            out.write(hs_r + "\n");
            out.flush();
            System.out.println("JSONObject sent");
            String message = null;
            while ((message = in.readLine()) != null)
            {
                JSONObject command = (JSONObject) parser.parse(message);
                System.out.println("Message from peer: " + command.toJSONString());
                //out.write("Server Ack " + command.toJSONString() + "\n");
                //out.flush();
                //System.out.println("Reply sent");
                //Execute command
            }
            // Receive the reply from the server by reading from the socket input stream
            String received = in.readLine(); // This method blocks until there
            // is something to read from the
            // input stream


        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
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
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
