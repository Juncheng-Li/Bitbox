package unimelb.bitbox;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.FileSystemManager;

import javax.net.ServerSocketFactory;
import java.net.*;
import java.util.Scanner;

public class Peer
{
    private static int counter = 0;
    private static int port = 3000;
    private static Logger log = Logger.getLogger(Peer.class.getName());
    private static String ip = "localhost";

    public static void main(String[] args) throws IOException, NumberFormatException, NoSuchAlgorithmException
    {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tc] %2$s %4$s: %5$s%n");
        log.info("BitBox Peer starting...");
        Configuration.getConfiguration();

        ServerMain f = new ServerMain();
        long lm = 1556443838000L;


        ByteBuffer jk1 = f.fileSystemManager.readFile("a64dd999ccfc0015e68c3551d871a809", 0, 14);
        jk1.rewind();
        System.out.println("remaining: " + jk1.remaining());
        byte[] arr1 = new byte[jk1.remaining()];
        System.out.println(jk1);
        jk1.get(arr1, 0, arr1.length);
        //System.out.println(arr1.length);
        String hold1 = Base64.getEncoder().encodeToString(arr1);
        System.out.println(hold1);
        byte[] arr2 = Base64.getDecoder().decode(hold1);

        JSONObject shit = new JSONObject();
        System.out.println(shit.getClass().getName().equals("org.json.simple.JSONObject"));


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
