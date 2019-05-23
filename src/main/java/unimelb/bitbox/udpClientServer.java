package unimelb.bitbox;

// Java program to illustrate Client side
// Implementation using DatagramSocket

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import unimelb.bitbox.util.Configuration;

import javax.swing.text.html.parser.Parser;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.Timer;

public class udpClientServer extends Thread
{
    private JSONParser parser = new JSONParser();
    private Timer timer = new Timer();
    private ServerMain f;

    udpClientServer() throws IOException, NoSuchAlgorithmException
    {
        f = new ServerMain();
    }

    public void run()
    {

        try
        {
            // Step 1: Preparing
            DatagramSocket dsSocket = new DatagramSocket();  //SocketException
            InetAddress ip = InetAddress.getByName("10.0.0.79"); //UnknownHostException
            int udpPort = Integer.parseInt(Configuration.getConfigurationValue("udpPort"));
            byte buf[] = null;


            // udp Client side
            // Handshake - fixed!
            JSONObject hs = new JSONObject();
            JSONObject hostPort = new JSONObject();
            hostPort.put("host", Configuration.getConfigurationValue("advertisedName"));
            hostPort.put("port", udpPort);
            hs.put("command", "HANDSHAKE_REQUEST");
            hs.put("hostPort", hostPort);
            buf = hs.toJSONString().getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, ip, udpPort);
            dsSocket.send(packet);  //IOException  //need "/n" ?
            System.out.println("udp sent: " + hs);


            //ServerSide
            // Step 1 : Create a socket to listen at port 1234
            //int udpPort = Integer.parseInt(Configuration.getConfigurationValue("udpPort"));
            DatagramSocket dsServerSocket = new DatagramSocket(udpPort); //
            byte[] receive = new byte[65535];
            System.out.println("UDP port " + udpPort);

            DatagramPacket serverPacket = null;
            while (true)
            {
                // Step 2 : create a DatagramPacket to receive the data.
                serverPacket = new DatagramPacket(receive, receive.length);

                // Step 3 : receive the data in byte buffer.
                dsServerSocket.receive(serverPacket);  //
                StringBuilder message = data(receive);
                InetAddress clientIp = null;
                int clientPort = -1;
                System.out.println("udpClientServer(" + serverPacket.getSocketAddress() + "): " + message);
                System.out.println(serverPacket.getLength());

                // Client side, copied from server
                JSONObject command = (JSONObject) parser.parse(message.toString());

                if (command.getClass().getName().equals("org.json.simple.JSONObject"))
                {
                    if (command.get("command").toString().equals("HANDSHAKE_RESPONSE"))
                    {
                        clientIp = InetAddress.getByName(((JSONObject) command.get("hostPort")).get("host").toString());
                        clientPort = Integer.parseInt(((JSONObject) command.get("hostPort")).get("port").toString());
                        // Synchronizing Events after Handshake!!!
                        timer.schedule(new SyncEvents(f), 0,
                                Integer.parseInt(Configuration.getConfigurationValue("syncInterval")) * 1000);
                    }
                    else if (command.get("command").toString().equals("CONNECTION_REFUSED"))
                    {
                        System.out.println("Peer(" +
                                serverPacket.getSocketAddress().toString().replaceAll("/", "")
                                + ") maximum connection limit reached...");
                        break;
                    }
                    else if (command.get("command").toString().equals("HANDSHAKE_REQUEST"))
                    {
                        // get client Ip and port from HANDSHAKE_REQUEST
                        clientIp = InetAddress.getByName(((JSONObject) command.get("hostPort")).get("host").toString());
                        clientPort = Integer.parseInt(((JSONObject) command.get("hostPort")).get("port").toString());

                        JSONObject hs_res = new JSONObject();
                        hostPort = new JSONObject();
                        hs_res.put("command", "HANDSHAKE_RESPONSE");
                        hostPort.put("host", ip.getHostAddress());
                        hostPort.put("port", udpPort);
                        hs_res.put("hostPort", hostPort);
                        send(hs_res, clientIp, clientPort);

                        timer.schedule(new SyncEvents(f), 0,
                                Integer.parseInt(Configuration.getConfigurationValue("syncInterval")) * 1000);
                    }
                    else
                    {
                        udpCommNProcess process_T = new udpCommNProcess(command, ip, udpPort, f);
                        process_T.start();
                    }

                    //Server side
                    //check has handshaked
                    System.out.println(clientPort);
                    if (clientPort != -1 && clientIp != null)
                    {
                        if (command.get("command").toString().equals("HANDSHAKE_REQUEST"))
                        {
                            JSONObject hs_res = new JSONObject();
                            hostPort = new JSONObject();
                            hs_res.put("command", "HANDSHAKE_RESPONSE");
                            hostPort.put("host", ip.getHostAddress());
                            hostPort.put("port", udpPort);
                            hs_res.put("hostPort", hostPort);
                            send(hs_res, clientIp, clientPort);

                            timer.schedule(new SyncEvents(f), 0,
                                    Integer.parseInt(Configuration.getConfigurationValue("syncInterval")) * 1000);
                        } else
                        {
                            udpCommNProcess command_T = new udpCommNProcess(command, ip, udpPort, f);
                            command_T.start();
                        }
                    }
                    else
                    {
                        System.out.println("have not handshaked!");
                    }
                }
                else
                {
                    // If not a JSONObject
                    JSONObject reply = new JSONObject();
                    reply.put("command", "INVALID_PROTOCOL");
                    reply.put("message", "message must contain a command field as string");
                    send(reply, ip, udpPort);
                }

                //Refresh receive block
                receive = new byte[65535];

            }
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            // kill timer
            timer.cancel();
            timer.purge();
            // kill serverMain
            f.fileSystemManager.stop();
            System.out.println("Peer - " + " disconnected.");
        }

    }


    // A utility method to convert the byte array
    // data into a string representation.
    public static StringBuilder data(byte[] a)
    {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0)
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }

    public static void send(JSONObject message, InetAddress ip, int udpPort) throws IOException
    {
        DatagramSocket dsSocket = new DatagramSocket();
        byte[] buf = message.toJSONString().getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, ip, udpPort);
        dsSocket.send(packet);
        System.out.println("udp sent: " + message.toJSONString());
    }
}
