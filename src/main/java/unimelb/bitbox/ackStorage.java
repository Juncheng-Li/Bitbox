package unimelb.bitbox;

import org.json.simple.JSONObject;

import java.net.InetAddress;

public class ackStorage
{
    private JSONObject request = new JSONObject();
    private InetAddress ip = null;
    private int udpPort;
    private boolean answered;

    public void init()
    {
        answered = false;
    }

    public void set(JSONObject request, InetAddress ip, int udpPort)
    {
        this.request = request;
        this.ip = ip;
        this.udpPort = udpPort;
    }

    public InetAddress getInetIp()
    {
        return ip;
    }

    public String getIp()
    {
        return ip.getHostAddress();
    }

    public int getUdpPort()
    {
        return udpPort;
    }

    public String desiredRespond()
    {
        String name = request.get("command").toString();
        return name.substring(0, name.lastIndexOf("_")) + "_RESPONSE";
    }

    public void setAnswered(boolean answered)
    {
        this.answered = answered;
    }

    public boolean getAnswered()
    {
        return answered;
    }
    /*
    public String getRequestName()
    {
        return request.get("command").toString();
    }
     */
}
