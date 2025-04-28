package com.example.worksyck;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.OutputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class QRUtils {

    private static final String SECRET_KEY = "gT7x!Dk3n2@9#pLs";

    static String encrypt(String input) throws Exception {
        SecretKeySpec secretKey = generateKey(SECRET_KEY);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encVal = cipher.doFinal(input.getBytes());
        return Base64.encodeToString(encVal, Base64.NO_WRAP);
    }

    private static SecretKeySpec generateKey(String password) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        return new SecretKeySpec(key, "AES");
    }

    public static String decrypt(String encryptedText) {
        try {
            SecretKeySpec secretKey = generateKey(SECRET_KEY);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decodedBytes = Base64.decode(encryptedText, Base64.NO_WRAP);
            byte[] decrypted = cipher.doFinal(decodedBytes);

            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    static Bitmap generateQRCodeBitmap(String content, int width, int height) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        com.google.zxing.common.BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height);

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bmp;
    }

    static void saveBitmapToDownloads(Context context, Bitmap bitmap, String fileName) {
        OutputStream outputStream;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/WorkSync/"); // Saves to DCIM/WorkSync/

        try {
            android.net.Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                outputStream = context.getContentResolver().openOutputStream(uri);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                    Toast.makeText(context, "QR saved to DCIM/WorkSync", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to save QR: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
