package unimelb.bitbox;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.FileSystemObserver;

import java.io.*;
import java.net.*;


class Client extends Thread
{
    private Thread t;
    private String threadName;
    private String ip;
    private int port;
    public Socket socket = null;
    private String eventString;
    private String pathName;
    //private ServerMain f;
    public BufferedReader in;
    public BufferedWriter out;

    Client(String threadName, String ip, int port)
    {
        this.threadName = threadName;
        this.ip = ip;
        this.port = port;
        //this.f = FileSystemManager;
    }

    public void run()
    {

        try
        {
            socket = new Socket(ip, port);
            System.out.println("Connection established");
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            JSONParser parser = new JSONParser();

            // Handshake - fixed!
            JSONObject hs = new JSONObject();
            JSONObject hostPort = new JSONObject();
            hostPort.put("host", "10.0.0.50");
            hostPort.put("port", 3000);
            hs.put("command", "HANDSHAKE_REQUEST");
            hs.put("hostPort", hostPort);
            out.write(hs + "\n");
            out.flush();
            System.out.println(hs.get("command") + " sent");

            /*
            // Send and Print what is sent
            JSONObject sendMessage = new JSONObject();
            sendMessage = parseRequest(eventString, pathName);
            out.write(sendMessage + "\n");
            out.flush();
            System.out.println(sendMessage.get("command") + " sent");
            */

            // Receive incoming reply
            String message = null;
            while ((message = in.readLine()) != null)
            {
                JSONObject command = (JSONObject) parser.parse(message);
                System.out.println("(Client)Message from peer server: " + command.toJSONString());
                //out.write("Server Ack " + command.toJSONString() + "\n");
                //out.flush();
                //System.out.println("Reply sent");
                //Execute command
                /*
                if (command.get("command").toString().equals("HANDSHAKE_RESPONSE"))
                {
                    System.out.println("(Client)Start Synchronizing Events------");
                    //System.out.println(f.fileSystemManager.generateSyncEvents());
                }

                 */
                //System.out.println("=======");
            }

        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            if(e.toString().contains("ConnectException"))
            {
                System.out.println("Peer working as a server");
            }
            else
            {
                e.printStackTrace();
            }
        } catch (ParseException e)
        {
            e.printStackTrace();
        }

        finally
        {
            // Close the socket
            if (socket != null)
            {
                try
                {
                    socket.close();
                    System.out.println("client socket closed...");
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }


    public JSONObject parseRequest(String eventString, String pathName)
    {
        JSONObject req = new JSONObject();
        if(eventString.equals("HANDSHAKE_REQUEST"))
        {
            req.put("command","HANDSHAKE_REQUEST");
            JSONObject hostPort = new JSONObject();
            hostPort.put("host",ip);
            hostPort.put("port",port);
            req.put("hostPort",hostPort);
        }
        else if (eventString.equals("DIRECTORY_CREATE_REQUEST"))
        {
            req.put("command","DIRECTORY_CREATE_REQUEST");
            req.put("pathName",pathName);
        }
        else if (eventString.equals("DIRECTORY_DELETE_REQUEST"))
        {
            req.put("command", "DIRECTORY_DELETE_REQUEST");
            req.put("pathName", pathName);
        }

        else
        {
            System.out.println("Wrong eventString!");
        }

        System.out.println(req.toJSONString());
        return req;
    }

}
