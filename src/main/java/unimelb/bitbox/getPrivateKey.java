package unimelb.bitbox;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sun.misc.BASE64Decoder;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.Document;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class getPrivateKey
{

    private final static String RESOURCES_DIR = "";
    private static PrivateKey priv = null;

    public static void main(String[] args) throws FileNotFoundException, InvalidKeySpecException, IOException,
            NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, GeneralSecurityException, ParseException
    {
        Security.addProvider(new BouncyCastleProvider());
        System.out.println("BouncyCastle provider added.");

        KeyFactory factory = KeyFactory.getInstance("RSA", "BC");
        KeyFactory kf = KeyFactory.getInstance("RSA");

        try
        {
            priv = generatePrivateKey(factory, RESOURCES_DIR + "bitboxclient_rsa");
            //PrivateKey priv2 = generatePrivateKey(kf, RESOURCES_DIR + "bitboxclient_rsa");
            System.out.println(String.format("Instantiated private key: %s", priv));
            //System.out.println(String.format("Instantiated private key2: %s", priv2));
            //Cipher cipher = Cipher.getInstance("RSA");
            //cipher.init(Cipher.ENCRYPT_MODE, priv);

            //PublicKey pub = generatePublicKey(factory, RESOURCES_DIR + "bitboxclient_rsa.pub");
            //System.out.println(String.format("Instantiated public key: %s", pub));
        } catch (InvalidKeySpecException e)
        {
            e.printStackTrace();
        }

        //other tests
        String[] keys = Configuration.getConfigurationValue("authorized_keys").split(",");
        for (String key : keys)
        {
            System.out.println(key);
            if (key.contains("jcl@Jcl-Akilos"))
            {
                System.out.println("yes");
                //take public key out
                String key_middle = key.split(" ")[1];
                System.out.println(key_middle);
                byte[] keyContent = Base64.getDecoder().decode(key_middle);
                //X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(keyContent);

                //PublicKey pubKey = kf.generatePublic(pubKeySpec);

                //PublicKey pubKey = factory.generatePublic(pubKeySpec);
                //System.out.println(String.format("Instantiated public key: %s", pubKey));

                PublicKey pubKey3 = demo(keyContent);

                RSAPublicKeySpec keySpec = decodeOpenSSH(key);
                PublicKey pubKey = decodeKey.decodeOpenSSH(key);
                PublicKey pubKey2 = kf.generatePublic(keySpec);

                System.out.println(pubKey.getFormat());  //both X.509
                System.out.println(pubKey2.getFormat());  //both X.509
                System.out.println(String.format("Instantiated public key: %s", pubKey));    //RSA public key
                System.out.println(String.format("Instantiated public key2: %s", pubKey2));  //Sun RSA public key
                System.out.println(String.format("Instantiated public key3: %s", pubKey3));  //Sun RSA public key


                //generate AES
                KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                keyGen.init(128);
                SecretKey secretKey = keyGen.generateKey();

                //And encode AES with Base64
                String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
                System.out.println("AES: ");
                System.out.println(encodedKey);

                //encrypt AES with public key
                SecureRandom random = new SecureRandom();
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.ENCRYPT_MODE, pubKey, random);
                byte[] AESinBag = cipher.doFinal(secretKey.getEncoded());
                System.out.println("Encoded: " + AESinBag);
                System.out.println("Bag size: " + AESinBag.length);

                //Decrypt AES with private key
                Cipher ciper1 = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                ciper1.init(Cipher.DECRYPT_MODE, priv);
                SecretKey ass = new SecretKeySpec(ciper1.doFinal(AESinBag), "AES");
                System.out.println(Base64.getEncoder().encodeToString(ass.getEncoded()));

                //encrypt JSON with AES
                JSONObject message = new JSONObject();
                message.put("command", "THis is an message.");
                message.put("content", "there you go!");
                Cipher cipherAES = Cipher.getInstance("AES");
                cipherAES.init(Cipher.ENCRYPT_MODE, ass);
                byte[] EncryptedMsg = cipherAES.doFinal(message.toJSONString().getBytes());

                //decrypt JSON with AES
                Cipher cipherAESde = Cipher.getInstance("AES");
                cipherAESde.init(Cipher.DECRYPT_MODE, ass);
                byte[] decryptedMsg = cipherAESde.doFinal(EncryptedMsg);
                System.out.println("final result:");
                System.out.println(new String(decryptedMsg));

                //String to JSON 1
                Document alexicia = Document.parse(new String(decryptedMsg));
                System.out.println(alexicia.toJson());

                //String to JSON 2
                JSONParser parser = new JSONParser();
                JSONObject store = (JSONObject) parser.parse(new String(decryptedMsg));
                System.out.println(store);
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

    private static PublicKey generatePublicKey(KeyFactory factory, String filename) throws InvalidKeySpecException, FileNotFoundException, IOException
    {
        PemFile pemFile = new PemFile(filename);
        byte[] content = pemFile.getPemObject().getContent();
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(content);
        return factory.generatePublic(pubKeySpec);
    }



    static PublicKey demo(byte[] keyContent) throws IOException, GeneralSecurityException {
        KeyFactory f = KeyFactory.getInstance("RSA");

        RSAPublicKeySpec pubspec = decodeRSAPublicSSH(keyContent);

        return f.generatePublic(pubspec);
    }

    static RSAPublicKeySpec decodeOpenSSH(String input) {
        String[] fields = input.split(" ");
        if ((fields.length < 2) || (!fields[0].equals("ssh-rsa"))) throw new IllegalArgumentException("Unsupported type");
        byte[] std = Base64.getDecoder().decode(fields[1]);
        return decodeRSAPublicSSH(std);
    }

    static RSAPublicKeySpec decodeRSAPublicSSH(byte[] encoded) {
        ByteBuffer input = ByteBuffer.wrap(encoded);
        String type = string(input);
        if (!"ssh-rsa".equals(type)) throw new IllegalArgumentException("Unsupported type");
        BigInteger exp = sshint(input);
        BigInteger mod = sshint(input);
        if (input.hasRemaining()) throw new IllegalArgumentException("Excess data");
        return new RSAPublicKeySpec(mod, exp);
    }

    static RSAPrivateCrtKeySpec decodeRSAPrivatePKCS1(byte[] encoded) {
        ByteBuffer input = ByteBuffer.wrap(encoded);
        if (der(input, 0x30) != input.remaining()) throw new IllegalArgumentException("Excess data");
        if (!BigInteger.ZERO.equals(derint(input))) throw new IllegalArgumentException("Unsupported version");
        BigInteger n = derint(input);
        BigInteger e = derint(input);
        BigInteger d = derint(input);
        BigInteger p = derint(input);
        BigInteger q = derint(input);
        BigInteger ep = derint(input);
        BigInteger eq = derint(input);
        BigInteger c = derint(input);
        return new RSAPrivateCrtKeySpec(n, e, d, p, q, ep, eq, c);
    }

    private static String string(ByteBuffer buf) {
        return new String(lenval(buf), Charset.forName("US-ASCII"));
    }

    private static BigInteger sshint(ByteBuffer buf) {
        return new BigInteger(+1, lenval(buf));
    }

    private static byte[] lenval(ByteBuffer buf) {
        byte[] copy = new byte[buf.getInt()];
        buf.get(copy);
        return copy;
    }

    private static BigInteger derint(ByteBuffer input) {
        int len = der(input, 0x02);
        byte[] value = new byte[len];
        input.get(value);
        return new BigInteger(+1, value);
    }

    private static int der(ByteBuffer input, int exp) {
        int tag = input.get() & 0xFF;
        if (tag != exp) throw new IllegalArgumentException("Unexpected tag");
        int n = input.get() & 0xFF;
        if (n < 128) return n;
        n &= 0x7F;
        if ((n < 1) || (n > 2)) throw new IllegalArgumentException("Invalid length");
        int len = 0;
        while (n-- > 0) {
            len <<= 8;
            len |= input.get() & 0xFF;
        }
        return len;
    }

    private static byte[] readAllBase64Bytes(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        BufferedReader r = new BufferedReader(new InputStreamReader(input, StandardCharsets.US_ASCII));
        Base64.Decoder decoder = Base64.getDecoder();
        while (true) {
            String line = r.readLine();
            if (line == null) break;
            if (line.startsWith("-----")) continue;
            output.write(decoder.decode(line));
        }
        return output.toByteArray();
    }
}
