package me.scryptminers.android.incognito.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyGenerator {
    /*
    * Function : generateKeys
    * Description : Generates the public and private key for the user when user verification is done successfully
    *   1. Generate 2048 bit public and private keys from RSA algorithm
    * */
    public static void generateKeys() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            String strPublicKey = Base64.encodeToString(publicKey.getEncoded(),Base64.DEFAULT);
            String strPrivateKey = Base64.encodeToString(privateKey.getEncoded(),Base64.DEFAULT);
            SharedValues.save("PUBLIC_KEY",strPublicKey);
            SharedValues.save("PRIVATE_KEY",strPrivateKey);
            Log.e("PublicKey",strPublicKey);
            Log.e("PrivateKey",strPrivateKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
