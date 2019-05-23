package unimelb.bitbox;

// Java program to illustrate Client side
// Implementation using DatagramSocket
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import unimelb.bitbox.util.Configuration;

import javax.swing.text.html.parser.Parser;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
            // Step 1:Create the socket object for
            // carrying the data.
            DatagramSocket dsSocket = new DatagramSocket();  //SocketException
            InetAddress ip = InetAddress.getByName("10.0.0.79"); //UnknownHostException
            int udpPort = Integer.parseInt(Configuration.getConfigurationValue("udpPort"));
            byte buf[] = null;

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
                System.out.println("udpClient: " + message);

                /*
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
                                socket.getRemoteSocketAddress().toString().replaceAll("/", "")
                                + ") maximum connection limit reached...");
                        break;
                    } else
                    {
                        commNProcess process_T = new commNProcess(command, socket, f);
                        process_T.start();
                    }
                } else
                {
                    // If not a JSONObject
                    JSONObject reply = new JSONObject();
                    reply.put("command", "INVALID_PROTOCOL");
                    reply.put("message", "message must contain a command field as string");
                    System.out.println("sent: " + reply);
                    out.write(reply + "\n");
                    out.flush();
                }
                 */

                // Exit the server if the client sends "bye"
                /*
                if (data(receive).toString().equals("bye"))
                {
                    System.out.println("udpClient sent bye.....EXITING");
                    break;
                }
                 */

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
