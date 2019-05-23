package unimelb.bitbox;

import org.json.simple.JSONObject;
import unimelb.bitbox.util.Configuration;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class udpCommNProcess extends Thread
{
    private JSONObject command;
    private ServerMain f;
    private InetAddress ip;
    private int udpPort;

    public udpCommNProcess(JSONObject command, InetAddress ip, int udpPort, ServerMain f) throws IOException
    {
        this.command = command;
        this.ip = ip;
        this.udpPort = udpPort;
        this.f = f;
    }

    public void run()
    {
        try
        {
            // Handle DIRECTORY_CREATE_REQUEST
            if (command.get("command").toString().equals("DIRECTORY_CREATE_REQUEST"))
            {
                String pathName = command.get("pathName").toString();
                try
                {
                    if (f.fileSystemManager.isSafePathName(pathName))
                    {
                        // Check if parent dirs exists when pathName contains parent dir:
                        if (pathName.contains("/"))
                        {
                            File dir = new File(pathName);
                            String parentDir = dir.getParent();
                            while (!f.fileSystemManager.dirNameExists(parentDir))
                            {
                                //Do nothing and Wait
                            }
                            System.out.println("Checking parent dir, wait for creating if does not exist");
                            // Create dir when parent dir is ready
                            f.fileSystemManager.makeDirectory(pathName);
                            JSONObject reply = new JSONObject();
                            reply.put("command", "DIRECTORY_CREATE_RESPONSE");
                            reply.put("pathName", pathName);
                            reply.put("message", "directory created");
                            reply.put("status", true);
                            send(reply, ip, udpPort);
                        }

                        if (f.fileSystemManager.dirNameExists(pathName))
                        {
                            JSONObject reply = new JSONObject();
                            reply.put("command", "DIRECTORY_CREATE_RESPONSE");
                            reply.put("pathName", pathName);
                            reply.put("message", "pathname already exists");
                            reply.put("status", false);
                            send(reply, ip, udpPort);
                        } else
                        {
                            f.fileSystemManager.makeDirectory(pathName);
                            JSONObject reply = new JSONObject();
                            reply.put("command", "DIRECTORY_CREATE_RESPONSE");
                            reply.put("pathName", pathName);
                            reply.put("message", "directory created");
                            reply.put("status", true);
                            send(reply, ip, udpPort);
                        }
                    } else
                    {
                        JSONObject reply = new JSONObject();
                        reply.put("command", "DIRECTORY_CREATE_RESPONSE");
                        reply.put("pathName", pathName);
                        reply.put("message", "unsafe pathname given");
                        reply.put("status", false);
                        send(reply, ip, udpPort);
                    }

                } catch (Exception e)
                {
                    JSONObject reply = new JSONObject();
                    reply.put("command", "DIRECTORY_CREATE_RESPONSE");
                    reply.put("pathName", pathName);
                    reply.put("message", "there was a problem creating the directory");
                    reply.put("status", false);
                    send(reply, ip, udpPort);
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
                            send(reply, ip, udpPort);
                        } else
                        {
                            JSONObject reply = new JSONObject();
                            reply.put("command", "DIRECTORY_DELETE_RESPONSE");
                            reply.put("pathName", pathName);
                            reply.put("message", "pathname does not exist");
                            reply.put("status", false);
                            send(reply, ip, udpPort);
                        }
                    } else
                    {
                        JSONObject reply = new JSONObject();
                        reply.put("command", "DIRECTORY_DELETE_RESPONSE");
                        reply.put("pathName", pathName);
                        reply.put("message", "unsafe pathname given");
                        reply.put("status", false);
                        send(reply, ip, udpPort);
                    }
                } catch (Exception e)
                {
                    System.out.println("There was a problem deleting the directory");
                    JSONObject reply = new JSONObject();
                    reply.put("command", "DIRECTORY_DELETE_RESPONSE");
                    reply.put("pathName", pathName);
                    reply.put("message", "there was a problem deleting the directory");
                    reply.put("status", false);
                    send(reply, ip, udpPort);
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
                    // Check if parent dirs exists when pathName contains parent dir:
                    if (pathName.contains("/"))
                    {
                        File dir = new File(pathName);
                        String parentDir = dir.getParent();
                        System.out.println("Check parent dir, wait for creating if does not exist");
                        while (!f.fileSystemManager.dirNameExists(parentDir))
                        {
                            //Do nothing and Wait
                        }
                        // Create file loader and ask for bytes when parent dir is ready
                        // check if file with same content exists?
                        if (f.fileSystemManager.fileNameExists(pathName, md5))    //should be if file already exists, should update or not? no need to delete
                        {                                                   //or should use checkShort cut to skip reCreating the file?
                            JSONObject reply = new JSONObject();
                            reply.put("command", "FILE_CREATE_RESPONSE");
                            reply.put("fileDescriptor", fd);
                            reply.put("pathName", pathName);
                            reply.put("message", "file with same content already exists");
                            reply.put("status", false);
                            send(reply, ip, udpPort);
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
                                send(reply, ip, udpPort);
                                e.printStackTrace();
                            }

                            // FILE_CREATE_RESPONSE
                            JSONObject reply = new JSONObject();
                            reply.put("command", "FILE_CREATE_RESPONSE");
                            reply.put("fileDescriptor", fd);
                            reply.put("pathName", pathName);
                            reply.put("message", "file loader ready");
                            reply.put("status", true);
                            send(reply, ip, udpPort);
                            // FILE_BYTES_REQUEST
                            if (fileSize <= Long.parseLong(Configuration.getConfigurationValue("blockSize")))
                            {
                                JSONObject req = new JSONObject();
                                req.put("command", "FILE_BYTES_REQUEST");
                                req.put("fileDescriptor", fd);
                                req.put("pathName", pathName);
                                req.put("position", 0);
                                req.put("length", fileSize);
                                send(req, ip, udpPort);
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
                                    send(req, ip, udpPort);
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
                                    send(req, ip, udpPort);
                                }
                            }
                        }
                    }

                    if (f.fileSystemManager.fileNameExists(pathName, md5))    //should be if file already exists, should update or not? no need to delete
                    {                                                   //or should use checkShort cut to skip reCreating the file?
                        JSONObject reply = new JSONObject();
                        reply.put("command", "FILE_CREATE_RESPONSE");
                        reply.put("fileDescriptor", fd);
                        reply.put("pathName", pathName);
                        reply.put("message", "file with same content already exists");
                        reply.put("status", false);
                        send(reply, ip, udpPort);
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
                            send(reply, ip, udpPort);
                            e.printStackTrace();
                        }

                        // FILE_CREATE_RESPONSE
                        JSONObject reply = new JSONObject();
                        reply.put("command", "FILE_CREATE_RESPONSE");
                        reply.put("fileDescriptor", fd);
                        reply.put("pathName", pathName);
                        reply.put("message", "file loader ready");
                        reply.put("status", true);
                        send(reply, ip, udpPort);
                        // FILE_BYTES_REQUEST
                        if (fileSize <= Long.parseLong(Configuration.getConfigurationValue("blockSize")))
                        {
                            JSONObject req = new JSONObject();
                            req.put("command", "FILE_BYTES_REQUEST");
                            req.put("fileDescriptor", fd);
                            req.put("pathName", pathName);
                            req.put("position", 0);
                            req.put("length", fileSize);
                            send(req, ip, udpPort);
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
                                send(req, ip, udpPort);
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
                                send(req, ip, udpPort);
                            }
                        }
                    }
                } else
                {
                    JSONObject reply = new JSONObject();
                    reply.put("command", "FILE_CREATE_RESPONSE");
                    reply.put("fileDescriptor", fd);
                    reply.put("pathName", pathName);
                    reply.put("message", "unsafe pathname given");
                    reply.put("status", false);
                    send(reply, ip, udpPort);
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
                                    send(reply, ip, udpPort);
                                } else
                                {
                                    JSONObject reply = new JSONObject();
                                    reply.put("command", "FILE_DELETE_RESPONSE");
                                    reply.put("fileDescriptor", fd);
                                    reply.put("pathName", pathName);
                                    reply.put("message", "there is a problem deleting the file");
                                    reply.put("status", false);
                                    send(reply, ip, udpPort);
                                }
                            } else
                            {
                                JSONObject reply = new JSONObject();
                                reply.put("command", "FILE_DELETE_RESPONSE");
                                reply.put("fileDescriptor", fd);
                                reply.put("pathName", pathName);
                                reply.put("message", "md5 does not match");
                                reply.put("status", false);
                                send(reply, ip, udpPort);
                            }
                        } else
                        {
                            JSONObject reply = new JSONObject();
                            reply.put("command", "FILE_DELETE_RESPONSE");
                            reply.put("fileDescriptor", fd);
                            reply.put("pathName", pathName);
                            reply.put("message", "pathname does not exist");
                            reply.put("status", false);
                            send(reply, ip, udpPort);
                        }
                    } else
                    {
                        JSONObject reply = new JSONObject();
                        reply.put("command", "FILE_DELETE_RESPONSE");
                        reply.put("fileDescriptor", fd);
                        reply.put("pathName", pathName);
                        reply.put("message", "unsafe pathname given");
                        reply.put("status", false);
                        send(reply, ip, udpPort);
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
                    send(reply, ip, udpPort);
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
                    send(reply, ip, udpPort);
                    //FILE_BYTES_REQUEST
                    if (fileSize <= Long.parseLong(Configuration.getConfigurationValue("blockSize")))
                    {
                        JSONObject req = new JSONObject();
                        req.put("command", "FILE_BYTES_REQUEST");
                        req.put("fileDescriptor", fd);
                        req.put("pathName", pathName);
                        req.put("position", 0);
                        req.put("length", fileSize);
                        send(req, ip, udpPort);
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
                            send(req, ip, udpPort);
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
                            send(req, ip, udpPort);
                        }
                    }
                } else
                {
                    JSONObject reply = new JSONObject();
                    reply.put("command", "FILE_MODIFY_RESPONSE");
                    reply.put("fileDescriptor", fd);
                    reply.put("pathName", pathName);
                    reply.put("message", "file with same content");
                    reply.put("status", false);
                    send(reply, ip, udpPort);
                }
            }
            // Moved from Client
            else if (command.get("command").toString().equals("FILE_BYTES_REQUEST"))
            {
                // Unmarshall request
                String pathName = command.get("pathName").toString();
                JSONObject fd = (JSONObject) command.get("fileDescriptor");
                String md5 = fd.get("md5").toString();
                long lastModified = (long) fd.get("lastModified");
                long fileSize = (long) fd.get("fileSize");
                long position = (long) command.get("position");
                long length = (long) command.get("length");

                ByteBuffer buf = f.fileSystemManager.readFile(md5, position, length);
                buf.rewind();
                byte[] arr = new byte[buf.remaining()];
                buf.get(arr, 0, arr.length);
                String content = Base64.getEncoder().encodeToString(arr);

                // Send BYTE
                JSONObject rep = new JSONObject();
                rep.put("command", "FILE_BYTES_RESPONSE");
                rep.put("fileDescriptor", fd);
                rep.put("pathName", pathName);
                rep.put("position", position);    //changed position from 0 to position
                rep.put("length", length);
                rep.put("content", content);
                rep.put("message", "successful read");
                rep.put("status", true);
                send(rep, ip, udpPort);
            } else if (command.get("command").toString().equals("DIRECTORY_CREATE_RESPONSE") ||
                    command.get("command").toString().equals("DIRECTORY_DELETE_RESPONSE") ||
                    command.get("command").toString().equals("FILE_DELETE_RESPONSE") ||
                    command.get("command").toString().equals("FILE_CREATE_RESPONSE") ||
                    command.get("command").toString().equals("FILE_MODIFY_RESPONSE") ||
                    command.get("command").toString().equals("INVALID_PROTOCOL"))
            {
                // Do nothing when receive these responses
            }
            // If command is invalid
            else
            {
                //System.out.println("INVALID_PROTOCOL");
                JSONObject reply = new JSONObject();
                reply.put("command", "INVALID_PROTOCOL");
                reply.put("message", "message must contain a command field as string");
                send(reply, ip, udpPort);
            }

        } catch (SocketException e)
        {
            System.out.println(e + "in class udpCommNProcess");

        } catch (IOException e)
        {
            // Includes no dir
            System.out.println("IOException cought!!!!!!!!!!!!!!!!!");
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e)
        {
            System.out.println(e + "in class udpCommNProcess");
        }
    }


    public static void send(JSONObject message, InetAddress ip, int udpPort) throws IOException
    {
        DatagramSocket dsSocket = new DatagramSocket();
        byte[] buf = message.toJSONString().getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, ip, udpPort);
        dsSocket.send(packet);
        System.out.println("udp sent: " + message.toJSONString());
    }

}
