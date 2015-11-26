package com.eyingsoft.image.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.eyingsoft.image.R;
/**
 * 设置调色Menu视图
 * @author eying
 *
 */
public class ToneView
{
	/**
	 * 饱和度
	 */
	private TextView mSaturation;
	private SeekBar mSaturationBar;
	
	/**
	 * 色调
	 */
	private TextView mHue;
	private SeekBar mHueBar;
	
	/**
	 * 亮度
	 */
	private TextView mLum;
	private SeekBar mLumBar;
	
	private float mDensity;
	private static final int TEXT_WIDTH = 50;
	
	private LinearLayout mParent;
	
	private ColorMatrix mLightnessMatrix;
	private ColorMatrix mSaturationMatrix;
	private ColorMatrix mHueMatrix;
	private ColorMatrix mAllMatrix;
	
	/**
	 * 亮度
	 */
	private float mLightnessValue = 1F;
	
	/**
	 * 饱和度
	 */
	private float mSaturationValue = 0F;
	
	/**
	 * 色相
	 */
	private float mHueValue = 0F;
	private final int MIDDLE_VALUE = 127;
	
	/**
	 * 处理后的图片
	 */
	private Bitmap mBitmap;
	
	public ToneView(Context context)
	{
		init(context);
	}
	
	private void init(Context context)
	{
		mDensity = context.getResources().getDisplayMetrics().density;
		
		mSaturation = new TextView(context);
		mSaturation.setText(R.string.saturation);
		mHue = new TextView(context);
		mHue.setText(R.string.contrast);
		mLum = new TextView(context);
		mLum.setText(R.string.lightness);
		
		mSaturationBar = new SeekBar(context);
		mSaturationBar.setMax(255);
		mSaturationBar.setProgress(127);
		mSaturationBar.setTag(1);
		
		mHueBar = new SeekBar(context);
		mHueBar.setMax(255);
		mHueBar.setProgress(127);
		mHueBar.setTag(2);
		
		mLumBar = new SeekBar(context);
		mLumBar.setMax(255);
		mLumBar.setProgress(127);
		mLumBar.setTag(3);
		
		LinearLayout saturation = new LinearLayout(context);
		saturation.setOrientation(LinearLayout.HORIZONTAL);
		saturation.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		
		LinearLayout.LayoutParams txtLayoutparams = new LinearLayout.LayoutParams((int) (TEXT_WIDTH * mDensity), LinearLayout.LayoutParams.MATCH_PARENT);
		mSaturation.setGravity(Gravity.CENTER);
		saturation.addView(mSaturation, txtLayoutparams);
		
		LinearLayout.LayoutParams seekLayoutparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		saturation.addView(mSaturationBar, seekLayoutparams);
		
		
		LinearLayout hue = new LinearLayout(context);
		hue.setOrientation(LinearLayout.HORIZONTAL);
		hue.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		
		mHue.setGravity(Gravity.CENTER);
		hue.addView(mHue, txtLayoutparams);
		
		hue.addView(mHueBar, seekLayoutparams);
		
		
		LinearLayout lum = new LinearLayout(context);
		lum.setOrientation(LinearLayout.HORIZONTAL);
		lum.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		
		mLum.setGravity(Gravity.CENTER);
		lum.addView(mLum, txtLayoutparams);
		lum.addView(mLumBar, seekLayoutparams);
		
		mParent = new LinearLayout(context);
		mParent.setOrientation(LinearLayout.VERTICAL);
		mParent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		mParent.addView(saturation);
		mParent.addView(hue);
		mParent.addView(lum);
	}
	
	public View getParentView()
	{
		return mParent;
	}
	
	public void setSaturationBarListener(OnSeekBarChangeListener l)
	{
		mSaturationBar.setOnSeekBarChangeListener(l);
	}
	
	public void setHueBarListener(OnSeekBarChangeListener l)
	{
		mHueBar.setOnSeekBarChangeListener(l);
	}
	
