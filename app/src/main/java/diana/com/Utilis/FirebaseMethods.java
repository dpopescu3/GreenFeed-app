package diana.com.Utilis;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import diana.com.Home.MainActivity;
import diana.com.Profile.AccountSettingsActivity;
import diana.com.R;
import diana.com.models.Photo;
import diana.com.models.User;
import diana.com.models.UserAccountSettings;
import diana.com.models.UserSettings;

public class FirebaseMethods {

    private static final String TAG = "FirebaseMethods";
    private String userID;
    private Context mContext;
    private double mPhotoUploadProgress = 0;
    private StorageReference mStorageReference;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    public FirebaseMethods(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mContext = context;
        if (mAuth.getCurrentUser() != null) {
            userID = mAuth.getCurrentUser().getUid();
        }

    }


    public void uploadNewPhoto(String photoType, final String caption, final int count, final String imgUrl,
                               Bitmap bm) {
        Log.d(TAG, "uploadNewPhoto: new photo uploaded");
        FilePath filePath = new FilePath();

        ///case 1 new photo
        if (photoType.equals(mContext.getString(R.string.new_photo))) {
            Log.d(TAG, "uploadNewPhoto: uploading new photo");
            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

            StorageReference storageReference = mStorageReference
                    .child(filePath.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo" + (count + 1));

            //convert image url to bitmap
            if(bm==null){
                bm = ImageManager.getBitmap(imgUrl);
            }

            byte[] bytes = ImageManager.getBytefromBitmap(bm, 100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri firebaseUrl = taskSnapshot.getDownloadUrl();
                    Toast.makeText(mContext, "photo upload success", Toast.LENGTH_SHORT).show();


                    ///add the new photo node to the user photo node
                    addPhotoToDatabase(caption,firebaseUrl.toString());


                    ///navigate to the main feed so the user sees their photo
                    Intent intent =new Intent(mContext, MainActivity.class);
                    mContext.startActivity(intent);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: photo upload failed");
                    Toast.makeText(mContext, "photo upload failed", Toast.LENGTH_SHORT).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if (progress - 15> mPhotoUploadProgress) {

                        Toast.makeText(mContext, "photo upload progress" + String.format("%.0f", progress)+"%", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onProgress: show"+progress);
                        mPhotoUploadProgress = progress;
                    }
                    Log.d(TAG, "onProgress: upload progress"+progress+ "% done");
                }
            });

        } else if (photoType.equals(mContext.getString(R.string.profile_photo))) {
            Log.d(TAG, "uploadNewPhoto: uploading new profile photo");


            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

            StorageReference storageReference = mStorageReference
                    .child(filePath.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo" );

            //convert image url to bitmap

            if(bm==null){
                bm = ImageManager.getBitmap(imgUrl);
            }
            byte[] bytes = ImageManager.getBytefromBitmap(bm, 100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri firebaseUrl = taskSnapshot.getDownloadUrl();
                    Toast.makeText(mContext, "photo upload success", Toast.LENGTH_SHORT).show();


                  ///insert into the user_account settings
                    setProfilePhoto(firebaseUrl.toString());
                    ((AccountSettingsActivity)mContext).setViewPager(
                            ((AccountSettingsActivity)mContext).pagerAdapter
                                    .getfragmentNumber(mContext.getString(R.string.edit_profile_fragment))
                    );



                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: photo upload failed");
                    Toast.makeText(mContext, "photo upload failed", Toast.LENGTH_SHORT).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if (progress - 15> mPhotoUploadProgress) {

                        Toast.makeText(mContext, "photo upload progress" + String.format("%.0f", progress)+"%", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onProgress: show"+progress);
                        mPhotoUploadProgress = progress;
                    }
                    Log.d(TAG, "onProgress: upload progress"+progress+ "% done");
                }
            });

        }



        ///case 2 new profile photo
    }

    private void setProfilePhoto(String url){
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(url);

    }

    private String getTimeStamp(){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date());
    }

    private void addPhotoToDatabase(String caption,String url){
        Log.d(TAG, "addPhotoToDatabase: sending photo to database");
        String tags =StringManipulation.getTags(caption);
        String newPhotoKey =myRef.child(mContext.getString(R.string.dbname_photos)).push().getKey();
        Photo photo =new Photo();
        photo.setCaption(caption);
        photo.setDate_created(getTimeStamp());
        photo.setImage_path(url);
        photo.setTags(tags);
        photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        photo.setPhoto_id(newPhotoKey);


        ///insert into database
        myRef.child(mContext.getString(R.string.dbname_user_photos)).child(FirebaseAuth.getInstance().getCurrentUser()
                .getUid()).child(newPhotoKey).setValue(photo);
        myRef.child(mContext.getString(R.string.dbname_photos)).child(newPhotoKey).setValue(photo);
    }



    public int getImageCount(DataSnapshot dataSnapshot) {
        int count = 0;
        for (DataSnapshot ds : dataSnapshot
                .child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .getChildren()) {
            count++;
        }
        return count;
    }


    public void updateUserAccountSettings(String displayName, String website, String description) {
        if (displayName != null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_display_name))
                    .setValue(displayName);
        }


        if (website != null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_website))
                    .setValue(website);
        }

        if (description != null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_description))
                    .setValue(description);

        }

    }

    public void updateUsername(String username) {
        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
    }

    public void updateEmail(String email) {
        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_email))
                .setValue(email);

    }

