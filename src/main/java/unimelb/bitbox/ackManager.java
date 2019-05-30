package unimelb.bitbox;

import org.json.simple.JSONObject;

import java.net.InetAddress;
import java.util.ArrayList;

public class ackManager
{
    private ArrayList<requestList> ackS = new ArrayList<>();

    public void add(ackObject ack, InetAddress ip)
    {
        for(requestList reqList : ackS)
        {
            if (reqList.getIp().equals(ip.getHostAddress()))
            {
                reqList.add(ack);
            }
        }
    }


}
