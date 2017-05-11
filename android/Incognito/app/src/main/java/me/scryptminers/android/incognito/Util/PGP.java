package me.scryptminers.android.incognito.Util;

import android.util.Log;

import org.spongycastle.util.encoders.Base64;

//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.bouncycastle.util.Arrays;
//import org.bouncycastle.util.encoders.Base64;

import java.io.UnsupportedEncodingException;
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

/*
* This class defines functions
* 1. To generate encryption and integration keys
* 2. To encrypt and decrypt the message
* */
public class PGP {
    private static SecretKey encryptionKey;
    private static SecretKey integrationKey;

    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String ENCRYPTION_MODE = "CTR";
    private static final String ENCRYPTION_PADDING = "NoPadding";
    private static final String HASH_ALGORITHM = "HmacSHA256";
    private static final int AES_KEY_SIZE = 256; // bits
    private static final int IV_SIZE = 16; // IV size = 16 bytes = 128 bits

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
        // SecureRandom class automatically seeds itself on the nextBytes call
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

            // Concatenate the encryption key and integration key
            byte[] keys = new byte[encKey.length + intKey.length];
            System.arraycopy(encKey, 0, keys, 0, encKey.length);
            System.arraycopy(intKey, 0, keys, intKey.length, intKey.length);

            // Get the receiver's public key and use RSA to encrypt the keys
            byte[] decodeKey = Base64.decode(receiverPK);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodeKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey receiverPublicKey = keyFactory.generatePublic(keySpec);
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

