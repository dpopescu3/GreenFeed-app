package diana.com;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.security.cert.PolicyNode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import diana.com.Utilis.Bottom_Navigation_View_Helper;
import diana.com.Utilis.FirebaseMethods;
import diana.com.Utilis.GridImageAdapter;
import diana.com.Utilis.Likes;
import diana.com.Utilis.SquareImageView;
import diana.com.Utilis.UniversalImageLoader;
import diana.com.models.Comment;
import diana.com.models.Like;
import diana.com.models.Photo;
import diana.com.models.User;
import diana.com.models.UserAccountSettings;

public class ViewPostFragment extends Fragment {


    private static final String TAG = "ViewPostFragment";

    public interface OnCommentThreadSelectedListener {
        void OnCommentThreadSelectedListener(Photo photo);
    }

    OnCommentThreadSelectedListener mOnCommentThreadSelectedListener;

    public ViewPostFragment() {
        super();
        setArguments(new Bundle());
    }

    private Photo mPhoto;
    private int mActivityNumber = 0;
    private String photoUsername = "";
    private String PhotoUrl = "";
    private UserAccountSettings mUserAccountSettings;
    private GestureDetector mGestureDetector;

    private SquareImageView mPostImage;
    private BottomNavigationViewEx bottomNavigationView;
    private TextView mBackLabel, mCaption, mComments, mUsername, mTimestamp, mLikes;
    private ImageView mBackArrow, mEllipsess, mHeartRed, mHeartWhite, mProfileImage;
    private Likes likes;
    private boolean mLikedbyCurrentUser;
    private StringBuilder mUsers;
    private String mLikesString = "";
    private ImageView mComment;
    private User mCurrentUser;


