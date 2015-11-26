package com.eyingsoft.image;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class ChooseImageActivity extends Activity {

	private static final int FLAG_CHOOSE = 1; //标记选择 目的是 是否传送到gallery中

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose);
	}

	/*
	 * 请选择图片按钮的单击事件
	 * 跳转到系统内部的选择界面 图片选择界面、音乐选择界面、视频选择界面等。。
	 */
	public void onClick(View v) {    
		switch (v.getId()) {
		case R.id.choose_img://当点击按钮时的id ，在button的id
			Intent intent = new Intent();//创建Intent对象
			intent.setAction(Intent.ACTION_PICK);//设置Intent的Action - 调用系统的选择界面Gallery的Action  "Intent.ACTION_PICK"
			intent.setType("image/*");// 选择图片
			// intent.setType("audio/*"); //选择音频
			// intent.setType("video/*"); //选择视频 （mp4 3gp 是android支持的视频格式）
			// intent.setType("video/*;image/*");//同时选择视频和图片
			startActivityForResult(intent, FLAG_CHOOSE); //intent和flag_choose共同开始 startActivityForResult（）这这个方法的的目的是 进行其他按钮事件后 能够返回到当前的界面
			break;
		}
	}

	/*
	 * 获取图片的路径信息，将路径path传递到MainActivity
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && null != data) {
			switch (requestCode) {
			case FLAG_CHOOSE: 
				Uri uri = data.getData();
				
				Log.d("ChooseImageActivity - uri", "uri=" + uri
						+ ", authority=" + uri.getAuthority());
				
				if (!TextUtils.isEmpty(uri.getAuthority())) {

					Cursor cursor = getContentResolver().query(
							uri,
							new String[] { MediaStore.Images.Media.DATA },
							null, 
							null, 
							null);
					if (null == cursor) {
						Toast.makeText(this, R.string.no_found,
								Toast.LENGTH_SHORT).show();
						return;
					}
					cursor.moveToFirst();
					String path = cursor.getString(cursor
							.getColumnIndex(MediaStore.Images.Media.DATA));
					
					Log.d("ChooseImageActivity - Path :", "path=" + path);
					
					Intent intent = new Intent(this, MainActivity.class);
					intent.putExtra("path", path);
					startActivity(intent);
				} else {
					Log.d("ChooseImageActivity - Path :",
							"path=" + uri.getPath());
					Intent intent = new Intent(this, MainActivity.class);
					intent.putExtra("path", uri.getPath());
					startActivity(intent);
				}
				break;
			default:
				break;
			}
		}
	}
}