        Cipher cipher = Cipher.getInstance(ALGORITHM + "/" + MODE_OF_OPERATION + "/" + PADDING);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        return cipher.doFinal(plaintext);
    }

    public static byte[] encryptDemo(String cleartext,SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        final int AES_KEYLENGTH = 128;
        byte[] iv = new byte[AES_KEYLENGTH / 8];
        SecureRandom prng = new SecureRandom();
        prng.nextBytes(iv);
        byte[] message = new byte[iv.length+cleartext.getBytes().length];
        Cipher aesCipherForEncryption = Cipher.getInstance("AES/CBC/PKCS7PADDING");
        aesCipherForEncryption.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(iv));
        byte[] byteDataToEncrypt = cleartext.getBytes();
        System.arraycopy(iv, 0, message, 0, iv.length);
        byte[] byteCipherText = aesCipherForEncryption.doFinal(byteDataToEncrypt);
        System.arraycopy(byteCipherText,0,message,iv.length,byteCipherText.length);

        return message;
    }

    public static  byte[] decryptDemo(String ciphertext, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException {
        final int AES_KEYLENGTH = 128;
        byte[] iv = new byte[AES_KEYLENGTH / 8];
        Cipher aesCipherForDecryption = Cipher.getInstance("AES/CBC/PKCS7PADDING");
        byte[] ciphertextb = ciphertext.getBytes();

        byte[] todecrppt = new byte[ciphertextb.length - iv.length];

        // Prepend IV to the ciphertext
        System.arraycopy(ciphertextb, 0, iv, 0, iv.length);
        System.arraycopy(ciphertextb, iv.length, todecrppt, 0, ciphertextb.length - iv.length);

        aesCipherForDecryption.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] byteDecryptedText = aesCipherForDecryption.doFinal(todecrppt);
        return byteDecryptedText;
    }


    public static byte[] encryptGroupMessage(String plaintext,SecretKey key) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, UnsupportedEncodingException {
        /*Cipher cipher =Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE,key);
        return cipher.doFinal(plaintext.getBytes());*/
        //IvParameterSpec iv = generateIV();
        String cipherTransformation = ENCRYPTION_ALGORITHM + "/" + ENCRYPTION_MODE + "/" + ENCRYPTION_PADDING;
      // String cipherTransformation = ENCRYPTION_ALGORITHM + "/CBC/PKCS5PADDING";
        Cipher cipher = Cipher.getInstance(cipherTransformation);//, PROVIDER);
        // Generate FRESH keys for every encrypted message
        byte[] ivBytes = new byte[cipher.getBlockSize()];

        // SecureRandom will automatically seed itself on the nextBytes call
        SecureRandom random = new SecureRandom();
        random.nextBytes(ivBytes);

        //IvParameterSpec iv = new IvParameterSpec(ivBytes);
        IvParameterSpec iv = generateIV();
        // Encrypt the plaintext
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
        byte[] message = new byte[iv.getIV().length + ciphertext.length];

        Log.e("IV length",""+iv.getIV().length);

        // Prepend IV to the ciphertext
        System.arraycopy(iv.getIV(), 0, message, 0, iv.getIV().length);

        // Attach the ciphertext
        System.arraycopy(ciphertext, 0, message, iv.getIV().length, ciphertext.length);

        return ciphertext;
        /*//initialize the secret key with the appropriate algorithm
        SecretKeySpec skeySpec = new SecretKeySpec(rawAesKey, ENCRYPTION_ALGORITHM);

        //get an instance of the symmetric cipher
        Cipher aesCipher = Cipher.getInstance(ENCRYPTION_ALGORITHM + "/CBC/PKCS5PADDING");

        //set it to encrypt mode, with the generated key
        aesCipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        //get the initialization vector being used (to be returned)
        byte[] aesIV = aesCipher.getIV();

        //encrypt the data
        byte[] encryptedData = aesCipher.doFinal(plaintext.getBytes("UTF-8"));
        return encryptedData;*/
    }

    public static byte[] decryptGroupMessage(String ciphertext, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, UnsupportedEncodingException {
        /*Cipher cipher =Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE,key);
        return cipher.doFinal(ciphertext.getBytes());*/
        byte[] ciphertextMessage = android.util.Base64.decode(ciphertext,android.util.Base64.DEFAULT);
        String cipherTransformation = ENCRYPTION_ALGORITHM + "/" + ENCRYPTION_MODE + "/" + ENCRYPTION_PADDING;
        //String cipherTransformation = ENCRYPTION_ALGORITHM +"/CBC/PKCS5PADDING";
        Cipher cipher = Cipher.getInstance(cipherTransformation);//, PROVIDER);
        // Extract the IV
        byte[] iv = new byte[cipher.getBlockSize()];
        System.arraycopy(ciphertextMessage, 0, iv, 0, IV_SIZE);

        // Extract the message
        int messageLengthInBytes = ciphertextMessage.length - iv.length;
        byte[] decodedMessage = new byte[messageLengthInBytes];
        System.arraycopy(ciphertextMessage, iv.length, decodedMessage, 0, messageLengthInBytes);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        // Decrypt
        byte[] plaintext = cipher.doFinal(decodedMessage);
        Log.d("plaintext", new String(plaintext));
        return plaintext;
        //initialize the secret key with the appropriate algorithm
        /*SecretKeySpec skeySpecDec = new SecretKeySpec(rawAesKey, ENCRYPTION_ALGORITHM);

        //get an instance of the symmetric cipher
        Cipher aesCipherDec = Cipher.getInstance(ENCRYPTION_ALGORITHM +"/CBC/PKCS5PADDING");
        Cipher aesCipher = Cipher.getInstance(ENCRYPTION_ALGORITHM +"/CBC/PKCS5PADDING");
        aesCipher.init(Cipher.ENCRYPT_MODE, skeySpecDec);
        byte[] aesIV = aesCipher.getIV();
        //set it to decrypt mode with the AES key, and IV
        aesCipherDec.init(Cipher.DECRYPT_MODE, skeySpecDec, new IvParameterSpec(aesIV));

        //decrypt and return the data
        byte[] decryptedData = aesCipherDec.doFinal(ciphertext.getBytes("UTF-8"));

        return new String(decryptedData, "UTF-8");*/
    }



    public static byte[] encrypt(String plaintext) throws InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException,
            BadPaddingException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
        IvParameterSpec iv = generateIV();
        String cipherTransformation = ENCRYPTION_ALGORITHM + "/" + ENCRYPTION_MODE + "/" + ENCRYPTION_PADDING;
        Cipher cipher = Cipher.getInstance(cipherTransformation);
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
     * Description: Decrypt ciphertext using the encryption key
     * 1. Get the message to be decrypt
     * 2. Get encryption key
     * 3. Get integration key
     * 4. Get the plaintext by decryption of the ciphertext
     */
    public static String decrypt(byte[] ciphertext, SecretKey encryptionKey, SecretKey integrityKey)
            throws InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
        String cipherTransformation = ENCRYPTION_ALGORITHM + "/" + ENCRYPTION_MODE + "/" + ENCRYPTION_PADDING;
        Cipher cipher = Cipher.getInstance(cipherTransformation);
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

        // If the tags match, no message tampering occurred
        if (HmacVerify(tag, myTag)) {
            // Decrypt message using encryption key
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, new IvParameterSpec(iv));

            // Decrypt
            byte[] plaintext = cipher.doFinal(decodedMessage);
            return new String(plaintext);
        } else {
            // Tags don't match. Message tampering has been occurred
            // Throw error
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
