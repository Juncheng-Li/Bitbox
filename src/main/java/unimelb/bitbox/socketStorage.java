package unimelb.bitbox;

import unimelb.bitbox.util.HostPort;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class socketStorage
{
    private ArrayList<Socket> sockets = new ArrayList<>();
    private ArrayList<HostPort> udpSockets = new ArrayList<>();

    // TCP
    public ArrayList<Socket> getSockets()
    {
        return sockets;
    }

    public void setSockets(ArrayList<Socket> sockets)
    {
        this.sockets = sockets;
    }

    public void add(Socket socket)
    {
        sockets.add(socket);
    }

    public void remove(Socket socket)
    {
        sockets.remove(socket);
    }

    // UDP
    public ArrayList<HostPort> getUdpSockets()
    {
        return udpSockets;
    }

    public void setUdpSockets(ArrayList<HostPort> udpSockets)
    {
        this.udpSockets = udpSockets;
    }

    public boolean add(HostPort udpSocket)
    {
        boolean contains = false;
        for(HostPort socket : udpSockets)
        {
            if (socket.toString().equals(udpSocket.toString()))
            {
                System.out.println("Same udp socket already exists, skip adding to socket map");
                contains = true;
            }
        }

        if (!contains)
        {
            udpSockets.add(udpSocket);
        }

        return contains;
    }

    public void remove(HostPort udpSocket)
    {
        udpSockets.remove(udpSocket);
    }

    public boolean contains(HostPort searchSocket)
    {
        boolean is = false;
        try
        {
            String localhostIP = InetAddress.getLocalHost().getHostAddress();
            String host = null;

            if (searchSocket.host.equals("localhost"))
            {
                host = localhostIP;
            }
            else
            {
                host = searchSocket.host;
            }

            for (HostPort element : udpSockets)
            {
                System.out.println(host + " " + element.host);
                if (host.equals(element.host) && searchSocket.port == element.port)
                {
                    is = true;
                }
            }
        }
        catch (UnknownHostException e)
        {
            System.out.println("socketStorage remove method, localhost does not exist");
        }
        return is;
    }

    public void remove(String mode, String hostPort)
    {
        try
        {
            HostPort temp = null;
            String localhostIP = InetAddress.getLocalHost().getHostAddress();
            if (mode.equals("udp"))
            {
                String host = null;
                if (hostPort.substring(0, hostPort.indexOf(":")).equals("localhost"))
                {
                    host = localhostIP;
                }
                else
                {
                    host = hostPort.substring(0, hostPort.indexOf(":"));
                }

                for (HostPort element : udpSockets)
                {
                    if (host.equals(element.host) &&
                            Integer.parseInt(hostPort.substring(hostPort.indexOf(":")+1)) == element.port)
                    {
                        temp = element;
                    }
                }
                udpSockets.remove(temp);
            }
        }
        catch (UnknownHostException e)
        {
            System.out.println("socketStorage remove method, localhost does not exist");
        }
    }
}
