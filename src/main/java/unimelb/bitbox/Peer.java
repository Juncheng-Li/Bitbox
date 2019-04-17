package unimelb.bitbox;

import java.io.*;
import java.nio.file.FileSystems;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.FileSystemManager;

import java.net.*;

public class Peer
{
    private static Logger log = Logger.getLogger(Peer.class.getName());

    public static void main(String[] args) throws IOException, NumberFormatException, NoSuchAlgorithmException
    {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tc] %2$s %4$s: %5$s%n");
        log.info("BitBox Peer starting...");
        Configuration.getConfiguration();

        Socket csocket = null;
        Socket ssocket = null;
        Socket referenceSocket = null;

        //Client side
        /*
        try
        {
            csocket = new Socket("localhost", 3000);

            BufferedReader in = new BufferedReader(new InputStreamReader(csocket.getInputStream(), "UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(csocket.getOutputStream(), "UTF-8"));

        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            if (csocket != null)
            {
                try
                {
                    csocket.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        */
        //Server side



        new ServerMain();
    }
}
