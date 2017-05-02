package me.scryptminers.android.incognito.Util;

import org.spongycastle.util.encoders.Base64;

//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.bouncycastle.util.Arrays;
//import org.bouncycastle.util.encoders.Base64;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Samruddhi on 4/29/2017.
 */

public class PGP {
    private static SecretKey encryptionKey;
    private static SecretKey integrationKey;

    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String ENCRYPTION_MODE = "CTR";
    private static final String ENCRYPTION_PADDING = "NoPadding";
    private static final String HASH_ALGORITHM = "HmacSHA256";
    //private static final String PROVIDER = "BC"; //Bouncy Castle
    private static final int AES_KEY_SIZE = 256; // bits
    private static final int IV_SIZE = 16; // IV size = 16 bytes or 128 bits

    // Define variables for use in RSA key exchange
    // Use RSA with OAEP padding, 2048 bit keys
    private static final String ALGORITHM = "RSA";
    private static final String MODE_OF_OPERATION = "NONE";
    private static final String PADDING = "OAEPWithSHA-256AndMGF1Padding";
    private static final int RSA_KEY_SIZE = 2048; // bits

    public static void generateEncryptionIntegrationKeys(){
        javax.crypto.KeyGenerator keyGenerator = null;
        try {
            keyGenerator = javax.crypto.KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            encryptionKey = keyGenerator.generateKey();
            integrationKey = keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    private static IvParameterSpec generateIV() {
        byte[] ivBytes = new byte[16];

        // SecureRandom will automatically seed itself on the nextBytes call
        SecureRandom random = new SecureRandom();
        random.nextBytes(ivBytes);

        return new IvParameterSpec(ivBytes);
    }

    public static String encryptMessage(String message, String receiverPK) {
        try {
            // Encrypt the given message with AES
            // This chunk of data contains the IV, the message, and then tag
            // but is still missing the symmetric keys needed for decryption
            byte[] messageBulk = encrypt(message);

            // Get the symmetric keys used in the AES encryption
            byte[] encKey = encryptionKey.getEncoded();
            byte[] intKey = integrationKey.getEncoded();

            // Concatenate the two keys
            // k(e) + k(i)
            byte[] keys = new byte[encKey.length + intKey.length];
            System.arraycopy(encKey, 0, keys, 0, encKey.length);
            System.arraycopy(intKey, 0, keys, intKey.length, intKey.length);

            // Get the receiver's public key and use RSA to encrypt the keys
            byte[] decodeKey = Base64.decode(receiverPK);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodeKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey receiverPublicKey = keyFactory.generatePublic(keySpec);
            //PublicKey receiverPublicKey = keyExchangeInstance.getPublicKey(receiver);
            byte[] encryptedKeys = encryptKeys(keys, receiverPublicKey);

            // Append result of AES encryption with RSA-encrypted keys
            byte[] encryptedMessage = new byte[messageBulk.length + encryptedKeys.length];
            System.arraycopy(messageBulk, 0, encryptedMessage, 0, messageBulk.length);
            System.arraycopy(encryptedKeys, 0, encryptedMessage, messageBulk.length, encryptedKeys.length);

            // Encode the encryptedMessage to String
            return Base64.toBase64String(encryptedMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error encrypt";
    }

    public static byte[] encryptKeys(byte[] plaintext, PublicKey key) throws InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {

        Cipher cipher = Cipher.getInstance(ALGORITHM + "/" + MODE_OF_OPERATION + "/" + PADDING);//, PROVIDER);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        return cipher.doFinal(plaintext);
    }

    public static byte[] encrypt(String plaintext) throws InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException,
            BadPaddingException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
        IvParameterSpec iv = generateIV();
        String cipherTransformation = ENCRYPTION_ALGORITHM + "/" + ENCRYPTION_MODE + "/" + ENCRYPTION_PADDING;
        Cipher cipher = Cipher.getInstance(cipherTransformation);//, PROVIDER);
        // Generate FRESH keys for every encrypted message
        generateEncryptionIntegrationKeys();

        // Encrypt the plaintext
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, iv);
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes());

        // HMAC the ciphertext
        byte[] tag = HmacSHA256(ciphertext, integrationKey);

        // Concatenate the two keys
        // k(e) + k(i)
        byte[] keys = new byte[getAESKeySizeInBytes() + getAESKeySizeInBytes()];
        System.arraycopy(encryptionKey.getEncoded(), 0, keys, 0, getAESKeySizeInBytes());
        System.arraycopy(integrationKey.getEncoded(), 0, keys, getAESKeySizeInBytes(), getAESKeySizeInBytes());

        // Create the bulk of the message
        // IV + ciphertext + tag
        byte[] message = new byte[IV_SIZE + ciphertext.length + tag.length];

        // Prepend IV to the ciphertext
        System.arraycopy(iv.getIV(), 0, message, 0, iv.getIV().length);

        // Attach the ciphertext
        System.arraycopy(ciphertext, 0, message, IV_SIZE, ciphertext.length);

        // Append the tag
        System.arraycopy(tag, 0, message, iv.getIV().length + ciphertext.length, tag.length);

        return message;
    }

    /**
     * Decrypt the given ciphertext using the given key
     *
     * @param ciphertext - Message to be decrypted
     * @param encryptionKey - Symmetric key used for AES decryption
     * @param integrityKey - Symmetric key used for HMAC tag
     * @return plaintext - the original message decrypted
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchAlgorithmException
     */
    public static String decrypt(byte[] ciphertext, SecretKey encryptionKey, SecretKey integrityKey)
            throws InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
        String cipherTransformation = ENCRYPTION_ALGORITHM + "/" + ENCRYPTION_MODE + "/" + ENCRYPTION_PADDING;
        Cipher cipher = Cipher.getInstance(cipherTransformation);//, PROVIDER);
        // Extract the IV
        byte[] iv = new byte[IV_SIZE];
        System.arraycopy(ciphertext, 0, iv, 0, IV_SIZE);

        // Extract the tag
        // HMAC tag is the same size of AES key = 256 bits
        byte[] tag = new byte[getAESKeySizeInBytes()];
        System.arraycopy(ciphertext, ciphertext.length - tag.length, tag, 0, tag.length);

        // Extract the message
        int messageLengthInBytes = ciphertext.length - IV_SIZE - tag.length;
        byte[] decodedMessage = new byte[messageLengthInBytes];
        System.arraycopy(ciphertext, IV_SIZE, decodedMessage, 0, messageLengthInBytes);

        // Check HMAC tags
        byte[] myTag = HmacSHA256(decodedMessage, integrityKey);

        // If the tags match, no tampering occurred
        // Decrypt message
        if (HmacVerify(tag, myTag)) {
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, new IvParameterSpec(iv));

            // Decrypt
            byte[] plaintext = cipher.doFinal(decodedMessage);
            return new String(plaintext);
        } else {
            // If the tags don't match, tampering occurred
            // Don't decrypt
            return new String("Error in decryption");
        }
    }

    public static String decryptMessage(String message, String receiverPrivateKey) {
        try {
            // Decode the message from Base64
            byte[] ciphertext = Base64.decode(message);
            byte[] receiverPrKey = Base64.decode(receiverPrivateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(receiverPrKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey recPrivateKey = keyFactory.generatePrivate(keySpec);
            // Extract the symmetric keys
            byte[] keys = new byte[getRSAKeySizeInBytes()];
            System.arraycopy(ciphertext, ciphertext.length - keys.length, keys, 0, keys.length);

            // Decrypt the keys with RSA
            byte[] decryptedKeys = decryptKeys(keys, recPrivateKey);

            // Separate the keys
            byte[] encodedEncryptionKey = new byte[getAESKeySizeInBytes()];
            byte[] encodedIntegrityKey = new byte[getAESKeySizeInBytes()];
            System.arraycopy(decryptedKeys, 0, encodedEncryptionKey, 0, encodedEncryptionKey.length);
            System.arraycopy(decryptedKeys, encodedIntegrityKey.length, encodedIntegrityKey, 0, encodedIntegrityKey.length);
            SecretKey encryptionKey = new SecretKeySpec(encodedEncryptionKey, ENCRYPTION_ALGORITHM);
            SecretKey integrityKey = new SecretKeySpec(encodedIntegrityKey, ENCRYPTION_ALGORITHM);

            // Obtain a message in the form of the AES encrypt
            // IV + message + tag
            byte[] encryptedMessage = new byte[ciphertext.length - keys.length];
            System.arraycopy(ciphertext, 0, encryptedMessage, 0, encryptedMessage.length);

            // Decrypt the message with AES
            return decrypt(encryptedMessage, encryptionKey, integrityKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Error Decrypt";
    }

    public static byte[] decryptKeys(byte[] ciphertext, PrivateKey privateKey) throws InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
        String transformation = ALGORITHM + "/" + MODE_OF_OPERATION + "/" + PADDING;
        Cipher cipher = Cipher.getInstance(transformation);//, PROVIDER);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return cipher.doFinal(ciphertext);
    }

    public static byte[] HmacSHA256(byte[] ciphertext, SecretKey intkey)
            throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] ik = intkey.getEncoded();
        Mac sha256_HMAC= Mac.getInstance(HASH_ALGORITHM);
        SecretKeySpec sk = new SecretKeySpec(ik, HASH_ALGORITHM);
        sha256_HMAC.init(sk);
        return sha256_HMAC.doFinal(ciphertext);
    }

    public static String CipherTagConcatenate(String ciphertext, String HmacTag){
        String combined = ciphertext+HmacTag;
        return combined;
    }

    public static boolean HmacVerify(byte[] tag1, byte[] tag2){
        return Arrays.equals(tag1, tag2);
    }

    static int getAESKeySizeInBytes() {
        return AES_KEY_SIZE / Byte.SIZE;
    }

    static int getRSAKeySizeInBytes() {
        return RSA_KEY_SIZE / Byte.SIZE;
    }
}
