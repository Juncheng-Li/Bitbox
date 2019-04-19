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
import java.util.Scanner;

public class Peer
{
    private static int counter = 0;
    private static int port = 3000;
    private static Logger log = Logger.getLogger(Peer.class.getName());
    private static String ip = "localhost";

    public static void main(String[] args) throws IOException, NumberFormatException, NoSuchAlgorithmException
    {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tc] %2$s %4$s: %5$s%n");
        log.info("BitBox Peer starting...");
        Configuration.getConfiguration();

        new ServerMain();

        //Start of project
        JSONClient();


    }


    public static void ClientConnect()
    {
        //Client side

        Socket socket = null;
        try
        {
            socket = new Socket("localhost", port);
            System.out.println("Connection to port " + port + " established");
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

            Scanner scanner = new Scanner(System.in);
            String inputStr = null;

            //While the user input differs from "exit"
            while (!(inputStr = scanner.nextLine()).equals("exit"))
            {
                out.write(inputStr + "\n");
                out.flush();
                System.out.println("Message sent");

                String received = in.readLine(); // This method blocks until there
                System.out.println("Message received: " + received);
            }

            scanner.close();

        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            if (socket != null)
            {
                try
                {
                    socket.close();
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

    public static void JSONClient()
    {
        try (Socket socket = new Socket(ip, port);)
        {
            // Output and Input Stream
            DataInputStream input = new DataInputStream(socket.
                    getInputStream());
            DataOutputStream output = new DataOutputStream(socket.
                    getOutputStream());

            output.writeUTF("I want to connect!");
            output.flush();

            JSONObject newCommand = new JSONObject();
            newCommand.put("command_name", "GET_FILE");
            newCommand.put("file_name", "sauron.jpg");

            System.out.println(newCommand.toJSONString());

            // Read hello from server..
            String message = input.readUTF();
            System.out.println(message);

            // Send RMI to Server
            output.writeUTF(newCommand.toJSONString());
            output.flush();

            JSONParser parser = new JSONParser();

            // Print out results received from server..
            while (true)
            {
                if (input.available() > 0)
                {

                    String result = input.readUTF();
                    System.out.println("Received from server: " + result);

                    JSONObject command = (JSONObject) parser.parse(result);


                    // Check the command name
                    if (command.containsKey("command_name"))
                    {

                        if (command.get("command_name").equals("SENDING_FILE"))
                        {

                            // The file location
                            String fileName = "client_files/" + command.get("file_name");

                            // Create a RandomAccessFile to read and write the output file.
                            RandomAccessFile downloadingFile = new RandomAccessFile(fileName, "rw");

                            // Find out how much size is remaining to get from the server.
                            long fileSizeRemaining = (Long) command.get("file_size");

                            int chunkSize = setChunkSize(fileSizeRemaining);

                            // Represents the receiving buffer
                            byte[] receiveBuffer = new byte[chunkSize];

                            // Variable used to read if there are remaining size left to read.
                            int num;

                            System.out.println("Downloading " + fileName + " of size " + fileSizeRemaining);
                            while ((num = input.read(receiveBuffer)) > 0)
                            {
                                // Write the received bytes into the RandomAccessFile
                                downloadingFile.write(Arrays.copyOf(receiveBuffer, num));

                                // Reduce the file size left to read..
                                fileSizeRemaining -= num;

                                // Set the chunkSize again
                                chunkSize = setChunkSize(fileSizeRemaining);
                                receiveBuffer = new byte[chunkSize];

                                // If you're done then break
                                if (fileSizeRemaining == 0)
                                {
                                    break;
                                }
                            }
                            System.out.println("File received!");
                            downloadingFile.close();
                        }
                    }
                }
            }

        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {

        } catch (ParseException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static int setChunkSize(long fileSizeRemaining)
    {
        // Determine the chunkSize
        int chunkSize = 1024 * 1024;

        // If the file size remaining is less than the chunk size
        // then set the chunk size to be equal to the file size.
        if (fileSizeRemaining < chunkSize)
        {
            chunkSize = (int) fileSizeRemaining;
        }

        return chunkSize;
    }
}
