package diana.com.Utilis;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import diana.com.Profile.AccountSettingsActivity;
import diana.com.Profile.ProfileActivity;
import diana.com.R;
import diana.com.models.Comment;
import diana.com.models.Like;
import diana.com.models.Photo;
import diana.com.models.User;
import diana.com.models.UserAccountSettings;
import diana.com.models.UserSettings;

public class ViewProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    public interface OnGridImageSelectedListener {
        void onGridImageSelected(Photo photo, int activityNumber);

    }

    OnGridImageSelectedListener mOnGridImageSelectedListener;
    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS = 2;

    private TextView mPosts, mFollowers, mFollowing, mUsername, mWebsite, mDescription, mDisplayName,mFollow,mUnfollow;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private GridView gridView;
    private ImageView profilemenu,mBackArrow,mEllipses;
    private BottomNavigationViewEx bottomNavigationView;
    private Context mContext;
    private User mUser;
    private TextView editProfile;

    private FirebaseMethods mFirebaseMethods;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;


    private int mFollowersCount=0;
    private int mFollowingCount=0;
    private int mPostsCount =0;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_profile, container, false);
        mDisplayName = view.findViewById(R.id.display_name);
        mUsername = view.findViewById(R.id.username);
        mWebsite = view.findViewById(R.id.website);
        mDescription = view.findViewById(R.id.description);
        mProfilePhoto = view.findViewById(R.id.profile_photo);
        mPosts = view.findViewById(R.id.textViewposts);
        mFollowers = view.findViewById(R.id.textViewfollowers);
        mBackArrow=view.findViewById(R.id.backArrow);
        mFollowing = view.findViewById(R.id.textViewfollowing);
        mProgressBar = view.findViewById(R.id.profilepregressbar);
        gridView = view.findViewById(R.id.gridView);
        profilemenu = view.findViewById(R.id.profileMenu);
        mFollow=view.findViewById(R.id.follow);
        mUnfollow=view.findViewById(R.id.unfollow);
        editProfile = view.findViewById(R.id.textEditProfile);
        bottomNavigationView = view.findViewById(R.id.bottomNavView);
        mContext = getActivity();
        mFirebaseMethods = new FirebaseMethods(getActivity());
        mEllipses=view.findViewById(R.id.ivEllipses);


        try {

            mUser = getUserFromBundle();
            init();

        } catch (NullPointerException e) {
            Log.d(TAG, "onCreateView:NullPointerException " + e.getMessage());
            Toast.makeText(mContext, "something went wrong", Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().popBackStack();
        }
        setupBottonNavigationView();
        setupFirebaseAuth();
        isFollowing();
        getFollowingCount();
        getFollowersCount();
        getPostsCount();
        //setupGridView();

        mFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: now following"+mUser.getUsername());
                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mUser.getUser_id())
                        .child(getString(R.string.field_user_id))
                        .setValue(mUser.getUser_id());

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_followers))
                        .child(mUser.getUser_id())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(getString(R.string.field_user_id))
                        .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                setFollowing();
            }

        });
        mUnfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: now unfollowing"+mUser.getUsername());
                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mUser.getUser_id())
                        .removeValue();

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_followers))
                        .child(mUser.getUser_id())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .removeValue();
                setUnFollowing();
            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.profile_activity));
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            }
        });
        return view;
    }

    private void init() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query1 = reference.child(getString(R.string.dbname_user_account_settings)).orderByChild(getString(R.string.field_user_id)).equalTo(mUser.getUser_id());
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    UserSettings settings = new UserSettings();
                    settings.setUser(mUser);
                    settings.setSettings(singleSnapshot.getValue(UserAccountSettings.class));
                    setupProfileWidgets(settings);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference();
        Query query2 = reference2
                .child(getString(R.string.dbname_user_photos))
                .child(mUser.getUser_id());
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Photo> photos = new ArrayList<Photo>();

                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Photo photo = new Photo();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                    photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                    photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                    photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                    photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                    photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                    photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                    ArrayList<Comment> comments = new ArrayList<Comment>();
                    for (DataSnapshot dSnapshot : singleSnapshot
                            .child(getString(R.string.field_comments)).getChildren()) {
                        Comment comment = new Comment();
                        comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                        comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                        comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                        comments.add(comment);

                    }
                    photo.setComments(comments);

                    List<Like> likesList = new ArrayList<Like>();
                    for (DataSnapshot dSnapshot : singleSnapshot
                            .child(getString(R.string.field_likes)).getChildren()) {
                        Like like = new Like();
                        like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                        likesList.add(like);

                    }
                    photo.setLikes(likesList);
                    photos.add(photo);


                }
                setupImageGrid(photos);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    private void getFollowersCount(){
        mFollowersCount=0;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query= reference.child(getString(R.string.dbname_followers))
                .child(mUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    mFollowersCount++;
                }
                mFollowers.setText(String.valueOf(mFollowersCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getFollowingCount(){
        mFollowingCount=0;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query= reference.child(getString(R.string.dbname_following))
                .child(mUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    mFollowingCount++;
                }
                mFollowing.setText(String.valueOf(mFollowingCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getPostsCount(){
        mPostsCount=0;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query= reference.child(getString(R.string.dbname_user_photos))
                .child(mUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    mPostsCount++;
                }
                mPosts.setText(String.valueOf(mPostsCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void isFollowing(){
        setUnFollowing();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query= reference.child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByChild(getString(R.string.field_user_id)).equalTo(mUser.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    setFollowing();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setFollowing(){
        mFollow.setVisibility(View.GONE);
        mUnfollow.setVisibility(View.VISIBLE);
        editProfile.setVisibility(View.GONE);


    }

    private void setUnFollowing(){
        mFollow.setVisibility(View.VISIBLE);
        mUnfollow.setVisibility(View.GONE);
        editProfile.setVisibility(View.GONE);

    }

    private void setCurrentUsersProfile(){
        mFollow.setVisibility(View.GONE);
        mUnfollow.setVisibility(View.GONE);
        editProfile.setVisibility(View.VISIBLE);


    }
    private void setupImageGrid(final ArrayList<Photo> photos) {
        ///set up image grid
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth / NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        ArrayList<String> imgUrls = new ArrayList<String>();
        for (int i = 0; i < photos.size(); i++) {
            imgUrls.add(photos.get(i).getImage_path());
        }
        GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview, "", imgUrls);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mOnGridImageSelectedListener.onGridImageSelected(photos.get(i), ACTIVITY_NUM);
            }
        });
    }

    private User getUserFromBundle() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.intent_user));
        } else {
            return null;
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        try {
            mOnGridImageSelectedListener = (OnGridImageSelectedListener) getActivity();
        } catch (ClassCastException e) {
            Log.d(TAG, "onAttach:ClassCastException " + e.getMessage());
        }
        super.onAttach(context);
    }

//    private void setupGridView() {
//        final ArrayList<Photo> photos = new ArrayList<>();
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
//        Query query = reference
//                .child(getString(R.string.dbname_user_photos))
//                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
//                    Photo photo = new Photo();
//                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
//                    photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
//                    photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
//                    photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
//                    photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
//                    photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
//                    photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());
//
//                    ArrayList<Comment> comments = new ArrayList<Comment>();
//                    for (DataSnapshot dSnapshot : singleSnapshot
//                            .child(getString(R.string.field_comments)).getChildren()) {
//                        Comment comment = new Comment();
//                        comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
//                        comment.setComment(dSnapshot.getValue(Comment.class).getComment());
//                        comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
//                        comments.add(comment);
//
//                    }
//                    photo.setComments(comments);
//
//                    List<Like> likesList = new ArrayList<Like>();
//                    for (DataSnapshot dSnapshot : singleSnapshot
//                            .child(getString(R.string.field_likes)).getChildren()) {
//                        Like like = new Like();
//                        like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
//                        likesList.add(like);
//
//                    }
//                    photo.setLikes(likesList);
//                    photos.add(photo);
//
//
//                }
//                ///set up image grid
//                int gridWidth = getResources().getDisplayMetrics().widthPixels;
//                int imageWidth = gridWidth / NUM_GRID_COLUMNS;
//                gridView.setColumnWidth(imageWidth);
//
//                ArrayList<String> imgUrls = new ArrayList<String>();
//                for (int i = 0; i < photos.size(); i++) {
//                    imgUrls.add(photos.get(i).getImage_path());
//                }
//                GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview, "", imgUrls);
//                gridView.setAdapter(adapter);
//
//                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                        mOnGridImageSelectedListener.onGridImageSelected(photos.get(i), ACTIVITY_NUM);
//                    }
//                });
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }

    private void setupProfileWidgets(UserSettings userSettings) {
        Log.d(TAG, "setProfileWidgets: settings widgets with data retrieving form firebase database " + userSettings.toString());
        Log.d(TAG, "setProfileWidgets: settings widgets with data retrieving form firebase database " + userSettings.getSettings().getUsername());

        //User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();
        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");
        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mPosts.setText(String.valueOf(settings.getPosts()));
        mFollowers.setText(String.valueOf(settings.getFollowers()));
        mFollowing.setText(String.valueOf(settings.getFollowing()));
        mProgressBar.setVisibility(View.GONE);
        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().finish();
            }
        });


    }



    private void setupBottonNavigationView() {
        Log.d(TAG, "setupBottonNavigationView: set up BottonNavigationView");
        Bottom_Navigation_View_Helper.setupBottomNavigationView(bottomNavigationView);
        Bottom_Navigation_View_Helper.enableNavigation(mContext, getActivity(), bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
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
