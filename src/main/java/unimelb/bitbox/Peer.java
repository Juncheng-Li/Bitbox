package unimelb.bitbox;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.HostPort;

import javax.crypto.*;
import java.net.*;

public class Peer
{
    private static Logger log = Logger.getLogger(Peer.class.getName());

    public static void main(String[] args) throws IOException, NumberFormatException, NoSuchAlgorithmException
    {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tc] %2$s %4$s: %5$s%n");
        log.info("BitBox Peer starting...");
        Configuration.getConfiguration();

        JSONArray connectedPeer = new JSONArray();
        JSONParser parser = new JSONParser();


        // Peer_clientSide Start here
        String peers = Configuration.getConfigurationValue("peers");
        String[] peersArray = peers.split(" ");
        for (String peer : peersArray)
        {
            try
            {
                //System.out.println(peer);
                HostPort peer_hp = new HostPort(peer);
                Socket socket = new Socket(peer_hp.host, peer_hp.port);
                connectedPeer.add((JSONObject) parser.parse(peer_hp.toDoc().toJson()));
                Peer_clientSide T_client = new Peer_clientSide(socket);
                T_client.start();
            } catch (IOException e)
            {
                System.out.println(peer + " cannot be connected.");
            } catch (ParseException e)
            {
                e.printStackTrace();
            }
        }


        // Peer_serverSide Start here
        listening Listening = new listening();
        Listening.start();


        // Server for Client - not finished

