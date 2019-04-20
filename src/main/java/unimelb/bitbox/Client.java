package unimelb.bitbox;

import org.json.simple.JSONObject;

public class Client
{
    public static void main(String[] args)
    {
        JSONObject newCommand = new JSONObject();
        newCommand.put("command_name", "HANDSHAKE_REQUEST");
        JSONObject hostPort = new JSONObject();
        hostPort.put("host", "43.240.97.106");
        hostPort.put("port", 3000);
        newCommand.put("hostPort", hostPort);
        //newCommand.put("file_name", fileName);
        // 1. Show the object on local
        System.out.println(newCommand.toJSONString());
    }

}
