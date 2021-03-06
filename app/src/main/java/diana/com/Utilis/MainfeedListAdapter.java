package diana.com.Utilis;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import diana.com.Home.MainActivity;
import diana.com.Profile.ProfileActivity;
import diana.com.R;
import diana.com.models.Comment;
import diana.com.models.Like;
import diana.com.models.Photo;
import diana.com.models.User;
import diana.com.models.UserAccountSettings;

public class MainfeedListAdapter extends ArrayAdapter<Photo> {
    public  interface  OnLoadMoreItemsListener{
        void onLoadMoreItemsListener();
        }
        OnLoadMoreItemsListener mOnLoadMoreItemsListener;
    private static final String TAG = "MainfeedListAdapter";
    private LayoutInflater mInflater;
    private int mLayoutReference;
    private Context mContext;
    private DatabaseReference mReference;
    private String currentUsername = "";

    public MainfeedListAdapter(Context context, int resource, List<Photo> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutReference = resource;
        this.mContext = context;
        mReference=FirebaseDatabase.getInstance().getReference();
    }

    static class ViewHolder {
        CircleImageView mProfileImage;
        String likesString;
        TextView username, timeData, caption, likes, comments;
        SquareImageView image;
        ImageView heartRed, heartWhite, comment;

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();
        StringBuilder users;
        String mLikesString;
        boolean likedByCurrentUser;
        Likes heart;
        GestureDetector detector;
        Photo photo;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetTextI18n")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutReference, parent, false);
            holder = new ViewHolder();
            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.image = convertView.findViewById(R.id.post_image);
            holder.heartRed = convertView.findViewById(R.id.image_like_red);
            holder.heartWhite = convertView.findViewById(R.id.image_like);
            holder.comment = convertView.findViewById(R.id.speech_bubble);
            holder.comments = convertView.findViewById(R.id.image_comments_link);
            holder.likes = convertView.findViewById(R.id.image_likes);
            holder.caption = convertView.findViewById(R.id.image_caption);
            holder.timeData = convertView.findViewById(R.id.image_time_posted);
            holder.mProfileImage = convertView.findViewById(R.id.profile_photo);
            holder.heart = new Likes(holder.heartWhite, holder.heartRed);
            holder.photo = getItem(position);
            holder.detector = new GestureDetector(mContext, new GestureListener(holder));
            holder.users = new StringBuilder();

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        getCurrentUsername();
//get likes string
        getLikesString(holder);
        ///set the caption
        holder.caption.setText(getItem(position).getCaption());
        //set comments
        List<Comment> comments = getItem(position).getComments();
        if(comments.size()==0){
            holder.comments.setText("");

        }else {
        holder.comments.setText("View all " + comments.size()+ " comments");
        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)mContext).onCommentThreadSelected(getItem(position),mContext.getString(R.string.main_activity));
                ///going to
                ((MainActivity)mContext).hideLayout();
            }
        });}
        ///set the time it was posted
        String timestampDifference=getTimeStampDifference(getItem(position));
        if(!timestampDifference.equals("0")){
            holder.timeData.setText(timestampDifference + " Days Ago");
        }else {
            holder.timeData.setText("TODAY");
        }

        //set the profile image
        final ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(getItem(position).getImage_path(),holder.image);
