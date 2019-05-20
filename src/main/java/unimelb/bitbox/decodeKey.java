package unimelb.bitbox;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

/**
 * A snip of code from      with some modification to make it useful in this project
 */

public class decodeKey
{
    public static PublicKey decodeOpenSSH(String input) throws NoSuchProviderException,
            NoSuchAlgorithmException, InvalidKeySpecException
    {
        String[] fields = input.split(" ");
        if ((fields.length < 2) || (!fields[0].equals("ssh-rsa")))
            throw new IllegalArgumentException("Unsupported type");
        byte[] std = Base64.getDecoder().decode(fields[1]);
        RSAPublicKeySpec keySpec = decodeRSAPublicSSH(std);
        KeyFactory factory = KeyFactory.getInstance("RSA", "BC");
        return factory.generatePublic(keySpec);
    }

    private static RSAPublicKeySpec decodeRSAPublicSSH(byte[] encoded)
    {
        ByteBuffer input = ByteBuffer.wrap(encoded);
        String type = string(input);
        if (!"ssh-rsa".equals(type)) throw new IllegalArgumentException("Unsupported type");
        BigInteger exp = sshint(input);
        BigInteger mod = sshint(input);
        if (input.hasRemaining()) throw new IllegalArgumentException("Excess data");
        return new RSAPublicKeySpec(mod, exp);
    }

    private static String string(ByteBuffer buf)
    {
        return new String(lenval(buf), Charset.forName("US-ASCII"));
    }

    private static BigInteger sshint(ByteBuffer buf)
    {
        return new BigInteger(+1, lenval(buf));
    }

    private static byte[] lenval(ByteBuffer buf) {
        byte[] copy = new byte[buf.getInt()];
        buf.get(copy);
        return copy;
    }
}
