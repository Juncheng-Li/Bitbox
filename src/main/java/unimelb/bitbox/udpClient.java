package unimelb.bitbox;

// Java program to illustrate Client side
// Implementation using DatagramSocket
import org.json.simple.JSONObject;
import unimelb.bitbox.util.Configuration;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class udpClient extends Thread
{
    public void run()
    {

        try
        {
            // Step 1:Create the socket object for
            // carrying the data.
            DatagramSocket dsSocket = new DatagramSocket();  //SocketException
            InetAddress ip = InetAddress.getByName("10.0.0.79"); //UnknownHostException
            int port = Integer.parseInt(Configuration.getConfigurationValue("udpPort"));
            byte buf[] = null;

            // Handshake - fixed!
            JSONObject hs = new JSONObject();
            JSONObject hostPort = new JSONObject();
            hostPort.put("host", Configuration.getConfigurationValue("advertisedName"));
            hostPort.put("port", Integer.parseInt(Configuration.getConfigurationValue("port")));
            hs.put("command", "HANDSHAKE_REQUEST");
            hs.put("hostPort", hostPort);
            buf = hs.toJSONString().getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, ip, port);
            dsSocket.send(packet);  //IOException  //need "/n" ?
            System.out.println("udp sent: " + hs);

            // loop while user not enters "bye"
            /*
            while (true)
            {

                // convert the String input into the byte array.


                // Step 2 : Create the datagramPacket for sending
                // the data.


                // Step 3 : invoke the send call to actually send
                // the data.


                // break the loop if user enters "bye"
                //if (inp.equals("quit"))
                    //break;
            }
             */

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        //ServerSide
        try
        {
            // Step 1 : Create a socket to listen at port 1234
            int udpPort = Integer.parseInt(Configuration.getConfigurationValue("udpPort"));
            DatagramSocket dsSocket = new DatagramSocket(udpPort); //
            byte[] receive = new byte[65535];
            System.out.println("UDP port " + udpPort);

            DatagramPacket packet = null;
            while (true)
            {

                // Step 2 : create a DatgramPacket to receive the data.
                packet = new DatagramPacket(receive, receive.length);

                // Step 3 : revieve the data in byte buffer.
                dsSocket.receive(packet);  //
                System.out.println(packet.getAddress());
                System.out.println(packet.getSocketAddress());
                System.out.println(packet.getPort());
                System.out.println(packet.getData());
                System.out.println(packet.getLength());
                System.out.println("udpClient: " + data(receive));

                // Exit the server if the client sends "bye"
                if (data(receive).toString().equals("bye"))
                {
                    System.out.println("udpClient sent bye.....EXITING");
                    break;
                }

                // Clear the buffer after every message.
                receive = new byte[65535];
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
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
}
