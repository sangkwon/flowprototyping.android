
package com.egloos.realmove.android.fp;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;

public class App extends Application {

	private static final int MAX_CACHE_HEIGHT = 1280;
	private static final int MAX_CACHE_WIDTH = 960;

	private static Application instance = null;

	private int mMaxHeight = MAX_CACHE_HEIGHT;
	private int mMaxWidth = MAX_CACHE_WIDTH;

	@Override
	public void onCreate() {
		super.onCreate();

		instance = this;

		this.initImageCache();
	}

	public static Application getInstacne() {
		return instance;
	}

	public int getMaxWidth() {
		return mMaxWidth;
	}

	public int getMaxHeight() {
		return mMaxHeight;
	}

	private void initImageCache() {
		DisplayImageOptions options = new DisplayImageOptions.Builder()
				.cacheInMemory(false)
				.cacheOnDisc(true)
				.showImageOnLoading(new ColorDrawable(0x00000000))
				.imageScaleType(ImageScaleType.EXACTLY)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.build();

		ImageLoaderConfiguration conf = new ImageLoaderConfiguration.Builder(getApplicationContext())
				.discCacheSize(50 * 1024 * 1024)
				.discCacheFileCount(100)
				.discCacheExtraOptions(getMaxWidth(), getMaxHeight(), CompressFormat.JPEG, 75, null)
				.memoryCacheExtraOptions(getMaxWidth(), getMaxHeight())
				.threadPriority(Thread.MIN_PRIORITY)
				.defaultDisplayImageOptions(options)
				.build();

		ImageLoader.getInstance().init(conf);
	}
}
