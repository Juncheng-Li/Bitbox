package unimelb.bitbox;

import org.json.simple.JSONObject;

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
}
