package diana.com.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import diana.com.R;
import diana.com.Utilis.FirebaseMethods;
import diana.com.Utilis.UniversalImageLoader;

public class NextActivity extends AppCompatActivity {

    private FirebaseMethods mFirebaseMethods;
    private String mAppend = "file:/";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private int imageCount = 0;
    private EditText mCaption;
    private String imgUrl;
    private Intent intent;
    private Bitmap bitmap;
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
        mFirebaseMethods = new FirebaseMethods(NextActivity.this);
        mCaption=findViewById(R.id.caption);
        setupFirebaseAuth();

        ImageView backArrow = findViewById(R.id.imageViewBackArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        TextView share = findViewById(R.id.tvShare);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //upload the image
                Toast.makeText(NextActivity.this, "Attempting to upload new photo", Toast.LENGTH_SHORT).show();
                String caption=mCaption.getText().toString();

                if(intent.hasExtra(getString(R.string.selected_image))){
                    imgUrl=intent.getStringExtra(getString(R.string.selected_image));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo),caption,imageCount,imgUrl,null);

                }else if(intent.hasExtra(getString(R.string.selected_bitmap))){
                    bitmap= intent.getParcelableExtra(getString(R.string.selected_bitmap));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo),caption,imageCount,null,bitmap);

                }

            }
        });
        setImage();
    }

    private void someMethod() {

        /**
         * STEP 1
         *Create a data model for photos
         *
         * STEP 2
         * Add properties to the Photo Objects(caption ,dete,imageURL,photo_id,tags,user_id)
         *
         *
         * STEP 3
         * Count the number the photos the user has
         *
         *
         * STEP 4
         * a)upload photo to Firebase Storage and insert two new nodes in the Firebase Database
         * b)insert  into ' photos' node
         * c)insert into ' user_photos' node
         */

    }


    /**
     * gets the image URL from the incoming intent and display the chosen image
     */
    private void setImage() {
        intent = getIntent();
        ImageView image = findViewById(R.id.imageShare);

        if(intent.hasExtra(getString(R.string.selected_image))){
            imgUrl=intent.getStringExtra(getString(R.string.selected_image));
            UniversalImageLoader.setImage(imgUrl, image, null, mAppend);

        }else if(intent.hasExtra(getString(R.string.selected_bitmap))){
            bitmap=intent.getParcelableExtra(getString(R.string.selected_bitmap));
            image.setImageBitmap(bitmap);
        }

}


    private void setupFirebaseAuth() {
        Log.d(String.valueOf(this), "setupFirebaseAuth:setting up firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        Log.d(String.valueOf(this), "onDataChange:image count "+imageCount);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //User is signed in
                    Log.d(String.valueOf(this), "onAuthStateChanged:" + user.getUid());
                } else {
                    ///User is signed out
                    Log.d(String.valueOf(this), "onAuthStateChanged:signed_out");
                }
            }
        };
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                imageCount = mFirebaseMethods.getImageCount(dataSnapshot);
                Log.d(String.valueOf(this), "onDataChange:image count "+imageCount);


            }



            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
