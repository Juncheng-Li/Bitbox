package unimelb.bitbox;

import unimelb.bitbox.util.HostPort;

import java.net.DatagramSocket;
import java.net.Socket;
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

    public void add(HostPort udpSocket)
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

    }

    public void remove(HostPort udpSocket)
    {
        udpSockets.remove(udpSocket);
    }
}
