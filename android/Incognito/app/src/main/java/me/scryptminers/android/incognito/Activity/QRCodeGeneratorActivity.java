package me.scryptminers.android.incognito.Activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;

import me.scryptminers.android.incognito.R;
import me.scryptminers.android.incognito.Util.SharedValues;
/*
* This class generate the QR Code using the public key and user id of the user
* */
public class QRCodeGeneratorActivity extends AppCompatActivity {
    private ImageView imageViewQRCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_generator);
        imageViewQRCode = (ImageView) findViewById(R.id.imageViewQRCode);
        generateQRCode();
    }

    /*
    * Function Name: generateQRCode
    * Description: This function does following tasks:
    *   1. Get the public key of self
    *   2. Get the user id
    *   3. Generate QR code using BarcodeEncoder() class
    *   4. Encode QR code into Bitmap image
    * */
    public void generateQRCode(){
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try{
            String publicKey = SharedValues.getValue("PUBLIC_KEY");
            long userId = SharedValues.getLong("USER_ID");
            BitMatrix bitMatrix = multiFormatWriter.encode(publicKey+SharedValues.DELIMITER+userId, BarcodeFormat.QR_CODE,200,200);

            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            imageViewQRCode.setImageBitmap(bitmap);
        } catch (WriterException e){
            e.printStackTrace();
        }
    }

}
