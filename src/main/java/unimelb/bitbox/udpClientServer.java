package unimelb.bitbox;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.HostPort;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Timer;

public class udpClientServer extends Thread
{
    private JSONParser parser = new JSONParser();
    private Timer timer = new Timer();
    private ServerMain f;
    private socketStorage ss;
    private DatagramSocket dsServerSocket = null;
    private ackStorage as;

    udpClientServer(ServerMain f, socketStorage ss, DatagramSocket dsServerSocket, ackStorage as)
    {
        this.f = f;
        this.ss = ss;
        this.dsServerSocket = dsServerSocket;
        this.as = as;
    }

    public void run()
    {
        try
        {
            // Step 1: Preparing
            byte[] buf = null;
            InetAddress clientIp = null;
            int clientPort;
            int udpServerPort = Integer.parseInt(Configuration.getConfigurationValue("udpPort"));
            String peers = Configuration.getConfigurationValue("peers");
            String[] peersArray = peers.split(", ");

            for (String peer : peersArray)
            {
                System.out.println(peer);
                HostPort peer_hp = new HostPort(peer);
                InetAddress ip = null;
                if (peer_hp.host.equals("localhost"))
                {
                    ip = InetAddress.getLocalHost();
                }
                else
                {
                    ip = InetAddress.getByName(peer_hp.host); //UnknownHostException
                }
                int udpPort = peer_hp.port;

                // udp Client side
                // Handshake - fixed!
                JSONObject hs = new JSONObject();
                JSONObject hostPort = new JSONObject();
                hostPort.put("host", InetAddress.getLocalHost().getHostAddress());
                hostPort.put("port", udpServerPort);
                hs.put("command", "HANDSHAKE_REQUEST");
                hs.put("hostPort", hostPort);
                buf = hs.toJSONString().getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, ip, udpPort);
                dsServerSocket.send(packet);  //IOException  //need "/n" ?              //////
                System.out.println("udp sent " + ip.getHostAddress() + ":" + udpPort + " : " + hs);

                // Handle packet loss
                ackObject ack = new ackObject(hs, ip, udpPort);
                if (!as.getAckMap().containsKey(ip.getHostAddress()))
                {
                    //System.out.println("no such host");
                    ArrayList<ackObject> newACKlist = new ArrayList<>();
                    as.getAckMap().put(ip.getHostAddress(), newACKlist);
                }
                // remove duplicated same content, diff obj ack
                int duplicatedIndex = -1;
                for (ackObject aa : as.getAckMap().get(ip.getHostAddress()))
                {
                    if (aa.getUdpPort() == ack.getUdpPort()
                        && aa.desiredRespond().equals(ack.desiredRespond())
                        && aa.getAnswered())
                    {
                        duplicatedIndex = as.getAckMap().get(ip.getHostAddress()).indexOf(aa);
                    }
                }
                if (duplicatedIndex != -1)
                {
                    as.getAckMap().get(ip.getHostAddress()).remove(duplicatedIndex);
                }
                as.getAckMap().get(ip.getHostAddress()).add(ack);
                UDPErrorHandling errorHandling = new UDPErrorHandling(hs, ack, ss, dsServerSocket, as);
                errorHandling.start();
            }



            //ServerSide
            // Step 1 : Create a socket to listen
            byte[] receive = new byte[65535];
            System.out.println("listening on UDP port " + udpServerPort);
            DatagramPacket serverPacket = null;
            while (true)
            {
                serverPacket = new DatagramPacket(receive, receive.length);
                dsServerSocket.receive(serverPacket);  //
                // If receive message
                StringBuilder message = data(receive);
                System.out.println("udpClientServer(" +
                        serverPacket.getSocketAddress().toString().replace("/", "") +
                        "): " + message);
                //System.out.println(serverPacket.getLength());
                // Client side, copied from server
                JSONObject command = (JSONObject) parser.parse(message.toString());
                // Match ACK
                if (as.getAckMap().containsKey(serverPacket.getAddress().getHostAddress()))
                {
                    for (ackObject i : as.getAckMap().get(serverPacket.getAddress().getHostAddress()))
                    {
                        i.match(command, serverPacket);
                    }
                }


                if (command.getClass().getName().equals("org.json.simple.JSONObject"))
                {
                    if (command.get("command").toString().equals("HANDSHAKE_RESPONSE"))
                    {
                        JSONObject hp = (JSONObject) command.get("hostPort");
                        String tempIP = hp.get("host").toString();
                        String tempPort = hp.get("port").toString();
                        String h_p = tempIP + ":" + tempPort;
                        HostPort hostP = new HostPort(h_p);
                        boolean contains = ss.add(hostP);

                        // Sync only starts when client does not in the list
                        if (!contains)
                        {
                            timer.schedule(new SyncEvents(f), 0,
                                    Integer.parseInt(Configuration.getConfigurationValue("syncInterval")) * 1000);
                        }
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
                        System.out.println("newClientIP: " + clientIp);
                        clientPort = Integer.parseInt(((JSONObject) command.get("hostPort")).get("port").toString());
                        System.out.println("newClientPort: " + clientPort);
                        // ready local host port
                        JSONObject hostPort = new JSONObject();
                        hostPort.put("host", InetAddress.getLocalHost().getHostAddress());
                        hostPort.put("port", udpServerPort);
                        JSONObject hs_res = new JSONObject();
                        hs_res.put("command", "HANDSHAKE_RESPONSE");
                        hs_res.put("hostPort", hostPort); //use the hostPort defined in the handshake
                        send(hs_res, clientIp, clientPort, dsServerSocket);

                        HostPort hp = new HostPort(clientIp.getHostAddress() + ":" + clientPort);
                        boolean contains = ss.add(hp);
                        if (!contains)
                        {
                            timer.schedule(new SyncEvents(f), 0,
                                    Integer.parseInt(Configuration.getConfigurationValue("syncInterval")) * 1000);
                        }
                    } else
                    {
                        InetAddress ip = serverPacket.getAddress();
                        int port = serverPacket.getPort();
                        udpCommNProcess process_T = new udpCommNProcess(command, ip, port, f, dsServerSocket, ss, as);
                        process_T.start();
                    }
                } else
                {
                    // If not a JSONObject
                    InetAddress ip = serverPacket.getAddress();
                    int port = serverPacket.getPort();
                    JSONObject reply = new JSONObject();
                    reply.put("command", "INVALID_PROTOCOL");
                    reply.put("message", "message must contain a command field as string");
                    send(reply, ip, port, dsServerSocket);
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
        } catch (NullPointerException e)
        {
            e.printStackTrace();
            System.out.println("Received message error, Null pointer");
        }
        finally
        {
            // kill timer
            timer.cancel();
            timer.purge();
            System.out.println("Peer - " + " disconnected.");
        }

    }


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


    private static void send(JSONObject message, InetAddress ip, int udpPort, DatagramSocket dsServerSocket) throws IOException
    {
        byte[] buf = message.toJSONString().getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, ip, udpPort);
        dsServerSocket.send(packet);
        System.out.println("udp sent: " + message.toJSONString());
    }
}
