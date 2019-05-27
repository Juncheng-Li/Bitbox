package unimelb.bitbox;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.HostPort;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;


public class UDPErrorHandling extends Thread
{
    private JSONObject command = new JSONObject();
    private int udpServerPort;
    private InetAddress ip = null;
    private int udpPort;
    private boolean respond = false;
    private Timer timer = new Timer();
    private int count = 1;
    private JSONObject respondCommand = null;
    private DatagramSocket dsServerSocket = null;
    private socketStorage ss;

    UDPErrorHandling(JSONObject command, int udpServerPort, InetAddress ip, int udpPort, DatagramSocket dsServerSocket, socketStorage ss)
    {
        this.command = command;
        this.udpServerPort = udpServerPort;
        this.ip = ip;
        this.udpPort = udpPort;
        this.dsServerSocket = dsServerSocket;
        this.ss = ss;
    }


    public void run()
    {

        try
        {
            JSONParser parser = new JSONParser();
            //DatagramSocket dsServerSocket = new DatagramSocket(udpServerPort); //
            byte[] receive = new byte[65535];
            DatagramPacket serverPacket = null;
            String requiredCommand = command.get("command").toString().substring(0, command.get("command").toString().lastIndexOf("_")) + "_RESPONSE";
            TimerTask tt = new TimerTask()
            {
                @Override
                public void run()
                {
                    try
                    {
                        send(command, ip, udpPort);
                        System.out.println("Packet loss!! - Retransmitting time " + count + ": " + command.toJSONString());

                        count++;
                        if (count > Integer.parseInt(Configuration.getConfigurationValue("tryTimes")))
                        {
                            System.out.println("udpPeer(" + ip.getHostAddress() + ":" + udpPort + ") lost... Removing it from socket list");
                            HostPort udpSocket = new HostPort(ip.getHostAddress() + ":" + udpPort);
                            ss.remove(udpSocket);
                            timer.cancel();
                            timer.purge();
                            //return;
                        }

                        if (respond)
                        {
                            System.out.println("Respond received!! - " + respondCommand.get("command").toString());
                            timer.cancel();
                            timer.purge();
                            //return;
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            };

            int timeout = 1000*Integer.parseInt(Configuration.getConfigurationValue("timeout"));
            timer.schedule(tt, timeout, timeout);

            while (true)
            {
                serverPacket = new DatagramPacket(receive, receive.length);

                dsServerSocket.receive(serverPacket);  //
                // if receive desired message
                String respondIP = serverPacket.getSocketAddress().toString().replace("/", "");
                StringBuilder message = data(receive);
                respondCommand = (JSONObject) parser.parse(message.toString());
                // check if desired command name, if desired address
                if ((respondCommand.get("command").equals("INVALID_COMMAND")
                        || respondCommand.get("command").equals(requiredCommand))
                        && (respondIP.equals(ip.getHostAddress())))
                {
                    //Kill retransmitting thread
                    respond = true;
                    //Kill this thread (listening)
                    break;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
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


    private static void send(JSONObject message, InetAddress ip, int udpPort) throws IOException
    {
        DatagramSocket dsSocket = new DatagramSocket();
        byte[] buf = message.toJSONString().getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, ip, udpPort);
        dsSocket.send(packet);
    }

}
