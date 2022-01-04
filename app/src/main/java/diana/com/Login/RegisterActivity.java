package diana.com.Login;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import diana.com.Fridge.FridgeActivity;
import diana.com.R;
import diana.com.Utilis.FirebaseMethods;
import diana.com.models.User;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private String email, username, password;
    private EditText mEmail, mPassword, mUsername;
    private TextView loadingPleaseWait;
    private Button btnRegister;
    private ProgressBar mProgressBar;
    private FirebaseMethods firebaseMethods;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String append;
    private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
        mContext = RegisterActivity.this;
        firebaseMethods = new FirebaseMethods(mContext);

        Log.d(TAG, "onCreate: started");

        initWidgets();
        setupFirebaseAuth();
        init();
    }

    private void init() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                email = mEmail.getText().toString();
                username = mUsername.getText().toString();
                password = mPassword.getText().toString();

                if (checkInput(email, username, password)) {

                    setLoading(true);

                    firebaseMethods.registerNewEmail(email, password, username, new FirebaseMethods.OnRegisterFinished() {
                        @Override
                        public void onSuccess() {
                            setLoading(false);
                            startActivity(new Intent(RegisterActivity.this, FridgeActivity.class));
                        }

                        @Override
                        public void onError(Exception exception) {
                            setLoading(false);
                            Toast.makeText(RegisterActivity.this, "Register failed."+exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private boolean checkInput(@Nullable String email, @Nullable String username, @Nullable String password) {
        Log.d(TAG, "checkInput: checking inputs for null values");

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "All fields must be filled out", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void initWidgets() {
        Log.d(TAG, "initWidgets:Initializing Widgets");

        mEmail = findViewById(R.id.input_email);
        mUsername = findViewById(R.id.input_username);
        btnRegister = findViewById(R.id.button_register);
        mProgressBar = findViewById(R.id.progressBar);
        loadingPleaseWait = findViewById(R.id.loadingPleaseWait);
        mPassword = findViewById(R.id.input_password);
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            mProgressBar.setVisibility(View.VISIBLE);
            loadingPleaseWait.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
            loadingPleaseWait.setVisibility(View.GONE);
        }
    }

    /**
     * Set up the firebase auth
     */
    /**
     * Check if @paran useranem already exists
     *
     * @param username
     */
    private void checkIfUsernameExists(final String username) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    if (singleSnapshot.exists()) {
                        Log.d(TAG, "onDataChange:Found a match " + singleSnapshot.getValue(User.class).getUsername());
                        append = myRef.push().getKey().substring(3, 10);
                        Log.d(TAG, "onDataChange: username is modified because it already exists");

                    }
                }

                String mUsername ="";
                if(append!=null){
                mUsername = username + append;}
                else {mUsername=username;}
                ///add new user to the database
                firebaseMethods.addNewUser(email, mUsername, "", "", "");
                mAuth.signOut();
                Intent intent= new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                Toast.makeText(RegisterActivity.this, "Sign up successful. Sending verification email", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

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
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            checkIfUsernameExists(username);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
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

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }


}
