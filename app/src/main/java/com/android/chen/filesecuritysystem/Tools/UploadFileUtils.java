package com.android.chen.filesecuritysystem.Tools;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by liming.zhang on 2017/1/5.
 */

public class UploadFileUtils {

    String BOUNDARY = java.util.UUID.randomUUID().toString();
    String PREFIX = "--";
    String POSTFIX = "\r\n";

    private Context mContext;
    HttpURLConnection httpURLConnection;

    public UploadFileUtils(Context context){
        mContext = context;
    }

    public void uploadFile(String uploadUrl,String filePath){
        try{
            //添加此句，是为了避免下面的异常
            // System.err: java.net.SocketException: sendto failed: EPIPE (Broken pipe)
            System.setProperty("http.keepAlive", "false");
            URL url = new URL(uploadUrl);
            httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Connection","Keep-Alive");
            httpURLConnection.setRequestProperty("Charset","UTF-8");
            httpURLConnection.setRequestProperty("Content-Type","multipart/form-data;boundary="+BOUNDARY);
            //httpURLConnection.setReadTimeout(20*1000);
            //httpURLConnection.setConnectTimeout(50*1000);
            httpURLConnection.setChunkedStreamingMode(1024);
            //httpURLConnection.connect();
            DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());

            File file = new File(filePath);
            Log.d("XXXXXXXXXXXXXX","file = "+file+",file.exists = "+file.exists());
            if(file != null && file.exists()){
                /*dos.writeBytes(PREFIX+BOUNDARY+POSTFIX);
                dos.writeBytes("Content-Disposition:form-data;name=\"Filedata\";filename="+"\""+file.getName()+"\""+POSTFIX);
                dos.writeBytes("Content-Type:application/octet-stream;charset=utf-8"+POSTFIX);
                dos.writeBytes(POSTFIX);*/

                StringBuilder sb = new StringBuilder();
                sb.append(PREFIX+BOUNDARY+POSTFIX);
                sb.append("Content-Disposition: form-data; name=\"Filedata\"; filename=\"" + file.getName() + "\"" +POSTFIX);
                sb.append("Content-Type:application/octet-stream;charset=utf-8"+POSTFIX);
                sb.append(POSTFIX);

                dos.write(sb.toString().getBytes());
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                byte []buffer = new byte[8192];
                int count = 0;
                //while((count = fis.read(buffer)) != -1){
                while((count = bis.read(buffer)) != -1){
                    dos.write(buffer,0,count);
                    /*try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }*/
                    //dos.flush();
                }
                fis.close();
                bis.close();
            }
            dos.write(POSTFIX.getBytes());
            dos.write((PREFIX+BOUNDARY+PREFIX+POSTFIX).getBytes());

            dos.flush();
            dos.close();


            //只有执行了这个，数据流才会正式发出去，之前的都只是建立了一个tcp连接。
            InputStream is = httpURLConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is,"utf-8");
            BufferedReader br = new BufferedReader(isr);
            String result = br.readLine();
            is.close();
            Log.d("XXXXXXXXXXXXXXXXXXX","httpURLConnection.getResponseCode() = "+httpURLConnection.getResponseCode()
                    +"\r\n upload result ......"+result);
            //httpURLConnection.disconnect();
        }catch(Exception e){
            Log.d("XXXXXXXXXXXXXXXXXXX","upload excepiton ......");
            e.printStackTrace();
        }finally {
            Log.d("XXXXXXXXXXXXXXXXXXX","upload finally ......");
            httpURLConnection.disconnect();
        }

    }

}
