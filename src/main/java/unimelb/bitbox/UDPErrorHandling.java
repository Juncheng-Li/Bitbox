package unimelb.bitbox;

import org.json.simple.JSONObject;
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
    private boolean respond = false;
    private Timer timer = new Timer();
    private int count = 1;
    private JSONObject respondCommand = null;
    private socketStorage ss;
    private DatagramPacket serverPacket = null;
    private ackObject ack;

    UDPErrorHandling(JSONObject command, ackObject ack, socketStorage ss)
    {
        this.command = command;
        this.ack = ack;
        this.ss = ss;
    }


    public void run()
    {
        System.out.println(">>>>>>>>>>");

        TimerTask tt = new TimerTask()
        {
            @Override
            public void run()
            {
                if (ack.getAnswered())
                {
                    //System.out.println("Respond received!! - " + ack.desiredRespond());
                    timer.cancel();
                    timer.purge();
                    return;
                }

                if (count > Integer.parseInt(Configuration.getConfigurationValue("tryTimes")))
                {
                    System.out.println("udpPeer(" + ack.getIp() + ":" + ack.getUdpPort() + ") lost... Removing it from socket list");
                    HostPort udpSocket = new HostPort(ack.getIp() + ":" + ack.getUdpPort());
                    ss.remove(udpSocket);
                    timer.cancel();
                    timer.purge();
                    return;
                }

                send(command, ack.getInetIp(), ack.getUdpPort());
                System.out.println("<<<Detect packet loss!>>> - Retransmitting time " + count + ": " + command.toJSONString());

                count++;
            }
        };
        int timeout = 1000 * Integer.parseInt(Configuration.getConfigurationValue("timeout"));
        timer.schedule(tt, timeout, timeout);

        /*
        while (true)
        {
            if(ack.getAnswered())
            {
                System.out.println("timer stopped");
                timer.cancel();
                timer.purge();
                break;
            }
        }
         */
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


    private static void send(JSONObject message, InetAddress ip, int udpPort)
    {
        try
        {
            DatagramSocket dsSocket = new DatagramSocket();
            byte[] buf = message.toJSONString().getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, ip, udpPort);
            dsSocket.send(packet);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
