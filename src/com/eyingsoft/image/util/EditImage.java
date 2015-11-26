package com.eyingsoft.image.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.FaceDetector;
import android.os.Handler;
import android.widget.Toast;

import com.eyingsoft.image.R;
import com.eyingsoft.image.view.CropImageView;
import com.eyingsoft.image.view.HighlightView;

public class EditImage
{
	public boolean mWaitingToPick;
    public boolean mSaving;
    public HighlightView mCrop;
    
	private Context mContext;
	private Handler mHandler = new Handler();
	private CropImageView mImageView;
	private Bitmap mBitmap;
	
	/**
	 * Constructor method 构造方法
	 * @param context
	 * @param imageView
	 * @param bm
	 */
	public EditImage(Context context, CropImageView imageView, Bitmap bm)
	{
		mContext = context;
		mImageView = imageView;
		mBitmap = bm;
	}
	
	/**
	 * 图片裁剪
	 */
	public void crop(Bitmap bm)
	{
		mBitmap = bm;
		startFaceDetection();
	}
	
	/**
	 * 图片旋转
	 * @param degree
	 */
	public Bitmap rotate(Bitmap bmp, float degree)
    {
    	Matrix matrix = new Matrix();
		matrix.postRotate(degree);
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bm = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
		return bm;
    }
	
	/**
	 * 图片反转
	 */
	public Bitmap reverse(Bitmap bmp, int flag)
	{
		float[] floats = null;
		switch (flag)
		{
		case 0: // 水平反转
			floats = new float[] { -1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f };
			break;
		case 1: // 垂直反转
			floats = new float[] { 1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 1f };
			break;
		}
		if (floats != null)
		{
			Matrix matrix = new Matrix();
			matrix.setValues(floats);//通过使用Matrix进行图片的反转
			Bitmap bm = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
			return bm;
		}
		return null;
	}
    
