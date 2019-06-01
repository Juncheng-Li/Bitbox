package unimelb.bitbox;

import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.FileSystemManager;
import unimelb.bitbox.util.FileSystemObserver;
import unimelb.bitbox.util.FileSystemManager.FileSystemEvent;
import unimelb.bitbox.util.HostPort;

public class ServerMain implements FileSystemObserver
{
    private static Logger log = Logger.getLogger(ServerMain.class.getName());
    private BufferedReader in;
    private BufferedWriter out;
    protected FileSystemManager fileSystemManager;
    private Socket socket = null;
    private InetAddress udpIP;
    private int udpPort;

    private ArrayList<Socket> sockets;
    socketStorage ss;
    private DatagramSocket dsServerSocket = null;
    private ackStorage as;

    public ServerMain(InetAddress udpIP, int udpPort) throws IOException, NoSuchAlgorithmException
    {
        this.udpIP = udpIP;
        this.udpPort = udpPort;
        fileSystemManager = new FileSystemManager(Configuration.getConfigurationValue("path"), this);
    }

    public ServerMain(socketStorage ss, DatagramSocket dsServerSocket, ackStorage as) throws IOException, NoSuchAlgorithmException
    {
        this.ss = ss;
        this.dsServerSocket = dsServerSocket;
        this.as = as;
        fileSystemManager = new FileSystemManager(Configuration.getConfigurationValue("path"), this);
    }

    @Override
    public void processFileSystemEvent(FileSystemEvent fileSystemEvent)
    {
        // TODO: process events

        try
        {
            if (fileSystemEvent.event.toString().equals("FILE_CREATE"))
            {
                //Ask to create file loader
                JSONObject req = new JSONObject();
                JSONObject fileDescriptor = new JSONObject();
                fileDescriptor.put("md5", fileSystemEvent.fileDescriptor.md5);
                fileDescriptor.put("lastModified", fileSystemEvent.fileDescriptor.lastModified);
                fileDescriptor.put("fileSize", fileSystemEvent.fileDescriptor.fileSize);
                req.put("command", "FILE_CREATE_REQUEST");
                req.put("fileDescriptor", fileDescriptor);
                req.put("pathName", fileSystemEvent.pathName);
                if (Configuration.getConfigurationValue("mode").equals("tcp"))
                {
                    send(req, ss);
                }
                else if (Configuration.getConfigurationValue("mode").equals("udp"))
                {
                    sendUDP(req, ss, dsServerSocket, as);
                }
                else
                {
                    System.out.println("wrong mode!");
                }
            }

            if (fileSystemEvent.event.toString().equals("FILE_DELETE"))
            {
                //Destination remove file
                JSONObject req = new JSONObject();
                JSONObject fileDescriptor = new JSONObject();
                fileDescriptor.put("md5", fileSystemEvent.fileDescriptor.md5);
                fileDescriptor.put("lastModified", fileSystemEvent.fileDescriptor.lastModified);
                fileDescriptor.put("fileSize", fileSystemEvent.fileDescriptor.fileSize);
                req.put("command", "FILE_DELETE_REQUEST");
                req.put("fileDescriptor", fileDescriptor);
                req.put("pathName", fileSystemEvent.pathName);
                if (Configuration.getConfigurationValue("mode").equals("tcp"))
                {
                    send(req, ss);
                }
                else if (Configuration.getConfigurationValue("mode").equals("udp"))
                {
                    sendUDP(req, ss, dsServerSocket, as);
                }
                else
                {
                    System.out.println("wrong mode!");
                }
            }

            if (fileSystemEvent.event.toString().equals("FILE_MODIFY"))
            {
                //System.out.println("Yes, there is a file modified");
                JSONObject req = new JSONObject();
                JSONObject fileDescriptor = new JSONObject();
                fileDescriptor.put("md5", fileSystemEvent.fileDescriptor.md5);
                fileDescriptor.put("lastModified", fileSystemEvent.fileDescriptor.lastModified);
                fileDescriptor.put("fileSize", fileSystemEvent.fileDescriptor.fileSize);
                req.put("command", "FILE_MODIFY_REQUEST");
                req.put("fileDescriptor", fileDescriptor);
                req.put("pathName", fileSystemEvent.pathName);
                if (Configuration.getConfigurationValue("mode").equals("tcp"))
                {
                    send(req, ss);
                }
                else if (Configuration.getConfigurationValue("mode").equals("udp"))
                {
                    sendUDP(req, ss, dsServerSocket, as);
                }
                else
                {
                    System.out.println("wrong mode!");
                }
            }

            if (fileSystemEvent.event.toString().equals("DIRECTORY_CREATE"))
            {
                //Destination create dir
                JSONObject req = new JSONObject();
                String pathName = fileSystemEvent.pathName;
                req.put("command", "DIRECTORY_CREATE_REQUEST");
                req.put("pathName", pathName);
                if (Configuration.getConfigurationValue("mode").equals("tcp"))
                {
                    send(req, ss);
                }
                else if (Configuration.getConfigurationValue("mode").equals("udp"))
                {
                    sendUDP(req, ss, dsServerSocket, as);
                }
                else
                {
                    System.out.println("wrong mode!");
                }
            }

            if (fileSystemEvent.event.toString().equals("DIRECTORY_DELETE"))
            {
                //Destination delete dir
                JSONObject req = new JSONObject();
                String pathName = fileSystemEvent.pathName;
                req.put("command", "DIRECTORY_DELETE_REQUEST");
                req.put("pathName", pathName);
                if (Configuration.getConfigurationValue("mode").equals("tcp"))
                {
                    send(req, ss);
                }
                else if (Configuration.getConfigurationValue("mode").equals("udp"))
                {
                    sendUDP(req, ss, dsServerSocket, as);
                }
                else
                {
                    System.out.println("wrong mode!");
                }
            }
        }
        catch (IOException e)
        {
            System.out.println("ServerMain " + e.getCause());
        }
    }


