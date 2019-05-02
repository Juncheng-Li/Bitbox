package unimelb.bitbox;

import java.io.*;
import java.nio.file.FileSystems;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.HostPort;
import java.net.*;


public class peer10
{
    private static Logger log = Logger.getLogger(Peer.class.getName());

    public static void main(String[] args) throws IOException, NumberFormatException, NoSuchAlgorithmException
    {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tc] %2$s %4$s: %5$s%n");
        log.info("BitBox Peer starting...");
        Configuration.getConfiguration();


        // Client Start
        try
        {
            HostPort hostPort = new HostPort(Configuration.getConfigurationValue("peers"));
            int port = hostPort.port;
            String ip = hostPort.host;
            Socket socket = new Socket(ip, port);
            System.out.println("Connection established");
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            ServerMain f = new ServerMain(in, out);

            // Try if connectable
            client_T T3 = new client_T(in, out, f);
            T3.start();
        }
        catch (IOException e)
        {
            System.out.println("Peer working as a server..");
        }


        // Server Start
        ServerSocket listeningSocket = null;
        Socket clientSocket = null;
        int i = 0; //counter to keep track of the number of clients
        try
        {
            listeningSocket = new ServerSocket(Integer.parseInt(Configuration.getConfigurationValue("port")));
            while (true)
            {
                System.out.println("listeining on port 3000");
                clientSocket = listeningSocket.accept();
                i++;
                System.out.println("Client " + i + " accepted.");

                Server T2 = new Server("peer4 server", 3000, clientSocket, i);
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