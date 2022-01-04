package diana.com.Share;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import diana.com.R;
import diana.com.Utilis.Bottom_Navigation_View_Helper;
import diana.com.Utilis.Permissions;
import diana.com.Utilis.SectionStatePagerAdapter;
import diana.com.Utilis.SectionsPagerAdapter;
import diana.com.Utilis.UniversalImageLoader;

public class ShareActivity extends AppCompatActivity {
    private static final String TAG = "ShareActivity";
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;
    private ViewPager mViewPager;
    private static final int ACTIVITY_NUM = 3;
    private Context mContext = ShareActivity.this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        Log.d(TAG, "onCreate: started");
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(ShareActivity.this));

        if (checkPermissionArray(Permissions.PERMISSIONS)) {
            setupViewPager();
        } else {
            verifyPermissions(Permissions.PERMISSIONS);
        }

        initImageLoader();
    }
    private void initImageLoader(){
        UniversalImageLoader universalImageLoader=new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }
public int getCurrentTabNumber(){
        return mViewPager.getCurrentItem();
}
    private void setupViewPager() {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new GalleryFragment());
        adapter.addFragment(new PhotoFragment());
        mViewPager = findViewById(R.id.viewpager_container);
        mViewPager.setAdapter(adapter);
        TabLayout tablayout = findViewById(R.id.tabsBottom);
        tablayout.setupWithViewPager(mViewPager);
        tablayout.getTabAt(0).setText(getString(R.string.gallery));
        tablayout.getTabAt(1).setText(getString(R.string.photo));

    }

    public int getTask(){
        return getIntent().getFlags();
    }

    public void verifyPermissions(String[] permissions) {
        ActivityCompat.requestPermissions(
                ShareActivity.this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );
    }

    /**
     * check an array of permissions
     *
     * @param permissions
     * @return
     */
    public boolean checkPermissionArray(String[] permissions) {
        for (int i = 0; i < permissions.length; i++) {
            String check = permissions[i];
            if (!checkPermissions(check)) {
                return false;
            }
        }
        return true;
    }

    /**
     * check single permission
     *
     * @param permission
     * @return
     */
    public boolean checkPermissions(String permission) {
        int permissionRequest = ActivityCompat.checkSelfPermission(ShareActivity.this, permission);
        if (permissionRequest != PackageManager.PERMISSION_GRANTED) {
            return false;

        } else {
            return true;
        }
    }

    private void setupBottonNavigationView() {
        Log.d(TAG, "setupBottonNavigationView: set up BottonNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavView);
        Bottom_Navigation_View_Helper.setupBottomNavigationView(bottomNavigationViewEx);
        Bottom_Navigation_View_Helper.enableNavigation(mContext,this,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

}
