package unimelb.bitbox;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ServerSocketFactory;
import java.io.*;
import java.net.*;
import java.nio.Buffer;
import java.security.NoSuchAlgorithmException;
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

import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.FileSystemManager;

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

    Server(String threadname, int port, Socket clientSocket, int i, ServerMain f)
    {
        this.threadName = threadname;
        this.port = port;
        this.clientSocket = clientSocket;
        this.i = i;
        this.f = f;
    }


    public void run()
    {
        System.out.println("Thread: " + threadName + "-" + i + " started...");
        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
            JSONParser parser = new JSONParser();
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
                            System.out.println("Respond flushed");
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }

                    }
                    // Handle DIRECTORY_CREATE_REQUEST
                    else if (command.get("command").toString().equals("DIRECTORY_CREATE_REQUEST"))
                    {
                        String pathName = command.get("pathName").toString();
                        try
                        {
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
                        String pathName = command.get("pathName").toString();
                        try
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
                        } catch (Exception e)
                        {
                            JSONObject reply = new JSONObject();
                            reply.put("command", "DIRECTORY_DELETE_RESPONSE");
                            reply.put("pathName", pathName);
                            reply.put("message", "there was a problem deleting the directory");
                            reply.put("status", false);
                            out.write(reply + "\n");
                            out.flush();
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
        }
    }

    public void doCommand(JSONObject command, BufferedWriter out)
    {
        if (command.get("command").equals("HANDSHAKE_REQUEST"))
        {
            JSONObject hs_res = new JSONObject();
            hs_res.put("command", "HANDSHAKE_RESPONSE");
            JSONObject hostPort = new JSONObject();
            hostPort.put("host", "localhost");
            hostPort.put("port", port);
            hs_res.put("hostPort", hostPort);
            try
            {
                out.write(hs_res.toJSONString());
                out.flush();
                System.out.println("Respond flushed");
            } catch (IOException e)
            {
                e.printStackTrace();
            }

        }
    }


}
