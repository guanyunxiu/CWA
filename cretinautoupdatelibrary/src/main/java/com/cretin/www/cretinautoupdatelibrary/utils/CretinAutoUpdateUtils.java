package com.cretin.www.cretinautoupdatelibrary.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cretin.www.cretinautoupdatelibrary.R;
import com.cretin.www.cretinautoupdatelibrary.model.LibraryUpdateEntity;
import com.cretin.www.cretinautoupdatelibrary.model.UpdateEntity;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.os.Build.VERSION_CODES.M;


/**
 * Created by cretin on 2017/3/13.
 */

public class CretinAutoUpdateUtils {
    //广播接受者
    private static MyReceiver receiver;
    private static CretinAutoUpdateUtils cretinAutoUpdateUtils;

    //定义一个展示下载进度的进度条
    private static ProgressDialog progressDialog;

    private static Context mContext;

    //检查更新的url
    private static String checkUrl;

    //展示下载进度的方式 对话框模式 通知栏进度条模式
    private static int showType = Builder.TYPE_DIALOG;
    //是否展示忽略此版本的选项 默认开启
    private static boolean canIgnoreThisVersion = true;
    //app图标
    private static int iconRes;
    //appName
    private static String appName;
    //是否开启日志输出
    private static boolean showLog = true;
    //自定义Bean类
    private static Object cls;
    //设置请求方式
    private static int requestMethod = Builder.METHOD_POST;

    //私有化构造方法
    private CretinAutoUpdateUtils() {

    }

    /**
     * 检查更新
     */
    public void check() {
        if ( TextUtils.isEmpty(checkUrl) ) {
            throw new RuntimeException("checkUrl is null. You must call init before using the cretin checking library.");
        } else {
            new DownDataAsyncTask().execute();
        }
    }

    /**
     * 初始化url
     *
     * @param url
     */
    public static void init(String url) {
        checkUrl = url;
    }

    /**
     * 初始化url
     *
     * @param builder
     */
    public static void init(Builder builder) {
        checkUrl = builder.baseUrl;
        showType = builder.showType;
        canIgnoreThisVersion = builder.canIgnoreThisVersion;
        iconRes = builder.iconRes;
        showLog = builder.showLog;
        requestMethod = builder.requestMethod;
        cls = builder.cls;
    }

    /**
     * getInstance()
     *
     * @param context
     * @return
     */
    public static CretinAutoUpdateUtils getInstance(Context context) {
        mContext = context;
        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.MY_RECEIVER");
        //注册
        context.registerReceiver(receiver, filter);
        requestPermission(null);
        if ( cretinAutoUpdateUtils == null ) {
            cretinAutoUpdateUtils = new CretinAutoUpdateUtils();
        }
        return cretinAutoUpdateUtils;
    }


    /**
     * 取消广播的注册
     */
    public void destroy() {
        //不要忘了这一步
        if ( mContext != null && intent != null )
            mContext.stopService(intent);
        if ( mContext != null && receiver != null )
            mContext.unregisterReceiver(receiver);
    }

    /**
     * 异步任务下载数据
     */
    class DownDataAsyncTask extends AsyncTask<String, Void, UpdateEntity> {

