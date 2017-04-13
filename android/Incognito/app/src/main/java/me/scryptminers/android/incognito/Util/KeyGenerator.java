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

/**
 * Created by Samruddhi on 4/10/2017.
 */

public class KeyGenerator {

    public static void generateKeys() {
        ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("secp224k1");
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDH","SC");
            keyPairGenerator.initialize(ecGenParameterSpec);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            String strPublicKey = HashFunctions.base64Encode(publicKey.getEncoded());
            String strPrivateKey = HashFunctions.base64Encode(privateKey.getEncoded());
            SharedValues.save("PUBLIC_KEY",strPublicKey);
            SharedValues.save("PRIVATE_KEY",strPrivateKey);
            Log.e("PublicKey",strPublicKey);
            Log.e("PrivateKey",strPrivateKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

    }
}