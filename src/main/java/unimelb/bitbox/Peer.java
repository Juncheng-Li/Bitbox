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
        Client T1 = new Client("peer4", "localhost", 3000);
        //T1.start();

        ServerSocket listeningSocket = null;
        Socket clientSocket = null;
        int i = 0; //counter to keep track of the number of clients
        try
        {
            listeningSocket = new ServerSocket(3000);
            while(true)
            {
                System.out.println("listeining on port 3000");
                clientSocket = listeningSocket.accept();
                i++;
                System.out.println("Client " + i + " accepted.");
                if(i<=10)
                {
                    Server T2 = new Server("peer4 server", 3000, clientSocket, i);
                    T2.start();
                    //ExecutorService pool = Executors.newFixedThreadPool(10);
                    //pool.execute(T2);
                }
                else
                {
                    System.out.println("Maximum 10 clients reached");
                }

            }


        } catch (SocketException ex)
        {
            ex.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            if (listeningSocket != null)
            {
                try
                {
                    listeningSocket.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }


    }










    //Client side

    public static void ClientConnect()
    {
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



    public static void JSONClient()
    {
        try (Socket socket = new Socket(ip, port))
        {
            // 0.
            String fileName = "mini_black_hole.jpg";
            // 0. Output and Input Stream, parser - setting
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            JSONParser parser = new JSONParser();
            // 0. Write a message
            output.writeUTF("Connecting ... " + ip + " " + port);
            output.flush();

            // 0. Read hello from server..
            String message = input.readUTF();
            System.out.println(message);

            // 1. Ready the object
            JSONObject newCommand = new JSONObject();
            newCommand.put("command_name", "GET_FILE");
            newCommand.put("file_name", fileName);
            // 1. Show the object on local
            System.out.println(newCommand.toJSONString());

            // 2. Send prepared object(RMI) to Server
            output.writeUTF(newCommand.toJSONString());
            output.flush();

            // 3. Receive and parse reply received from server
            while (true)
            {
                if (input.available() > 0)
                {
                    //3.1 Receive reply
                    String reply = input.readUTF();
                    System.out.println("Received from server: " + reply);
                    //3.2 Parse reply
                    JSONObject command = (JSONObject) parser.parse(reply);
                    System.out.println("Command: " + command);
                    //3.3 Interpret the reply
                    // Check the command name
                    if (command.containsKey("command_name"))
                    {
                        // Command = "SENDING_FILE"
                        if (command.get("command_name").equals("SENDING_FILE"))
                        {

                            // 1. Set The download location
                            String downloadPath = "share/" + command.get("file_name");

                            // 2. Create a RandomAccessFile to read and write the output file to disk.
                            RandomAccessFile downloadingFile = new RandomAccessFile(downloadPath, "rw");

                            // 3. Find out how much size is remaining to get from the server.
                            long fileSizeRemaining = (Long) command.get("file_size");

                            int chunkSize = setChunkSize(fileSizeRemaining);

                            // Represents the receiving buffer
                            byte[] receiveBuffer = new byte[chunkSize];

                            // Variable used to read if there are remaining size left to read.
                            int num;

                            System.out.println("Downloading " + downloadPath + " of size " + fileSizeRemaining);
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
            e.printStackTrace();
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
