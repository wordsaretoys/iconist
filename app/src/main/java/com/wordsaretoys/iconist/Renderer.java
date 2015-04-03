package com.wordsaretoys.iconist;

import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.Float2;
import android.renderscript.Float4;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

/**
 * maintains renderscript objects/interface
 */
public class Renderer {

	static String TAG = "Renderer";
	
	static int MaxCoeffs = 4;
	static int MaxColors = 32;
	static int Threshold = 8;
	
	// render script context
	private RenderScript script;
	
	// bitmap backing allocation
	private Bitmap bitmap;
	
	// image array allocation
	private Allocation imageArray;
	
	// generator script
	private ScriptC_generator generator;
	
	// blur script kernel
	private ScriptIntrinsicBlur blur;
	
	// script globals
	private Float4[] coeffs;
	private Float4[] colors;
	private Float2 size;
	private Float2 aspect;

	// RNG
	private Random rng;
	
	// mirror settings
	boolean mirrorX, mirrorY;
	
	// color settings
	int colorCount;
	
	// detail settings
	int detail;
	
	/**
	 * ctor, creates rendering objects
	 */
	public Renderer(Context context) {
		script = RenderScript.create(context);
		generator = new ScriptC_generator(script);

		float[] sine = new float[65536];
		float w = (float)(2 * Math.PI) / (sine.length - 1);
		for (int i = 0; i < sine.length; i++) {
			sine[i] = 0.5f + (float)(0.5 * Math.sin(w * i));
		}
		generator.set_sine(sine);
		
		blur = ScriptIntrinsicBlur.create(script, Element.U8_4(script));
		blur.setRadius(0.5f);

		coeffs = new Float4[MaxCoeffs];
		for (int i = 0; i < coeffs.length; i++) {
			coeffs[i] = new Float4();
		}
		
		colors = new Float4[MaxColors];
		for (int i = 0; i < colors.length; i++) {
			colors[i] = new Float4();
		}

		size = new Float2();
		aspect = new Float2();
		rng = new Random();
		
		mirrorX = true;
		mirrorY = true;
		colorCount = 4;
		detail = 1;
	}

	/**
	 * render and return image array for view
	 */
	public Allocation renderForView(long seed, int width, int height) {
		render(seed, width, height, false);
		return imageArray;
	}
	
	/**
	 * render, smooth, return bitmap for file copy
	 */
	public Bitmap renderForFile(long seed, int width, int height) {
		render(seed, width, height, true);
		return bitmap;
	}
	
	/**
	 * tear down script context
	 */
	public void destroy() {
		script.destroy();
	}

	/**
	 * perform render
	 */
	void render(long seed, int width, int height, boolean smooth) {
		rng.setSeed(seed);

		for (int i = 0; i < coeffs.length; i++) {
			coeffs[i].x = 2 + detail * rng.nextFloat();
			coeffs[i].y = 2 + detail * rng.nextFloat();
			coeffs[i].z = 2 + detail * rng.nextFloat();
			coeffs[i].w = 2 + detail * rng.nextFloat();
		}
		generator.set_coeffs(coeffs);

		generator.set_mirrorX(mirrorX ? 1 : 0);
		generator.set_mirrorY(mirrorY ? 1 : 0);

		generator.set_phase(rng.nextInt(65536));
		
		if (bitmap == null || bitmap.getWidth() != width ||	bitmap.getHeight() != height) {
			bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			bitmap.setHasAlpha(true);
			imageArray = Allocation.createFromBitmap(script, bitmap);
		}
		
		size.x = width;
		size.y = height;
		generator.set_size(size);
		if (size.x > size.y) {
			aspect.x = 1;
			aspect.y = size.y / size.x;
		} else {
			aspect.x = size.x / size.y;
			aspect.y = 1;
		}
		generator.set_aspect(aspect);

		// bottom n colors all transparent
		// remainder max - n colors (div by 2, 3, or 4)
		int avail = MaxColors - Threshold;
		int binsz = avail / colorCount;
		float r = 0, g = 0, b = 0;
		// this goes last because the number of random
		// numbers we pull depends on color count, and
		// will interfere with other random parameters
		for (int i = Threshold; i < MaxColors; i++) {
			int j = i - Threshold;
			if ((j % binsz) == 0) {
				r = colorCount == 1 ? 0 : rng.nextFloat();
				g = colorCount == 1 ? 0 : rng.nextFloat();
				b = colorCount == 1 ? 0 : rng.nextFloat();
			}
			colors[i].x = r;
			colors[i].y = g;
			colors[i].z = b;
			colors[i].w = 1;
		}
		generator.set_colors(colors);
		
		generator.forEach_root(imageArray);
		script.finish();

		if (smooth) {
			blur.setInput(imageArray);
			blur.forEach(imageArray);
			script.finish();
		}
	}
	
}
