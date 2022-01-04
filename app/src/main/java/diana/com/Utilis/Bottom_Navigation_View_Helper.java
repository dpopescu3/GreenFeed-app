package diana.com.Utilis;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import diana.com.Fridge.FridgeActivity;
import diana.com.Home.MainActivity;
import diana.com.Profile.ProfileActivity;
import diana.com.R;
import diana.com.Search.SearchActivity;
import diana.com.Share.ShareActivity;

public class Bottom_Navigation_View_Helper {
    private static String TAG = "Bottom_NavigationView_Hel";

    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx) {
        Log.d(TAG, "setupBottomNavigationView: Setting up BottomNavigationView");


    }

    public static void enableNavigation(final Context context, final Activity callingActivity, BottomNavigationViewEx view) {
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.ic_home:
                        Intent intent1 = new Intent(context, FridgeActivity.class);
                        context.startActivity(intent1);
                        callingActivity.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                        break;
                    case R.id.ic_feed:
                        Intent intent2 = new Intent(context, MainActivity.class);
                        context.startActivity(intent2);
                        callingActivity.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);

                        break;
                    case R.id.ic_search:
                        Intent intent3 = new Intent(context, SearchActivity.class);
                        context.startActivity(intent3);
                        callingActivity.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);

                        break;
                    case R.id.ic_new:
                        Intent intent4 = new Intent(context, ShareActivity.class);
                        context.startActivity(intent4);
                        callingActivity.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                        break;
                    case R.id.ic_user:
                        Intent intent5 = new Intent(context, ProfileActivity.class);
                        context.startActivity(intent5);
                        callingActivity.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);

                        break;

                }

                return false;
            }
        });
    }
}

