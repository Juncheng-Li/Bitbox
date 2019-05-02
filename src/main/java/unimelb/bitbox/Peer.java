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
import unimelb.bitbox.util.HostPort;

import javax.net.ServerSocketFactory;
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

        ServerMain f = new ServerMain();


    }



}