	/**
	 * 图片缩放
	 */
	public Bitmap resize(Bitmap bm, float scale)
	{
		Bitmap BitmapOrg = bm;
		
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, BitmapOrg.getWidth(), BitmapOrg.getHeight(), matrix, true);
		return resizedBitmap;
	}
	/**
	 * 图片缩放
	 */
    public Bitmap resize(Bitmap bm, int w, int h)
    {
    	Bitmap BitmapOrg = bm;
		int width = BitmapOrg.getWidth();
		int height = BitmapOrg.getHeight();
		int newWidth = w;
		int newHeight = h;
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width, height, matrix, true);
		return resizedBitmap;
    }
    
	private void startFaceDetection() {
        if (((Activity)mContext).isFinishing()) {
            return;
        }

        showProgressDialog(mContext.getResources().getString(R.string.running_face_detection), new Runnable() {
            public void run() {
                final CountDownLatch latch = new CountDownLatch(1);
                final Bitmap b = mBitmap;
                mHandler.post(new Runnable() {
                    public void run() {
                        if (b != mBitmap && b != null) {
                            mImageView.setImageBitmapResetBase(b, true);
                            mBitmap.recycle();
                            mBitmap = b;
                        }
                        if (mImageView.getScale() == 1.0f) {
                            mImageView.center(true, true);
                        }
                        latch.countDown();
                    }
                });
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                mRunFaceDetection.run();
            }
        }, mHandler);
    }

	/**
	 * 裁剪并保存
	 */
	public Bitmap cropAndSave(Bitmap bm)
	{
		final Bitmap bmp = onSaveClicked(bm);
		mImageView.setState(CropImageView.STATE_NONE);
		mImageView.mHighlightViews.clear();
		return bmp;
	}
	
	/**
	 * 取消裁剪
	 */
	public void cropCancel()
	{
		mImageView.setState(CropImageView.STATE_NONE);
		mImageView.invalidate();
	}
	
    private Bitmap onSaveClicked(Bitmap bm) {
        if (mSaving)
            return bm;
        if (mCrop == null) {
            return bm;
        }
        mSaving = true;
        Rect r = mCrop.getCropRect();
        int width = r.width(); 
        int height = r.height();
        Bitmap croppedImage = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        {
            Canvas canvas = new Canvas(croppedImage);
            Rect dstRect = new Rect(0, 0, width, height);
            canvas.drawBitmap(bm, r, dstRect, null);
        }
        return croppedImage;
    }
    
    //将处理过的图片保存到本地
    public String saveToLocal(Bitmap bm)
    {
    	Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
		String picName = sdf.format(date);
    	String path = "/sdcard/"+picName+".jpg";
    	try
		{
			FileOutputStream fos = new FileOutputStream(path);
			bm.compress(CompressFormat.JPEG, 75, fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		} catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
		
		return path;//返回给 path
    }
    
    private void showProgressDialog(String msg, Runnable job, Handler handler)
    {
    	final ProgressDialog progress = ProgressDialog.show(mContext, null, msg);
    	new Thread(new BackgroundJob(progress, job, handler)).start();
    }
    
    Runnable mRunFaceDetection = new Runnable() {
        float mScale = 1F;
        Matrix mImageMatrix;
        FaceDetector.Face[] mFaces = new FaceDetector.Face[3];
        int mNumFaces;

        private void handleFace(FaceDetector.Face f) {
            PointF midPoint = new PointF();

            int r = ((int) (f.eyesDistance() * mScale)) * 2;
            f.getMidPoint(midPoint);
            midPoint.x *= mScale;
            midPoint.y *= mScale;

            int midX = (int) midPoint.x;
            int midY = (int) midPoint.y;

            HighlightView hv = new HighlightView(mImageView);

            int width = mBitmap.getWidth();
            int height = mBitmap.getHeight();

            Rect imageRect = new Rect(0, 0, width, height);

            RectF faceRect = new RectF(midX, midY, midX, midY);
            faceRect.inset(-r, -r);
            if (faceRect.left < 0) {
                faceRect.inset(-faceRect.left, -faceRect.left);
            }

            if (faceRect.top < 0) {
                faceRect.inset(-faceRect.top, -faceRect.top);
            }

            if (faceRect.right > imageRect.right) {
                faceRect.inset(faceRect.right - imageRect.right, faceRect.right - imageRect.right);
            }

            if (faceRect.bottom > imageRect.bottom) {
                faceRect.inset(faceRect.bottom - imageRect.bottom, faceRect.bottom - imageRect.bottom);
            }

            hv.setup(mImageMatrix, imageRect, faceRect, false, false);

            mImageView.add(hv);
        }

        private void makeDefault() {
            HighlightView hv = new HighlightView(mImageView);

            int width = mBitmap.getWidth();
            int height = mBitmap.getHeight();

            Rect imageRect = new Rect(0, 0, width, height);

            int cropWidth = Math.min(width, height) * 4 / 5;
            int cropHeight = cropWidth;

            int x = (width - cropWidth) / 2;
            int y = (height - cropHeight) / 2;

            RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);
            hv.setup(mImageMatrix, imageRect, cropRect, false, false);
            mImageView.add(hv);
        }

        private Bitmap prepareBitmap() {
            if (mBitmap == null) {
                return null;
            }

            if (mBitmap.getWidth() > 256) {
                mScale = 256.0F / mBitmap.getWidth(); 
            }
            Matrix matrix = new Matrix();
            matrix.setScale(mScale, mScale);
            Bitmap faceBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
            return faceBitmap;
        }

        public void run() {
            mImageMatrix = mImageView.getImageMatrix();
            Bitmap faceBitmap = prepareBitmap();

            mScale = 1.0F / mScale;
            if (faceBitmap != null) {
                FaceDetector detector = new FaceDetector(faceBitmap.getWidth(), faceBitmap.getHeight(), mFaces.length);
                mNumFaces = detector.findFaces(faceBitmap, mFaces);
            }

            if (faceBitmap != null && faceBitmap != mBitmap) {
                faceBitmap.recycle();
            }

            mHandler.post(new Runnable() {
                public void run() {
                    mWaitingToPick = mNumFaces > 1;
                    if (mNumFaces > 0) {
                        for (int i = 0; i < mNumFaces; i++) {
                            handleFace(mFaces[i]);
                        }
                    } else {
                        makeDefault();
                    }
                    mImageView.invalidate();
                    if (mImageView.mHighlightViews.size() == 1) {
                        mCrop = mImageView.mHighlightViews.get(0);
                        mCrop.setFocus(true);
                    }

                    if (mNumFaces > 1) {
                        Toast t = Toast.makeText(mContext, R.string.multiface_crop_help, Toast.LENGTH_SHORT);
                        t.show();
                    }
                }
            });
        }
    };
	
	class BackgroundJob implements Runnable
    {
    	private ProgressDialog mProgress;
    	private Runnable mJob;
    	private Handler mHandler;
    	public BackgroundJob(ProgressDialog progress, Runnable job, Handler handler)
    	{
    		mProgress = progress;
    		mJob = job;
    		mHandler = handler;
    	}
    	
    	public void run()
    	{
    		try 
    		{
    			mJob.run();
    		}
    		finally
    		{
    			mHandler.post(new Runnable()
    			{
    				public void run()
    				{
    					if (mProgress != null && mProgress.isShowing())
    					{
    						mProgress.dismiss();
    						mProgress = null;
    					}
    				}
    			});
    		}
    	}
    }
}
