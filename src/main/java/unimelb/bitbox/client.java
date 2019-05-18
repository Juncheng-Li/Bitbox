package unimelb.bitbox;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.openssl.PEMKeyPair;

import java.io.*;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;


public class client
{
    public static void main(String[] args)
    {
        String KeyPath = "bitboxclient-rsa";
        String passwordString = "comp90015";
        char[] password = passwordString.toCharArray();
        File privateKeyFile = new File(KeyPath);
        try
        {
            PEMParser pemParser = new PEMParser(new FileReader(privateKeyFile));
            Object object = pemParser.readObject();
            PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(password);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            PrivateKey kp;
            if(object instanceof PEMEncryptedKeyPair)
            {
                System.out.println("Encrypted key - we will use provided password");
                kp = converter.getPrivateKey(((PrivateKeyInfo) object));
            }
            else
            {
                System.out.println("Unencrypted key - no password needed");
                kp = converter.getPrivateKey((PrivateKeyInfo) object);
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


    }
}
