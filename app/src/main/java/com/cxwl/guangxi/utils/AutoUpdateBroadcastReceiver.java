package com.cxwl.guangxi.utils;

import java.io.File;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

public class AutoUpdateBroadcastReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		long myDwonloadID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
		SharedPreferences sPreferences = context.getSharedPreferences("downloadplato", 0);
		long refernece = sPreferences.getLong("plato", 0);
		if (refernece == myDwonloadID) {
			String serviceString = Context.DOWNLOAD_SERVICE;
			DownloadManager dManager = (DownloadManager) context.getSystemService(serviceString);
//			Intent install = new Intent(Intent.ACTION_VIEW);
			Uri downloadFileUri = dManager.getUriForDownloadedFile(myDwonloadID);
//			install.setDataAndType(downloadFileUri, "application/vnd.android.package-archive");
//			install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			context.startActivity(install);
			
			if (Build.VERSION.SDK_INT < 23) {
	            Intent intents = new Intent();
	            intents.setAction("android.intent.action.VIEW");
	            intents.addCategory("android.intent.category.DEFAULT");
	            intents.setType("application/vnd.android.package-archive");
	            intents.setData(downloadFileUri);
	            intents.setDataAndType(downloadFileUri, "application/vnd.android.package-archive");
	            intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            context.startActivity(intents);
	        } else {
	            File file = queryDownloadedApk(context, myDwonloadID);
	            if (file.exists()) {
	                openFile(file, context);
	            }
	        }
		}
	}
	
	/**
     * 通过downLoadId查询下载的apk，解决6.0以后安装的问题
     * @param context
     * @return
     */
    public static File queryDownloadedApk(Context context, long downloadId) {
        File targetApkFile = null;
        DownloadManager downloader = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadId != -1) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
            Cursor cur = downloader.query(query);
            if (cur != null) {
                if (cur.moveToFirst()) {
                    String uriString = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    if (!TextUtils.isEmpty(uriString)) {
                        targetApkFile = new File(Uri.parse(uriString).getPath());
                    }
                }
                cur.close();
            }
        }
        return targetApkFile;

    }

    private void openFile(File file, Context context) {
        Intent intent = new Intent();
        intent.addFlags(268435456);
        intent.setAction("android.intent.action.VIEW");
        String type = getMIMEType(file);
        intent.setDataAndType(Uri.fromFile(file), type);
        try {
            context.startActivity(intent);
        } catch (Exception var5) {
            var5.printStackTrace();
            Toast.makeText(context, "没有找到打开此类文件的程序", Toast.LENGTH_SHORT).show();
        }

    }

    private String getMIMEType(File var0) {
        String var1 = "";
        String var2 = var0.getName();
        String var3 = var2.substring(var2.lastIndexOf(".") + 1, var2.length()).toLowerCase();
        var1 = MimeTypeMap.getSingleton().getMimeTypeFromExtension(var3);
        return var1;
    }

}
