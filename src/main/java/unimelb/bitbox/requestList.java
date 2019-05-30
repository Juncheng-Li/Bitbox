package unimelb.bitbox;

import java.net.InetAddress;
import java.util.ArrayList;

public class requestList
{
    private ArrayList<ackObject> reqList = new ArrayList<>();
    private InetAddress ip = null;
    private int udpPort = 0;

    requestList(InetAddress ip, int udpPort)
    {
        this.ip = ip;
        this.udpPort = udpPort;
    }

    public void add(ackObject ack)
    {
        reqList.add(ack);
    }

    public InetAddress getInetIp()
    {
        return ip;
    }

    public ArrayList<ackObject> getReqList()
    {
        return reqList;
    }

    public String getIp()
    {
        return ip.getHostAddress();
    }

    public int getUdpPort()
    {
        return udpPort;
    }

}
