package diana.com.Utilis;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import diana.com.R;

public class UniversalImageLoader  {
    private static final int defaultImage= R.drawable.network;
    private Context mContext;
    public UniversalImageLoader(Context context){
        mContext=context;
    }
    public ImageLoaderConfiguration getConfig(){
        DisplayImageOptions defaultOptions=new DisplayImageOptions.Builder()
                .showImageOnFail(defaultImage)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .considerExifParams(true)
                .cacheInMemory(true).cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();
        ImageLoaderConfiguration configuration=new ImageLoaderConfiguration.Builder(mContext)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .diskCacheSize(100*1024*1024).build();
        return configuration;

    }

    /**
     * this method can be used to det images that are static, It can't be used if the images are being chanced in the Fragment/Activity Or if they are being set in a list or grillview
     * @param ImgURL
     * @param image
     * @param mProgressBar
     * @param append
     */
    public static void setImage(String ImgURL, ImageView image,final ProgressBar mProgressBar,String append){

    ImageLoader imageLoader=ImageLoader.getInstance();
    imageLoader.displayImage(append + ImgURL, image, new ImageLoadingListener() {
        @Override
        public void onLoadingStarted(String imageUri, View view) {
            if(mProgressBar!=null){
                mProgressBar.setVisibility(View.VISIBLE);

            }
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            if(mProgressBar!=null){
                mProgressBar.setVisibility(View.GONE);

            }
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if(mProgressBar!=null){
                mProgressBar.setVisibility(View.GONE);

            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            if(mProgressBar!=null){
                mProgressBar.setVisibility(View.GONE);

            }
        }
    });
    }

}
