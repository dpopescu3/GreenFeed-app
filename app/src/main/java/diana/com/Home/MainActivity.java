package diana.com.Home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

import diana.com.R;
import diana.com.Utilis.Bottom_Navigation_View_Helper;
import diana.com.Utilis.MainfeedListAdapter;
import diana.com.Utilis.SectionsPagerAdapter;
import diana.com.Utilis.UniversalImageLoader;
import diana.com.Utilis.ViewCommentsFragment;
import diana.com.models.Photo;
import diana.com.models.UserAccountSettings;

public class MainActivity extends AppCompatActivity implements MainfeedListAdapter.OnLoadMoreItemsListener {
    private static final String TAG = "MainActivity";
    private static final int ACTIVITY_NUM=1;

    private Context mContext=MainActivity.this;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private ViewPager mViewPager;
    private FrameLayout mFrameLayout;
    private RelativeLayout mRelativeLayout;
    private static final int HOME_FRAGMENT =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: starting");
        mViewPager=findViewById(R.id.viewpager_container);
        mFrameLayout=findViewById(R.id.container);
        mRelativeLayout=findViewById(R.id.relllayoutParent);

        setupFirebaseAuth();
        initImageLoader();
        setupBottonNavigationView();
        setupViewPager();

    }

    public  void onCommentThreadSelected(Photo photo,String callingActivity){
        ViewCommentsFragment fragment=new ViewCommentsFragment();
        Bundle args=new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putString(getString(R.string.main_activity),getString(R.string.main_activity));
        fragment.setArguments(args);

        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container ,fragment);
        transaction.addToBackStack(getString(R.string.view_comment_fragment));
        transaction.commit();
    }

    public void  hideLayout(){
        mRelativeLayout.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.VISIBLE);
    }
    public void  showLayout(){
        mRelativeLayout.setVisibility(View.VISIBLE);
        mFrameLayout.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mFrameLayout.getVisibility()==View.VISIBLE){
            showLayout();

        }
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader=new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }
/*
Resposible for ading the 3 tabs : Camera,Home,MessagesF;
 */
    private  void setupViewPager(){
        SectionsPagerAdapter adapter=new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new HomeFragment());
        mViewPager.setAdapter(adapter);
        TabLayout tabLayout =(TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
//        tabLayout.getTabAt(0).setIcon(R.drawable.);
    }


    private void setupBottonNavigationView(){
        Log.d(TAG, "setupBottonNavigationView: set up BottonNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx=(BottomNavigationViewEx) findViewById(R.id.bottomNavView);
        bottomNavigationViewEx.enableAnimation(true);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);
        Bottom_Navigation_View_Helper.setupBottomNavigationView(bottomNavigationViewEx);
        Bottom_Navigation_View_Helper.enableNavigation(mContext,this,bottomNavigationViewEx);
        Menu menu=bottomNavigationViewEx.getMenu();
        MenuItem menuItem=menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }


    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth:setting up firebase auth");
        mAuth = FirebaseAuth.getInstance();
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
        mViewPager.setCurrentItem(HOME_FRAGMENT);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void onLoadMoreItemsListener() {
        HomeFragment fragment=(HomeFragment)getSupportFragmentManager()
                .findFragmentByTag("android: switcher" +R.id.viewpager_container+ ":"+mViewPager.getCurrentItem());
        if(fragment!=null){
            fragment.displayMorePhoto();
        }
    }
}
