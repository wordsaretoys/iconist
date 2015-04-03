package com.wordsaretoys.iconist;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Point;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.renderscript.Allocation;
import android.widget.Toast;

/**
 * maintains rendering queue and worker thread
 */
public class RenderQueue {

	static String TAG = "RenderQueue";
	
	/**
	 * rendering request object
	 */
	class RenderingRequest {
		// image generation seed
		public long seed;
		// view to receive new image
		// (null if creating a file)
		public ClipView clip;
		// image dimensions
		// (null if copying to view)
		public Point imageSize;
	}
	
	// rendering object
	Renderer renderer;
	
	// render request queue
	ArrayBlockingQueue<RenderingRequest> queue;

	// current activity
	Activity activity;
	
	// application context
	Context context;
	
	// worker thread instance
	Thread worker;

	/**
	 * creates worker thread, queue, renderer
	 */
	public void onCreate(Activity a) {
		activity = a;
		// if used as static activity member,
		// may be called multiple times
		if (context == null) {
			context = activity.getApplicationContext();
			queue = new ArrayBlockingQueue<RenderingRequest>(1000);
			worker = new Thread() {
				@Override
				public void run() {
					init();
					loop();
				}
			};
			worker.start();
		}
	}

	/**
	 * queue up a clip rendering request
	 */
	public void addRequest(ClipView view) {
		RenderingRequest req = new RenderingRequest();
		req.clip = view;
		req.seed = view.getSeed();
		queue.add(req);
	}

	/**
	 * queue up a file rendering request
	 */
	public void addRequest(Point size, long seed) {
		RenderingRequest req = new RenderingRequest();
		req.imageSize = size;
		req.seed = seed;
		queue.add(req);
	}

	/**
	 * return renderer object
	 */
	public Renderer getRenderer() {
		return renderer;
	}
	
	/**
	 * stop worker thread
	 */
	public void stop() {
		RenderingRequest req = new RenderingRequest();
		queue.add(req);
	}
	
	/**
	 * initialize renderer
	 */
	private void init() {
		// creates renderscript context, which
		// may be a blocking op; do in worker
		renderer = new Renderer(context);
	}
	
	/**
	 * render request handling loop
	 */
	private void loop() {
		
		while (true) {
			
			try {
				
				final RenderingRequest req = queue.take();
				
				// poison object exits loop
				if (req.clip == null && req.imageSize == null) {
					renderer.destroy();
					return;
				}

				// prevent clip overdraw due to backlog
				if (req.clip != null && req.seed != req.clip.getSeed()) {
					continue;
				}
				
				// for view rendering
				if (req.clip != null) {
					Bitmap target = req.clip.bitmap;
					Allocation source = renderer.renderForView(req.seed, 
							target.getWidth(), target.getHeight());
					source.copyTo(target);
					req.clip.redraw();
				} else {

					// this may take a while
					activity.runOnUiThread(new Runnable() {
						public void run() {
							activity.setProgressBarIndeterminateVisibility(true);
						}
					});
					
					// rendering to file
					Bitmap source = renderer.renderForFile(req.seed, 
							req.imageSize.x, req.imageSize.y);
					
					try {
						
						String name = UUID.randomUUID().toString()+ ".png";
						File file = new File(getTargetPath(), name);
						FileOutputStream out = new FileOutputStream(file);
						source.compress(CompressFormat.PNG, 90, out);
						out.close();

						MediaScannerConnection.scanFile(
							context, 
							new String[] { file.getAbsolutePath() }, 
							null, null);
						
						activity.runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(
									activity, 
									R.string.imageSaveOK, 
									Toast.LENGTH_SHORT
								).show();
								activity.setProgressBarIndeterminateVisibility(false);
							}
						});
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}

			} catch (InterruptedException e) {
				renderer.destroy();
			}
		}
	}

	/**
	 * return the path to where we store images
	 */
	File getTargetPath() {
		String album = context.getResources().getString(R.string.app_name);
	    File dir = new File(Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_PICTURES), album);
	    dir.mkdirs();
	    return dir;
	}	
	
}