    private FirebaseMethods mFirebaseMethods;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_posts, container, false);
        mPostImage = view.findViewById(R.id.post_image);
        bottomNavigationView = view.findViewById(R.id.bottomNavView);
        mBackArrow = view.findViewById(R.id.BackArrow);
        mBackLabel = view.findViewById(R.id.tvBackLabel);
        mCaption = view.findViewById(R.id.image_caption);
        mUsername = view.findViewById(R.id.username);
        mTimestamp = view.findViewById(R.id.image_time_posted);
        mEllipsess = view.findViewById(R.id.ivEllipses);
        mHeartRed = view.findViewById(R.id.image_like_red);
        mHeartWhite = view.findViewById(R.id.image_like);
        mProfileImage = view.findViewById(R.id.profile_photo);
        likes = new Likes(mHeartWhite, mHeartRed);
        mLikes = view.findViewById(R.id.image_likes);
        mComment = view.findViewById(R.id.speech_bubble);
        mComments = view.findViewById(R.id.image_comments_link);
        mGestureDetector = new GestureDetector(getActivity(), new GestureListener());
        mEllipsess.setVisibility(View.GONE);
        setupFirebaseAuth();
        setupBottonNavigationView();
        mEllipsess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete();
            }
        });

        return view;


    }

    private void delete() {
        myRef.child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .removeValue();
        myRef.child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mPhoto.getPhoto_id())
                .removeValue();
        getActivity().getSupportFragmentManager().popBackStack();


    }

    private void init() {
        try {
//            mPhoto = getPhotofromBundle();

            UniversalImageLoader.setImage(getPhotofromBundle().getImage_path(), mPostImage, null, "");
            mActivityNumber = getActivityNumberfromBundle();

            String photo_id = getPhotofromBundle().getPhoto_id();
            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.dbname_photos))
                    .orderByChild(getString(R.string.field_photo_id))
                    .equalTo(photo_id);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        Photo newPhoto = new Photo();
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                        newPhoto.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        newPhoto.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                        newPhoto.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                        newPhoto.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        newPhoto.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        newPhoto.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                        List<Comment> commentsList = new ArrayList<Comment>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child(getString(R.string.field_comments)).getChildren()) {
                            Comment comment = new Comment();
                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                            comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                            commentsList.add(comment);

                        }

                        newPhoto.setComments(commentsList);
                        mPhoto = newPhoto;
                        getCurrentUser();
                        getPhotoDetails();
                        //getLikesString();

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        } catch (NullPointerException e) {
            Log.d(TAG, "onCreateView:NullPointerException: photo was null from bundle " + e.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isAdded()){
            init();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mOnCommentThreadSelectedListener = (OnCommentThreadSelectedListener) getActivity();
        } catch (ClassCastException e) {
            Log.d(TAG, "onAttach: " + e);
        }
    }

    private void getLikesString() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUsers = new StringBuilder();
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    Query query = reference
                            .child(getString(R.string.dbname_users))
                            .orderByChild(getString(R.string.field_user_id))
                            .equalTo(singleSnapshot.getValue(Like.class).getUser_id());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot singleSnaphot : dataSnapshot.getChildren()) {

                                mUsers.append(singleSnaphot.getValue(User.class).getUsername());
                                mUsers.append(",");
                            }
                            String[] splitUsers = mUsers.toString().split(",");
                            if (mUsers.toString().contains(mCurrentUser.getUsername() + ",")) {
                                mLikedbyCurrentUser = true;
                            } else {
                                mLikedbyCurrentUser = false;
                            }
                            int length = splitUsers.length;
                            Log.d(TAG, "onDataChange: likes" + length);
                            if (length == 1) {
                                mLikesString = "Liked by " + splitUsers[0];

                            } else if (length == 2) {
                                mLikesString = "Liked by " + splitUsers[0] + " and " + splitUsers[1];


                            } else if (length == 3) {
                                mLikesString = "Liked by " + splitUsers[0] + "," + splitUsers[1] + " and " + splitUsers[2];


                            } else if (length == 4) {
                                mLikesString = "Liked by " + splitUsers[0] + "," + splitUsers[1] + "," + splitUsers[2] + " and " + splitUsers[3];


                            } else if (length > 4) {
                                mLikesString = "Liked by " + splitUsers[0] + "," + splitUsers[1] + "," + splitUsers[2] + " and" + (splitUsers.length - 3) + " others";


                            }
                            Log.d(TAG, "onDataChange: likes" + length);
                            setupWidgets();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
                if (!dataSnapshot.exists()) {
                    mLikesString = "";
                    mLikedbyCurrentUser = false;
                    setupWidgets();
                }
                mLikes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent =new Intent(getContext(), AdmirersActivity.class);
                        String profileid = "";
                        intent.putExtra("id",profileid);
                        intent.putExtra("title","likes");
                        startActivity(intent);
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getCurrentUser() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        final Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    mCurrentUser = singleSnapshot.getValue(User.class);
                    mEllipsess.setVisibility(View.VISIBLE);
                    Log.d(TAG, "onDataChange: haa" + mUserAccountSettings);
                }
                getLikesString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(getString(R.string.dbname_photos))
                    .child(mPhoto.getPhoto_id())
                    .child(getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnaphot : dataSnapshot.getChildren()) {

                        String keyID = singleSnaphot.getKey();
                        ///the photo is liked
                        if (mLikedbyCurrentUser &&
                                singleSnaphot.getValue(Like.class).getUser_id()
                                        .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            myRef.child(getString(R.string.dbname_photos))
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();
                            myRef.child(getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();
                            likes.toggleLike();
                            getLikesString();
                        } else if (!mLikedbyCurrentUser) {
                            ///the photo is not
                            addNewLike();
                            break;
                        }

                    }
                    if (!dataSnapshot.exists()) {
                        //add new like
                        addNewLike();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return true;
        }
    }

    private void addNewLike() {
        String newLikeID = myRef.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());


        myRef.child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);
        myRef.child(getString(R.string.dbname_user_photos))
                .child(mPhoto.getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);
        likes.toggleLike();
        getLikesString();

    }

    private void getPhotoDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(mPhoto.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    mUserAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);

                    Log.d(TAG, "onDataChange: haa" + mUserAccountSettings);
                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    private void setupWidgets() {
        String timestampDiff = getTimeStampDifference();
        if (!timestampDiff.equals("0")) {
            mTimestamp.setText(timestampDiff + R.string.days_ago);

        } else {
            mTimestamp.setText(R.string.today);
        }
        UniversalImageLoader.setImage(mUserAccountSettings.getProfile_photo(), mProfileImage, null, "");
        mUsername.setText(mUserAccountSettings.getUsername());
        mLikes.setText(mLikesString);
        mCaption.setText(mPhoto.getCaption());
        if (mPhoto.getComments().size() > 0) {
            mComments.setText("View all " + mPhoto.getComments().size() + " comments");

        } else {
            mComments.setText("");
        }
        mComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnCommentThreadSelectedListener.OnCommentThreadSelectedListener(mPhoto);
            }
        });
        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: nav back");
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        mComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: nav back");
                mOnCommentThreadSelectedListener.OnCommentThreadSelectedListener(mPhoto);
            }
        });

        if (mLikedbyCurrentUser) {
            mHeartWhite.setVisibility(View.GONE);
            mHeartRed.setVisibility(View.VISIBLE);
            mHeartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return mGestureDetector.onTouchEvent(motionEvent);
                }
            });
        } else {
            mHeartWhite.setVisibility(View.VISIBLE);
            mHeartRed.setVisibility(View.GONE);
            mHeartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return mGestureDetector.onTouchEvent(motionEvent);
                }
            });

        }

    }

    private String getTimeStampDifference() {
        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String phototimestamp = mPhoto.getDate_created();
        try {
            timestamp = sdf.parse(phototimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24)));
        } catch (ParseException e) {
            Log.d(TAG, "getTimeStampDifference:ParseException " + e.getMessage());
            difference = "0";
        }

        return difference;
    }

    private int getActivityNumberfromBundle() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getInt(getString(R.string.photo));
        } else {
            return 0;
        }
    }

    private Photo getPhotofromBundle() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.photo));
        } else {
            return null;
        }
    }

    private void setupBottonNavigationView() {
        Log.d(TAG, "setupBottonNavigationView: set up BottonNavigationView");
        Bottom_Navigation_View_Helper.setupBottomNavigationView(bottomNavigationView);
        Bottom_Navigation_View_Helper.enableNavigation(getActivity(), getActivity(), bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(mActivityNumber);
        menuItem.setChecked(true);
    }


    /**
     * Set up the firebase auth
     */

    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth:setting up firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //User is signed in
                    Log.d(TAG, "onAuthStateChanged:" + user.getUid());
                } else {
                    ///User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

}