	public void setLumBarListener(OnSeekBarChangeListener l)
	{
		mLumBar.setOnSeekBarChangeListener(l);
	}
	
	public void setSaturation(int saturation)
	{
		mSaturationValue = (float) (saturation * 1.0D / MIDDLE_VALUE);
	}
	
	public void setHue(int hue)
	{
		mHueValue = (float) (hue * 1.0D / MIDDLE_VALUE);
	}
	
	public void setLum(int lum)
	{
		mLightnessValue = (float) ((lum - MIDDLE_VALUE) * 1.0D / MIDDLE_VALUE * 180);
	}
	
	/**
	 * 返回处理后的图片
	 * @return
	 */
	public Bitmap getBitmap()
	{
		return mBitmap;
	}
	
	/**
	 * 
	 * @param flag
	 *            比特位0 表示是否改变色相，比位1表示是否改变饱和度,比特位2表示是否改变明亮度
	 */
	public Bitmap handleImage(Bitmap bm, int flag)
	{
		Bitmap bmp = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
		// 创建一个相同尺寸的可变的位图区,用于绘制调色后的图片
		Canvas canvas = new Canvas(bmp); // 得到画笔对象
		Paint paint = new Paint(); // 新建paint
		paint.setAntiAlias(true); // 设置抗锯齿,也即是边缘做平滑处理
		if (null == mAllMatrix)
		{
			mAllMatrix = new ColorMatrix();
		}
		
		if (null == mLightnessMatrix)
		{
			mLightnessMatrix = new ColorMatrix(); // 用于颜色变换的矩阵，android位图颜色变化处理主要是靠该对象完成
		}
		
		if (null == mSaturationMatrix)
		{
			mSaturationMatrix = new ColorMatrix();
		}
		
		if (null == mHueMatrix)
		{
			mHueMatrix = new ColorMatrix();
		}

		switch (flag)
		{
		case 0: // 需要改变色相
			// f 表示亮度比例，取值小于1，表示亮度减弱，否则亮度增强
			mHueMatrix.reset();
			mHueMatrix.setScale(mHueValue, mHueValue, mHueValue, 1); // 红、绿、蓝三分量按相同的比例,最后一个参数1表示透明度不做变化，此函数详细说明参考
			// // android
			// doc
			Log.d("may", "改变色相");
			break;
		case 1: // 需要改变饱和度
			// saturation 饱和度值，最小可设为0，此时对应的是灰度图(也就是俗话的“黑白图”)，
			// 为1表示饱和度不变，设置大于1，就显示过饱和
			mSaturationMatrix.reset();
			mSaturationMatrix.setSaturation(mSaturationValue);
			Log.d("may", "改变饱和度");
			break;
		case 2: // 亮度
			// hueColor就是色轮旋转的角度,正值表示顺时针旋转，负值表示逆时针旋转
			mLightnessMatrix.reset(); // 设为默认值
			mLightnessMatrix.setRotate(0, mLightnessValue); // 控制让红色区在色轮上旋转hueColor葛角度
			mLightnessMatrix.setRotate(1, mLightnessValue); // 控制让绿红色区在色轮上旋转hueColor葛角度
			mLightnessMatrix.setRotate(2, mLightnessValue); // 控制让蓝色区在色轮上旋转hueColor葛角度
			// 这里相当于改变的是全图的色相
			Log.d("may", "改变亮度");
			break;
		}
		mAllMatrix.reset();
		mAllMatrix.postConcat(mHueMatrix);
		mAllMatrix.postConcat(mSaturationMatrix); // 效果叠加
		mAllMatrix.postConcat(mLightnessMatrix); // 效果叠加

		paint.setColorFilter(new ColorMatrixColorFilter(mAllMatrix));// 设置颜色变换效果
		canvas.drawBitmap(bm, 0, 0, paint); // 将颜色变化后的图片输出到新创建的位图区
		// 返回新的位图，也即调色处理后的图片
		mBitmap = bmp;
		return bmp;
	}
	
}
