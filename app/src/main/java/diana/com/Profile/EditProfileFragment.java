package diana.com.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;
import diana.com.Dialogs.ConfirmPassworsDialog;
import diana.com.R;
import diana.com.Share.ShareActivity;
import diana.com.Utilis.FirebaseMethods;
import diana.com.Utilis.UniversalImageLoader;
import diana.com.models.User;
import diana.com.models.UserAccountSettings;
import diana.com.models.UserSettings;

public class EditProfileFragment extends Fragment implements
        ConfirmPassworsDialog.OnConfirmPasswordListener {

    @Override
    public void onConfirmPassword(String password) {
        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(mAuth.getCurrentUser().getEmail(), password);

        // Prompt the user to re-provide their sign-in credentials
        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
///check to see if the email is not already present in the database
                            Log.d(TAG, "User re-authenticated.");
                            mAuth.fetchSignInMethodsForEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                    if (task.isSuccessful()) {
                                        try {
                                            if (task.getResult().getSignInMethods().size() == 1) {
                                                Toast.makeText(getActivity(), "That email is already in use", Toast.LENGTH_SHORT).show();
                                            } else {
                                                ////the email is available to update
                                                mAuth.getCurrentUser().updateEmail(mEmail.getText().toString())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d(TAG, "User email address updated.");
                                                                    Toast.makeText(getActivity(), "Your email address is updated.", Toast.LENGTH_SHORT).show();
                                                                    mFirebaseMethods.updateEmail(mEmail.getText().toString());
                                                                    mFirebaseMethods.sendVerificationEmail();
                                                                }

                                                            }
                                                        });


                                            }
                                        } catch (NullPointerException e) {
                                        }
                                    }
                                }
                            });

                        } else {
                            Log.d(TAG, "onComplete:re-authenticated failed ");
                        }
                    }
                });

    }


    private static final String TAG = "EditProfileFragment";

    private FirebaseMethods mFirebaseMethods;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String userId;


    private CircleImageView mProfilePhoto;
    private EditText mDisplayName, mUsername, mWebsite, mDescription, mEmail;
    private TextView mChangeProfilePhoto;
    private UserSettings mUserSettings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        mDisplayName = view.findViewById(R.id.display_name);
        mUsername = view.findViewById(R.id.username);
        mWebsite = view.findViewById(R.id.webiste);
        mDescription = view.findViewById(R.id.description);
        mEmail = view.findViewById(R.id.email);
        mChangeProfilePhoto = view.findViewById(R.id.changeProfilePhoto);
        mFirebaseMethods = new FirebaseMethods(getActivity());

        //setProfileImage();
        setupFirebaseAuth();
        ///back arrow for navigating back to "ProfileActivity"
        ImageView backArrow = (ImageView) view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to Profile Activity");
                getActivity().finish();
            }
        });
        ImageView checkMark = view.findViewById(R.id.savechanges);
        checkMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProfileSettings();
            }
        });

        return view;
    }

    private void saveProfileSettings() {
        final String displayName = mDisplayName.getText().toString();
        final String username = mUsername.getText().toString();
        final String website = mWebsite.getText().toString();
        final String description = mDescription.getText().toString();
        final String email = mEmail.getText().toString();


        ///case 1: the user  changed their username
        if (!mUserSettings.getUser().getUsername().equals(username)) {
            checkIfUsernameExists(username);

        }///case2:the user changed their email
        if (!mUserSettings.getUser().getEmail().equals(email)) {
            ///step 1: Reauthenticate
            ///.confirm the password and email
            ConfirmPassworsDialog dialog = new ConfirmPassworsDialog();
            dialog.show(getFragmentManager(), getString(R.string.confirm_passwors_dialog));
            dialog.setTargetFragment(EditProfileFragment.this, 1);


            ///Step 2 check id the email already is registered
            // .fetchProviderForEmail(String email)


            //step3 change the email
            //.submit the new email to the database and auth
            /**
             * change the rest of the settings
             */
        }
            if (!mUserSettings.getSettings().getDisplay_name().equals(displayName)) {
                ///update displayname
                mFirebaseMethods.updateUserAccountSettings(displayName, null, null);
            }
            if (!mUserSettings.getSettings().getWebsite().equals(website)) {
                ///update website
                mFirebaseMethods.updateUserAccountSettings(null, website, null);
                Toast.makeText(getActivity(), "updated", Toast.LENGTH_SHORT).show();

            }
            if (!mUserSettings.getSettings().getDescription().equals(description)) {
                ///update description
                mFirebaseMethods.updateUserAccountSettings(null, null, description);

            }


        }





    /**
     * Check if @paran username already exists
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
                if (!dataSnapshot.exists()) {
                    //add the username
                    mFirebaseMethods.updateUsername(username);
                    Toast.makeText(getActivity(), "Saved username", Toast.LENGTH_SHORT).show();

                }
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    if (singleSnapshot.exists()) {
                        Log.d(TAG, "onDataChange:Found a match " + singleSnapshot.getValue(User.class).getUsername());
                        Toast.makeText(getActivity(), "That username already exists", Toast.LENGTH_SHORT).show();


                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Set up the firebase auth
     */
    private void setupProfileWidgets(UserSettings userSettings) {
        //Log.d(TAG, "setProfileWidgets: settings widgets with data retrieving form firebase database " + userSettings.toString());
        //Log.d(TAG, "setProfileWidgets: settings widgets with data retrieving form firebase database " + userSettings.getSettings().getUser_name());
        mUserSettings = userSettings;
        //User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();
        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");
        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mEmail.setText(userSettings.getUser().getEmail());
        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(), ShareActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });


    }


    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth:setting up firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        userId = mAuth.getCurrentUser().getUid();
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
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //retrieve user info from database
                setupProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));

                //retrieve image for the user in question
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
