package com.android.chen.filesecuritysystem.DES;

import com.android.chen.filesecuritysystem.Tools.FileByteUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;

import static android.R.attr.data;

/**
 * Created by leixun on 16/12/1.
 */

public class DesEncryption {

    private byte[] mData;
    private byte[] mKey;

//    public byte[] encrypt(byte[] data, byte[] key) throws Exception {
    public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
//        mData = data;
//        mKey = key;

        SecureRandom secureRandom = new SecureRandom();

        //DESedeKeySpec deSedeKeySpec = new DESedeKeySpec(key);
        DESKeySpec deSedeKeySpec = new DESKeySpec(key);

        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = secretKeyFactory.generateSecret(deSedeKeySpec);

        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, secureRandom);

        return cipher.doFinal(data);
    }

    /*
    * 用这个方法，在加密大文件的时候，在Android上80M的文件，也没有出现OOM的异常。
    * */
    public static File encrypt(File inFile,byte []data,byte[]key,String outFilePath,String outFileName){

        File outFile = null;
        try{

            outFile = new File(outFilePath+"/"+outFileName);
            FileInputStream fis = new FileInputStream(inFile/*FileByteUtils.getFile(data,filePath,fileName)*/);
            FileOutputStream fos = new FileOutputStream(outFile);


            SecureRandom secureRandom = new SecureRandom();
            DESKeySpec deSedeKeySpec = new DESKeySpec(key);

            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = secretKeyFactory.generateSecret(deSedeKeySpec);

            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, secureRandom);

            byte []buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer,0,4096)) != -1)
            {
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) fos.write(output);
            }

            byte[] output = cipher.doFinal();
            if (output != null) fos.write(output);

            fis.close();
            fos.close();
        }catch(Exception e){
            e.printStackTrace();
        }

        return outFile;
    }


}
