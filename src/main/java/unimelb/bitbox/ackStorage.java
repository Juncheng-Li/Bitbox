package unimelb.bitbox;

import java.util.*;

public class ackStorage
{
    private Map<String, ArrayList<ackObject>> ackMap = new HashMap<>();

    ackStorage()
    {

    }

    public Map<String, ArrayList<ackObject>> getAckMap()
    {
        return ackMap;
    }

    public void setAckMap(Map<String, ArrayList<ackObject>> ackMap)
    {
        this.ackMap = ackMap;
    }
}
