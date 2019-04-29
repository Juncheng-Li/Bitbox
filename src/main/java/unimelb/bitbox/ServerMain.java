package unimelb.bitbox;

import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.FileSystemManager;
import unimelb.bitbox.util.FileSystemObserver;
import unimelb.bitbox.util.FileSystemManager.FileSystemEvent;

public class ServerMain implements FileSystemObserver
{
    private static Logger log = Logger.getLogger(ServerMain.class.getName());
    private BufferedReader in;
    private BufferedWriter out;
    protected FileSystemManager fileSystemManager;
    private String ip = "10.0.0.79";
    private int port = 3000;
    private Socket socket = null;


    public ServerMain(BufferedReader in, BufferedWriter out) throws NumberFormatException, IOException, NoSuchAlgorithmException
    {
        //Client T1 = new Client("peer5", "10.0.0.79", 3000);
        //T1.start();
        this.in = in;
        this.out = out;
        fileSystemManager = new FileSystemManager(Configuration.getConfigurationValue("path"), this);
    }

    public ServerMain() throws NumberFormatException, IOException, NoSuchAlgorithmException
    {
        fileSystemManager = new FileSystemManager(Configuration.getConfigurationValue("path"), this);
    }

    @Override
    public void processFileSystemEvent(FileSystemEvent fileSystemEvent)
    {
        // TODO: process events

        //System.out.println(fileSystemEvent.pathName);
        //System.out.println(fileSystemEvent.path);
        //System.out.println(fileSystemEvent.name);
        //System.out.println(fileSystemEvent.event);
        //System.out.println(fileSystemEvent.fileDescriptor.md5);
        //System.out.println(fileSystemEvent.fileDescriptor.lastModified);
        //System.out.println(fileSystemEvent.fileDescriptor.fileSize);
        //System.out.println(fileSystemEvent.fileDescriptor.toDoc().toJson());
        //System.out.println(fileSystemEvent.toString());

        if (fileSystemEvent.event.toString().equals("FILE_CREATE"))
        {
            //Transmit file to destination
            //System.out.println(fileSystemEvent.fileDescriptor.md5);
            //System.out.println(fileSystemEvent.fileDescriptor.lastModified);
            //System.out.println(fileSystemEvent.fileDescriptor.fileSize);

            try
            {
                JSONObject req = new JSONObject();
                JSONObject fileDescriptor = new JSONObject();
                fileDescriptor.put("md5", fileSystemEvent.fileDescriptor.md5);
                fileDescriptor.put("lastModified", fileSystemEvent.fileDescriptor.lastModified);
                fileDescriptor.put("fileSize", fileSystemEvent.fileDescriptor.fileSize);
                req.put("command", "FILE_CREATE_REQUEST");
                req.put("fileDescriptor", fileDescriptor);
                req.put("pathName", fileSystemEvent.pathName);
                System.out.println(req.toJSONString());
                out.write(req.toJSONString() + "\n");
                out.flush();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }


        }

        if (fileSystemEvent.event.toString().equals("FILE_DELETE"))
        {
            //Destination remove file
            System.out.println("Yes, there is a file deleted");
        }

        if (fileSystemEvent.event.toString().equals("FILE_MODIFY"))
        {
            System.out.println("Yes, there is a file modified");
        }

        if (fileSystemEvent.event.toString().equals("DIRECTORY_CREATE"))
        {
            //Destination create dir
            //System.out.println("Yes, a directory needs to be created");
            try
            {
                JSONObject req = new JSONObject();
                req = parseRequest("DIRECTORY_CREATE_REQUEST", fileSystemEvent.pathName);
                out.write(req.toJSONString() + "\n");
                out.flush();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        if (fileSystemEvent.event.toString().equals("DIRECTORY_DELETE"))
        {
            //Destination delete dir
            //System.out.println("Yes, a directory needs to be deleted");
            try
            {
                JSONObject req = new JSONObject();
                req = parseRequest("DIRECTORY_DELETE_REQUEST", fileSystemEvent.pathName);
                out.write(req + "\n");
                out.flush();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public JSONObject parseRequest(String eventString, String pathName)
    {
        JSONObject req = new JSONObject();
        if (eventString.equals("HANDSHAKE_REQUEST"))
        {
            req.put("command", "HANDSHAKE_REQUEST");
            JSONObject hostPort = new JSONObject();
            hostPort.put("host", ip);
            hostPort.put("port", port);
            req.put("hostPort", hostPort);
        } else if (eventString.equals("DIRECTORY_CREATE_REQUEST"))
        {
            req.put("command", "DIRECTORY_CREATE_REQUEST");
            req.put("pathName", pathName);
        } else if (eventString.equals("DIRECTORY_DELETE_REQUEST"))
        {
            req.put("command", "DIRECTORY_DELETE_REQUEST");
            req.put("pathName", pathName);
        } else
        {
            System.out.println("Wrong eventString!");
        }

        System.out.println(req.toJSONString());
        return req;
    }

}
