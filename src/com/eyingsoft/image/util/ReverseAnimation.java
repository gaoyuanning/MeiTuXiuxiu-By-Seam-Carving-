package com.eyingsoft.image.util;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ReverseAnimation extends Animation
{
	private final float mFromDegrees;
	private final float mToDegrees;
	private final float mCenterX;
	private final float mCenterY;
	private final float mDepthZ;
	private final boolean mReverse;
	private Camera mCamera;

	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;
	private int mReverseType = HORIZONTAL;
	
	public ReverseAnimation(float fromDegrees, float toDegrees, float centerX,
			float centerY, float depthZ, boolean reverse)
	{
		mFromDegrees = fromDegrees;
		mToDegrees = toDegrees;
		mCenterX = centerX;
		mCenterY = centerY;
		mDepthZ = depthZ;
		mReverse = reverse;
	}
	
	public void setReverseType(int type)
	{
		mReverseType = type;
	}

	@Override
	public void initialize(int width, int height, int parentWidth,
			int parentHeight)
	{
		super.initialize(width, height, parentWidth, parentHeight);
		mCamera = new Camera();
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t)
	{
		final float fromDegrees = mFromDegrees;
		float degrees = fromDegrees
				+ ((mToDegrees - fromDegrees) * interpolatedTime);
		final float centerX = mCenterX;
		final float centerY = mCenterY;
		final Camera camera = mCamera;
		final Matrix matrix = t.getMatrix();
		camera.save();
		if (mReverse)
		{
			camera.translate(0.0f, 0.0f, mDepthZ * interpolatedTime);
		} else
		{
			camera.translate(0.0f, 0.0f, mDepthZ * (1.0f - interpolatedTime));
		}
		
		switch (mReverseType)
		{
		case HORIZONTAL:
			camera.rotateY(degrees);
			break;
		case VERTICAL:
			camera.rotateX(degrees);
			break;
		}
		camera.getMatrix(matrix);
		camera.restore();

		matrix.preTranslate(-centerX, -centerY);
		matrix.postTranslate(centerX, centerY);
	}
}
