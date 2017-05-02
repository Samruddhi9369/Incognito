package me.scryptminers.android.incognito.Util;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.*;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Samruddhi on 4/23/2017.
 * Sources: https://www.quickprogrammingtips.com/java/how-to-encrypt-and-decrypt-data-in-java-using-aes-algorithm.html
 */

public class PGPFunctions {
    private static SecretKey encryptionKey;
    private static SecretKey integrationKey;

    public static void generateEncryptionIntegrationKeys(){
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            encryptionKey = keyGenerator.generateKey();
            integrationKey = keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static byte[] encryptPlaintext(String message){
        // AES defaults to AES/ECB/PKCS5Padding
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }

        byte[] byteCipherText = new byte[0];
        try {
            byteCipherText = cipher.doFinal(message.getBytes());
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return byteCipherText;
    }

    public static byte[] decryptMessage(byte[] message, Key encryptionKey){
        // AES defaults to AES/ECB/PKCS5Padding in Java 7
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "BC");
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        byte[] bytePlainText = new byte[0];
        try {
            bytePlainText = cipher.doFinal(message);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return bytePlainText;
    }

    public static String generateKeysPlaintext(){
        String encodedEncryptionKey = new String(encryptionKey.getEncoded());
        String encodedIntegrationKey = new String(integrationKey.getEncoded());
        return encodedEncryptionKey + SharedValues.DELIMITER + encodedIntegrationKey;
    }

    public static byte[] encryptKeysPlaintext(byte[] keysPlaintext, PublicKey receiverPublicKey){
        Cipher cipher = null;
        byte[] key = null;
        try {
            cipher = Cipher.getInstance("RSA/ECB/NOPADDING","BC");
            // receiverPublicKey is the receiver's piblic key retrieved from QR code.
            cipher.init(Cipher.ENCRYPT_MODE, receiverPublicKey);
           // keysPlaintext is the concatenated AES keys
            key = cipher.doFinal(keysPlaintext);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return key;
    }

    public static String generateEncryptedMessage(String message, String receiverPublicKey) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
        //generates a pair of two independent FRESH 256-bit long symmetric keys for every encrypted message
        generateEncryptionIntegrationKeys();
        Log.e("EncryptionKey", encryptionKey.getEncoded().length+"");
        Log.e("IntegrationKey", String.valueOf(integrationKey.getEncoded().length));
        // Encrypt plaintext
        byte[] ciphertext = encryptPlaintext(message);
        Log.e("Ciphertext", String.valueOf(ciphertext.length));
        // HMAC the ciphertext to generate the tag
        byte[] tag = HmacSHA256(ciphertext,integrationKey);
        Log.e("tag", String.valueOf(tag.length));
        //both keys are concatenated to generate the keys plaintext
        // k(e) + k(i)
        byte[] keys = concatenateKeys();
        Log.e("keys", String.valueOf(keys.length));
        //keys plaintext is then encrypted using public key of the receiver
        byte[] encrytedMessage = new byte[ciphertext.length + tag.length];
        // Prepend keys to the ciphertext
        //System.arraycopy(keys, 0, encrytedMessage, 0, keys.length);

        //ciphertext + tag 48 bytes
        // Attach the ciphertext to encrytedMessage
        System.arraycopy(ciphertext, 0, encrytedMessage, 0, ciphertext.length);
        // Append the tag to encrytedMessage
        System.arraycopy(tag, 0, encrytedMessage, ciphertext.length, tag.length);

        // Get the receiver's public key and use RSA to encrypt the keys
        byte[] decodeKey = Base64.decode(receiverPublicKey,Base64.DEFAULT);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodeKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey recPublicKey = keyFactory.generatePublic(keySpec);

        byte[] encryptedKeys = encryptKeysPlaintext(keys,recPublicKey);
        Log.e("encryptedKeys", String.valueOf(encryptedKeys.length));
        // Append result of AES encryption with RSA-encrypted keys
        byte[] encryptMessageToSend = new byte[encrytedMessage.length + encryptedKeys.length];
        Log.e("encryptMessageToSend", String.valueOf(encryptMessageToSend.length));
        // (ciphertext + tag) + encrypted_keys
        System.arraycopy(encrytedMessage, 0, encryptMessageToSend, 0, encrytedMessage.length);
        System.arraycopy(encryptedKeys, 0, encryptMessageToSend, encrytedMessage.length, encryptedKeys.length);
        // Encode the encryptedMessage to String
        return Base64.encodeToString(encryptMessageToSend,Base64.DEFAULT);
    }

    public static int getAESKeySizeInBytes() {
        return 256 / Byte.SIZE;
    }

    private static byte[] concatenateKeys() {
        byte[] keys = new byte[getAESKeySizeInBytes() + getAESKeySizeInBytes()];
        System.arraycopy(encryptionKey.getEncoded(), 0, keys, 0, getAESKeySizeInBytes());
        System.arraycopy(integrationKey.getEncoded(), 0, keys, getAESKeySizeInBytes(), getAESKeySizeInBytes());
        return keys;
    }

    public static byte[] decryptKeysPlaintext(byte[] cipherTextKeys, PrivateKey receiverPrivateKey){
        Cipher cipher = null;
        byte[] key = null;
        try {
            cipher = Cipher.getInstance("RSA/ECB/NOPADDING","BC");
            // receiverPrivateKey is the user's private key
            cipher.init(Cipher.DECRYPT_MODE, receiverPrivateKey);
            // keysPlaintext is the concatenated RSA keys
            key = cipher.doFinal(cipherTextKeys);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return key;
    }

    public static String generateDecryptedMessage(String receivedMessage, String receiverPrivateKey) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException {
        //Decode the message from base64
        byte[] ciphertext = Base64.decode(receivedMessage,Base64.DEFAULT);
        Log.e("receiverPrivateKey", receiverPrivateKey);
        Log.e("CIPHER", String.valueOf(ciphertext.length));
        // Extract the symmetric keys
        byte[] keys = new byte[128];
        Log.e("keys", String.valueOf(keys.length));
        System.arraycopy(ciphertext, ciphertext.length - keys.length, keys, 0, keys.length);
        Log.e("KEYS",keys.toString());
        // Get the receiver's private key and use RSA to encrypt the keys
        byte[] decodeKey = Base64.decode(receiverPrivateKey,Base64.DEFAULT);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec (decodeKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey recPrivateKey = keyFactory.generatePrivate(keySpec);

        // Decrypt the keys with RSA
        byte[] decryptedKeys = decryptKeysPlaintext(keys,recPrivateKey);

        // Separate the keys
        byte[] encodedEncryptionKey = new byte[getAESKeySizeInBytes()];
        byte[] encodedIntegrityKey = new byte[getAESKeySizeInBytes()];
        System.arraycopy(decryptedKeys, 0, encodedEncryptionKey, 0, encodedEncryptionKey.length);
        System.arraycopy(decryptedKeys, encodedIntegrityKey.length, encodedIntegrityKey, 0, encodedIntegrityKey.length);
        SecretKey encryptionKey = new SecretKeySpec(encodedEncryptionKey, "AES");
        SecretKey integrationKey = new SecretKeySpec(encodedIntegrityKey, "AES");

        // Obtain a message in the form of the AES encrypt
        // ciphertext + tag
        byte[] encryptedMessage = new byte[ciphertext.length - keys.length];
        System.arraycopy(ciphertext, 0, encryptedMessage, 0, encryptedMessage.length);

        // Extract the tag
        // HMAC tag is the same size of AES key = 256 bits
        byte[] tag = new byte[getAESKeySizeInBytes()];
        System.arraycopy(ciphertext, ciphertext.length - tag.length - keys.length, tag, 0, tag.length);

        // Extract the message
        int messageLengthInBytes = ciphertext.length - tag.length - keys.length;
        byte[] decodedMessage = new byte[messageLengthInBytes];
        System.arraycopy(ciphertext, 0, decodedMessage, 0, messageLengthInBytes);
        Log.e("decodedMessage",Base64.encodeToString(decodedMessage,Base64.DEFAULT));
        // Check HMAC tags
        byte[] myTag = HmacSHA256(decodedMessage, integrationKey);

        // If the tags match, no tampering occurred
        // Decrypt message
        Log.e("TAG",Base64.encodeToString(tag,Base64.DEFAULT));
        Log.e("MYTAG",Base64.encodeToString(myTag,Base64.DEFAULT));
        if (HmacVerify(tag, myTag)) {
            byte[] plaintext = decryptMessage(decodedMessage,encryptionKey);
            return Base64.encodeToString(plaintext,Base64.DEFAULT);
        } else {
            // If the tags don't match, tampering occurred
            // Don't decrypt
            return new String("Error in Decryption");
        }
    }

    public static boolean HmacVerify(byte[] tag1, byte[] tag2){
        return Arrays.equals(tag1, tag2);
    }

    private static int getRSAKeySizeInBytes() {
        return 2048/ Byte.SIZE;
    }

    private static String generateDecryptedTag(String encryptedMessage) {
        String stringTagPrime="";
        try {
            stringTagPrime = HashFunctions.HMAC(integrationKey.toString(),encryptedMessage);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return stringTagPrime;
    }

    public static byte[] HmacSHA256(byte[] ciphertext, SecretKey intkey)
            throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] ik = intkey.getEncoded();
        Mac sha256_HMAC= Mac.getInstance("HmacSHA256");
        SecretKeySpec sk = new SecretKeySpec(ik, "HmacSHA256");
        sha256_HMAC.init(sk);
        return sha256_HMAC.doFinal(ciphertext);
    }
}
