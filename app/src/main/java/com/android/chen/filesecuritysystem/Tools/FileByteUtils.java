package com.android.chen.filesecuritysystem.Tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by liming.zhang on 2016/12/23.
 */

public class FileByteUtils {
    /**
     * 获得指定文件的byte数组
     */
    /*public static byte[] getBytes(String filePath){
        ArrayList<Byte> result = new ArrayList<Byte>();
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
                buffer = bos.toByteArray();
                bos.flush();
                for(int i=0;i<buffer.length;i++){
                    result.add(buffer[i]);
                }
            }
            fis.close();
            bos.close();
            //buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //return buffer;
        buffer = null;
        for(int j = 0;j<result.size();j++){
            buffer[j] = result.get(j);
        }
        return buffer;
    }*/

    public static byte[] getBytes(String filePath){
        byte[] buffer = null;
        /*try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);

            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        try{

            File file = new File(filePath);
            BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(file));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();  /* no hint !! */

            int b;
            while ((b = bis.read()) != -1) {
                baos.write((byte) b);
            }
            //buffer = baos.toByteArray();
            buffer = getBuffer(baos);
        }catch(Exception e){
            e.printStackTrace();
        }
        return buffer;

    }

    /**
     * Returns the internal raw buffer of a ByteArrayOutputStream, without copying.
     */
    public static byte[] getBuffer(ByteArrayOutputStream bout) {
        final byte[][] result = new byte[1][];
        try {
            bout.writeTo(new OutputStream() {
                @Override
                public void write(byte[] buf, int offset, int length) {
                    result[0] = buf;
                }

                @Override
                public void write(int b) {}
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result[0];
    }

    /**
     * 根据byte数组，生成文件
     */
    public static File getFile(byte[] bfile, String filePath,String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if(!dir.exists()&&dir.isDirectory()){//判断文件目录是否存在
                dir.mkdirs();
            }
            file = new File(filePath+"/"+fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return file;
    }
}