//    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//    public boolean checkifUsernameExists(String username, DataSnapshot dataSnapshot) {
//        Log.d(TAG, "checkifUsernameExists: if username already exists");
//        User user = new User();
//        for (DataSnapshot ds : dataSnapshot.child(userID).getChildren()) {
//            Log.d(TAG, "checkifUsernameExists: datesnapshot" + ds);
//            user.setUsername(Objects.requireNonNull(ds.getValue(User.class)).getUser_name());
//            if (StringManipulation.expandUsername(user.getUser_name()).equals(username)) {
//                Log.d(TAG, "checkifUsernameExists: found match");
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * This method registers a user with Firebase
     * </p>
     *
     * @param email    - the email of the user
     * @param password
     * @param username
     */
    public void registerNewEmail(final String email, String password, final String username, final OnRegisterFinished onRegisterFinished) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // TODO: error handling (e.g. email in use, password not strong enough, etc)
                        if (task.isSuccessful()) {
                            //send verif email
                            sendVerificationEmail();
                            userID = mAuth.getCurrentUser().getUid();
                            // Sign in success, update UI with the signed-in user's information
                            updateProfileWithUsername(mAuth.getCurrentUser(), username);
                            onRegisterFinished.onSuccess();
                        } else {
                            // If sign in fails, display a message to the user.
                            onRegisterFinished.onError(task.getException());
                        }
                    }
                });
    }

    public void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                            } else {
                                Toast.makeText(mContext, "Couldn't send verification email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    /**
     * Add info to th euser nodes
     * Add info to the user_account_settings_node
     *
     * @param email
     * @param username
     * @param description
     * @param website
     * @param profile_photo
     */

    public void addNewUser(String email, String username, String description, String website, String profile_photo) {
        User user = new User(userID, StringManipulation.condenseUsername(username), email);
        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .setValue(user);

        UserAccountSettings settings = new UserAccountSettings(
                description,
                username,
                0,
                0,
                0,
                profile_photo,
                StringManipulation.condenseUsername(username)
                , website,
                userID
        );
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .setValue(settings);
    }

    /**
     * Retrieves the account settings for thr user currently logged in
     * Database: user_account_Settings_node
     *
     * @param dataSnapshot
     * @return
     */
    public UserSettings getUserSettings(DataSnapshot dataSnapshot) {
        Log.d(TAG, "getUserAccountSettings: retrieving user account settings from database");
        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            //user_account_settings node
            if (ds.getKey().equals(mContext.getString(R.string.dbname_user_account_settings))) {
                Log.d(TAG, "getUserAccountSettings: datasnapshot" + ds);
                try {


                    settings.setDisplay_name(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDisplay_name()

                    );
                    settings.setUsername(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getUsername()

                    );
                    settings.setWebsite(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getWebsite()

                    );
                    settings.setDescription(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDescription()

                    );
                    settings.setProfile_photo(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getProfile_photo()

                    );
                    settings.setPosts(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getPosts()

                    );
                    settings.setFollowers(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowers()

                    );
                    settings.setFollowing(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowing()

                    );
                } catch (NullPointerException e) {
                    Log.d(TAG, "getUserAccountSettings:NullPointerException " + e.getMessage());
                }
//user_account_settings node

            }
            if (ds.getKey().equals(mContext.getString(R.string.dbname_users))) {
                Log.d(TAG, "getUserAccountSettings: datasnapshot" + ds);
                user.setUsername(
                        ds.child(userID)
                                .getValue(User.class)
                                .getUsername()

                );
                user.setEmail(
                        ds.child(userID)
                                .getValue(User.class)
                                .getEmail()

                );
                user.setUser_id(
                        ds.child(userID)
                                .getValue(User.class)
                                .getUser_id()

                );

            }
        }
        return new UserSettings(user, settings);
    }


    private void updateProfileWithUsername(@Nullable FirebaseUser currentUser, String username) {

        if (currentUser == null) {
            return;
        }

        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();

        currentUser.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    //success on updating user profile
                    // TODO: treat this if necessary
                } else {
                    //failed on updating user profile
                    // TODO: treat this if necessary
                }
            }
        });
    }


    public interface OnRegisterFinished {

        void onSuccess();

        void onError(Exception exception);
    }


}

