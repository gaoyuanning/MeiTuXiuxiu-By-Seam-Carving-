package com.eyingsoft.image;

import java.util.LinkedList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.eyingsoft.image.util.EditImage;
import com.eyingsoft.image.util.ImageFrameAdder;
import com.eyingsoft.image.util.ReverseAnimation;
import com.eyingsoft.image.view.CropImageView;
import com.eyingsoft.image.view.ToneView;
import com.eyingsoft.image.view.menu.MenuView;
import com.eyingsoft.image.view.menu.OnMenuClickListener;
import com.eyingsoft.image.view.menu.SecondaryListMenuView;
import com.eyingsoft.image.view.menu.ToneMenuView;


public class MainActivity extends Activity implements OnSeekBarChangeListener {
	public boolean mWaitingToPick; 
	public boolean mSaving; 
	private Handler mHandler = null;
	private ProgressDialog mProgress;
	private Bitmap mBitmap;
	private Bitmap mTmpBmp;
	
	//用于seamcarving绘制图像
	SeamCarving sc;
	Paint paint;
	Canvas canvas;
	LinkedList<int[]> seams = new LinkedList<int[]>();
	private int SEAM_SUM = 1;
	boolean init = false;
	boolean initSeamSum = false;
	boolean hideLine = false;
	
	private CropImageView mImageView; 
	private EditImage mEditImage;    
	private ImageFrameAdder mImageFrame;

	private MenuView mMenuView; 
	//设置一级菜单下每个item的图片显示
	private final int[] EDIT_IMAGES = new int[] { 
			R.drawable.ic_menu_crop,//裁剪按钮
			R.drawable.ic_menu_rotate_left, //旋转按钮
			R.drawable.ic_menu_mapmode,//缩放按钮
			R.drawable.btn_rotate_horizontalrotate, //反转按钮
			R.drawable.btn_mainmenu_frame_normal,
			R.drawable.ic_menu_mapmode,//缩放按钮
			};
	//设置一级菜单下每个item的文字显示
	private final int[] EDIT_TEXTS = new int[] { 
			R.string.crop,
			R.string.rotate,
			R.string.resize, 
			R.string.reverse_transform,
			R.string.frame,
			R.string.SC
			};

	 //二级菜单
	private SecondaryListMenuView mSecondaryListMenu;
	//旋转图片设置
	private final int[] ROTATE_IMGRES = new int[] {
			R.drawable.ic_menu_rotate_left, //左旋转按钮
			R.drawable.ic_menu_rotate_right //右旋转按钮
			};
	//旋转文字设置
	private final int[] ROTATE_TEXTS = new int[] {
			R.string.rotate_left,
			R.string.rotate_right 
			};
	//缩放文字设置
	private final int[] RESIZE_TEXTS = new int[] {
			R.string.oneLine ,
			R.string.threeLine ,
			R.string.fiveLine,
			R.string.hideLine
			};
	//缩放图片设置
	private final int[] RESIZE_IMGRES = new int[] {
			R.drawable.ic_menu_mapmode,//缩放按钮 
			R.drawable.ic_menu_mapmode,
			R.drawable.ic_menu_mapmode,
			R.drawable.ic_menu_mapmode
			};
    //边框图片设置
	private final int[] FRAME_ADD_IMAGES = new int[] {
			R.drawable.frame_around1, 
			R.drawable.frame_around2,
			R.drawable.frame_small1 
			};
    //反转图片设置
	private final int[] FANZHUAN_TEXTS = new int[]{
			R.string.fanzhuan_left_right,
			R.string.fanzhuan_top_bottom 
	};
	//反转文字设置
	private final int[] FANZHUAN_IMAGES = new int[]{
			R.drawable.btn_rotate_horizontalrotate,
			R.drawable.btn_rotate_verticalrotate 
			};
	
	// 调色菜单
	private ToneMenuView mToneMenu;
	private ToneView mToneView;
	
	/** 调色 */
	private final int FLAG_TONE = 0x1;
	/** 添加边框 */
	private final int FLAG_FRAME_ADD = FLAG_TONE + 6;
	/** 编辑 */
	private final int FLAG_EDIT = FLAG_TONE + 2;
	/** 旋转 */
	private final int FLAG_EDIT_ROTATE = FLAG_TONE + 4;
	/** 缩放 */
	private final int FLAG_EDIT_RESIZE = FLAG_TONE + 5;
	/** 反转 */
	private final int FLAG_EDIT_REVERSE = FLAG_TONE + 8;
	//SC处理
	private final int FLAG_EDIT_SC = FLAG_TONE + 10;

