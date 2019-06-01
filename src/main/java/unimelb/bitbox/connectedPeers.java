package unimelb.bitbox;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class connectedPeers
{
    private JSONArray connectedList = new JSONArray();

    public JSONArray getConnectedList()
    {
        return connectedList;
    }

    public void add(JSONObject peer)
    {
        connectedList.add(peer);
    }

    public void remove(String host, int port)
    {
        Object temp = null;
        for (Object element : connectedList)
        {
            JSONObject ele = (JSONObject) element;
            if (ele.get("host").equals(host)
                    && Integer.parseInt(ele.get("port").toString()) == port)
            {
                temp = element;
            }
        }
        connectedList.remove(temp);
    }

    public String getStringList()
    {
        return connectedList.toString();
    }
}
