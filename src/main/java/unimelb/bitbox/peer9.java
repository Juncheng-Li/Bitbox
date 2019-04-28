package unimelb.bitbox;

import java.io.*;
import java.nio.file.FileSystems;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.FileSystemManager;

import javax.net.ServerSocketFactory;
import java.net.*;
import java.util.Scanner;

public class peer9
{
    public static void main(String[] args)
    {
        /*
        //Client T1 = new Client("peer5", "43.240.97.106", 3000);
        //T1.start();
        //String pathName = null;
        //Client T1 = new Client("peer5", "10.0.0.79", 3000, "HANDSHAKE_REQUEST", "dragon1");
        //T1.start();

        ServerSocket listeningSocket = null;
        Socket clientSocket = null;
        int i = 0; //counter to keep track of the number of clients
        try
        {
            Socket socket = null;
            socket = new Socket("10.0.0.79", 3000);
            System.out.println("Connection established");
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            JSONParser parser = new JSONParser();


            client_T T1 = new client_T(in, out);

            client_T T11 = new client_T(in, out);
            T1.start();
            try
            {
                T11.join();
            }catch (Exception e)
            {
                e.printStackTrace();
            }



            listeningSocket = new ServerSocket(3000);
            while (true)
            {
                System.out.println("listeining on port 3000");
                clientSocket = listeningSocket.accept();
                i++;
                System.out.println("Client " + i + " accepted.");

                Server T2 = new Server("peer4 server", 3000, clientSocket, i);
                T2.start();
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

         */


    }

    public static JSONObject parseRequest(String eventString, String pathName)
    {
        JSONObject req = new JSONObject();
        if(eventString.equals("HANDSHAKE_REQUEST"))
        {
            req.put("command","HANDSHAKE_REQUEST");
            JSONObject hostPort = new JSONObject();
            hostPort.put("host","10.0.0.50");
            hostPort.put("port",3000);
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
