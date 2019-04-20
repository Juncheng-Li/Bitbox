package unimelb.bitbox;

import java.io.*;
import java.nio.file.FileSystems;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.FileSystemManager;
import unimelb.bitbox.util.HostPort;

import javax.net.ServerSocketFactory;
import java.net.*;
import java.util.Scanner;

public class Peer1
{
    private static int counter = 0;
    private static int port = 3000;
    private static Logger log = Logger.getLogger(Peer.class.getName());
    private static String ip = "43.240.97.106";

    public static void main(String[] args) throws IOException, NumberFormatException, NoSuchAlgorithmException
    {


        //Start of project
        JSONClient();


    }

    //Client side


    public static void JSONClient()
    {
        try (Socket socket = new Socket(ip, port))
        {
            // 0.
            String fileName = "mini_black_hole.jpg";
            // 0. Output and Input Stream, parser - setting
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            JSONParser parser = new JSONParser();
            // 0. Write a message
            //output.writeUTF("Connecting ... " + ip + " " + port);
            //output.flush();

            // 0. Read hello from server..
            //String message = input.readUTF();
            //System.out.println(message);

            // 1. Ready the object
            JSONObject newCommand = new JSONObject();
            newCommand.put("command", "HANDSHAKE_REQUEST");
            JSONObject hostPort = new JSONObject();
            hostPort.put("host", "localhost");
            hostPort.put("port", 3000);
            newCommand.put("hostPort", hostPort);

            //newCommand.put("file_name", fileName);
            // 1. Show the object on local
            System.out.println(newCommand.toJSONString());

            // 2. Send prepared object(RMI) to Server
            output.writeUTF(newCommand.toJSONString() + "\n");
            output.flush();

            // 3. Receive and parse reply received from server
            while (true)
            {
                if (input.available() > 0)
                {
                    System.out.println("111111111");
                    //3.1 Receive reply
                    String reply = input.readUTF();
                    System.out.println("Received from server: " + reply);
                    //3.2 Parse reply
                    JSONObject command = (JSONObject) parser.parse(reply);
                    System.out.println(command);
                    //3.3 Interpret the reply
                    // Check the command name

                }
            }

        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {

        } catch (ParseException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static int setChunkSize(long fileSizeRemaining)
    {
        // Determine the chunkSize
        int chunkSize = 1024 * 1024;

        // If the file size remaining is less than the chunk size
        // then set the chunk size to be equal to the file size.
        if (fileSizeRemaining < chunkSize)
        {
            chunkSize = (int) fileSizeRemaining;
        }

        return chunkSize;
    }
}
