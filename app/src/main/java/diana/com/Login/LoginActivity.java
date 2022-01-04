package diana.com.Login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import diana.com.Fridge.FridgeActivity;
import diana.com.R;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private Context mContext;
    private ProgressBar mProgressBar;
    private EditText mEmail, mPassword;
    private TextView mPleaseWait;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mProgressBar = findViewById(R.id.progressBar);
        mPleaseWait = findViewById(R.id.pleasewait);
        mEmail = findViewById(R.id.input_email);
        mPassword = findViewById(R.id.input_password);
        mContext = LoginActivity.this;

        Log.d(TAG, "onCreate: started");

        setupFirebaseAuth();
        init();

    }

    private boolean isStringNull(String string) {
        Log.d(TAG, "StringNull:isStringNull:checking string if null ");
        if (string.equals("")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Set up the firebase auth
     */
    private void init() {
        Button btnlogin = findViewById(R.id.button_login);
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick:attempting to log in");
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                if (isStringNull(email) || isStringNull(password)) {
                    Toast.makeText(mContext, "You must fill out alll the fields", Toast.LENGTH_SHORT).show();
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mPleaseWait.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Log.d(TAG, "signInWithEmail:success" + task.isSuccessful());
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (task.isSuccessful()) {
                                        try {
                                            assert user != null;
                                            if (!user.isEmailVerified()) {
                                                Toast.makeText(mContext, "Your email is not verified.\n Please click the link from your email to verify your email", Toast.LENGTH_SHORT).show();
                                                mProgressBar.setVisibility(View.GONE);
                                                mPleaseWait.setVisibility(View.GONE);
                                                mAuth.signOut();
                                            } else {
                                                startActivity(new Intent(LoginActivity.this, FridgeActivity.class));
                                            }
                                        } catch (NullPointerException e) {
                                            Log.d(TAG, "onComplete:NullPointerException ");
                                        }

                                        //startActivity(new Intent(LoginActivity.this, FridgeActivity.class));
                                    } else {
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                        Toast.makeText(LoginActivity.this, getString(R.string.auth_failed),
                                                Toast.LENGTH_SHORT).show();
                                        mProgressBar.setVisibility(View.GONE);
                                        mPleaseWait.setVisibility(View.GONE);
                                    }
                                }
                            });
                }
            }
        });

        TextView linkSignUp = findViewById(R.id.link_signup);
        linkSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick:navigation to register screen");
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        /*
         If the user is logged in he can navigate to fridge activity
        */
        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(LoginActivity.this, FridgeActivity.class);
            startActivity(intent);
            finish();
        }
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
                    // TODO: go to home screen
                    Intent intent = new Intent(LoginActivity.this, FridgeActivity.class);
                    startActivity(intent);
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
