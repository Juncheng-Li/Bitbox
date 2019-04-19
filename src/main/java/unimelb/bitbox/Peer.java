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

import java.net.*;
import java.util.Scanner;

public class Peer
{
    private static int counter = 0;
    private static int port = 3000;
    private static Logger log = Logger.getLogger(Peer.class.getName());

    public static void main(String[] args) throws IOException, NumberFormatException, NoSuchAlgorithmException
    {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tc] %2$s %4$s: %5$s%n");
        log.info("BitBox Peer starting...");
        Configuration.getConfiguration();

        new ServerMain();

        //Start of project



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

    public static void ServerSide(Socket client)
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
