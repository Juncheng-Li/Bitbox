package unimelb.bitbox;

import java.net.DatagramSocket;
import java.net.Socket;
import java.util.ArrayList;

public class socketStorage
{
    private ArrayList<Socket> sockets = new ArrayList<>();
    private ArrayList<DatagramSocket> udpSockets = new ArrayList<>();

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
    public ArrayList<DatagramSocket> getUdpSockets()
    {
        return udpSockets;
    }

    public void setUdpSockets(ArrayList<DatagramSocket> udpSockets)
    {
        this.udpSockets = udpSockets;
    }

    public void add(DatagramSocket udpSocket)
    {
        udpSockets.add(udpSocket);
    }

    public void remove(DatagramSocket udpSocket)
    {
        udpSockets.remove(udpSocket);
    }
}
