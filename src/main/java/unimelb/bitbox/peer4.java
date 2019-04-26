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

public class peer4
{
    public static void main(String[] args)
    {
        Client T1 = new Client("peer4", "localhost", 3000);

        ServerSocket listeningSocket = null;
        Socket clientSocket = null;
        int i = 0; //counter to keep track of the number of clients
        try
        {
            listeningSocket = new ServerSocket(3000);
            while(true)
            {
                System.out.println("listeining on port 3000");
                clientSocket = listeningSocket.accept();
                i++;
                System.out.println("Client " + i + " accepted.");
                Server T2 = new Server("peer4 server-1", 3000, clientSocket, i);
                T2.start();


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