//get the profile image

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    //currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    holder.username.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            holder.user.getUsername();
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.main_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                            mContext.startActivity(intent);
                        }
                    });
                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                            holder.mProfileImage);
                    holder.mProfileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            holder.user.getUsername();
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity),
                                    mContext.getString(R.string.main_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                            mContext.startActivity(intent);
                        }
                    });

                    holder.settings=singleSnapshot.getValue(UserAccountSettings.class);
                    holder.comment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ((MainActivity)mContext).onCommentThreadSelected(getItem(position),mContext.getString(R.string.main_activity));

                            //another thing

                            ((MainActivity)mContext).hideLayout();


                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Query userquery = mReference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        userquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    holder.user=singleSnapshot.getValue(User.class);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if(reachedEndofList(position)){
            loadMoreData();

        }


        return convertView;
    }

    private boolean reachedEndofList(int position){
        return position==getCount()-1;

    }
    private void loadMoreData(){
        try {
            mOnLoadMoreItemsListener=(OnLoadMoreItemsListener) getContext();
        }catch (ClassCastException e){

        }

        try {
            mOnLoadMoreItemsListener.onLoadMoreItemsListener();
        }catch (NullPointerException e){

        }
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {
        ViewHolder mHolder;

        public GestureListener(ViewHolder holder) {
            mHolder = holder;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(mHolder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnaphot : dataSnapshot.getChildren()) {

                        String keyID = singleSnaphot.getKey();
                        ///the photo is liked
                        if (mHolder.likedByCurrentUser &&
                                singleSnaphot.getValue(Like.class).getUser_id()
                                        .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            mReference.child(mContext.getString(R.string.dbname_photos))
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();
                            mReference.child(mContext.getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();
                            mHolder.heart.toggleLike();
                            getLikesString(mHolder);
                        } else if (!mHolder.likedByCurrentUser) {
                            ///the photo is not
                            addNewLike(mHolder);
                            break;
                        }

                    }
                    if (!dataSnapshot.exists()) {
                        //add new like
                        addNewLike(mHolder);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return true;
        }
    }

    private void addNewLike(final ViewHolder holder) {
        String newLikeID = mReference.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());


        mReference.child(mContext.getString(R.string.dbname_photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);
        mReference.child(mContext.getString(R.string.dbname_user_photos))
                .child(holder.photo.getUser_id())
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);
        holder.heart.toggleLike();
        getLikesString(holder);

    }

    private void getCurrentUsername() {


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getLikesString(final ViewHolder holder) {
        try {


            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(holder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    holder.users = new StringBuilder();
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        Query query = reference
                                .child(mContext.getString(R.string.dbname_users))
                                .orderByChild(mContext.getString(R.string.field_user_id))
                                .equalTo(singleSnapshot.getValue(Like.class).getUser_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot singleSnaphot : dataSnapshot.getChildren()) {

                                    holder.users.append(singleSnaphot.getValue(User.class).getUsername());
                                    holder.users.append(",");
                                }
                                String[] splitUsers = holder.users.toString().split(",");
                                if (holder.users.toString().contains(currentUsername + ",")) {
                                    holder.likedByCurrentUser = true;
                                } else {
                                    holder.likedByCurrentUser = false;
                                }
                                int length = splitUsers.length;
                                Log.d(TAG, "onDataChange: likes" + length);
                                if (length == 1) {
                                    holder.likesString = "Liked by " + splitUsers[0];

                                } else if (length == 2) {
                                    holder.likesString = "Liked by " + splitUsers[0] + "and" + splitUsers[1];


                                } else if (length == 3) {
                                    holder.likesString = "Liked by " + splitUsers[0] + "," + splitUsers[1] + "and" + splitUsers[2];


                                } else if (length == 4) {
                                    holder.likesString = "Liked by " + splitUsers[0] + "," + splitUsers[1] + "," + splitUsers[2] + "and" + splitUsers[3];


                                } else if (length > 4) {
                                    holder.likesString = "Liked by " + splitUsers[0] + "," + splitUsers[1] + "," + splitUsers[2] + "and" + (splitUsers.length - 3) + "others";


                                }
                                Log.d(TAG, "onDataChange: likes" + length);
                                setupLikesString(holder, holder.likesString);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                    if (!dataSnapshot.exists()) {
                        holder.likesString = "";
                        holder.likedByCurrentUser = false;
                        setupLikesString(holder, holder.likesString);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } catch (NullPointerException e) {
            Log.e(TAG, "getLikesString:NullPointerException " + e.getMessage());
            holder.likesString = "";
            holder.likedByCurrentUser = false;
            setupLikesString(holder, holder.likesString);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupLikesString(final ViewHolder holder, String likesString) {
        if (holder.likedByCurrentUser) {
            holder.heartWhite.setVisibility(View.GONE);
            holder.heartRed.setVisibility(View.VISIBLE);
            holder.heartRed.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return holder.detector.onTouchEvent(motionEvent);

                }
            });

        } else {
            holder.heartWhite.setVisibility(View.VISIBLE);
            holder.heartRed.setVisibility(View.GONE);
            holder.heartWhite.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return holder.detector.onTouchEvent(motionEvent);

                }
            });
        }
        holder.likes.setText(likesString);
    }

    private String getTimeStampDifference(Photo photo) {
        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String phototimestamp = photo.getDate_created();
        try {
            timestamp = sdf.parse(phototimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24)));
        } catch (ParseException e) {
            Log.d(TAG, "getTimeStampDifference:ParseException " + e.getMessage());
            difference = "0";
        }

        return difference;
    }
}

