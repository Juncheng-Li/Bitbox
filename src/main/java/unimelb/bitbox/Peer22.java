package unimelb.bitbox;

import java.io.*;
import java.nio.file.FileSystems;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.FileSystemManager;

import javax.net.ServerSocketFactory;
import java.net.*;
import java.util.logging.SocketHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Peer22
{
    private static int port = 3000;
    private static int counter = 0;
    private static int count = 0;
    public static void main(String[] args)
    {
        JSONServer();


    }

    public static void servers()
    {
        ServerSocket sSocket = null;
        Socket rSocket = null;
        try
        {
            sSocket = new ServerSocket(port);
            while (true)
            {
                System.out.println("Listening on port: " + port);
                rSocket = sSocket.accept();
                count++;
                System.out.println("Client connection number " + count + " accepted:");
                System.out.println("Remote Port: " + rSocket.getPort());
                System.out.println("Remote Hostname: " + rSocket.getInetAddress().getHostName());
                System.out.println("Local Port: " + rSocket.getLocalPort());

                BufferedReader in = new BufferedReader(new InputStreamReader(rSocket.getInputStream(), "UTF-8"));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(rSocket.getOutputStream(), "UTF-8"));

                String Message = null;
                try
                {
                    while((Message = in.readLine()) != null)
                    {
                        System.out.println("Received message: " + Message + " from " + rSocket.getInetAddress());
                        out.write("Server Ack " + Message + "\n");
                        out.flush();
                        System.out.println("Respond sent");
                    }
                }catch (SocketException e)
                {
                    System.out.println("Closed...");
                }
                rSocket.close();
            }
        } catch (SocketException ex)
        {
            ex.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            if (sSocket != null)
            {
                try
                {
                    sSocket.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

        }
    }

    public static void JSONServer()
    {
        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        try (ServerSocket server = factory.createServerSocket(port))
        {
            System.out.println("Waiting for client connection..");

            // Wait for connections.
            while (true)
            {
                Socket client = server.accept();
                counter++;
                System.out.println("Client " + counter + ": Applying for connection!");


                // Start a new thread for a connection
                Thread t = new Thread(() -> serveClient(client));

                t.start();
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void serveClient(Socket client)
    {
        //Server side
        try (Socket clientSocket = client)
        {

            // The JSON Parser - setting
            JSONParser parser = new JSONParser();
            // Input and Output stream - setting
            DataInputStream input = new DataInputStream(clientSocket.
                    getInputStream());
            DataOutputStream output = new DataOutputStream(clientSocket.
                    getOutputStream());
            // 1. Read client message
            System.out.println("CLIENT: " + input.readUTF());
            // 2. Greeting client by telling it number
            output.writeUTF("Server: Hi Client " + counter + " !!!");

            // Receive more data..
            while (true)
            {
                if (input.available() > 0)
                {
                    // Attempt to convert read data to JSON
                    JSONObject command = (JSONObject) parser.parse(input.readUTF());
                    System.out.println("COMMAND RECEIVED: " + command.toJSONString());
                    // Calculate the result
                    Integer result = parseCommand(command, output);

                }
            }
        } catch (IOException | ParseException e)
        {
            e.printStackTrace();
        }
    }

    private static Integer parseCommand(JSONObject command, DataOutputStream output)
    {

        int result = 0;

        // This section deals with the file handler
        //DIY ensure what Keys are in received JSON
        if (command.get("command_name").equals("GET_FILE"))
        {
            String fileName = (String) command.get("file_name");
            // Check if file exists
            File f = new File("server_files/" + fileName);
            if (f.exists())
            {

                // Send this back to client so that they know what the file is.
                JSONObject trigger = new JSONObject();
                trigger.put("command_name", "SENDING_FILE");
                trigger.put("file_name", "sauron.jpg");
                trigger.put("file_size", f.length());
                try
                {
                    // Send trigger to client
                    output.writeUTF(trigger.toJSONString());

                    // Start sending file
                    RandomAccessFile byteFile = new RandomAccessFile(f, "r");
                    byte[] sendingBuffer = new byte[1024 * 1024];
                    int num;
                    // While there are still bytes to send..
                    while ((num = byteFile.read(sendingBuffer)) > 0)
                    {
                        System.out.println(num);
                        output.write(Arrays.copyOf(sendingBuffer, num));
                    }
                    byteFile.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            } else
            {
                System.out.println("There is no file named as " + fileName);
            }
        }
        // TODO Auto-generated method stub
        return result;
    }
}
