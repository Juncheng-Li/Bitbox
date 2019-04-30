package unimelb.bitbox;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ServerSocketFactory;
import java.io.*;
import java.net.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import javax.net.ServerSocketFactory;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.FileSystemManager;

import java.nio.file.Files;

public class Server extends Thread
{
    private Thread t;
    private String threadName;
    private String ip;
    private int port;
    private int counter = 0;
    private int i = 0;
    private ServerSocket listeningSocket = null;
    private Socket clientSocket = null;
    private String clientMsg = null;
    private JSONObject command = null;
    private Document Msg = null;
    private ServerMain f;

    Server(String threadname, int port, Socket clientSocket, int i)
    {
        this.threadName = threadname;
        this.port = port;
        this.clientSocket = clientSocket;
        this.i = i;
        //this.f = f;
    }


    public void run()
    {
        System.out.println("Thread: Server for client-" + i + " started...");
        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
            JSONParser parser = new JSONParser();
            f = new ServerMain(in, out);
            //test message
            /*
            JSONObject sh = new JSONObject();
            sh.put("command", "aaaaa");
            out.write(sh + "\n");
            out.flush();
            */
            //Read the message from the client and reply
            //Notice that no other connection can be accepted and processed until the last line of
            //code of this loop is executed, incoming connections have to wait until the current
            //one is processed unless...we use threads!
            try
            {
                while ((clientMsg = in.readLine()) != null)
                {
                    command = (JSONObject) parser.parse(clientMsg);
                    System.out.println("(Server)Message from client " + i + ": " + command.toJSONString());
                    //out.write("Server Ack " + command.toJSONString() + "\n");
                    //out.flush();
                    //System.out.println("Reply sent");
                    //Execute command
                    //doCommand(command, out);
                    if (command.getClass().getName().equals("org.json.simple.JSONObject"))
                    {
                        if (command.get("command").toString().equals("HANDSHAKE_REQUEST"))
                        {
                            JSONObject hs_res = new JSONObject();
                            hs_res.put("command", "HANDSHAKE_RESPONSE");
                            JSONObject hostPort = new JSONObject();
                            hostPort.put("host", "10.0.0.79");
                            hostPort.put("port", port);
                            hs_res.put("hostPort", hostPort);
                            try
                            {
                                out.write(hs_res + "\n");
                                out.flush();
                                System.out.println("Replied");
                            } catch (IOException e)
                            {
                                e.printStackTrace();
                            }

                        }
                        // Handle DIRECTORY_CREATE_REQUEST
                        else if (command.get("command").toString().equals("DIRECTORY_CREATE_REQUEST"))
                        {
                            int count = 0;
                            String[] folders;
                            String pathName = command.get("pathName").toString();

                            // Check if parent dir exists:
                            if (pathName.contains("/"))
                            {
                                //Split pathName with "/"
                                folders = pathName.split("/");
                                System.out.println("Path Spliter");
                                String temp = "";
                                for (int i = 0; i < folders.length - 1; i++)
                                {
                                    temp = temp + folders[i];
                                    //System.out.println(temp);
                                    if (f.fileSystemManager.dirNameExists(temp))
                                    {
                                        count++;
                                    }
                                    temp = temp + "/";
                                }
                                count = count - (folders.length - 1);
                            }
                            //System.out.println(count);
                            try
                            {
                                // Check if parent dir exists
                                if (count == 0)
                                {
                                    //System.out.println("correct");
                                    if (f.fileSystemManager.isSafePathName(pathName))
                                    {
                                        if (f.fileSystemManager.dirNameExists(pathName))
                                        {
                                            JSONObject reply = new JSONObject();
                                            reply.put("command", "DIRECTORY_CREATE_RESPONSE");
                                            reply.put("pathName", pathName);
                                            reply.put("message", "pathname already exists");
                                            reply.put("status", false);
                                            out.write(reply + "\n");
                                            out.flush();
                                        } else
                                        {
                                            f.fileSystemManager.makeDirectory(pathName);
                                            JSONObject reply = new JSONObject();
                                            reply.put("command", "DIRECTORY_CREATE_RESPONSE");
                                            reply.put("pathName", pathName);
                                            reply.put("message", "directory created");
                                            reply.put("status", true);
                                            out.write(reply + "\n");
                                            out.flush();
                                        }
                                    } else
                                    {
                                        JSONObject reply = new JSONObject();
                                        reply.put("command", "DIRECTORY_CREATE_RESPONSE");
                                        reply.put("pathName", pathName);
                                        reply.put("message", "unsafe pathname given");
                                        reply.put("status", false);
                                        out.write(reply + "\n");
                                        out.flush();
                                    }
                                } else
                                {
                                    JSONObject reply = new JSONObject();
                                    reply.put("command", "DIRECTORY_CREATE_RESPONSE");
                                    reply.put("pathName", pathName);
                                    reply.put("message", "there was a problem creating the directory");
                                    reply.put("status", false);
                                    out.write(reply + "\n");
                                    out.flush();
                                }
                            } catch (Exception e)
                            {
                                JSONObject reply = new JSONObject();
                                reply.put("command", "DIRECTORY_CREATE_RESPONSE");
                                reply.put("pathName", pathName);
                                reply.put("message", "there was a problem creating the directory");
                                reply.put("status", false);
                                out.write(reply + "\n");
                                out.flush();
                            }
                        }
                        // Handle DIRECTORY_DELETE_REQUEST
                        else if (command.get("command").toString().equals("DIRECTORY_DELETE_REQUEST"))
                        {
                            int count = 0;
                            String[] folders;
                            String pathName = command.get("pathName").toString();

                            // Check if parent dir exists:
                            if (pathName.contains("/"))
                            {
                                //Split pathName with "/"
                                folders = pathName.split("/");
                                System.out.println("Path Spliter");
                                String temp = "";
                                for (int i = 0; i < folders.length - 1; i++)
                                {
                                    temp = temp + folders[i];
                                    //System.out.println(element);
                                    System.out.println(temp);
                                    if (f.fileSystemManager.dirNameExists(temp))
                                    {
                                        count++;
                                    }
                                    temp = temp + "/";
                                }
                                count = count - (folders.length - 1);
                            }
                            //System.out.println(count);
                            try
                            {
                                if (count == 0)
                                {
                                    if (f.fileSystemManager.isSafePathName(pathName))
                                    {
                                        if (f.fileSystemManager.dirNameExists(pathName))
                                        {
                                            f.fileSystemManager.deleteDirectory(pathName);
                                            JSONObject reply = new JSONObject();
                                            reply.put("command", "DIRECTORY_DELETE_RESPONSE");
                                            reply.put("pathName", pathName);
                                            reply.put("message", "directory deleted");
                                            reply.put("status", true);
                                            out.write(reply + "\n");
                                            out.flush();
                                        } else
                                        {
                                            JSONObject reply = new JSONObject();
                                            reply.put("command", "DIRECTORY_DELETE_RESPONSE");
                                            reply.put("pathName", pathName);
                                            reply.put("message", "pathname does not exist");
                                            reply.put("status", false);
                                            out.write(reply + "\n");
                                            out.flush();
                                        }
                                    } else
                                    {
                                        JSONObject reply = new JSONObject();
                                        reply.put("command", "DIRECTORY_DELETE_RESPONSE");
                                        reply.put("pathName", pathName);
                                        reply.put("message", "unsafe pathname given");
                                        reply.put("status", false);
                                        out.write(reply + "\n");
                                        out.flush();
                                    }
                                } else
                                {
                                    JSONObject reply = new JSONObject();
                                    reply.put("command", "DIRECTORY_DELETE_RESPONSE");
                                    reply.put("pathName", pathName);
                                    reply.put("message", "there was a problem deleting the directory");
                                    reply.put("status", false);
                                    out.write(reply + "\n");
                                    out.flush();
                                }
                            } catch (Exception e)
                            {
                                System.out.println("There was a problem deleting the directory");
                                JSONObject reply = new JSONObject();
                                reply.put("command", "DIRECTORY_DELETE_RESPONSE");
                                reply.put("pathName", pathName);
                                reply.put("message", "there was a problem deleting the directory");
                                reply.put("status", false);
                                out.write(reply + "\n");
                                out.flush();
                            }
                        }
                        // Handle FILE_CREATE_REQUEST
                        else if (command.get("command").toString().equals("FILE_CREATE_REQUEST"))
                        {
                            String pathName = command.get("pathName").toString();
                            JSONObject fd = (JSONObject) command.get("fileDescriptor");
                            String md5 = fd.get("md5").toString();
                            long lastModified = (long) fd.get("lastModified");
                            long fileSize = (long) fd.get("fileSize");
                            if (f.fileSystemManager.isSafePathName(pathName))
                            {
                                if (f.fileSystemManager.dirNameExists(pathName))    //should be if file already exists, should update or not? no need to delete
                                {                                                   //or should use checkShort cut to skip reCreating the file?
                                    JSONObject reply = new JSONObject();
                                    reply.put("command", "FILE_CREATE_RESPONSE");
                                    reply.put("pathName", pathName);
                                    reply.put("message", "pathName already exists");
                                    reply.put("status", false);
                                    out.write(reply + "\n");
                                    out.flush();
                                } else
                                {
                                    // do here
                                    // create fileLoader
                                    try
                                    {
                                        f.fileSystemManager.createFileLoader(pathName, md5, fileSize, lastModified);
                                    } catch (NoSuchAlgorithmException e)
                                    {
                                        JSONObject reply = new JSONObject();
                                        reply.put("command", "FILE_CREATE_RESPONSE");
                                        reply.put("fileDescriptor", fd);
                                        reply.put("pathName", pathName);
                                        reply.put("message", "there was a problem creating file");
                                        reply.put("status", false);
                                        out.write(reply + "\n");
                                        out.flush();
                                        e.printStackTrace();
                                    }

                                    // FILE_CREATE_RESPONSE
                                    JSONObject reply = new JSONObject();
                                    reply.put("command", "FILE_CREATE_RESPONSE");
                                    reply.put("fileDescriptor", fd);
                                    reply.put("pathName", pathName);
                                    reply.put("message", "file loader ready");
                                    reply.put("status", true);
                                    out.write(reply + "\n");
                                    out.flush();
                                    // FILE_BYTES_REQUEST
                                    if (fileSize <= Long.parseLong(Configuration.getConfigurationValue("blockSize")))
                                    {
                                        JSONObject req = new JSONObject();
                                        req.put("command", "FILE_BYTES_REQUEST");
                                        req.put("fileDescriptor", fd);
                                        req.put("pathName", pathName);
                                        req.put("position", 0);
                                        req.put("length", fileSize);
                                        out.write(req + "\n");
                                        out.flush();
                                    } else
                                    {
                                        long remainingSize = fileSize;
                                        long position = 0;
                                        while (remainingSize > Long.parseLong(Configuration.getConfigurationValue("blockSize")))
                                        {
                                            System.out.println("Large file transfering!");
                                            JSONObject req = new JSONObject();
                                            req.put("command", "FILE_BYTES_REQUEST");
                                            req.put("fileDescriptor", fd);
                                            req.put("pathName", pathName);
                                            req.put("position", position);
                                            req.put("length", Long.parseLong(Configuration.getConfigurationValue("blockSize")));
                                            out.write(req + "\n");
                                            out.flush();
                                            // Update position
                                            position = position + Long.parseLong(Configuration.getConfigurationValue("blockSize"));
                                            remainingSize = remainingSize - Long.parseLong(Configuration.getConfigurationValue("blockSize"));
                                        }
                                        if (remainingSize != 0)
                                        {
                                            JSONObject req = new JSONObject();
                                            req.put("command", "FILE_BYTES_REQUEST");
                                            req.put("fileDescriptor", fd);
                                            req.put("pathName", pathName);
                                            req.put("position", position);
                                            req.put("length", remainingSize);
                                            out.write(req + "\n");
                                            out.flush();
                                        }
                                    }

                                }
                            } else
                            {
                                JSONObject reply = new JSONObject();
                                reply.put("command", "FILE_CREATE_RESPONSE");
                                reply.put("pathName", pathName);
                                reply.put("message", "unsafe pathname given");
                                reply.put("status", false);
                                out.write(reply + "\n");
                                out.flush();
                            }
                        }
                        // Handle FILE_BYTES_RESPONSE
                        else if (command.get("command").toString().equals("FILE_BYTES_RESPONSE"))
                        {
                            String pathName = command.get("pathName").toString();
                            String content = command.get("content").toString();
                            long position = (long) command.get("position");
                            //convert contentStr to byte then put into buffer
                            byte[] arr = Base64.getDecoder().decode(content);
                            ByteBuffer buf = ByteBuffer.wrap(arr);
                            f.fileSystemManager.writeFile(pathName, buf, position);
                            if (f.fileSystemManager.checkWriteComplete(pathName))
                            {
                                System.out.println(pathName + " write complete!");
                            }
                        }
                        //Handle FILE_DELETE_REQUEST
                        else if (command.get("command").toString().equals("FILE_DELETE_REQUEST"))
                        {
                            String pathName = command.get("pathName").toString();
                            JSONObject fd = (JSONObject) command.get("fileDescriptor");
                            String md5 = fd.get("md5").toString();
                            long lastModified = (long) fd.get("lastModified");
                            try
                            {
                                // check safe pathname
                                if (f.fileSystemManager.isSafePathName(pathName))
                                {
                                    // check if dir exists
                                    if (f.fileSystemManager.fileNameExists(pathName))
                                    {
                                        if (f.fileSystemManager.fileNameExists(pathName, md5))
                                        {
                                            boolean ok = f.fileSystemManager.deleteFile(pathName, lastModified, md5);
                                            if (ok)
                                            {
                                                JSONObject reply = new JSONObject();
                                                reply.put("command", "FILE_DELETE_RESPONSE");
                                                reply.put("fileDescriptor", fd);
                                                reply.put("pathName", pathName);
                                                reply.put("message", "file deleted");
                                                reply.put("status", true);
                                                out.write(reply + "\n");
                                                out.flush();
                                            } else
                                            {
                                                JSONObject reply = new JSONObject();
                                                reply.put("command", "FILE_DELETE_RESPONSE");
                                                reply.put("fileDescriptor", fd);
                                                reply.put("pathName", pathName);
                                                reply.put("message", "there is a problem deleting the file");
                                                reply.put("status", false);
                                                out.write(reply + "\n");
                                                out.flush();
                                            }
                                        } else
                                        {
                                            JSONObject reply = new JSONObject();
                                            reply.put("command", "FILE_DELETE_RESPONSE");
                                            reply.put("fileDescriptor", fd);
                                            reply.put("pathName", pathName);
                                            reply.put("message", "md5 does not match");
                                            reply.put("status", false);
                                            out.write(reply + "\n");
                                            out.flush();
                                        }
                                    } else
                                    {
                                        JSONObject reply = new JSONObject();
                                        reply.put("command", "FILE_DELETE_RESPONSE");
                                        reply.put("fileDescriptor", fd);
                                        reply.put("pathName", pathName);
                                        reply.put("message", "pathname does not exist");
                                        reply.put("status", false);
                                        out.write(reply + "\n");
                                        out.flush();
                                    }
                                } else
                                {
                                    JSONObject reply = new JSONObject();
                                    reply.put("command", "FILE_DELETE_RESPONSE");
                                    reply.put("fileDescriptor", fd);
                                    reply.put("pathName", pathName);
                                    reply.put("message", "unsafe pathname given");
                                    reply.put("status", false);
                                    out.write(reply + "\n");
                                    out.flush();
                                }
                            } catch (Exception e)
                            {
                                System.out.println("There was a problem deleting the file");
                                JSONObject reply = new JSONObject();
                                reply.put("command", "FILE_DELETE_RESPONSE");
                                reply.put("fileDescriptor", fd);
                                reply.put("pathName", pathName);
                                reply.put("message", "There was a problem deleting the file");
                                reply.put("status", false);
                                out.write(reply + "\n");
                                out.flush();
                            }
                        }
                        //Handle FILE_MODIFY_REQUEST
                        else if (command.get("command").toString().equals("FILE_MODIFY_REQUEST"))
                        {
                            String pathName = command.get("pathName").toString();
                            JSONObject fd = (JSONObject) command.get("fileDescriptor");
                            String md5 = fd.get("md5").toString();
                            long lastModified = (long) fd.get("lastModified");
                            long fileSize = (long) fd.get("fileSize");
                            if (f.fileSystemManager.modifyFileLoader(pathName, md5, lastModified))
                            {
                                //FILE_MODIFY_RESPONSE
                                JSONObject reply = new JSONObject();
                                reply.put("command", "FILE_MODIFY_RESPONSE");
                                reply.put("fileDescriptor", fd);
                                reply.put("pathName", pathName);
                                reply.put("message", "file loader ready");
                                reply.put("status", true);
                                //FILE_BYTES_REQUEST
                                if (fileSize <= Long.parseLong(Configuration.getConfigurationValue("blockSize")))
                                {
                                    JSONObject req = new JSONObject();
                                    req.put("command", "FILE_BYTES_REQUEST");
                                    req.put("fileDescriptor", fd);
                                    req.put("pathName", pathName);
                                    req.put("position", 0);
                                    req.put("length", fileSize);
                                    out.write(req + "\n");
                                    out.flush();
                                }
                                else
                                {
                                    long remainingSize = fileSize;
                                    long position = 0;
                                    while (remainingSize > Long.parseLong(Configuration.getConfigurationValue("blockSize")))
                                    {
                                        System.out.println("Large file transfering!");
                                        JSONObject req = new JSONObject();
                                        req.put("command", "FILE_BYTES_REQUEST");
                                        req.put("fileDescriptor", fd);
                                        req.put("pathName", pathName);
                                        req.put("position", position);
                                        req.put("length", Long.parseLong(Configuration.getConfigurationValue("blockSize")));
                                        out.write(req + "\n");
                                        out.flush();
                                        // Update position
                                        position = position + Long.parseLong(Configuration.getConfigurationValue("blockSize"));
                                        remainingSize = remainingSize - Long.parseLong(Configuration.getConfigurationValue("blockSize"));
                                    }
                                    if (remainingSize != 0)
                                    {
                                        System.out.println(fileSize);
                                        System.out.println(position);
                                        System.out.println(remainingSize);
                                        JSONObject req = new JSONObject();
                                        req.put("command", "FILE_BYTES_REQUEST");
                                        req.put("fileDescriptor", fd);
                                        req.put("pathName", pathName);
                                        req.put("position", position);
                                        req.put("length", remainingSize);
                                        out.write(req + "\n");
                                        out.flush();
                                    }
                                }
                            }
                        }

                        // If command is invalid
                        else
                        {
                            System.out.println("INVALID_PROTOCOL");
                            JSONObject reply = new JSONObject();
                            reply.put("command", "INVALID_PROTOCOL");
                            reply.put("message", "message must contain a command field as string");
                            out.write(reply + "\n");
                            out.flush();
                        }
                    } else
                    {
                        // If not a JSONObject
                        JSONObject reply = new JSONObject();
                        reply.put("command", "INVALID_PROTOCOL");
                        reply.put("message", "message must contain a command field as string");
                        out.write(reply + "\n");
                        out.flush();
                    }
                }
            } catch (SocketException e)
            {
                System.out.println("closed...");
            } catch (ParseException e)
            {
                e.printStackTrace();
            }
            clientSocket.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
    }


}
