package diana.com.Utilis;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

import diana.com.R;

public class GridImageAdapter extends ArrayAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private int layoutResource;
    private String nAppend;
    private ArrayList<String> imgURL;

    public GridImageAdapter(Context context, int layoutResource, String nAppend, ArrayList<String> imgURL) {
        super(context,layoutResource,imgURL);
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        this.layoutResource = layoutResource;
        this.nAppend = nAppend;
        this.imgURL = imgURL;

    }

    private static class ViewHolder{
        SquareImageView image;
        ProgressBar mProgressBar;
    }

    @Override
    public View getView(int position,  View convertView, ViewGroup parent) {
/**
 Viewholder build pattern
 */
        final ViewHolder holder;
if(convertView==null){
    convertView=mInflater.inflate(layoutResource,parent,false);
    holder=new ViewHolder();
    holder.mProgressBar=(ProgressBar)convertView.findViewById(R.id.gridImageProgressBar);
    holder.image=(SquareImageView) convertView.findViewById(R.id.gridImageView);
    convertView.setTag(holder);
}
 else{
     holder=(ViewHolder)convertView.getTag();
 }
   String imgURL= (String) getItem(position);
        ImageLoader imageLoader=ImageLoader.getInstance();
        imageLoader.displayImage(nAppend + imgURL, holder.image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                if(holder.mProgressBar!=null){
                    holder.mProgressBar.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if(holder.mProgressBar!=null){
                    holder.mProgressBar.setVisibility(View.GONE);

                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if(holder.mProgressBar!=null){
                    holder.mProgressBar.setVisibility(View.GONE);

                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if(holder.mProgressBar!=null){
                    holder.mProgressBar.setVisibility(View.GONE);

                }
            }
        });
 return convertView ;
    }
}
