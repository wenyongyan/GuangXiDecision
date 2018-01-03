package com.cxwl.guangxi.utils;

import android.app.Dialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cxwl.guangxi.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 自动更新
 * @author shawn_sun
 *
 */

public class AutoUpdateUtil {
	
	private static Context mContext = null;
	private static String appName = null;
	private static boolean flag = true;
	
	/**
	 * 获取版本号
	 * @return 当前应用的版本号
	 */
	public static int getVersionCode(Context context) {
	    try {
	        PackageManager manager = context.getPackageManager();
	        PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
	        return info.versionCode;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return 0;
	    }
	}

	/**
	 * 检查更新
	 * @param context
	 * @param app_id
	 * @param is_flag true为主界面自己请求，false为个人点击获取
	 */
	public static void checkUpdate(Context context, String app_id, String app_name, boolean is_flag) {
		mContext = context;
		appName = app_name;
		flag = is_flag;
		if (TextUtils.isEmpty(app_id)) {
			Toast.makeText(context, "The app_id is empty", Toast.LENGTH_SHORT).show();
			return;
		}
		HttpAsyncTaskUpdate task = new HttpAsyncTaskUpdate(app_id);
		task.setMethod("POST");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute("https://app.tianqi.cn/update/check");
	}
	
	/**
	 * 异步请求方法
	 * @author dell
	 *
	 */
	private static class HttpAsyncTaskUpdate extends AsyncTask<String, Void, String> {
		private String method = "POST";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		private String app_id = null;
		
		public HttpAsyncTaskUpdate(String app_id) {
			this.app_id = app_id;
			transParams();
		}
		
		/**
		 * 传参数
		 */
		private void transParams() {
			NameValuePair pair1 = new BasicNameValuePair("app_id", app_id);
			nvpList.add(pair1);
		}

		@Override
		protected String doInBackground(String... url) {
			String result = null;
			if (method.equalsIgnoreCase("POST")) {
				result = CustomHttpClient.post(url[0], nvpList);
			} else if (method.equalsIgnoreCase("GET")) {
				result = CustomHttpClient.get(url[0]);
			}
			return result;
		}

		@Override
		protected void onPostExecute(String requestResult) {
			super.onPostExecute(requestResult);
			if (requestResult != null) {
				try {
					JSONObject obj = new JSONObject(requestResult);
					UpdateDto dto = new UpdateDto();
					if (!obj.isNull("version")) {
						dto.version = obj.getString("version");
					}
					if (!obj.isNull("update_info")) {
						dto.update_info = obj.getString("update_info");
					}
					if (!obj.isNull("dl_url")) {
						dto.dl_url = obj.getString("dl_url");
					}
					if (!obj.isNull("versionCode")) {
						dto.versionCode = obj.getInt("versionCode");
					}
					
					//检查版本不一样时候才更新
					if (dto.versionCode > getVersionCode(mContext)) {
						Message msg = new Message();
						msg.what = 1000;
						msg.obj = dto;
						handler.sendMessage(msg);
					}else {
						if (flag == false) {
							Toast.makeText(mContext, "已经是最新版本", Toast.LENGTH_SHORT).show();
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		@SuppressWarnings("unused")
		private void setParams(NameValuePair nvp) {
			nvpList.add(nvp);
		}

		private void setMethod(String method) {
			this.method = method;
		}

		private void setTimeOut(int timeOut) {
			CustomHttpClient.TIME_OUT = timeOut;
		}

		/**
		 * 取消当前task
		 */
		@SuppressWarnings("unused")
		private void cancelTask() {
			CustomHttpClient.shuttdownRequest();
			this.cancel(true);
		}
	}
	
	private static class UpdateDto {
		public String version = "";
		public String update_info = "";
		public String dl_url = "";
		public int versionCode = 0;
	}
	
	private static Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1000:
				UpdateDto dto = (UpdateDto) msg.obj;
				updateDialog(dto);
				break;

			default:
				break;
			}
		};
	};
	
	private static void updateDialog(final UpdateDto dto) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.auto_update_dialog, null);
		TextView tvTitle = (TextView) view.findViewById(R.id.tvTitle);
		TextView tvVersion = (TextView) view.findViewById(R.id.tvVersion);
		TextView tvContent = (TextView) view.findViewById(R.id.tvContent);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		tvTitle.setText("更新提醒");
		if (!TextUtils.isEmpty(dto.version)) {
			tvVersion.setText("更新版本："+dto.version);
		}
		if (!TextUtils.isEmpty(dto.update_info)) {
			tvContent.setText(dto.update_info);
		}
		llNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		
		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				if (!TextUtils.isEmpty(dto.dl_url)) {
					new Thread() {
						public void run() {
							intoDownloadManager(dto.dl_url);
						}
					}.start();
				}
			}
		});
	}
	
	private static void intoDownloadManager(String dl_url) {
		DownloadManager dManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
		Uri uri = Uri.parse(dl_url);
		Request request = new Request(uri);
		// 设置下载路径和文件名
		String filename = dl_url.substring(dl_url.lastIndexOf("/") + 1);//获取文件名称
		request.setDestinationInExternalPublicDir("download", filename);
		request.setDescription(appName);
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		request.setMimeType("application/vnd.android.package-archive");
		// 设置为可被媒体扫描器找到
		request.allowScanningByMediaScanner();
		// 设置为可见和可管理
		request.setVisibleInDownloadsUi(true);
		long refernece = dManager.enqueue(request);
//		// 把当前下载的ID保存起来
		SharedPreferences sPreferences = mContext.getSharedPreferences("downloadplato", 0);
		sPreferences.edit().putLong("plato", refernece).commit();

	} 

}
