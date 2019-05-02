package unimelb.bitbox;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
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
    private Socket socket = null;

    public ServerMain(Socket socket) throws NumberFormatException, IOException, NoSuchAlgorithmException
    {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
        this.out = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream(), "UTF-8"));

        fileSystemManager = new FileSystemManager(Configuration.getConfigurationValue("path"), this);
    }

    @Override
    public void processFileSystemEvent(FileSystemEvent fileSystemEvent)
    {
        // TODO: process events

        try
        {
            if (fileSystemEvent.event.toString().equals("FILE_CREATE"))
            {
                //Ask to create file loader
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

            if (fileSystemEvent.event.toString().equals("FILE_DELETE"))
            {
                //Destination remove file
                JSONObject req = new JSONObject();
                JSONObject fileDescriptor = new JSONObject();
                fileDescriptor.put("md5", fileSystemEvent.fileDescriptor.md5);
                fileDescriptor.put("lastModified", fileSystemEvent.fileDescriptor.lastModified);
                fileDescriptor.put("fileSize", fileSystemEvent.fileDescriptor.fileSize);
                req.put("command", "FILE_DELETE_REQUEST");
                req.put("fileDescriptor", fileDescriptor);
                req.put("pathName", fileSystemEvent.pathName);
                System.out.println(req.toJSONString());
                out.write(req.toJSONString() + "\n");
                out.flush();
            }

            if (fileSystemEvent.event.toString().equals("FILE_MODIFY"))
            {
                System.out.println("Yes, there is a file modified");
                JSONObject req = new JSONObject();
                JSONObject fileDescriptor = new JSONObject();
                fileDescriptor.put("md5", fileSystemEvent.fileDescriptor.md5);
                fileDescriptor.put("lastModified", fileSystemEvent.fileDescriptor.lastModified);
                fileDescriptor.put("fileSize", fileSystemEvent.fileDescriptor.fileSize);
                req.put("command", "FILE_MODIFY_REQUEST");
                req.put("fileDescriptor", fileDescriptor);
                req.put("pathName", fileSystemEvent.pathName);
                System.out.println(req.toJSONString() + "\n");
                out.write(req.toJSONString() + "\n");
                out.flush();
            }

            if (fileSystemEvent.event.toString().equals("DIRECTORY_CREATE"))
            {
                //Destination create dir
                JSONObject req = new JSONObject();
                String pathName = fileSystemEvent.pathName;
                req.put("command", "DIRECTORY_CREATE_REQUEST");
                req.put("pathName", pathName);
                out.write(req.toJSONString() + "\n");
                out.flush();
            }

            if (fileSystemEvent.event.toString().equals("DIRECTORY_DELETE"))
            {
                //Destination delete dir
                JSONObject req = new JSONObject();
                String pathName = fileSystemEvent.pathName;
                req.put("command", "DIRECTORY_DELETE_REQUEST");
                req.put("pathName", pathName);
                out.write(req + "\n");
                out.flush();
            }
        }catch (IOException e)
        {
            if (e.toString().equals("java.net.SocketException: Socket closed"))
            {
                try
                {
                    socket.close();
                    System.out.println("Socket closed!");
                }catch (IOException es)
                {
                    es.printStackTrace();
                }
            }
            else
            {
                System.out.println(e.toString());
                e.printStackTrace();
            }
        }
    }
}
