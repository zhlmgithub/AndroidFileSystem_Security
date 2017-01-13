package com.android.chen.filesecuritysystem.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Message;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.chen.filesecuritysystem.Bean.FileItem;
import com.android.chen.filesecuritysystem.Callback.ItemClickCallback;
import com.android.chen.filesecuritysystem.DES.DesEncryption;
import com.android.chen.filesecuritysystem.R;
import com.android.chen.filesecuritysystem.Tools.FileByteUtils;
import com.android.chen.filesecuritysystem.Tools.FilePathHeap;
import com.android.chen.filesecuritysystem.Tools.SGFileUpload;
import com.android.chen.filesecuritysystem.Tools.UploadFileUtils;

import java.io.File;
import java.util.List;
import android.os.Handler;

/**
 * Created by leixun on 16/12/2.
 */

public class FileListAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private LayoutInflater layoutInflater;
    private Context mContext;
    private List<FileItem> mFiles;

    private ItemClickCallback itemClickCallback;

    static final String TAG = "TAG_FileAdapter";


    public FileListAdapter(Context mContext, List<FileItem> mFiles, ItemClickCallback itemClickCallback) {
        this.mContext = mContext;
        this.mFiles = mFiles;
        this.itemClickCallback = itemClickCallback;
        layoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_filelist, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String type = mFiles.get(position).getType();
        switch (type) {
            case FileItem.TYPE_FILE_ENCRYPTED:
                holder.ivFileIcon.setImageResource(R.mipmap.file_encrypt);
                break;
            case FileItem.TYPE_FILE_DECRYPT:
                holder.ivFileIcon.setImageResource(R.mipmap.file);
                break;
            case FileItem.TYPE_DIRECTPRY:
                holder.ivFileIcon.setImageResource(R.mipmap.directory);
                break;
        }

        holder.tvFileName.setText(mFiles.get(position).getFileName());
        holder.itemClickCallback = itemClickCallback;
        /**
         * 判断是否是第一个返回上一级的item，并传入对应的文件路径。
         */
        holder.filePath = mFiles.get(position).getFilePath();
        holder.type = type;
        holder.mContext = mContext;
        holder.position = position;

    }

}