        @Override
        protected UpdateEntity doInBackground(String... params) {
            HttpURLConnection httpURLConnection = null;
            InputStream is = null;
            StringBuilder sb = new StringBuilder();
            try {
                //准备请求的网络地址
                URL url = new URL(checkUrl);
                //调用openConnection得到网络连接，网络连接处于就绪状态
                httpURLConnection = ( HttpURLConnection ) url.openConnection();
                //设置网络连接超时时间5S
                httpURLConnection.setConnectTimeout(5 * 1000);
                //设置读取超时时间
                httpURLConnection.setReadTimeout(5 * 1000);
                if ( requestMethod == Builder.METHOD_POST ) {
                    httpURLConnection.setRequestMethod("POST");
                } else {
                    httpURLConnection.setRequestMethod("GET");
                }
                httpURLConnection.connect();
                //if连接请求码成功
                if ( httpURLConnection.getResponseCode() == httpURLConnection.HTTP_OK ) {
                    is = httpURLConnection.getInputStream();
                    byte[] bytes = new byte[1024];
                    int i = 0;
                    while ( (i = is.read(bytes)) != -1 ) {
                        sb.append(new String(bytes, 0, i, "utf-8"));
                    }
                    is.close();
                }
                if ( showLog ) {
                    if ( TextUtils.isEmpty(sb.toString()) ) {
                        Log.e("cretinautoupdatelibrary", "自动更新library返回的数据为空，" +
                                "请检查请求方法是否设置正确，默认为post请求，再检查地址是否输入有误");
                    } else {
                        Log.e("cretinautoupdatelibrary", "自动更新library返回的数据：" + sb.toString());
                    }
                }
                if ( cls != null ) {
                    if ( cls instanceof LibraryUpdateEntity ) {
                        LibraryUpdateEntity o = ( LibraryUpdateEntity )
                                JSONHelper.parseObject(sb.toString(), cls.getClass());//反序列化
                        UpdateEntity updateEntity = new UpdateEntity();
                        updateEntity.setVersionCode(o.getVersionCodes());
                        updateEntity.setIsForceUpdate(o.getIsForceUpdates());
                        updateEntity.setPreBaselineCode(o.getPreBaselineCodes());
                        updateEntity.setVersionName(o.getVersionNames());
                        updateEntity.setDownurl(o.getDownurls());
                        updateEntity.setUpdateLog(o.getUpdateLogs());
                        updateEntity.setSize(o.getApkSizes());
                        updateEntity.setHasAffectCodes(o.getHasAffectCodess());
                        return updateEntity;
                    } else {
                        throw new RuntimeException("未实现接口：" +
                                cls.getClass().getName()+"未实现LibraryUpdateEntity接口");
                    }
                }
                return JSONHelper.parseObject(sb.toString(), UpdateEntity.class);//反序列化
            } catch ( MalformedURLException e ) {
                e.printStackTrace();
            } catch ( IOException e ) {
                e.printStackTrace();
            } catch ( JSONException e ) {
                throw new RuntimeException("json解析错误，" +
                        "请按照library中的UpdateEntity所需参数返回数据，json必须包含UpdateEntity所需全部字段");
            } catch ( Exception e ) {
                e.printStackTrace();
            } finally {
                if ( httpURLConnection != null ) {
                    httpURLConnection.disconnect();
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(UpdateEntity data) {
            super.onPostExecute(data);
            if ( data != null ) {
                if ( data.isForceUpdate == 2 ) {
                    //所有旧版本强制更新
                    showUpdateDialog(data, true, false);
                } else if ( data.isForceUpdate == 1 ) {
                    //hasAffectCodes提及的版本强制更新
                    if ( data.versionCode > getVersionCode(mContext) ) {
                        //有更新
                        String[] hasAffectCodes = data.hasAffectCodes.split("\\|");
                        if ( Arrays.asList(hasAffectCodes).contains(getVersionCode(mContext) + "") ) {
                            //被列入强制更新 不可忽略此版本
                            showUpdateDialog(data, true, false);
                        } else {
                            String dataVersion = data.versionName;
                            if ( !TextUtils.isEmpty(dataVersion) ) {
                                List listCodes = loadArray();
                                if ( !listCodes.contains(dataVersion) ) {
                                    //没有设置为已忽略
                                    showUpdateDialog(data, false, true);
                                }
                            }
                        }
                    }
                } else if ( data.isForceUpdate == 0 ) {
                    if ( data.versionCode > getVersionCode(mContext) ) {
                        showUpdateDialog(data, false, true);
                    }
                }
            } else {
                Toast.makeText(mContext, "网络错误", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 显示更新对话框
     *
     * @param data
     */
    private void showUpdateDialog(final UpdateEntity data, boolean isForceUpdate, boolean showIgnore) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        AlertDialog alertDialog = builder.create();
        String updateLog = data.updateLog;
        if ( TextUtils.isEmpty(updateLog) )
            updateLog = "新版本，欢迎更新";
        String versionName = data.versionName;
        if ( TextUtils.isEmpty(versionName) ) {
            versionName = "1.1";
        }
        alertDialog.setTitle("新版本v" + versionName);
        alertDialog.setMessage(updateLog);
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startUpdate(data);
            }
        });
        if ( canIgnoreThisVersion && showIgnore ) {
            final String finalVersionName = versionName;
            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "忽略此版本", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //忽略此版本
                    List listCodes = loadArray();
                    if ( listCodes != null ) {
                        listCodes.add(finalVersionName);
                    } else {
                        listCodes = new ArrayList();
                        listCodes.add(finalVersionName);
                    }
                    saveArray(listCodes);
                    Toast.makeText(mContext, "此版本已忽略", Toast.LENGTH_SHORT).show();
                }
            });
        }
        if ( isForceUpdate ) {
            alertDialog.setCancelable(false);
        }
        alertDialog.show();
        (( TextView ) alertDialog.findViewById(android.R.id.message)).setLineSpacing(5, 1);
        Button btnPositive =
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button btnNegative =
                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        btnNegative.setTextColor(Color.parseColor("#16b2f5"));
        if ( canIgnoreThisVersion && showIgnore ) {
            Button btnNeutral =
                    alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
            btnNeutral.setTextColor(Color.parseColor("#16b2f5"));
        }
        btnPositive.setTextColor(Color.parseColor("#16b2f5"));
    }

