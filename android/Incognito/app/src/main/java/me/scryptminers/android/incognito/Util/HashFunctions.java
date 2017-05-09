package me.scryptminers.android.incognito.Util;

import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HashFunctions {
    /*
    * Function : HMAC
    * Description : Generates the tag
    *   1. Get the challenge
    *   2. Get the password hash
    *   3. Generate password hash using HMAC SHA-512 algorithm
    * */
    public static String HMAC(String key, String data) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA512");
        sha512_HMAC.init(secret_key);

        byte[] bytes= sha512_HMAC.doFinal(data.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    /*
    * Function : getPasswordHash
    * Description : Generates the password hash
    *   1. Get the password
    *   2. Get the salt
    *   3. Generate password hash using SHA-512 algorithm
    * */
    public static String getPasswordHash(String passwordToHash, String salt) throws UnsupportedEncodingException{
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt.getBytes());
            byte[] bytes = md.digest(passwordToHash.getBytes());
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++){
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return generatedPassword;
    }

}
