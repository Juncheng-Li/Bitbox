package unimelb.bitbox;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.HostPort;

import javax.crypto.*;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Base64;

public class Peer_serverForClient extends Thread
{
    private Socket clientSocket = null;
    private JSONArray peerList;
    private SecretKey secretKey = null;

    Peer_serverForClient(Socket clientSocket, JSONArray peerList)
    {
        this.clientSocket = clientSocket;
        this.peerList = peerList;
    }


    public void run()
    {

    }
}
