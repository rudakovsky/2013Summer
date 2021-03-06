package me.xiangchen.ui;

import me.xiangchen.app.duetos.LauncherManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

public class xacToast extends ViewGroup {

	public static final float FADERATE = 0.975f;
	public static final float LOWALPHA = 0.01f;
	ImageView imageView;
	float alpha = 1.0f;
	int imageSource;
	boolean dead = true;

	public xacToast(Context context) {
		super(context);
		imageView = new ImageView(context);
//		imageView.setimageb
		this.addView(imageView);
	}

	public void setImage(int resId) {
		try {
			imageView.setImageResource(resId);
		} catch(Exception e) {
			LauncherManager.doAndriodToast("wait...");
		}
//		imageView.setImageBitmap(LauncherManager.getBitmap(resId));
	}
	
	public void setImage() {
		imageView.setImageResource(imageSource);
	}
	
	private ViewGroup tmpParent;

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub

		int numChildren = this.getChildCount();
		for (int i = 0; i < numChildren; i++) {
			final View child = this.getChildAt(i);
			BitmapDrawable bitmapDrawable = ((BitmapDrawable) imageView.getDrawable());
			Bitmap bitmap = bitmapDrawable .getBitmap();
			int wChild = bitmap.getWidth();//child.getMeasuredWidth();
			int hChild = bitmap.getHeight();//child.getMeasuredHeight();

			int lChild = l + ((r - l) - wChild) / 2;
			int tChild = t + ((b - t) - hChild) / 2;
			child.layout(lChild, tChild, lChild + wChild, tChild + hChild);
		}
	}

	@Override
	public void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);

	}

	public void fadeIn(ViewGroup parent) {
		if(parent != null) {
			parent.addView(this);
			tmpParent = parent;
		}
		alpha = 1.0f;
		dead = false;
	}

	public void fadeOut() {
		if(alpha <= 0) {
			return;
		}
		
		alpha *= FADERATE;
		this.setAlpha(alpha);

		if (alpha < LOWALPHA) {
			alpha = 0;
			kill(tmpParent);
			tmpParent = null;
		}
	}
	
	public void kill(ViewGroup parent) {
		if(parent != null) {
			parent.removeView(this);
			dead = true;
		}
	}
	
	public boolean isActive() {
		return !(this.getParent() == null);
	}
	
	
	public void setImgSrc(int src) {
		imageSource = src;
	}
	
	public Bitmap getBitmap() {
		BitmapDrawable bitmapDrawable = ((BitmapDrawable) imageView.getDrawable());
		Bitmap bitmap = bitmapDrawable.getBitmap();
		return bitmap;
	}
	
	public boolean isDead() {
		return dead;
	}
}
