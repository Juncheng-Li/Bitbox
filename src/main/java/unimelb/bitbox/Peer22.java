package unimelb.bitbox;

import java.io.*;
import java.nio.file.FileSystems;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.FileSystemManager;

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
    public static void main(String[] args)
    {
        //ServerSocket listeningsocket = null;
        ServerSocket sSocket = null;
        Socket rSocket = null;
        int port = 3400;
        int count = 0;

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
}
