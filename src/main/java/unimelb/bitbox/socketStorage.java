package unimelb.bitbox;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import unimelb.bitbox.util.HostPort;

import java.io.IOException;
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

    public boolean contains(String host, int port)
    {

        boolean is = false;
        for (Socket socketInList : sockets)
        {
            if (host.equals("localhost"))
            {
                if (host.equals(socketInList.getInetAddress().toString().substring(0, socketInList.getInetAddress().toString().indexOf("/")))
                        && port == socketInList.getPort())
                {
                    is = true;
                }
            }
            else
            {
                if (host.equals(socketInList.getInetAddress().getHostAddress())
                        && port == socketInList.getPort())
                {
                    is = true;
                }
            }
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


    public void disNremove(String host, int port)
    {
        Socket temp = null;
        for (Socket socketInList : sockets)
        {
            if (host.equals("localhost"))
            {
                if (host.equals(socketInList.getInetAddress().toString().substring(0, socketInList.getInetAddress().toString().indexOf("/")))
                        && port == socketInList.getPort())
                {
                    temp = socketInList;
                }
            }
            else
            {
                if (host.equals(socketInList.getInetAddress().getHostAddress())
                        && port == socketInList.getPort())
                {
                    temp = socketInList;
                }
            }
        }
        if (temp != null)
        {
            try
            {
                temp.close();
                sockets.remove(temp);
            }
            catch (IOException e)
            {
                System.out.println("Did not close socket");
            }
        }
    }

    public String getTcpList ()
    {
        JSONArray list = new JSONArray();
        for (Socket element : sockets)
        {
            int port = element.getLocalPort();
            String host = null;
            if (element.getInetAddress().toString().contains("localhost"))
            {
                host = "localhost";
            }
            else
            {
                host = element.getInetAddress().getHostAddress();
            }

            JSONObject hp = new JSONObject();
            hp.put("host", host);
            hp.put("port", port);
            list.add(hp);
        }
        return list.toString();
    }

    public String getUdpList()
    {
        JSONArray list = new JSONArray();
        for (HostPort element : udpSockets)
        {
            int port = element.port;
            String host = element.host;

            JSONObject hp = new JSONObject();
            hp.put("host", host);
            hp.put("port", port);
            list.add(hp);
        }
        return list.toString();
    }
}
