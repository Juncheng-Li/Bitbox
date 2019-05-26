package unimelb.bitbox;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.HostPort;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class udpClientServer extends Thread
{
    private JSONParser parser = new JSONParser();
    private Timer timer = new Timer();
    private ServerMain f;
    private InetAddress ip = null;
    private int udpPort;
    private static final ScheduledExecutorService delayedTask = Executors.newSingleThreadScheduledExecutor();
    private static boolean clientAnswer = false;
    private socketStorage ss;

    udpClientServer(InetAddress ip, int udpPort, ServerMain f, socketStorage ss)
    {
        this.ip = ip;
        this.udpPort = udpPort;
        this.f = f;
        this.ss = ss;
    }

    public void run()
    {
        try
        {
            // Step 1: Preparing
            DatagramSocket dsSocket = new DatagramSocket();  //SocketException  //self socket]

            byte buf[] = null;
            InetAddress clientIp = null;
            int clientPort;
            int udpServerPort = Integer.parseInt(Configuration.getConfigurationValue("udpPort"));

            // udp Client side
            // Handshake - fixed!
            handShake(udpServerPort, ip, udpPort, dsSocket);


            //ServerSide
            // Step 1 : Create a socket to listen at port 1234

            DatagramSocket dsServerSocket = new DatagramSocket(udpServerPort); //
            byte[] receive = new byte[65535];
            System.out.println("listening on UDP port " + udpServerPort);

            DatagramPacket serverPacket = null;
            while (true)
            {
                // Step 2 : create a DatagramPacket to receive the data.
                serverPacket = new DatagramPacket(receive, receive.length);

                // Step 3 : receive the data in byte buffer.
                // If time out N seconds
                Runnable task = new Runnable()
                {
                    public void run()
                    {
                        if (!clientAnswer)
                        {
                            /* Send a message to the client */
                            System.out.println("no reply");
                        }
                    }
                };
                delayedTask.schedule(task, Integer.parseInt(Configuration.getConfigurationValue("timeout")), TimeUnit.SECONDS);

                dsServerSocket.receive(serverPacket);  //
                // if receive message
                clientAnswer = true;
                StringBuilder message = data(receive);
                System.out.println("udpClientServer(" + serverPacket.getSocketAddress() + "): " + message);
                //System.out.println(serverPacket.getLength());

                // Client side, copied from server
                JSONObject command = (JSONObject) parser.parse(message.toString());

                if (command.getClass().getName().equals("org.json.simple.JSONObject"))
                {
                    if (command.get("command").toString().equals("HANDSHAKE_RESPONSE"))
                    {
                        JSONObject hp = (JSONObject) command.get("hostPort");
                        String tempIP = hp.get("host").toString();
                        String tempPort = hp.get("port").toString();
                        String h_p = tempIP + ":" + tempPort;
                        HostPort hostP = new HostPort(h_p);
                        ss.add(hostP);
                        // Synchronizing Events after Handshake!!!
                        timer.schedule(new SyncEvents(f), 0,
                                Integer.parseInt(Configuration.getConfigurationValue("syncInterval")) * 1000);
                    } else if (command.get("command").toString().equals("CONNECTION_REFUSED"))
                    {
                        System.out.println("Peer(" +
                                serverPacket.getSocketAddress().toString().replaceAll("/", "")
                                + ") maximum connection limit reached...");
                        break;
                    } else if (command.get("command").toString().equals("HANDSHAKE_REQUEST"))
                    {
                        // get client Ip and port from HANDSHAKE_REQUEST
                        clientIp = InetAddress.getByName(((JSONObject) command.get("hostPort")).get("host").toString());
                        System.out.println("clientIP: " + clientIp);
                        clientPort = Integer.parseInt(((JSONObject) command.get("hostPort")).get("port").toString());
                        System.out.println("clientPort" + clientPort);
                        JSONObject hs_res = new JSONObject();
                        JSONObject hostPort = new JSONObject();
                        hs_res.put("command", "HANDSHAKE_RESPONSE");
                        hostPort.put("host", Configuration.getConfigurationValue("advertisedName"));
                        hostPort.put("port", udpServerPort);
                        hs_res.put("hostPort", hostPort);
                        send(hs_res, clientIp, clientPort);

                        HostPort hp = new HostPort(clientIp + ":" + clientPort);
                        ss.add(hp);
                        timer.schedule(new SyncEvents(f), 0,
                                Integer.parseInt(Configuration.getConfigurationValue("syncInterval")) * 1000);
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
                    send(reply, ip, udpPort);
                }

                //Refresh receive block
                receive = new byte[65535];

            }
        } catch (ParseException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            // kill timer
            timer.cancel();
            timer.purge();
            System.out.println("Peer - " + " disconnected.");
        }

    }


    // A utility method to convert the byte array
    // data into a string representation.
    private static StringBuilder data(byte[] a)
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

    private static void send(JSONObject message, InetAddress ip, int udpPort) throws IOException
    {
        DatagramSocket dsSocket = new DatagramSocket();
        byte[] buf = message.toJSONString().getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, ip, udpPort);
        dsSocket.send(packet);
        System.out.println("udp sent: " + message.toJSONString());
    }

    private static void handShake(int udpServerPort, InetAddress ip, int udpPort, DatagramSocket dsSocket) throws IOException
    {
        byte[] buf = null;
        JSONObject hs = new JSONObject();
        JSONObject hostPort = new JSONObject();
        hostPort.put("host", Configuration.getConfigurationValue("advertisedName"));
        hostPort.put("port", udpServerPort);
        hs.put("command", "HANDSHAKE_REQUEST");
        hs.put("hostPort", hostPort);
        buf = hs.toJSONString().getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, ip, udpPort);
        dsSocket.send(packet);  //IOException  //need "/n" ?
        System.out.println("udp sent: " + hs);
    }
}