    private static void send(JSONObject message, socketStorage ss)
    {
        for (Socket socket : ss.getSockets())
        {
            try
            {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                out.write(message.toJSONString() + "\n");
                out.flush();
                System.out.println("sent: " + message.toJSONString());
            } catch (IOException e)
            {
                if (e.toString().equals("java.net.SocketException: Socket closed"))
                {
                    try
                    {
                        socket.close();
                        System.out.println("ServerMain Socket closed!");
                    }
                    catch (IOException es)
                    {
                        System.out.println("Socket cannot be closed.");
                        //es.printStackTrace();
                    }
                } else
                {
                    System.out.println("Socket have problems");
                    //System.out.println(e.toString());
                    //e.printStackTrace();
                }
            }
        }
    }


    private static void sendUDP(JSONObject message, socketStorage ss, DatagramSocket dsServerSocket, ackStorage as) throws IOException
    {
        for (HostPort hp : ss.getUdpSockets())
        {
            InetAddress ip = InetAddress.getByName(hp.host);
            int port = hp.port;
            byte[] buf = message.toJSONString().getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, ip, port);
            dsServerSocket.send(packet);
            System.out.println("udp sent: " + message.toJSONString());

            /*
            //Error handling
            ackObject ack = new ackObject(message, ip, port);
            if (!as.getAckMap().containsKey(ip.getHostAddress()))
            {
                //System.out.println("no such host");
                ArrayList<ackObject> newACKlist = new ArrayList<>();
                as.getAckMap().put(ip.getHostAddress(), newACKlist);
            }
            as.getAckMap().get(ip.getHostAddress()).add(ack);
            UDPErrorHandling errorHandling = new UDPErrorHandling(message, ack, ss);
            errorHandling.start();
            */

            // Handle packet loss
            ackObject ack = new ackObject(message, ip, port);
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
            UDPErrorHandling errorHandling = new UDPErrorHandling(message, ack, ss, dsServerSocket, as);
            errorHandling.start();
        }
    }


}
