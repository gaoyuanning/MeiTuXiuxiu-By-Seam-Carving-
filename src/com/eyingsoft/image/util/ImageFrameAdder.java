package com.eyingsoft.image.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import com.eyingsoft.image.R;
import com.eyingsoft.image.view.CropImageView;

public class ImageFrameAdder {
	public static final int FRAME_BIG = 0x1;
	public static final int FRAME_SMALL = FRAME_BIG + 1;
	private Context mContext;
	private CropImageView mImageView;

	// 边框1
	private final int[] mFrame1Res = new int[] {
			R.drawable.frame_around1_left_top, 
			R.drawable.frame_around1_left,
			R.drawable.frame_around1_left_bottom,
			R.drawable.frame_around1_bottom,
			R.drawable.frame_around1_right_bottom,
			R.drawable.frame_around1_right, 
			R.drawable.frame_around1_right_top,
			R.drawable.frame_around1_top };
	// 边框2
	private final int[] mFrame2Res = new int[] {
			R.drawable.frame_around2_left_top, 
			R.drawable.frame_around2_left,
			R.drawable.frame_around2_left_bottom,
			R.drawable.frame_around2_bottom,
			R.drawable.frame_around2_right_bottom,
			R.drawable.frame_around2_right, 
			R.drawable.frame_around2_right_top,
			R.drawable.frame_around2_top };

	private Bitmap mBitmap= null;

	public ImageFrameAdder(Context context, CropImageView view, Bitmap bm) {
		mContext = context;
		mImageView = view;
		mBitmap = bm;
	}
	
	public Bitmap getBitmap(){
		return mBitmap;
	}

	/**
	 * 添加边框
	 */
	public Bitmap addFrame(int flag, Bitmap bm, int res) {
		Bitmap bmp = null;
		switch (flag) {
		case FRAME_BIG:
			bmp = addBigFrame(bm, res);
			break;
		case FRAME_SMALL:
			bmp = addSmallFrame(bm, res);
			break;
		}
		return bmp;
	}

	/**
	 * 添加图片边框
	 */
	private Bitmap addBigFrame(Bitmap bm, int res) {
		Bitmap bitmap = decodeBitmap(res);
		Drawable[] array = new Drawable[2];
		array[0] = new BitmapDrawable(bm);
		Bitmap b = resize(bitmap, bm.getWidth(), bm.getHeight());
		array[1] = new BitmapDrawable(b);
		LayerDrawable layer = new LayerDrawable(array);
		return drawableToBitmap(layer);
	}
	
	/**
	 * 添加自定义边框
	 */
	private Bitmap addSmallFrame(Bitmap bm, int res) {
		Bitmap bmp = null;

		switch (res) // 目前定义两种边框
		{
		case 0:
			bmp = combinateFrame(bm, mFrame1Res);
			break;
		case 1:
			bmp = combinateFrame(bm, mFrame2Res);
			break;
		}

		return bmp;
	}

	/**
	 * 将Drawable转换成Bitmap
	 */
	private Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	/**
	 * 将R.drawable.*转换成Bitmap
	 */
	private Bitmap decodeBitmap(int res) {
		return BitmapFactory.decodeResource(mContext.getResources(), res);
	}

	/**
	 * 图片缩放
	 */
	public Bitmap resize(Bitmap bm, int w, int h) {
		Bitmap BitmapOrg = bm;

		int width = BitmapOrg.getWidth();
		int height = BitmapOrg.getHeight();
		int newWidth = w;
		int newHeight = h;

		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		// if you want to rotate the Bitmap
		// matrix.postRotate(45);

		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
				height, matrix, true);
		return resizedBitmap;
	}
	
	/**
	 * 往图片外部嵌入边框
	 */
	private Bitmap combinateFrame(Bitmap bm, int[] res) {
		Bitmap bmp = decodeBitmap(res[0]);
		// 边框的宽高
		final int smallW = bmp.getWidth();
		final int smallH = bmp.getHeight();
		// 原图片的宽高
		final int bigW = bm.getWidth();
		final int bigH = bm.getHeight();

		int wCount = (int) Math.ceil(bigW * 1.0 / smallW);
		int hCount = (int) Math.ceil(bigH * 1.0 / smallH);
		// 组合后图片的宽高
		int newW = (wCount + 2) * smallW;
		int newH = (hCount + 2) * smallH;
		// 重新定义大小
		Bitmap newBitmap = Bitmap.createBitmap(newW, newH, Config.ARGB_8888);
		Canvas canvas = new Canvas(newBitmap);
		Paint p = new Paint();
		p.setColor(Color.TRANSPARENT);
		canvas.drawRect(new Rect(0, 0, newW, newH), p);

		Rect rect = new Rect(smallW, smallH, newW - smallW, newH - smallH);
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		canvas.drawRect(rect, paint);

		// 绘原图
		canvas.drawBitmap(bm, (newW - bigW - 2 * smallW) / 2 + smallW, (newH
				- bigH - 2 * smallH)
				/ 2 + smallH, null);
		/****************** 绘边框*******************/
		// 绘四个角
		int startW = newW - smallW;
		int startH = newH - smallH;
		Bitmap leftTopBm = decodeBitmap(res[0]); // 左上角
		Bitmap leftBottomBm = decodeBitmap(res[2]); // 左下角
		Bitmap rightBottomBm = decodeBitmap(res[4]); // 右下角
		Bitmap rightTopBm = decodeBitmap(res[6]); // 右上角

		canvas.drawBitmap(leftTopBm, 0, 0, null);
		canvas.drawBitmap(leftBottomBm, 0, startH, null);
		canvas.drawBitmap(rightBottomBm, startW, startH, null);
		canvas.drawBitmap(rightTopBm, startW, 0, null);

		leftTopBm.recycle();
		leftTopBm = null;
		leftBottomBm.recycle();
		leftBottomBm = null;
		rightBottomBm.recycle();
		rightBottomBm = null;
		rightTopBm.recycle();
		rightTopBm = null;

		// 绘左右边框
		Bitmap leftBm = decodeBitmap(res[1]);
		Bitmap rightBm = decodeBitmap(res[5]);
		for (int i = 0, length = hCount; i < length; i++) {
			int h = smallH * (i + 1);
			canvas.drawBitmap(leftBm, 0, h, null);
			canvas.drawBitmap(rightBm, startW, h, null);
		}

		leftBm.recycle();
		leftBm = null;
		rightBm.recycle();
		rightBm = null;

		// 绘上下边框
		Bitmap bottomBm = decodeBitmap(res[3]);
		Bitmap topBm = decodeBitmap(res[7]);
		for (int i = 0, length = wCount; i < length; i++) {
			int w = smallW * (i + 1);
			canvas.drawBitmap(bottomBm, w, startH, null);
			canvas.drawBitmap(topBm, w, 0, null);
		}

		bottomBm.recycle();
		bottomBm = null;
		topBm.recycle();
		topBm = null;

		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();

		return newBitmap;
	}
	
	public void cancelCombinate() {
		mImageView.setState(CropImageView.STATE_NONE);
		mImageView.invalidate();
	}


}
