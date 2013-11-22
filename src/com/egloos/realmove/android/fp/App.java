
package com.egloos.realmove.android.fp;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;

public class App extends Application {

	private static Application instance = null;

	@Override
	public void onCreate() {
		super.onCreate();

		instance = this;

		initImageCache(getApplicationContext());
	}

	public static Application getInstacne() {
		return instance;
	}

	private static void initImageCache(Context context) {
		DisplayImageOptions options = new DisplayImageOptions.Builder()
				.cacheInMemory(true)
				.cacheOnDisc(true)
				.showImageOnLoading(new ColorDrawable(0x00000000))
				.build();

		ImageLoaderConfiguration conf = new ImageLoaderConfiguration.Builder(context)
				.discCacheSize(50 * 1024 * 1024)
				.discCacheFileCount(100)
				.defaultDisplayImageOptions(options).build();

		ImageLoader.getInstance().init(conf);
	}
}
