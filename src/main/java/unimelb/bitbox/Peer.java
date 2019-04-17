package unimelb.bitbox;

import java.io.*;
import java.nio.file.FileSystems;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.FileSystemManager;

import java.net.*;
import java.util.Scanner;

public class Peer
{
    private static Logger log = Logger.getLogger(Peer.class.getName());

    public static void main(String[] args) throws IOException, NumberFormatException, NoSuchAlgorithmException
    {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tc] %2$s %4$s: %5$s%n");
        log.info("BitBox Peer starting...");
        Configuration.getConfiguration();

        new ServerMain();

        //Start of project

        Socket ServerSocket = null;
        Socket refSocket = null;

        //Client side

        Socket socket = null;
        try
        {
            int port = 3400;
            socket = new Socket("localhost", port);
            System.out.println("Connection to port " + port + " established");
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

            Scanner scanner = new Scanner(System.in);
            String inputStr = null;

            //While the user input differs from "exit"
            while (!(inputStr = scanner.nextLine()).equals("exit"))
            {
                out.write(inputStr + "\n");
                out.flush();
                System.out.println("Message sent");

                String received = in.readLine(); // This method blocks until there
                System.out.println("Message received: " + received);
            }

            scanner.close();

        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
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

        //Server side

    }
}
