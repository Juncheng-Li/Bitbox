package unimelb.bitbox;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;

import java.io.*;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.net.*;
import java.util.Timer;

public class commNProcess extends Thread
{
    private JSONObject command;
    private BufferedReader in;
    private BufferedWriter out;
    private ServerMain f;
    private Socket socket;
    private Timer timer = new Timer();

    public commNProcess(JSONObject command, Socket socket, ServerMain f) throws IOException
    {
        this.command = command;
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
        this.out = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream(), "UTF-8"));
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
                            System.out.println("sent: " + reply);
                            out.write(reply.toJSONString() + "\n");
                            out.flush();
                        }

                        if (f.fileSystemManager.dirNameExists(pathName))
                        {
                            JSONObject reply = new JSONObject();
                            reply.put("command", "DIRECTORY_CREATE_RESPONSE");
                            reply.put("pathName", pathName);
                            reply.put("message", "pathname already exists");
                            reply.put("status", false);
                            System.out.println("sent: " + reply);
                            out.write(reply.toJSONString() + "\n");
                            out.flush();
                        } else
                        {
                            f.fileSystemManager.makeDirectory(pathName);
                            JSONObject reply = new JSONObject();
                            reply.put("command", "DIRECTORY_CREATE_RESPONSE");
                            reply.put("pathName", pathName);
                            reply.put("message", "directory created");
                            reply.put("status", true);
                            System.out.println("sent: " + reply);
                            out.write(reply.toJSONString() + "\n");
                            out.flush();
                        }
                    } else
                    {
                        JSONObject reply = new JSONObject();
                        reply.put("command", "DIRECTORY_CREATE_RESPONSE");
                        reply.put("pathName", pathName);
                        reply.put("message", "unsafe pathname given");
                        reply.put("status", false);
                        System.out.println("sent: " + reply);
                        out.write(reply.toJSONString() + "\n");
                        out.flush();
                    }

                } catch (Exception e)
                {
                    JSONObject reply = new JSONObject();
                    reply.put("command", "DIRECTORY_CREATE_RESPONSE");
                    reply.put("pathName", pathName);
                    reply.put("message", "there was a problem creating the directory");
                    reply.put("status", false);
                    System.out.println("sent: " + reply);
                    out.write(reply.toJSONString() + "\n");
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
                            System.out.println("sent: " + reply);
                            out.write(reply.toJSONString() + "\n");
                            out.flush();
                        } else
                        {
                            JSONObject reply = new JSONObject();
                            reply.put("command", "DIRECTORY_DELETE_RESPONSE");
                            reply.put("pathName", pathName);
                            reply.put("message", "pathname does not exist");
                            reply.put("status", false);
                            System.out.println("sent: " + reply);
                            out.write(reply.toJSONString() + "\n");
                            out.flush();
                        }
                    } else
                    {
                        JSONObject reply = new JSONObject();
                        reply.put("command", "DIRECTORY_DELETE_RESPONSE");
                        reply.put("pathName", pathName);
                        reply.put("message", "unsafe pathname given");
                        reply.put("status", false);
                        System.out.println("sent: " + reply);
                        out.write(reply.toJSONString() + "\n");
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
                    System.out.println("sent: " + reply);
                    out.write(reply.toJSONString() + "\n");
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
                            System.out.println("sent: " + reply);
                            out.write(reply.toJSONString() + "\n");
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
                                System.out.println("sent: " + reply);
                                out.write(reply.toJSONString() + "\n");
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
                            System.out.println("sent: " + reply);
                            out.write(reply.toJSONString() + "\n");
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
                                System.out.println("sent: " + req);
                                out.write(req.toJSONString() + "\n");
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
                                    System.out.println("sent: " + req);
                                    out.write(req.toJSONString() + "\n");
                                    out.flush();
                                    // Update position
                                    position = position + Long.parseLong(Configuration.getConfigurationValue("blockSize"));
                                    remainingSize = remainingSize - Long.parseLong(Configuration.getConfigurationValue("blockSize"));
                                    try
                                    {
                                        Thread.sleep(20);
                                    }
                                    catch (InterruptedException e)
                                    {
                                        System.out.println("File_byte_request in commNProcess - Thread.sleep() interrupted");
                                        e.printStackTrace();
                                    }
                                }
                                if (remainingSize != 0)
                                {
                                    JSONObject req = new JSONObject();
                                    req.put("command", "FILE_BYTES_REQUEST");
                                    req.put("fileDescriptor", fd);
                                    req.put("pathName", pathName);
                                    req.put("position", position);
                                    req.put("length", remainingSize);
                                    System.out.println("sent: " + req);
                                    out.write(req.toJSONString() + "\n");
                                    out.flush();
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
                        System.out.println("sent: " + reply);
                        out.write(reply.toJSONString() + "\n");
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
                            System.out.println("sent: " + reply);
                            out.write(reply.toJSONString() + "\n");
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
                        System.out.println("sent: " + reply);
                        out.write(reply.toJSONString() + "\n");
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
                            System.out.println("sent: " + req);
                            out.write(req.toJSONString() + "\n");
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
                                System.out.println("sent: " + req);
                                out.write(req.toJSONString() + "\n");
                                out.flush();
                                // Update position
                                position = position + Long.parseLong(Configuration.getConfigurationValue("blockSize"));
                                remainingSize = remainingSize - Long.parseLong(Configuration.getConfigurationValue("blockSize"));
                                try
                                {
                                    Thread.sleep(20);
                                }
                                catch (InterruptedException e)
                                {
                                    System.out.println("File_byte_request in commNProcess - Thread.sleep() interrupted");
                                    e.printStackTrace();
                                }
                            }
                            if (remainingSize != 0)
                            {
                                JSONObject req = new JSONObject();
                                req.put("command", "FILE_BYTES_REQUEST");
                                req.put("fileDescriptor", fd);
                                req.put("pathName", pathName);
                                req.put("position", position);
                                req.put("length", remainingSize);
                                System.out.println("sent: " + req);
                                out.write(req.toJSONString() + "\n");
                                out.flush();
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
                    System.out.println("sent: " + reply);
                    out.write(reply.toJSONString() + "\n");
                    out.flush();
                }
            }
            // Handle FILE_BYTES_RESPONSE
            else if (command.get("command").toString().equals("FILE_BYTES_RESPONSE"))
            {
                String pathName = command.get("pathName").toString();
                String content = command.get("content").toString();
                long position = (long) command.get("position");
                long length = (long) command.get("length");

                JSONObject fd = (JSONObject) command.get("fileDescriptor");
                long fileSize = (long) fd.get("fileSize");

                //convert contentStr to byte then put into buffer
                byte[] arr = Base64.getDecoder().decode(content);
                ByteBuffer buf = ByteBuffer.wrap(arr);
                //Writing file
                System.out.println("Writing file " + pathName + " Position: " + position);
                f.fileSystemManager.writeFile(pathName, buf, position);

                if (fileSize - position == length)
                {
                    if (f.fileSystemManager.checkWriteComplete(pathName))
                    {
                        System.out.println(pathName + " write complete!");
                    }
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
                                    System.out.println("sent: " + reply);
                                    out.write(reply.toJSONString() + "\n");
                                    out.flush();
                                } else
                                {
                                    JSONObject reply = new JSONObject();
                                    reply.put("command", "FILE_DELETE_RESPONSE");
                                    reply.put("fileDescriptor", fd);
                                    reply.put("pathName", pathName);
                                    reply.put("message", "there is a problem deleting the file");
                                    reply.put("status", false);
                                    System.out.println("sent: " + reply);
                                    out.write(reply.toJSONString() + "\n");
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
                                System.out.println("sent: " + reply);
                                out.write(reply.toJSONString() + "\n");
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
                            System.out.println("sent: " + reply);
                            out.write(reply.toJSONString() + "\n");
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
                        System.out.println("sent: " + reply);
                        out.write(reply.toJSONString() + "\n");
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
                    System.out.println("sent: " + reply);
                    out.write(reply.toJSONString() + "\n");
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
                    System.out.println("sent: " + reply);
                    out.write(reply.toJSONString() + "\n");
                    out.flush();
                    //FILE_BYTES_REQUEST
                    if (fileSize <= Long.parseLong(Configuration.getConfigurationValue("blockSize")))
                    {
                        JSONObject req = new JSONObject();
                        req.put("command", "FILE_BYTES_REQUEST");
                        req.put("fileDescriptor", fd);
                        req.put("pathName", pathName);
                        req.put("position", 0);
                        req.put("length", fileSize);
                        System.out.println("sent: " + req);
                        out.write(req.toJSONString() + "\n");
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
                            System.out.println("sent: " + req);
                            out.write(req.toJSONString() + "\n");
                            out.flush();
                            // Update position
                            position = position + Long.parseLong(Configuration.getConfigurationValue("blockSize"));
                            remainingSize = remainingSize - Long.parseLong(Configuration.getConfigurationValue("blockSize"));
                            try
                            {
                                Thread.sleep(20);
                            }
                            catch (InterruptedException e)
                            {
                                System.out.println("File_byte_request in commNProcess - Thread.sleep() interrupted");
                                e.printStackTrace();
                            }
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
                            System.out.println("sent: " + req);
                            out.write(req.toJSONString() + "\n");
                            out.flush();
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
                    System.out.println("sent: " + reply);
                    out.write(reply.toJSONString() + "\n");
                    out.flush();
                }
            }
            // Moved from Client
            else if (command.get("command").toString().equals("FILE_BYTES_REQUEST"))
            {
                // Unmarshall request
                String pathName = command.get("pathName").toString();
                JSONObject fd = (JSONObject) command.get("fileDescriptor");
                //Document fd2 = (Document) command.get("fileDescriptor");
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
                //System.out.println("Content: " + content);
                System.out.println();

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
                String mess = rep.toJSONString();
                out.write(mess);
                out.newLine();
                out.flush();
                System.out.println("sent: " + mess);


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
                System.out.println("sent: " + reply);
                out.write(reply.toJSONString() + "\n");
                out.flush();
            }

        } catch (SocketException e)
        {
            System.out.println("Socket Exception cought!!!!!!!!!!!!!!!!!!!!!!!!!");
            try
            {
                System.out.println(e.toString());
                //e.printStackTrace();
                socket.close();
                System.out.println("Socket closed.");
            } catch (IOException ee)
            {
                ee.printStackTrace();
            }
        } catch (IOException e)
        {
            // Includes no dir
            System.out.println("IOException cought!!!!!!!!!!!!!!!!!");
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
    }

    public static void send(Document message, BufferedWriter out) throws IOException
    {
        out.write(message.toJson() + "\n");
        out.flush();
        System.out.println("sent: " + message.toJson());
    }

}
