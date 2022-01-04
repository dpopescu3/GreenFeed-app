package diana.com.Utilis;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class Likes {
    private static final String TAG = "Likes";
    private static final DecelerateInterpolator DECELERATE_INTERPOLATOR=new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR=new AccelerateInterpolator();
    public ImageView like,like_full;

    public Likes(ImageView like, ImageView like_full) {
        this.like = like;
        this.like_full = like_full;
    }
    public void toggleLike(){
        AnimatorSet animatorSet=new AnimatorSet();
        if(like_full.getVisibility()== View.VISIBLE){
            like_full.setScaleX(0.1f);
            like_full.setScaleY(0.1f);

            ObjectAnimator scaleDownY=ObjectAnimator.ofFloat(like_full,"scaleY",1f,0f);
            scaleDownY.setDuration(300);
            scaleDownY.setInterpolator(ACCELERATE_INTERPOLATOR);


            ObjectAnimator scaleDownX=ObjectAnimator.ofFloat(like_full,"scaleX",1f,0f);
            scaleDownX.setDuration(300);
            scaleDownX.setInterpolator(ACCELERATE_INTERPOLATOR);

            like_full.setVisibility(View.GONE);
            like.setVisibility(View.VISIBLE);
            animatorSet.playTogether(scaleDownY,scaleDownX);
        }

        else if(like_full.getVisibility()== View.GONE){
            like_full.setScaleX(0.1f);
            like_full.setScaleY(0.1f);

            ObjectAnimator scaleDownY=ObjectAnimator.ofFloat(like_full,"scaleY",0.1f,1f);
            scaleDownY.setDuration(300);
            scaleDownY.setInterpolator(DECELERATE_INTERPOLATOR);


            ObjectAnimator scaleDownX=ObjectAnimator.ofFloat(like_full,"scaleX",0.1f,1f);
            scaleDownX.setDuration(300);
            scaleDownX.setInterpolator(DECELERATE_INTERPOLATOR);

            like_full.setVisibility(View.VISIBLE);
            like.setVisibility(View.GONE);
            animatorSet.playTogether(scaleDownY,scaleDownX);
        }
        animatorSet.start();
    }
}