    private static int PERMISSON_REQUEST_CODE = 2;

    @TargetApi( M )
    private static void requestPermission(final UpdateEntity data) {
        if ( ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ) {
            // 第一次请求权限时，用户如果拒绝，下一次请求shouldShowRequestPermissionRationale()返回true
            // 向用户解释为什么需要这个权限
            if ( ActivityCompat.shouldShowRequestPermissionRationale(( Activity ) mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) ) {
                new AlertDialog.Builder(mContext)
                        .setMessage("申请存储权限")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //申请相机权限
                                ActivityCompat.requestPermissions(( Activity ) mContext,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSON_REQUEST_CODE);
                            }
                        })
                        .show();
            } else {
                //申请相机权限
                ActivityCompat.requestPermissions(( Activity ) mContext,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSON_REQUEST_CODE);
            }
        } else {
            if ( data != null ) {
                if ( !TextUtils.isEmpty(data.downurl) ) {
                    if ( Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ) {
                        try {
                            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                            final String fileName = filePath + "/" + getPackgeName(mContext) + "-v" + getVersionName(mContext) + ".apk";
                            final File file = new File(fileName);
                            //如果不存在
                            if ( !file.exists() ) {
                                //检测当前网络状态
                                if ( !NetWorkUtils.getCurrentNetType(mContext).equals("wifi") ) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                    builder.setTitle("提示");
                                    builder.setMessage("当前处于非WIFI连接，是否继续？");
                                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            createFileAndDownload(file, data.downurl);
                                        }
                                    });
                                    builder.setNegativeButton("取消", null);
                                    builder.show();
                                } else {
                                    createFileAndDownload(file, data.downurl);
                                }
                            } else {
                                if ( file.length() == Long.parseLong(data.size) ) {
                                    installApkFile(mContext, file);
                                    return;
                                } else {
                                    if ( !NetWorkUtils.getCurrentNetType(mContext).equals("wifi") ) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                        builder.setTitle("提示");
                                        builder.setMessage("当前处于非WIFI连接，是否继续？");
                                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                createFileAndDownload(file, data.downurl);
                                            }
                                        });
                                        builder.setNegativeButton("取消", null);
                                        builder.show();
                                    } else {
                                        createFileAndDownload(file, data.downurl);
                                    }
                                }
                            }
                        } catch ( Exception e ) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(mContext, "没有挂载的SD卡", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, "下载路径为空", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private static Intent intent;

    //创建文件并下载文件
    private static void createFileAndDownload(File file, String downurl) {
        if ( !file.getParentFile().exists() ) {
            file.getParentFile().mkdirs();
        }
        try {
            if ( !file.createNewFile() ) {
                Toast.makeText(mContext, "文件创建失败", Toast.LENGTH_SHORT).show();
            } else {
                //文件创建成功
                intent = new Intent(mContext, DownloadService.class);
                intent.putExtra("downUrl", downurl);
                intent.putExtra("appName", mContext.getString(R.string.app_name));
                intent.putExtra("type", showType);
                if ( iconRes != 0 )
                    intent.putExtra("icRes", iconRes);
                mContext.startService(intent);

                //显示dialog
                if ( showType == Builder.TYPE_DIALOG ) {
                    progressDialog = new ProgressDialog(mContext);
                    if ( iconRes != 0 ) {
                        progressDialog.setIcon(iconRes);
                    } else {
                        progressDialog.setIcon(R.mipmap.ic_launcher1);
                    }
                    progressDialog.setTitle("正在更新...");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//设置进度条对话框//样式（水平，旋转）
                    //进度最大值
                    progressDialog.setMax(100);
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                }
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     * 开始更新操作
     */
    public void startUpdate(UpdateEntity data) {
        requestPermission(data);
    }

    /**
     * 广播接收器
     *
     * @author user
     */
    private static class MyReceiver extends DownloadReceiver {
        @Override
        protected void downloadComplete() {
            if ( mContext != null && intent != null )
                mContext.stopService(intent);
            if ( mContext != null && receiver != null )
                mContext.unregisterReceiver(receiver);
            if ( progressDialog != null )
                progressDialog.dismiss();
        }

        @Override
        protected void downloading(int progress) {
            if ( progressDialog != null )
                progressDialog.setProgress(progress);
        }

        @Override
        protected void downloadFail(String e) {
            if ( progressDialog != null )
                progressDialog.dismiss();
            Toast.makeText(mContext, "下载失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 安装app
     *
     * @param context
     * @param file
     */
    public static void installApkFile(Context context, File file) {
        Intent intent1 = new Intent(Intent.ACTION_VIEW);
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
            intent1.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
            intent1.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent1.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if ( context.getPackageManager().queryIntentActivities(intent1, 0).size() > 0 ) {
            context.startActivity(intent1);
        }
    }

    /**
     * 获得apkPackgeName
     *
     * @param context
     * @return
     */
    public static String getPackgeName(Context context) {
        String packName = "";
        PackageInfo packInfo = getPackInfo(context);
        if ( packInfo != null ) {
            packName = packInfo.packageName;
        }
        return packName;
    }

    private static String getVersionName(Context context) {
        String versionName = "";
        PackageInfo packInfo = getPackInfo(context);
        if ( packInfo != null ) {
            versionName = packInfo.versionName;
        }
        return versionName;
    }

    /**
     * 获得apk版本号
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        int versionCode = 0;
        PackageInfo packInfo = getPackInfo(context);
        if ( packInfo != null ) {
            versionCode = packInfo.versionCode;
        }
        return versionCode;
    }


    /**
     * 获得apkinfo
     *
     * @param context
     * @return
     */
    public static PackageInfo getPackInfo(Context context) {
        // 获取packagemanager的实例
        PackageManager packageManager = context.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch ( PackageManager.NameNotFoundException e ) {
            e.printStackTrace();
        }
        return packInfo;
    }

    //建造者模式
    public static final class Builder {
        private String baseUrl;
        private int showType = TYPE_DIALOG;
        //是否显示忽略此版本 true 是 false 否
        private boolean canIgnoreThisVersion = true;
        //在通知栏显示进度
        public static final int TYPE_NITIFICATION = 1;
        //对话框显示进度
        public static final int TYPE_DIALOG = 2;
        //POST方法
        public static final int METHOD_POST = 3;
        //GET方法
        public static final int METHOD_GET = 4;
        //显示的app资源图
        private int iconRes;
        //显示的app名
        private String appName;
        //显示log日志
        private boolean showLog;
        //设置请求方式
        private int requestMethod = METHOD_POST;
        //自定义Bean类
        private Object cls;

        public final CretinAutoUpdateUtils.Builder setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public final CretinAutoUpdateUtils.Builder setTransition(Object cls) {
            this.cls = cls;
            return this;
        }

        public final CretinAutoUpdateUtils.Builder showLog(boolean showLog) {
            this.showLog = showLog;
            return this;
        }

        public final CretinAutoUpdateUtils.Builder setRequestMethod(int requestMethod) {
            this.requestMethod = requestMethod;
            return this;
        }

        public final CretinAutoUpdateUtils.Builder setShowType(int showType) {
            this.showType = showType;
            return this;
        }

        public final CretinAutoUpdateUtils.Builder setIconRes(int iconRes) {
            this.iconRes = iconRes;
            return this;
        }

        public final CretinAutoUpdateUtils.Builder setAppName(String appName) {
            this.appName = appName;
            return this;
        }

        public final CretinAutoUpdateUtils.Builder setIgnoreThisVersion(boolean canIgnoreThisVersion) {
            this.canIgnoreThisVersion = canIgnoreThisVersion;
            return this;
        }

        public final Builder build() {
            return this;
        }
    }

    public boolean saveArray(List<String> list) {
        SharedPreferences sp = mContext.getSharedPreferences("ingoreList", mContext.MODE_PRIVATE);
        SharedPreferences.Editor mEdit1 = sp.edit();
        mEdit1.putInt("Status_size", list.size());

        for ( int i = 0; i < list.size(); i++ ) {
            mEdit1.remove("Status_" + i);
            mEdit1.putString("Status_" + i, list.get(i));
        }
        return mEdit1.commit();
    }

    public List loadArray() {
        List<String> list = new ArrayList<>();
        SharedPreferences mSharedPreference1 = mContext.getSharedPreferences("ingoreList", mContext.MODE_PRIVATE);
        list.clear();
        int size = mSharedPreference1.getInt("Status_size", 0);
        for ( int i = 0; i < size; i++ ) {
            list.add(mSharedPreference1.getString("Status_" + i, null));
        }
        return list;
    }
}
