package unimelb.bitbox;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.FileSystemObserver;

import java.io.*;
import java.net.*;
import java.nio.Buffer;


class client_T extends Thread
{
    private Thread t;
    private String threadName;
    private String ip;
    private int port;
    public Socket socket = null;
    //private ServerMain f;
    public BufferedReader in;
    public BufferedWriter out;

    client_T(BufferedReader in, BufferedWriter out)
    {
        this.in = in;
        this.out = out;
    }

    public void run()
    {

        try
        {
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
            JSONParser parser = new JSONParser();
            String message = null;
            while ((message = in.readLine()) != null)
            {
                JSONObject command = (JSONObject) parser.parse(message);
                System.out.println("(Client)Message from peer server: " + command.toJSONString());
                //out.write("Server Ack " + command.toJSONString() + "\n");
                //out.flush();
                //System.out.println("Reply sent");
                //Execute command
                //doCommand(command, out);
                /*
                if (command.get("command").toString().equals("HANDSHAKE_RESPONSE"))
                {
                    System.out.println("(Client)Start Synchronizing Events------");
                    //System.out.println(f.fileSystemManager.generateSyncEvents());
                }
                //System.out.println("=======");
                */
            }

        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
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


    public void doCommand(JSONObject command, BufferedWriter out)
    {
        try
        {
            JSONObject test = new JSONObject();
            test.put("command", "awoken");
            out.write(test + "\n");
            out.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
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