        ServerSocket listeningSocket_client = null;
        Socket clientSocket_client = null;
        try
        {
            listeningSocket_client = new ServerSocket(Integer.parseInt(Configuration.getConfigurationValue("clientPort")));
            while (true)
            {
                System.out.println("listening on port " +
                        Integer.parseInt(Configuration.getConfigurationValue("clientPort")));
                clientSocket_client = listeningSocket_client.accept();
                System.out.println("Secure Client accepted.");

                // Server for Client
                try
                {
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket_client.getInputStream(), "UTF-8"));
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket_client.getOutputStream(), "UTF-8"));
                    SecretKey secretKey = null;

                    String clientMsg = null;
                    while ((clientMsg = in.readLine()) != null)
                    {
                        JSONObject command = (JSONObject) parser.parse(clientMsg);
                        System.out.println("(Server for secure Client)Message from Client: " + command.toJSONString());
                        //Make sure it is an JSONObject
                        if (command.getClass().getName().equals("org.json.simple.JSONObject"))
                        {
                            //Handle encryptedCommand
                            if (command.containsKey("payload"))
                            {
                                if (secretKey != null)
                                {
                                    // Firstly, decrypt the command
                                    JSONObject decryptedCommand = wrapPayload.unWrap(command, secretKey);
                                    // Handle LIST_PEERS_REQUEST
                                    if (decryptedCommand.get("command").equals("LIST_PEERS_REQUEST"))
                                    {
                                        JSONObject reply = new JSONObject();
                                        reply.put("command", "LIST_PEERS_RESPONSE");
                                        reply.put("peers", connectedPeer);
                                        System.out.println("Sent encrypted: " + reply);
                                        out.write(wrapPayload.payload(reply, secretKey) + "\n");
                                        out.flush();
                                    }
                                    // Handle CONNECT_PEER_REQUEST
                                    else if (decryptedCommand.get("command").equals("CONNECT_PEER_REQUEST"))
                                    {
                                        //Connect peer
                                        try
                                        {
                                            //Connect
                                            String host = command.get("host").toString();
                                            int port = Integer.parseInt(command.get("port").toString());
                                            Socket socket = new Socket(host, port);
                                            System.out.println(host + ":" + port + " successfully connected.");
                                            //add to successful connected peerList
                                            JSONObject peer = new JSONObject();
                                            peer.put("host", command.get("host").toString());
                                            peer.put("port", Integer.parseInt(command.get("port").toString()));
                                            connectedPeer.add(peer);
                                            //Start thread
                                            Peer_clientSide T_client = new Peer_clientSide(socket);
                                            T_client.start();
                                            //reply
                                            JSONObject reply = new JSONObject();
                                            reply.put("command", "CONNECT_PEER_RESPONSE");
                                            reply.put("host", host);
                                            reply.put("port", port);
                                            reply.put("status", true);
                                            reply.put("message", "connected to peer");
                                            System.out.println("Sent encrypted: " + reply);
                                            out.write(wrapPayload.payload(reply, secretKey) + "\n");
                                            out.flush();
                                        }
                                        // If connection unsuccessful
                                        catch (IOException e)
                                        {
                                            System.out.println(command.get("host").toString() + ":" +
                                                    command.get("port").toString() + " cannot be connected.");
                                            //reply
                                            JSONObject reply = new JSONObject();
                                            reply.put("command", "CONNECT_PEER_RESPONSE");
                                            reply.put("host", command.get("host").toString());
                                            reply.put("port", Integer.parseInt(command.get("port").toString()));
                                            reply.put("status", false);
                                            reply.put("message", "connection failed");
                                            System.out.println("Sent encrypted: " + reply);
                                            out.write(wrapPayload.payload(reply, secretKey) + "\n");
                                            out.flush();
                                        }
                                    }
                                    // Handle DISCONNECT_PEER_REQUEST
                                    else if (decryptedCommand.get("command").equals("DISCONNECT_PEER_REQUEST"))
                                    {
                                        //Disconnect peer
                                        System.out.println("how to close a socket with address and port number");
                                    }
                                }
                                else
                                {
                                    System.out.println("Secret key is null!");
                                }
                            }
                            else
                            {
                                //Handle AUTH_RESPONSE
                                if (command.get("command").toString().equals("AUTH_REQUEST"))
                                {
                                    String id = command.get("identity").toString();
                                    String[] keys = Configuration.getConfigurationValue("authorized_keys").split(",");
                                    boolean ifContains = false;
                                    for (String key : keys)
                                    {
                                        //See if public key exists
                                        String key_id = key.split(" ")[2];
                                        if (key_id.equals(id))
                                        {
                                            ifContains = true;
                                            System.out.println("Identity exists, creating AES");
                                            //generate AES
                                            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                                            keyGen.init(128);
                                            secretKey = keyGen.generateKey();
                                            //take public key out
                                            PublicKey pubKey = decodeKey.decodeOpenSSH(key);
                                            //encrypt AES with public key
                                            SecureRandom random = new SecureRandom();
                                            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                                            cipher.init(Cipher.ENCRYPT_MODE, pubKey, random);
                                            byte[] encryptedAES = cipher.doFinal(secretKey.getEncoded());
                                            String encryptedAES_Base64 = Base64.getEncoder().encodeToString(encryptedAES);
                                            //reply with AES secret key
                                            JSONObject reply = new JSONObject();
                                            reply.put("command", "AUTH_RESPONSE");
                                            reply.put("AES128", encryptedAES_Base64);
                                            reply.put("status", true);
                                            reply.put("message", "public key found");
                                            System.out.println("Sent: " + reply);
                                            out.write(reply + "\n");
                                            out.flush();
                                        }
                                    }
                                    if (!ifContains)
                                    {
                                        //reply pub key not found
                                        JSONObject reply = new JSONObject();
                                        reply.put("command", "AUTH_RESPONSE");
                                        reply.put("status", false);
                                        reply.put("message", "public key found");
                                        System.out.println("Sent: " + reply);
                                        out.write(reply + "\n");
                                        out.flush();
                                    }
                                } else
                                {
                                    //other options
                                }
                            }

                        }
                        else
                        {
                            // If not a JSONObject
                            JSONObject reply = new JSONObject();
                            reply.put("command", "INVALID_PROTOCOL");
                            reply.put("message", "message must contain a command field as string");
                            System.out.println("sent: " + reply);
                            out.write(reply + "\n");
                            out.flush();
                        }
                    }
                } catch (IOException e)
                {
                    e.printStackTrace();
                } catch (ParseException e)
                {
                    e.printStackTrace();
                } catch (NoSuchProviderException e)
                {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e)
                {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e)
                {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e)
                {
                    e.printStackTrace();
                } catch (BadPaddingException e)
                {
                    e.printStackTrace();
                } catch (InvalidKeyException e)
                {
                    e.printStackTrace();
                } catch (InvalidKeySpecException e)
                {
                    e.printStackTrace();
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
            if (listeningSocket_client != null)
            {
                try
                {
                    listeningSocket_client.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }



    }


}
