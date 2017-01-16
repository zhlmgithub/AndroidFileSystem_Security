package com.android.chen.filesecuritysystem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.chen.filesecuritysystem.Adapter.FileListAdapter;
import com.android.chen.filesecuritysystem.Bean.FileItem;
import com.android.chen.filesecuritysystem.Callback.ItemClickCallback;
import com.android.chen.filesecuritysystem.Tools.FilePathHeap;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

//    static final String ROOT_PATH = "/";
    static final String ROOT_PATH = "/storage";
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    List<FileItem> fileItems = new ArrayList<>();

    FileListAdapter mAdapter;

    RecyclerView rvFileList;

    LinearLayoutManager linearLayoutManager;

    private AlertDialog mIPDialog ;
    private RelativeLayout mIPDialogLayout ;
    private EditText mIPEt ;
    private Button mIPPositiveBtn ;
    private Button mIPNegativeBtn ;

    private ProgressDialog mPDialog = null;

    ItemClickCallback itemClickCallback = new ItemClickCallback() {
        @Override
        public void updateView(String path) {
            showFileDir(path);
        }
    };

    private static final int MSG_SHOW_PROGRESSDIALOG = 1;
    private static final int MSG_CANCEL_PROGRESSDIALOG = 2;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_SHOW_PROGRESSDIALOG:
                    mPDialog = ProgressDialog.show(MainActivity.this,"","正在加载文件，请稍后...");
                    break;
                case MSG_CANCEL_PROGRESSDIALOG:
                    if(mPDialog != null){
                        mPDialog.cancel();
                    }
            }
        }
    };

    static final String TAG = "TAG_MainActivity";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        FilePathHeap.push(ROOT_PATH);
        showFileDir(ROOT_PATH);
        Log.d("XXXXXXXXXXXXXXXX","test jni str = "+stringFromJNI());
    }

    private void initView() {
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar);
        mCollapsingToolbarLayout.setTitle("文件加解密系统");
        mCollapsingToolbarLayout.setExpandedTitleColor(R.color.white);
        mCollapsingToolbarLayout.setCollapsedTitleTextColor(R.color.black);
        rvFileList = (RecyclerView) findViewById(R.id.rvFileList);
        linearLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
        rvFileList.setLayoutManager(linearLayoutManager);
        rvFileList.setNestedScrollingEnabled(false);
    }

    /*private void showFileDir(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        FileItem fileItem;
        String fileName;
        String filePath;
        File typeFile;
        fileItems = new ArrayList<>();

        //增加第一个返回上个路径的item

        addFirstItem();
        if (files != null) {
            for (File file1 : files) {
                fileItem = new FileItem();
                fileName = file1.getName();
                filePath = file1.getAbsolutePath();
                fileItem.setFileName(fileName);
                fileItem.setFilePath(filePath);
                typeFile = new File(filePath);
                if (!typeFile.isDirectory()) {
                    fileItem.setType(FileItem.TYPE_FILE_DECRYPT);
                    if (fileName.length() > 7) {
                        if (fileName.substring(fileName.length() - 7).equalsIgnoreCase(".cipher")) {
                            fileItem.setType(FileItem.TYPE_FILE_ENCRYPTED);
                        }
                    }
                } else {
                    fileItem.setType(FileItem.TYPE_DIRECTPRY);
                }
                fileItems.add(fileItem);
            }
        }
        mAdapter = new FileListAdapter(MainActivity.this, fileItems, itemClickCallback);
        rvFileList.setAdapter(mAdapter);
    }*/

    private void showFileDir(String path) {
        final File file = new File(path);
        final File[] files = file.listFiles();
        FileItem fileItem;
        String fileName;
        String filePath;
        File typeFile;
        fileItems = new ArrayList<>();

        //增加第一个返回上个路径的item

        addFirstItem();
        if (files != null) {
            if(files.length > 0){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(files.length > 500){
                            mHandler.sendEmptyMessage(MSG_SHOW_PROGRESSDIALOG);
                        }
                        for (File file1 : files) {
                            FileItem fileItem = new FileItem();
                            String fileName = file1.getName();
                            String filePath = file1.getAbsolutePath();
                            fileItem.setFileName(fileName);
                            fileItem.setFilePath(filePath);
                            File typeFile = new File(filePath);
                            if (!typeFile.isDirectory()) {
                                fileItem.setType(FileItem.TYPE_FILE_DECRYPT);
                                if (fileName.length() > 7) {
                                    if (fileName.substring(fileName.length() - 7).equalsIgnoreCase(".cipher")) {
                                        fileItem.setType(FileItem.TYPE_FILE_ENCRYPTED);
                                    }
                                }
                            } else {
                                fileItem.setType(FileItem.TYPE_DIRECTPRY);
                            }
                            fileItems.add(fileItem);
                        }
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter = new FileListAdapter(MainActivity.this, fileItems, itemClickCallback);
                                rvFileList.setAdapter(mAdapter);
                                mHandler.sendEmptyMessage(MSG_CANCEL_PROGRESSDIALOG);
                            }
                        },10);
                    }
                }).start();

            }
        }
        //mAdapter = new FileListAdapter(MainActivity.this, fileItems, itemClickCallback);
        //rvFileList.setAdapter(mAdapter);
    }


    private void addFirstItem() {
        FileItem fileItem = new FileItem();
        fileItem.setType(FileItem.TYPE_DIRECTPRY);
        fileItem.setFileName("..");
        fileItems.add(fileItem);
    }

    private void showInputIPDialog(){
        mIPDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = LayoutInflater.from(this);
        mIPDialogLayout = (RelativeLayout) inflater.inflate(R.layout.input_ip_layout, null);
        mIPEt = (EditText) mIPDialogLayout.findViewById(R.id.input_ip_et);
        mIPPositiveBtn = (Button) mIPDialogLayout.findViewById(R.id.btn_save);
        mIPNegativeBtn = (Button) mIPDialogLayout.findViewById(R.id.btn_cancel);
        mIPDialog.show();
        mIPDialog.getWindow().setContentView(mIPDialogLayout);
        mIPDialog.getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        mIPDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mIPPositiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ipAddress = mIPEt.getText().toString();
                if(!TextUtils.isEmpty(ipAddress)){
                    ipAddress.replaceAll(" ","");
                    getSharedPreferences("ip_sp", Context.MODE_PRIVATE).edit().putString("ip_address",ipAddress).apply();
                }else if(TextUtils.isEmpty(ipAddress)){
                    Toast.makeText(MainActivity.this,"输入的IP地址为空", Toast.LENGTH_SHORT).show();
                }
                mIPDialog.dismiss();
            }
        });
        mIPNegativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIPDialog.dismiss();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE,Menu.FIRST+0,0,"修改IP地址");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("XXXXXXXXXXXXXX","itemId = "+item.getItemId());
        switch(item.getItemId()){
            case Menu.FIRST+0:
                showInputIPDialog();
                break;
            default:break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();


}
