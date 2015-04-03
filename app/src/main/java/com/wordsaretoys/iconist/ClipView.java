package com.wordsaretoys.iconist;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class ClipView extends ImageView {

	static String TAG = "ClipView";
	
	// image bitmap
	Bitmap bitmap;

	// generator seed
	long seed;
	
	// redraw runnable for out-of-thread calls
	Runnable redraw;
	
	/**
	 * xml-compatible ctor 
	 */
	public ClipView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MainActivity main = (MainActivity) getContext();
				main.submitFile(seed);
			}
		});
		
		redraw = new Runnable(){
			@Override
			public void run() {
				setImageBitmap(bitmap);
			}
		};
	}

	/**
	 * set generator seed
	 */
	public void setSeed(long s) {
		seed = s;
	}
	
	/**
	 * get clip generator seed
	 */
	public long getSeed() {
		return seed;
	}

	/**
	 * set image bitmap size
	 */
	public void setImageSize(int width, int height) {
		if (bitmap == null || bitmap.getWidth() != width || bitmap.getHeight() != height) {
			bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			bitmap.setHasAlpha(true);
			setImageBitmap(bitmap);
		}
	}
	
	/**
	 * clear bitmap to neutral pattern
	 */
	public void clearBitmap() {
		bitmap.eraseColor(Color.GRAY);
	}
	
	/**
	 * redraw changed bitmap (may be called from non-UI thread)
	 */
	public void redraw() {
		((Activity) getContext()).runOnUiThread(redraw);
	}
	
	/**
	 * disable default click behavior
	 */
	public void disableClick() {
		setOnClickListener(null);
	}
}
