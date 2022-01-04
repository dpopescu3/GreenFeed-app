package diana.com.Fridge;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import diana.com.Fridge_Manager.FridgeManagerActivity;
import diana.com.Fridge_Manager.NewProductActivity;
import diana.com.Login.LoginActivity;
import diana.com.R;
import diana.com.Recipe_List.Recipe;
import diana.com.Recipe_List.RecipeList;
import diana.com.Shopping_List.ShoppingListActivity;
import diana.com.Utilis.Bottom_Navigation_View_Helper;
import me.dm7.barcodescanner.zxing.ZXingScannerView;


@RequiresApi(api = Build.VERSION_CODES.M)
public class FridgeActivity<PERMISSION_GRANTED> extends AppCompatActivity {

    int REQUEST_CODE = 123;

    private static final String TAG = "FridgeActivity";
    private static final int ACTIVITY_NUM = 0;
    private Context mContext = FridgeActivity.this;
    private ZXingScannerView zXingScannerView;

    public TextView resultTextView;
    CardView button_scan,button_add;

    ///Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    public FridgeActivity() {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fridge);

        Log.d(TAG, "onCreate: started");

        setupBottomNavigationView();
        setupFirebaseAuth();

        CardView floatingActionButton = findViewById(R.id.product_card);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCreateRecipe();
            }
        });
        CardView floatingActionButton2 = findViewById(R.id.shoppinglistsection);

        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCreateShopping();
            }
        });

        CardView fridgeButton = findViewById(R.id.fridgesection);
        fridgeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openManager();
            }
        });

        resultTextView = findViewById(R.id.result_text);

        button_add=findViewById(R.id.addproductsection);
        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FridgeActivity.this, NewProductActivity.class));

            }
        });
    }

    public void openManager() {
        Intent intent = new Intent(this, FridgeManagerActivity.class);
        startActivity(intent);
    }

    public void openCreateShopping() {
        Intent intent = new Intent(this, ShoppingListActivity.class);
        startActivity(intent);
    }
    public void openCreateRecipe() {
        Intent intent = new Intent(this, Recipe.class);
        startActivity(intent);
    }

    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: set up BottonNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavView);
        Bottom_Navigation_View_Helper.setupBottomNavigationView(bottomNavigationViewEx);
        Bottom_Navigation_View_Helper.enableNavigation(mContext,this,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    /*
    ---------------Firebase------------------------
    */
    private void checkCurrentUser(FirebaseUser user) {
        Log.d(TAG, "CheckCurrentUser:checking if the user is logged in");
        if (user == null) {
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Set up the firebase auth
     */

    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth:setting up firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                checkCurrentUser(user);
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
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);

        checkCurrentUser(mAuth.getCurrentUser());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }


}


