package com.android.chen.filesecuritysystem.Tools;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class SGFileUpload {
	private static final String TAG = "uploadFile";
	private static final int TIME_OUT = 10 * 10000000; // 超时时间
	private static final String CHARSET = "utf-8"; // 设置编码
	public static final String SUCCESS = "1";
	public static final String FAILURE = "0";

	
	/**
	 * android上传文件到服务器
	 * 
	 * @param file
	 *            需要上传的文件
	 * @param RequestURL
	 *            请求的url
	 * @return 返回响应的内容
	 */
	public static String uploadFile(File file, String RequestURL, String fileContentType) {
		String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
		String PREFIX = "--", LINE_END = "\r\n";
		String CONTENT_TYPE = "multipart/form-data"; // 内容类型
		
		try {
			URL url = new URL(RequestURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(TIME_OUT);
			conn.setConnectTimeout(TIME_OUT);
			conn.setDoInput(true); // 允许输入流
			conn.setDoOutput(true); // 允许输出流
			conn.setUseCaches(false); // 不允许使用缓存
			conn.setRequestMethod("POST"); // 请求方式
			conn.setRequestProperty("Charset", CHARSET); // 设置编码
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
			
			if (file != null) {
				/**
				 * 当文件不为空，把文件包装并且上传
				 */
				OutputStream outputSteam = conn.getOutputStream();

				DataOutputStream dos = new DataOutputStream(outputSteam);
				StringBuffer sb = new StringBuffer();
				sb.append(PREFIX);
				sb.append(BOUNDARY);
				sb.append(LINE_END);
				
				/**
				 * 这里重点注意： name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
				 * filename是文件的名字，包含后缀名的 比如:abc.png
				 */

				sb.append(
						"Content-Disposition: form-data; name=\"Filedata\"; filename=\"" + file.getName() + "\"" + LINE_END);
				sb.append("Content-Type: "+fileContentType+"; charset=" + CHARSET + LINE_END);
						
				sb.append(LINE_END);
				dos.write(sb.toString().getBytes());
				InputStream is = new FileInputStream(file);
				byte[] bytes = new byte[4096];
				int len = 0;
				while ((len = is.read(bytes)) != -1) {
					dos.write(bytes, 0, len);
				}
				is.close();
				dos.write(LINE_END.getBytes());				
				
//				StringBuffer txtData = new StringBuffer();
//				txtData.append("Content-Disposition: form-data; name=\"attachType\"" + LINE_END); 
//				txtData.append(LINE_END);
//				txtData.append("123"+LINE_END);
//				dos.write((PREFIX + BOUNDARY).getBytes());
//				dos.write(txtData.toString().getBytes());
//				dos.write(LINE_END.getBytes());
				
				byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
				dos.write(end_data);
				dos.flush();
				/**
				 * 获取响应码 200=成功 当响应成功，获取响应的流
				 */
				int res = conn.getResponseCode();
				if (res == 200) {
					InputStream iss = conn.getInputStream();
					InputStreamReader isr = new InputStreamReader(iss,"utf-8");
					BufferedReader br = new BufferedReader(isr);
					String result = br.readLine();
					//Toast.makeText(mContext,result,Toast.LENGTH_SHORT).show();
					Log.d("XXXXXXXXXXXXXXX SG","httpURLConnection.getResponseCode() = "+conn.getResponseCode()
							+"\r\n upload result ......"+result);
					return SUCCESS;
				}
				dos.close();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return FAILURE;
	}
	
	public static void main(String[] arg){
		SGFileUpload sgUpload = new SGFileUpload();
		
		File file = new File("C:\\Users\\Public\\Pictures\\Sample Pictures\\encrypt_IMG_20150107_012130.jpg");
//		String url = "http://192.168.163.158:8088/Sg_sns/index.php?app=api&mod=MobileUser&act=uploadFile&oauth_token=528c07e254c6ae3eada378f3a2b4a34c&oauth_token_secret=71ca870be041e0a0f94bb3e039b009e1";
		String url = "http://poc.chinashuguo.com:8989/upload/mobile?uname=admin&pwd=123456&ismobile=1";
		sgUpload.uploadFile(file, url, "application/octet-stream");
		
		
	}
}
