package unimelb.bitbox;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.Document;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;   //maybe public key is not X509?

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import unimelb.bitbox.util.HostPort;


public class Client
{
    private final static String keyDir = "bitboxclient-rsa";
    private static PrivateKey priv = null;
    private static SecretKey secretKey = null;

    public static void main(String[] args) throws FileNotFoundException, IOException, NoSuchAlgorithmException,
            NoSuchProviderException, ParseException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException
    {
        CmdLineArgs argsBean = new CmdLineArgs();
        CmdLineParser cmdParser = new CmdLineParser(argsBean);
        try
        {
            //Parse the arguments
            cmdParser.parseArgument(args);
            System.out.println("Command name: " + argsBean.getCommandName());
            System.out.println("Server hostPort: " + argsBean.getServer());
            System.out.println("Peer hostPort: " + argsBean.getPeer());
            System.out.println("Identity: " + argsBean.getId());
        }
        catch (CmdLineException e)
        {
            System.err.println(e.getMessage());
            //Print the usage to help the user understand the arguments expected
            //by the program
            cmdParser.printUsage(System.err);
        }

        //Connect to server
        HostPort server = new HostPort(argsBean.getServer());
        Socket socket = new Socket(server.host, server.port);
        System.out.println("Secure Client connection established.");
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        JSONObject req = new JSONObject();
        req.put("command", "AUTH_REQUEST");
        req.put("identity", argsBean.getId());
        System.out.println(req.toJSONString());
        out.write("sent: " + req + "\n");
        out.flush();

        // Receive incoming reply
        JSONParser parser = new JSONParser();
        String message = null;
        while ((message = in.readLine()) != null)
        {
            JSONObject command = (JSONObject) parser.parse(message);
            System.out.println("Message from peer: " + command.toJSONString());

            //If encrypted message
            if (command.containsKey("payload"))
            {
                JSONObject decryptedCommand = wrapPayload.unWrap(command, secretKey);
                System.out.println(decryptedCommand);
            }

            //If server has key
            if (command.get("command").equals("AUTH_RESPONSE") && ((boolean) command.get("status")))
            {
                //Get PrivateKey, ready to deEncrypt AES
                Security.addProvider(new BouncyCastleProvider());
                System.out.println("BouncyCastle provider added.");
                KeyFactory factory = KeyFactory.getInstance("RSA", "BC");
                try
                {
                    priv = generatePrivateKey(factory, keyDir);
                    System.out.println(String.format("Instantiated private key: %s", priv));
                }
                catch (InvalidKeySpecException e)
                {
                    e.printStackTrace();
                }

                //Decode AES
                String secretKey_Encrypted_Base64 = command.get("AES128").toString();
                byte [] secretKey_Encrypted = Base64.getDecoder().decode(secretKey_Encrypted_Base64);
                //Decrypt AES
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.DECRYPT_MODE, priv);
                secretKey = new SecretKeySpec(cipher.doFinal(secretKey_Encrypted), "AES");
                //If argument is list_peer
                if (argsBean.getCommandName().equals("list_peers"))
                {
                    JSONObject req1 = new JSONObject();
                    req1.put("command", "LIST_PEERS_REQUEST");
                    System.out.println("Sent encrypted: " + req1);
                    //Encrypt and encode
                    JSONObject payLoad = wrapPayload.payload(req1, secretKey);
                    //Send out payload
                    out.write(payLoad + "\n");
                    out.flush();
                }
                //If argument is connect_peer
                else if (argsBean.getCommandName().equals("connect_peer"))
                {
                    JSONObject req1 = new JSONObject();
                    HostPort peer = new HostPort(argsBean.getPeer());
                    req1.put("command", "CONNECT_PEER_REQUEST");
                    req1.put("host", peer.host);
                    req1.put("port", peer.port);
                    System.out.println("Sent encrypted: " + req1);
                    //Encrypt and encode
                    JSONObject payLoad = wrapPayload.payload(req1, secretKey);
                    //Send out payload
                    out.write(payLoad + "\n");
                    out.flush();
                }
                //If argument is disconnect_peer
                else if (argsBean.getCommandName().equals("disconnect_peer"))
                {
                    JSONObject req1 = new JSONObject();
                    HostPort peer = new HostPort(argsBean.getPeer());
                    req1.put("command", "DISCONNECT_PEER_REQUEST");
                    req1.put("host", peer.host);
                    req1.put("port", peer.port);
                    System.out.println("Sent encrypted: " +req1);
                    //Encrypt and encode
                    JSONObject payLoad = wrapPayload.payload(req1, secretKey);
                    //Send out payload
                    out.write(payLoad + "\n");
                    out.flush();
                }
                //If argument is Invalid
                else
                {
                    System.out.println("commandName argument is INVALID");
                }

            }
            //If server does not have key
            else if (command.get("command").equals("AUTH_RESPONSE") && !((boolean) command.get("status")))
            {
                System.out.println("Sever did not find corresponding public key");
            }
            else
            {
                System.out.println("It is not a correct AUTH_RESPONSE: " + command.toJSONString());
            }
        }
    }


    private static PrivateKey generatePrivateKey(KeyFactory factory, String filename) throws InvalidKeySpecException, FileNotFoundException, IOException
    {
        PemFile pemFile = new PemFile(filename);
        byte[] content = pemFile.getPemObject().getContent();
        PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(content);
        return factory.generatePrivate(privKeySpec);
    }
}
