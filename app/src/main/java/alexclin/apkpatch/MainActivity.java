package alexclin.apkpatch;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dodola.patcher.utils.ApkInfoTool;
import com.dodola.patcher.utils.AppUtils;

import java.io.File;

/**
 * 增量升级Demo主逻辑文件(客户端)
 * 
 */
public class MainActivity extends Activity implements OnClickListener {
	private static final String tag = "MainActivity";

	private String rootPath;

	private Button btn;
	private EditText mTxtOld;
	private EditText mTxtPatcher;
	private EditText mTxtNew;

	private String urlPath;

	static {
		// 调用.so文件,引入打包库
		System.loadLibrary("Patcher");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		try {
			super.onActivityResult(requestCode, resultCode, data);
			switch (requestCode) {
			case 0:
				mTxtOld.setText(getRealFilePath(this,data.getData()));
				break;

			case 1:
				mTxtPatcher.setText(getRealFilePath(this,data.getData()));
				break;

			}
		} catch (Exception ex) {

		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		setContentView(R.layout.activity_main);
		initView();
		setListenet();
	}

	/**
	 * 初始化控件
	 */
	private void initView() {
		btn = (Button) this.findViewById(R.id.button1);
		mTxtOld = (EditText) this.findViewById(R.id.text_old);
		mTxtNew = (EditText) this.findViewById(R.id.text_new);
		mTxtPatcher = (EditText) this.findViewById(R.id.text_patcher);

		Log.i(tag, String.valueOf(ApkInfoTool.getVersionCode(this)));
		Log.i(tag, ApkInfoTool.getVersionName(this));

	}

	/**
	 * 设置监听
	 */
	private void setListenet() {
		btn.setOnClickListener(this);
		mTxtOld.setOnClickListener(this);
		mTxtPatcher.setOnClickListener(this);

	}

	/**
	 * 所有的点击事件
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button1:
			if (isFileEmpty()) {
				exAsyncTask();
			}
			break;
		case R.id.text_old:
			selectFile(0);
			break;
		case R.id.text_patcher:
			selectFile(1);
			break;
		default:
			break;
		}
	}

	/**
	 * 选择文件
	 * 
	 * @param n
	 *            返回的标记
	 */
	private void selectFile(int n) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(intent, n);
	}

	/**
	 * 判断选择的3个文件如果不为空返回true
	 * 
	 * @return
	 */
	private boolean isFileEmpty() {
		if (TextUtils.isEmpty(mTxtOld.getText().toString())) {
			Toast.makeText(MainActivity.this, "请选择旧版本文件", Toast.LENGTH_SHORT)
					.show();
			return false;
		}

		if (TextUtils.isEmpty(mTxtNew.getText().toString())) {
			Toast.makeText(MainActivity.this, "请输入合并后新版本文件名",
					Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(mTxtPatcher.getText().toString())) {
			Toast.makeText(MainActivity.this, "请选择补丁文件", Toast.LENGTH_SHORT)
					.show();
			return false;
		}
		return true;
	}

	/**
	 * 在子线程中完成apk合并,并在主线程安装apk
	 */
	private void exAsyncTask() {
		AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {

			private ProgressDialog progressDialog;
			private String oldFile;
			private String newFile;
			private String pacth;

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				newFile = mTxtNew.getText().toString();
				oldFile = mTxtOld.getText().toString();
				pacth = mTxtPatcher.getText().toString();
				progressDialog = ProgressDialog.show(MainActivity.this,
						"正在生成APK...", "请稍等...", true, false);
				progressDialog.show();
			}

			@Override
			protected Integer doInBackground(Void... arg0) {
				String newApk = rootPath + File.separator + newFile + ".apk";
				File file = new File(newApk);
				if (file.exists())
					file.delete();// 如果newApk文件已经存在,先删除

				// 调用.so库中的方法,把增量包和老的apk包合并成新的apk

				return AppUtils.patcher(oldFile, newApk, pacth);
			}

			@Override
			protected void onPostExecute(Integer result) {
				progressDialog.dismiss();
				if(result==0){
					Toast.makeText(MainActivity.this, "打包完成，安装。。。。", Toast.LENGTH_SHORT).show();
					String newApk = rootPath + File.separator + mTxtNew.getText().toString() + ".apk";
					installApk(newApk);
				}else{
					Toast.makeText(MainActivity.this, "合并apk补丁失败，请查看日志定位错误", Toast.LENGTH_SHORT).show();
				}
			}
		};
		task.execute();
	}

	/**
	 * 安装指定地址(filePath)的apk
	 */
	private void installApk(String filePath) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + filePath), "application/vnd.android.package-archive");
		startActivity(i);
	}

	/**
	 * Try to return the absolute file path from the given Uri
	 *
	 * @param context
	 * @param uri
	 * @return the file path or null
	 */
	public static String getRealFilePath(final Context context, final Uri uri ) {
		if ( null == uri ) return null;
		final String scheme = uri.getScheme();
		String data = null;
		if ( scheme == null )
			data = uri.getPath();
		else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
			data = uri.getPath();
		} else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
			Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
			if ( null != cursor ) {
				if ( cursor.moveToFirst() ) {
					int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
					if ( index > -1 ) {
						data = cursor.getString( index );
					}
				}
				cursor.close();
			}
		}
		return data;
	}
}
