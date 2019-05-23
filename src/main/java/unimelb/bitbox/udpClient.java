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

public class udpClient extends Thread
{
    private JSONParser parser = new JSONParser();
    private ServerMain f;
    private Timer timer = new Timer();

    udpClient()
    {

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
            hostPort.put("port", Integer.parseInt(Configuration.getConfigurationValue("port")));
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
                System.out.println("udpClient(" + serverPacket.getSocketAddress() + "): " + message);


                // Client side, copied from server
                JSONObject command = (JSONObject) parser.parse(message.toString());
                System.out.println("Message from UDP peer: " + command.toJSONString());

                if (command.getClass().getName().equals("org.json.simple.JSONObject"))
                {
                    if (command.get("command").toString().equals("HANDSHAKE_RESPONSE"))
                    {
                        // Synchronizing Events after Handshake!!!
                        timer.schedule(new SyncEvents(f), 0,
                                Integer.parseInt(Configuration.getConfigurationValue("syncInterval")) * 1000);
                    } else if (command.get("command").toString().equals("CONNECTION_REFUSED"))
                    {
                        System.out.println("Peer(" +
                                serverPacket.getSocketAddress().toString().replaceAll("/", "")
                                + ") maximum connection limit reached...");
                        break;
                    } else
                    {
                        udpCommNProcess process_T = new udpCommNProcess(command, ip, udpPort, f);
                        process_T.start();
                    }
                } else
                {
                    // If not a JSONObject
                    JSONObject reply = new JSONObject();
                    reply.put("command", "INVALID_PROTOCOL");
                    reply.put("message", "message must contain a command field as string");
                    System.out.println("udp Sent: " + reply);
                    buf = reply.toJSONString().getBytes();
                    packet = new DatagramPacket(buf, buf.length, ip, udpPort);
                    dsSocket.send(packet);
                }


                //Server side
                //System.out.println("Thread: Peer_serverSide for Client-" + i + " started...");

                //f = new ServerMain(clientSocket);
                //System.out.println("(Peer_serverSide)Message from Client " + i + ": " + command.toJSONString());
                if (command.getClass().getName().equals("org.json.simple.JSONObject"))
                {
                    if (command.get("command").toString().equals("HANDSHAKE_REQUEST"))
                    {
                        JSONObject hs_res = new JSONObject();
                        hostPort = new JSONObject();
                        hs_res.put("command", "HANDSHAKE_RESPONSE");
                        hostPort.put("host", ip);
                        hostPort.put("port", udpPort);
                        hs_res.put("hostPort", hostPort);
                        send(hs_res, ip, udpPort);

                        timer.schedule(new SyncEvents(f), 0,
                                Integer.parseInt(Configuration.getConfigurationValue("syncInterval")) * 1000);
                    } else
                    {
                        udpCommNProcess command_T = new udpCommNProcess(command, ip, udpPort, f);
                        command_T.start();
                    }
                } else
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
            System.out.println(e);
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
