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
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
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
            hostPort.put("host", ip);
            hostPort.put("port", 3000);
            newCommand.put("hostPort", hostPort);

            //newCommand.put("file_name", fileName);
            // 1. Show the object on local
            System.out.println("Send message: ");
            System.out.println(newCommand.toJSONString());

            // 2. Send prepared object(RMI) to Server
            output.write(newCommand.toJSONString());
            output.write("\n");
            output.flush();

            // 3. Receive and parse reply received from server
            System.out.println();
            System.out.println("Receive message: ");
            try
            {
                while (true)
                {
                    if (input.readLine() != null)
                    {

                        System.out.println(input.readLine());
                        System.out.println();
                        //3.1 Receive reply
                    /*
                    String reply = input.readLine();
                    System.out.println("Received from server: " + reply);
                    //3.2 Parse reply
                    JSONObject command = (JSONObject) parser.parse(reply);
                    System.out.println(command);
                    //3.3 Interpret the reply
                    // Check the command name

                     */

                    }
                }
            } catch (SocketException e)
            {
                System.out.println("1");
                System.out.println("closed...");
            }

        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            System.out.println("2");
            e.printStackTrace();
        }
    }

}
