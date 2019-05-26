package unimelb.bitbox;

import java.net.DatagramSocket;
import java.net.Socket;
import java.util.ArrayList;

public class socketStorage
{
    private ArrayList<Socket> sockets;
    private ArrayList<DatagramSocket> udpSockets;


    public ArrayList<Socket> getSockets()
    {
        return sockets;
    }

    public void setSockets(ArrayList<Socket> sockets)
    {
        this.sockets = sockets;
    }

    public ArrayList<DatagramSocket> getUdpSockets()
    {
        return udpSockets;
    }

    public void setUdpSockets(ArrayList<DatagramSocket> udpSockets)
    {
        this.udpSockets = udpSockets;
    }
}
