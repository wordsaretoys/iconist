package com.wordsaretoys.iconist;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {

	// rendering queue
	// one instance maintained between activity lifetimes
	static RenderQueue renderQ;
	
	// list of clips
	ListView clipList;

	// clip list adapter
	ClipListAdapter adapter;
	
	// base generator seed
	int baseSeed;
	
	// clip list column count
	int columns;
	
	// target image dimensions
	Point imageSize = new Point();
	
	// clip view dimensions
	Point clipSize = new Point();
	
	@Override
	protected void onCreate(Bundle inState) {
		super.onCreate(inState);
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);
		
		// create rendering queue when needed
		if (renderQ == null) {
			renderQ = new RenderQueue();
		}
		renderQ.onCreate(this);
		
		// get the base seed, if available
		SharedPreferences prefs = getSharedPreferences(getPackageName(), 0);
		baseSeed = prefs.getInt("baseSeed", -1);
		if (baseSeed == -1) {
			createBaseSeed();
		}

		// set up the clip list & adapter
		clipList = (ListView) findViewById(R.id.clipList);
		clipList.setDivider(null);
		adapter = new ClipListAdapter();
		clipList.setAdapter(adapter);
		
		if (inState != null) {
		
			Renderer rn = renderQ.getRenderer();
			rn.colorCount = inState.getInt("colorCount", 4);
			rn.detail = inState.getInt("detail", 1);
			rn.mirrorX = inState.getBoolean("mirrorX");
			rn.mirrorY = inState.getBoolean("mirrorY");
			
			int w = inState.getInt("width");
			int h = inState.getInt("height");
			setImageSize(w, h);
			
			int pos = inState.getInt("listIndex") / columns;
			clipList.setSelection(pos);

		} else {
			// set default image size
			setImageSize(512, 512);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		
		case R.id.shuffle:
			createBaseSeed();
			adapter.notifyDataSetChanged();
			break;
			
		case R.id.mirror:
			MirrorDialog.create(
					renderQ.getRenderer().mirrorX, 
					renderQ.getRenderer().mirrorY)
				.show(getFragmentManager(), "mirror");
			break;
			
		case R.id.colors:
			ColorsDialog.create(
					renderQ.getRenderer().colorCount)
				.show(getFragmentManager(), "colors");
			break;
			
		case R.id.detail:
			DetailDialog.create(
					renderQ.getRenderer().detail)
				.show(getFragmentManager(), "detail");
			break;
			
		case R.id.resize:
			SizeDialog.create(imageSize.x, imageSize.y)
				.show(getFragmentManager(), "size");
			break;

		default:
			return false;
		
		}
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		Renderer rn = renderQ.getRenderer();

		outState.putInt("colorCount", rn.colorCount);
		outState.putInt("detail", rn.detail);
		outState.putBoolean("mirrorX", rn.mirrorX);
		outState.putBoolean("mirrorY", rn.mirrorY);
		
		outState.putInt("width", imageSize.x);
		outState.putInt("height", imageSize.y);
		
		int index = clipList.getFirstVisiblePosition() * columns;
		outState.putInt("listIndex", index);
	}
	
	/**
	 * generate new base seed
	 */
	void createBaseSeed() {
		SharedPreferences prefs = getSharedPreferences(getPackageName(), 0);
		Editor editor = prefs.edit();
		baseSeed = (int)(Math.random() * Integer.MAX_VALUE);
		editor.putInt("baseSeed", baseSeed);
		editor.apply();
	}
	
	/**
	 * (re)set target image size
	 */
	void setImageSize(int width, int height) {
		imageSize.x = width;
		imageSize.y = height;
		// adjust width & height for best clip display
		if (width > height) {
			height = 256 * height / width;
			width = 256;
		} else {
			width = 256 * width / height;
			height = 256;
		}
		// number of columns based on image width
		int screenWidth = getResources().getDisplayMetrics().widthPixels;
		columns = screenWidth / width;
		// adjust for margins
		// TODO: if possible, load margins from resources
		columns -= (columns * 16) / width;
		// store off new dimensions
		clipSize.x = width;
		clipSize.y = height;
		// and redraw if possible
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * sumbit file generation request to renderer
	 */
	void submitFile(long seed) {
		renderQ.addRequest(imageSize, seed);
	}
	
	/**
	 * clip list adapter
	 */
	class ClipListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return Integer.MAX_VALUE;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
			ViewGroup ll = (ViewGroup) convertView;

			if (ll == null) {
				ll = (ViewGroup) inflater.inflate(R.layout.clip_item, null);
			}
			
			// ensure that clip view count is the same as column count
			while(ll.getChildCount() < columns) {
				View clip = inflater.inflate(R.layout.clip, ll, false);
				ll.addView(clip);
			}
			while (ll.getChildCount() > columns) {
				ll.removeViewAt(ll.getChildCount() - 1);
			}
			
			// prepare each clip for rendering
			long seed = (baseSeed << 32) + position * columns;
			for (int i = 0; i < columns; i++) {
				ClipView clip = (ClipView) ll.getChildAt(i);
				clip.setImageSize(clipSize.x, clipSize.y);
				clip.clearBitmap();
				clip.setSeed(seed + i);
				renderQ.addRequest(clip);
			}
			
			return ll;
		}
		
	}

	/**
	 * get debug cert status of app
	 * @param ctx context
	 * @return true if app built with debug cert
	 */
	public static boolean isDebuggable(Context ctx)
	{
	    boolean debuggable = false;
		// debug cert 
		final X500Principal DEBUG_DN = 
				new X500Principal("CN=Android Debug,O=Android,C=US");

	    try
	    {
	        PackageInfo pinfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(),PackageManager.GET_SIGNATURES);
	        Signature signatures[] = pinfo.signatures;

	        CertificateFactory cf = CertificateFactory.getInstance("X.509");

	        for ( int i = 0; i < signatures.length;i++)
	        {   
	            ByteArrayInputStream stream = new ByteArrayInputStream(signatures[i].toByteArray());
	            X509Certificate cert = (X509Certificate) cf.generateCertificate(stream);       
	            debuggable = cert.getSubjectX500Principal().equals(DEBUG_DN);
	            if (debuggable)
	                break;
	        }
	    }
	    catch (NameNotFoundException e)
	    {
	        //debuggable variable will remain false
	    }
	    catch (CertificateException e)
	    {
	        //debuggable variable will remain false
	    }
	    return debuggable;
	}	
	
}
