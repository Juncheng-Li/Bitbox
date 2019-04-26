package unimelb.bitbox;
import org.json.simple.JSONObject;
import unimelb.bitbox.util.Document;

public class document
{
    public static void main(String[] args)
    {
        JSONObject command = new JSONObject();
        command.put("command_name", "GET_FILE");
        command.put("file_name", "sauron.jpg");
        System.out.println(command);
        String sc = "{\"file_name\":\"sauron.jpg\",\"command_name\":\"GET_FILE\"}";

    }
}