	private View mSaveAll;//保存全部视图
	private View mSaveStep;//记录各个步骤的视图

	private final int STATE_CROP = 0x1;
	private final int STATE_NONE = STATE_CROP << 2;
	private final int STATE_TONE = STATE_CROP << 3;
	private final int STATE_REVERSE = STATE_CROP << 4;
	private int mState;
	//反转动画
	private ReverseAnimation mReverseAnim;
	private int mImageViewWidth;
	private int mImageViewHeight;
	private ProgressDialog mProgressDialog;
	private TextView mShowHandleName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				closeProgress();
				reset();
			}
		};
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.image_main);
		
		mSaveAll = findViewById(R.id.save_all);
		mSaveStep = findViewById(R.id.save_step);
		mShowHandleName = (TextView) findViewById(R.id.handle_name);

		Intent intent = getIntent();
		String path = intent.getStringExtra("path");
		Log.d("MainActivity", "path=" + path);
		if (null == path) {
			Toast.makeText(this, R.string.load_failure, Toast.LENGTH_SHORT)
					.show();
			finish();
		}
		mBitmap = BitmapFactory.decodeFile(path);
		mTmpBmp = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
		mImageView = (CropImageView) findViewById(R.id.crop_image);
		mImageView.setImageBitmap(mBitmap);
	    mImageView.setImageBitmapResetBase(mBitmap, true);//递归调用将图片的具体视图进行重置
		mEditImage = new EditImage(this, mImageView, mBitmap);//编辑图片
		mImageFrame = new ImageFrameAdder(this, mImageView, mBitmap);//图片边框设置 mBitmap原来的图片，mImageView渲染的图片
		mImageView.setEditImage(mEditImage);//当编辑渲染操作完成时，还能继续进行其他的功能渲染通过这个方法
	
		paint = new Paint();
		paint.setColor(Color.RED);
	}

	//-----------------菜单事件----------------
	public void onClick(View v) {
		int flag = -1;
		switch (v.getId()) {
		case R.id.save:
			String path = saveBitmap(mBitmap);//invoke saveBitmap();
			Log.v("savePath", path);
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			Intent data = new Intent();
			data.putExtra("path", path);
			setResult(RESULT_OK, data);
			finish();
			return;
		case R.id.cancel://取消
			setResult(RESULT_CANCELED);
			finish();
			return;
		case R.id.save_step://步骤保存
			if (mState == STATE_CROP) {
				mTmpBmp = mEditImage.cropAndSave(mTmpBmp);
			} else if (mState == STATE_TONE) {
				mTmpBmp = mToneView.getBitmap();
			}else if (mState == STATE_REVERSE) {
				mReverseAnim.cancel();
				mReverseAnim = null;
			}
			mBitmap = mTmpBmp;
			showSaveAll();
			reset();
			mEditImage.mSaving = true;
			mImageViewWidth = mImageView.getWidth();
			mImageViewHeight = mImageView.getHeight();
			return;
			
		case R.id.cancel_step://步骤取消
			if (mState == STATE_CROP) {
				mEditImage.cropCancel();
			}  else if (mState == STATE_REVERSE) {
				mReverseAnim.cancel();
			}
			showSaveAll();
			resetToOriginal();
			return;
		case R.id.edit://编辑
			flag = FLAG_EDIT;
			break;
		case R.id.tone://色调
			initTone();
			showSaveStep();
			return;
		}
		initMenu(flag);
	}
	
	// 调色功能初始化
	private void initTone() {
		if (null == mToneMenu) {
			mToneMenu = new ToneMenuView(this);
		}
		mToneMenu.show();
		mState = STATE_TONE;
		mToneView = mToneMenu.getToneView();
		mToneMenu.setHueBarListener(this);
		mToneMenu.setLumBarListener(this);
		mToneMenu.setSaturationBarListener(this);
	}

	//编辑按钮的一级菜单初始化
	private void initMenu(int flag) {
		if (null == mMenuView) {
			mMenuView = new MenuView(this);
			mMenuView.setBackgroundResource(R.drawable.popup);
			mMenuView.setTextSize(16);
			switch (flag) {
			case FLAG_EDIT:
				mMenuView.setImageRes(EDIT_IMAGES);
				mMenuView.setText(EDIT_TEXTS);
				mMenuView.setOnMenuClickListener(editListener);
				break;
			}
		}
		mMenuView.show();
	}
	//对一级菜单的各个按钮添加监听事件
	private OnMenuClickListener editListener = new OnMenuClickListener() {
		public void onMenuItemClick(AdapterView<?> parent, View view,
				int position) {
			int[] location = new int[2];
			view.getLocationInWindow(location);//记录点击的位置数
			int left = location[0];//二级菜单的位置
			int flag = -1;
			switch (position) {//返回所点击的数
			case 0: // 裁剪
				mMenuView.hide();//隐藏一级菜单
				crop();//进入剪切状态
				showSaveStep();//步骤保存
				return;
			case 1: // 旋转
				flag = FLAG_EDIT_ROTATE;
				break;
			case 2:// 缩放
				flag = FLAG_EDIT_RESIZE;
				break;
			case 3: // 反转
				flag = FLAG_EDIT_REVERSE;
				break;
			case 4://边框
				flag = FLAG_FRAME_ADD;
				break;
			case 5://SC处理
				flag = FLAG_EDIT_SC;
				break;
			}
			initSecondaryMenu(flag, left);
		}
		
		@Override
		public void hideMenu() {
			dimissMenu();
		}
	};

	//菜单消失处理
	private void dimissMenu() {
		mMenuView.dismiss();
		mMenuView = null;
	}

	//初始化二级菜单
	private void initSecondaryMenu(int flag, int left) {
		mSecondaryListMenu = new SecondaryListMenuView(this);
		mSecondaryListMenu.setBackgroundResource(R.drawable.popup_bottom_tip);
		mSecondaryListMenu.setTextSize(16);
		mSecondaryListMenu.setWidth(240);
		//mSecondaryListMenu.setHeight(240);
		mSecondaryListMenu.setMargin(left);
		switch (flag) {
		case FLAG_EDIT_ROTATE: // 旋转
			mSecondaryListMenu.setImageRes(ROTATE_IMGRES);
			mSecondaryListMenu.setText(ROTATE_TEXTS);
			mSecondaryListMenu.setOnMenuClickListener(rotateListener());
			break;
		case FLAG_EDIT_RESIZE: // 缩放
				mSecondaryListMenu.setImageRes(RESIZE_IMGRES);
				mSecondaryListMenu.setText(RESIZE_TEXTS);
				mSecondaryListMenu.setOnMenuClickListener(resizeListener());
				initSeamSum = true;
			break;
		case FLAG_EDIT_REVERSE: // 反转
			mSecondaryListMenu.setImageRes(FANZHUAN_IMAGES);
			mSecondaryListMenu.setText(FANZHUAN_TEXTS);
			mSecondaryListMenu.setOnMenuClickListener(reverseListener());
			break;
		case FLAG_FRAME_ADD: // 添加边框
			mSecondaryListMenu.setImageRes(FRAME_ADD_IMAGES);
			mSecondaryListMenu.setOnMenuClickListener(addFrameListener());
			break;
		case FLAG_EDIT_SC:
			if(initSeamSum) {
				resize();
			}
			break;
		}
		mSecondaryListMenu.show();
	}

	//旋转事件监听
	private OnMenuClickListener rotateListener() {
		return new OnMenuClickListener() {
			@Override
			public void onMenuItemClick(AdapterView<?> parent, View view,
					int position) {
				switch (position) {
				case 0: // 左旋转
					rotate(-90);
					break;
				case 1: // 右旋转
					rotate(90);
					break;
				}
				// 一级菜单隐藏
				mMenuView.hide();
				showSaveStep();
			}

			@Override
			public void hideMenu() {
				dismissSecondaryMenu();
			}

		};
	}
	//图片缩放事件监听
	private OnMenuClickListener resizeListener() {
		return new OnMenuClickListener() {
			@Override
			public void onMenuItemClick(AdapterView<?> parent, View view,
					int position) {
				float scale = 1.0F;
				switch (position) {
				case 0: // 1:2
					//scale /= 2;
					SEAM_SUM = 1;
					initSeamSum = true;
					break;
				case 1: // 1:3
					//scale /= 4;
					SEAM_SUM = 3;
					initSeamSum = true;
					break;
				case 2: // 1:4
					//scale *= 2;
					SEAM_SUM = 5;
					initSeamSum = true;
					break;
				case 3: // 1:4
					//scale *= 4;
					hideLine = hideLine ? false : true;
					break;
				}

				//resize(scale);
				//mMenuView.hide();
				//showSaveStep();
			}

			@Override
			public void hideMenu() {
				dismissSecondaryMenu();
			}

		};
	}
	//图片反转事件监听
	private OnMenuClickListener reverseListener() {
		return new OnMenuClickListener() {
			@Override
			public void onMenuItemClick(AdapterView<?> parent, View view,
					int position) {
				int flag = -1;
				switch (position) {
				case 0: // 水平反转
					flag = 0;
					break;
				case 1: // 垂直反转
					flag = 1;
					break;
				}
				reverse(flag);
				// 一级菜单隐藏
				mMenuView.hide();
				showSaveStep();
			}

			@Override
			public void hideMenu() {
				dismissSecondaryMenu();
			}

		};
	}
	//添加边框事件监听
	private OnMenuClickListener addFrameListener() {
		return new OnMenuClickListener() {
			@Override
			public void onMenuItemClick(AdapterView<?> parent, View view,
					int position) {
				int flag = -1;
				int res = 0;
				switch (position) {
				case 0: // 边框1
					flag = ImageFrameAdder.FRAME_SMALL;
					res = 0;
					break;
				case 1: // 边框2
					flag = ImageFrameAdder.FRAME_SMALL;
					res = 1;
					break;
				case 2: // 边框3
					flag = ImageFrameAdder.FRAME_BIG;
					res = R.drawable.frame_big1;
					break;
				case 3: // 边框4
					flag = ImageFrameAdder.FRAME_BIG;
					res = 2;
					break;
				}

				addFrame(flag, res);
				// mImageView.center(true, true);

				// 一级菜单隐藏
				mMenuView.hide();
				showSaveStep();
			}

			@Override
			public void hideMenu() {
				dismissSecondaryMenu();
			}

		};
	}
	
	//隐藏二级菜单
	private void dismissSecondaryMenu() {
		mSecondaryListMenu.dismiss();
		mSecondaryListMenu = null;
	}

	//步骤操作方法（保存与取消）
	private void showSaveStep() {
		mSaveStep.setVisibility(View.VISIBLE);
		mSaveAll.setVisibility(View.GONE);
	}

	private void showSaveAll() {
		mSaveStep.setVisibility(View.GONE);
		mSaveAll.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (mMenuView != null && mMenuView.isShow() || null != mToneMenu
					&& mToneMenu.isShow()) {
				mMenuView.hide();
				mToneMenu.hide();
				mToneMenu = null;
			} else {
				if (mSaveAll.getVisibility() == View.GONE) {
					showSaveAll();
				} else {
					finish();
				}
			}
			break;
		case KeyEvent.KEYCODE_MENU:
			break;

		}
		return super.onKeyDown(keyCode, event);
	}

	// --------------功能---------------
	/**
	 * 进行操作前的准备
	 * 
	 * @param state
	 *            当前准备进入的操作状态
	 * @param imageViewState
	 *            ImageView要进入的状态
	 * @param hideHighlight
	 *            是否隐藏裁剪框
	 */
	private void prepare(int state, int imageViewState, boolean hideHighlight) {
		resetToOriginal();
		mEditImage.mSaving = false;
		if (null != mReverseAnim) {
			mReverseAnim.cancel();
			mReverseAnim = null;
		}

		if (hideHighlight) {
			mImageView.hideHighlightView();
		}
		mState = state;
		mImageView.setState(imageViewState);
		mImageView.invalidate();
	}

	//裁剪
	private void crop() {
		// 进入裁剪状态
		prepare(STATE_CROP, CropImageView.STATE_HIGHLIGHT, false);
		mShowHandleName.setText(R.string.crop);
		mEditImage.crop(mTmpBmp);
		reset();
	}

	//旋转
	private void rotate(float degree) {
		// 未进入特殊状态
		mImageViewWidth = mImageView.getWidth();
		mImageViewHeight = mImageView.getHeight();
		prepare(STATE_NONE, CropImageView.STATE_NONE, true);
		mShowHandleName.setText(R.string.rotate);
		Bitmap bm = mEditImage.rotate(mTmpBmp, degree);
		mTmpBmp = bm;
		reset();
	}

	//反转
	private void reverse(int flag) {
		// 未进入特殊状态
		prepare(STATE_REVERSE, CropImageView.STATE_NONE, true);
		mShowHandleName.setText(R.string.reverse_transform);
		int type = 0;
		switch (flag) {
		case 0:
			type = ReverseAnimation.HORIZONTAL;
			break;
		case 1:
			type = ReverseAnimation.VERTICAL;
			break;
		}

		mReverseAnim = new ReverseAnimation(0F, 180F,
				mImageViewWidth == 0 ? mImageView.getWidth() / 2
						: mImageViewWidth / 2,
				mImageViewHeight == 0 ? mImageView.getHeight() / 2
						: mImageViewHeight / 2, 0, true);
		mReverseAnim.setReverseType(type);
		mReverseAnim.setDuration(1000);
		mReverseAnim.setFillEnabled(true);
		mReverseAnim.setFillAfter(true);
		mImageView.startAnimation(mReverseAnim);
		Bitmap bm = mEditImage.reverse(mTmpBmp, flag);
		mTmpBmp = bm;
		//reset();
	}

    //缩放
	private void resize() {
		// 未进入特殊状态
		prepare(STATE_NONE, CropImageView.STATE_NONE, true);
		if(!init) {
			sc = new SeamCarving(mTmpBmp);
			init = true;
		}
		
		Bitmap bufferBitmap = sc.getImage();
		//Bitmap bufferBitmap = Bitmap.createBitmap(img.getWidth(),
			//	img.getHeight(), Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bufferBitmap);
		//canvas.drawBitmap(img, 0, 0, paint);

		for (int j = 0; j < SEAM_SUM; j++) {
			seams.add(sc.findAndRemoveSeam());
		}
		
		if (!hideLine) {
			int height = bufferBitmap.getHeight();
			for (int[] seam : seams) {
				for (int y = 0; y < height; y++) {
					int x = seam[y];
					canvas.drawCircle(x, y, 1, paint);
				}
			}
		}
	
		mTmpBmp = bufferBitmap;
		seams.clear();
		//mShowHandleName.setText(R.string.resize);
		//Bitmap bmp = mEditImage.resize(mTmpBmp, scale);
		//mTmpBmp = bmp;
		reset();
	}

	//添加边框
	private void addFrame(int flag, int res) {
		// 未进入特殊状态
		prepare(STATE_NONE, CropImageView.STATE_NONE, true);
		mShowHandleName.setText(R.string.frame);
		mTmpBmp = mImageFrame.addFrame(flag, mBitmap, res);
		reset();
	}
	
	//重新设置一下图片
	private void reset() {
		mImageView.setImageBitmap(mTmpBmp);
		mImageView.invalidate();
	}

	private void resetToOriginal() {
		mTmpBmp = mBitmap;
		mImageView.setImageBitmap(mBitmap);
		// 已经保存图片
		mEditImage.mSaving = true;
		// 清空裁剪操作
		mImageView.mHighlightViews.clear();
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		int flag = -1;
		switch ((Integer) seekBar.getTag()) {
		case 1: // 饱和度
			flag = 1;
			mToneView.setSaturation(progress);
			break;
		case 2: // 色调
			flag = 0;
			mToneView.setHue(progress);
			break;
		case 3: // 亮度
			flag = 2;
			mToneView.setLum(progress);
			break;
		}

		Bitmap bm = mToneView.handleImage(mTmpBmp, flag);
		mImageView.setImageBitmapResetBase(bm, true);
		mImageView.center(true, true);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}
	/**
	 * 显示进度条
	 */
	private void showProgress() {
		Context context = this;
		mProgress = ProgressDialog.show(context, null, context.getResources()
				.getString(R.string.handling));
		mProgress.show();
		Log.d("may", "show Progress");
	}

	/**
	 * 关闭进度条
	 */
	private void closeProgress() {
		if (null != mProgress) {
			mProgress.dismiss();
			mProgress = null;
		}
	}
	/**
	 * 保存图片到本地同时 然后进行保存图片的操作 图片进行等待画面
	 */
	private String saveBitmap(Bitmap bm) {
		mProgressDialog = ProgressDialog.show(this, null, getResources()
				.getString(R.string.save_bitmap));
		mProgressDialog.show();
		return mEditImage.saveToLocal(bm);//saveToLocal()这个方法是输入年月日的基本操作
	}

}
