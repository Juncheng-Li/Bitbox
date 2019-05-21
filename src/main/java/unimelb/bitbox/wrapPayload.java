package unimelb.bitbox;

import org.bouncycastle.jcajce.provider.symmetric.ARC4;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.omg.CORBA.DynAnyPackage.Invalid;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class wrapPayload
{
    public static JSONObject payload(JSONObject request, SecretKey secretKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException
    {
        //Encrypt the JSONObject
        Cipher cipherAES = Cipher.getInstance("AES");
        cipherAES.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] EncryptedJSON = cipherAES.doFinal(request.toJSONString().getBytes());
        String EncryptedJSON_Base64 = Base64.getEncoder().encodeToString(EncryptedJSON);
        //Prepare payload
        JSONObject payLoad = new JSONObject();
        payLoad.put("payload", EncryptedJSON_Base64);
        return payLoad;
    }


    public static JSONObject unWrap(JSONObject payload, SecretKey secretKey) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, ParseException
    {
        //Decode the JSONObject
        byte[] encryptedJSON = Base64.getDecoder().decode(payload.get("payload").toString());
        //Decrypt the JSONObject
        Cipher cipherAES = Cipher.getInstance("AES");
        cipherAES.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedJSON = cipherAES.doFinal(encryptedJSON);
        //Parse to String then to JSON
        String JsonString = new String(decryptedJSON);
        JSONParser parser = new JSONParser();
        JSONObject Json = (JSONObject) parser.parse(JsonString);
        System.out.println("Receive decrypted: " + Json);
        return Json;
    }
}