class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    public static final String ENCRYPT = "ENCRYPT";
    public static final String DECRYPT = "DECRYPT";

    ImageView ivFileIcon;
    TextView tvFileName;

    Context mContext;
    ItemClickCallback itemClickCallback;
    String filePath;
    String type;
    int position;
    private AlertDialog mPwdDialog ;
    private RelativeLayout mPwdDialogLayout ;
    private EditText mPwdEt ;
    private Button mPwdPositiveBtn ;
    private Button mPwdNegativeBtn ;

    private Handler mHandler = new Handler(){
        public int MSG_UPLOAD_FINISH = 1001;
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == MSG_UPLOAD_FINISH){
                Toast.makeText(mContext,"上传完成",Toast.LENGTH_SHORT).show();
            }
        }
    };

    static final String TAG = "TAG_MyViewHolder";

    public MyViewHolder(View itemView) {
        super(itemView);
        ivFileIcon = (ImageView) itemView.findViewById(R.id.ivFileIcon);
        tvFileName = (TextView) itemView.findViewById(R.id.tvFileName);
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (type.equals(FileItem.TYPE_DIRECTPRY)) {
            if (position == 0) {
                filePath = FilePathHeap.pop();
                if (filePath == null){
                    Toast.makeText(mContext, "已经是根目录了", Toast.LENGTH_SHORT).show();
                    return;
                }
                filePath = FilePathHeap.filePathList.get(0);
            } else {
                FilePathHeap.push(filePath);
            }
            itemClickCallback.updateView(filePath);
        } else {
            Toast.makeText(mContext, "这不是一个文件夹", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onLongClick(View v) {

        //encrypt
//        File file = new File(filePath);
//        File encryptFile = null;
//        Log.d("encrypt============","filepath = "+filePath+",filename = "+tvFileName.getText());
//        byte [] keyByte = "12345678".getBytes();
//        if(file != null && file.exists()){
//            byte [] fileByte = FileByteUtils.getBytes(filePath);
//            byte [] encryptByte = null;
//            try {
//                encryptByte = DesEncryption.encrypt(fileByte,keyByte);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            encryptFile = FileByteUtils.getFile(encryptByte,
//                    filePath.substring(0,filePath.lastIndexOf("/")),"encrypt_"+tvFileName.getText());
//            Toast.makeText(mContext,"encrypt file end...",Toast.LENGTH_SHORT).show();
//            final String  uploadFilepath = encryptFile.getAbsolutePath();
//            new Thread(){
//                public void run(){
//                    Log.d("XXXXXXXXXXXXXXXXXX","uploadFilepath = "+uploadFilepath);
//                    new UploadFileUtils(mContext).uploadFile(
//                            "http://poc.chinashuguo.com:8989/upload/mobile?uname=admin&pwd=123456&ismobile=1"
//                            ,uploadFilepath);
//                    /*SGFileUpload.uploadFile(new File(uploadFilepath)
//                            ,"http://poc.chinashuguo.com:8989/upload/mobile?uname=admin&pwd=123456&ismobile=1"
//                            ,"application/octet-stream");*/
//                }
//            }.start();
//
//            /*if(encryptFile != null && encryptFile.exists()){
//                try {
//                    byte[] decryptByte = DESDecryption.decrypt(FileByteUtils.getBytes(encryptFile.getAbsolutePath()),keyByte);
//                    FileByteUtils.getFile(decryptByte,
//                            encryptFile.getAbsolutePath().substring(0,encryptFile.getAbsolutePath().indexOf("encrypt_")),
//                            "decrypt_"+tvFileName.getText());
//                    Toast.makeText(mContext,"decrypt file end....",Toast.LENGTH_SHORT).show();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }*/
//        }
        //showPopMenu(v);
        if(type.equals(FileItem.TYPE_FILE_DECRYPT)){
            showEncryptDialog(filePath,ENCRYPT);
        }
        return true;
    }

    public void showPopMenu(View view){
        PopupMenu popupMenu = new PopupMenu(mContext,view);
        Menu menu = null;
        menu = popupMenu.getMenu();
        menu.add(Menu.NONE, Menu.FIRST + 0, 0, "加密");
        menu.add(Menu.NONE, Menu.FIRST + 1, 1, "解密");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case Menu.FIRST+0:
                        //showEncryptDialog(filePath);
                        break;
                    case Menu.FIRST+1:
                        Toast.makeText(mContext,"解密。。。",Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });

        popupMenu.show();

    }

    public void showEncryptDialog(final String filePath, final String enOrde){
        mPwdDialog = new AlertDialog.Builder(mContext).create();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mPwdDialogLayout = (RelativeLayout) inflater.inflate(R.layout.pwd_dialog_layout, null);
        mPwdEt = (EditText) mPwdDialogLayout.findViewById(R.id.sq_et);
        mPwdPositiveBtn = (Button) mPwdDialogLayout.findViewById(R.id.btn_save);
        mPwdNegativeBtn = (Button) mPwdDialogLayout.findViewById(R.id.btn_cancel);
        mPwdDialog.show();
        mPwdDialog.getWindow().setContentView(mPwdDialogLayout);
        mPwdDialog.getWindow().clearFlags(
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        mPwdDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mPwdPositiveBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String pwdStr = mPwdEt.getText().toString();
                pwdStr = "1234%^&*";
                if(!TextUtils.isEmpty(pwdStr)){
                    if(enOrde.equals(ENCRYPT)){

                    }
                    //encrypt
                    File file = new File(filePath);
                    File encryptFile = null;
                    Log.d("encrypt============","filepath = "+filePath+",filename = "+tvFileName.getText());
                    byte [] keyByte = pwdStr.getBytes();
                    if(file != null && file.exists()){
                        //byte [] fileByte = FileByteUtils.getBytes(filePath);
                        /*byte [] encryptByte = null;
                        try {
                            encryptByte = DesEncryption.encrypt(fileByte,keyByte);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        encryptFile = FileByteUtils.getFile(encryptByte,
                                filePath.substring(0,filePath.lastIndexOf("/")),"encrypt_"+tvFileName.getText());*/
                        encryptFile = DesEncryption.encrypt(file
                                ,null
                                ,keyByte
                                ,filePath.substring(0,filePath.lastIndexOf("/"))
                                ,"encrypt_"+tvFileName.getText());
                        Toast.makeText(mContext,"加密完成，开始上传文件",Toast.LENGTH_SHORT).show();
                        final String  uploadFilepath = encryptFile.getAbsolutePath();
                        new Thread(){
                            public void run(){
                                String ipAddress =
                                        mContext.getSharedPreferences("ip_sp",Context.MODE_PRIVATE).getString("ip_address","");
                                Log.d("XXXXXXXXXXXXXXXXXX","uploadFilepath = "+uploadFilepath+",ip address = "+ipAddress);
                                new UploadFileUtils(mContext).uploadFile(
                                        //"http://poc.chinashuguo.com:8989/upload/mobile?uname=admin&pwd=123456&ismobile=1"
                                        "http://"+ipAddress+":8080/upload/mobile?uname=admin&pwd=123456&ismobile=1"
                                        ,uploadFilepath);
                                /*SGFileUpload.uploadFile(new File(uploadFilepath),
                                    //,"http://poc.chinashuguo.com:8989/upload/mobile?uname=admin&pwd=123456&ismobile=1"
                                        "http://"+ipAddress+":8080/upload/mobile?uname=admin&pwd=123456&ismobile=1"
                                    ,"application/octet-stream");*/
                                mHandler.sendEmptyMessage(1001);
                            }
                        }.start();
                    }
                }
                mPwdDialog.dismiss();
            }
        });

        mPwdNegativeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mPwdDialog.dismiss();
            }
        });
    }
}