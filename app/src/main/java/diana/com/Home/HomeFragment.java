package diana.com.Home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import diana.com.R;
import diana.com.Utilis.MainfeedListAdapter;
import diana.com.models.Comment;
import diana.com.models.Like;
import diana.com.models.Photo;
import diana.com.models.UserAccountSettings;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private ArrayList<Photo> mPhotos;
    private ArrayList<String> mFollowing;
    private ListView mListView;
    private MainfeedListAdapter mAdapter;
    private int mResults;
    private ArrayList<Photo> mPaginatedPhotos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mListView = (ListView) view.findViewById(R.id.listView);
        mFollowing = new ArrayList<>();
        mPhotos = new ArrayList<>();
        getFollowing();

        return view;
    }

    private void getFollowing() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    mFollowing.add(singleSnapshot.child(getString(R.string.field_user_id)).getValue().toString());

                }
                mFollowing.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                getPhoto();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getPhoto() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        for (int i = 0; i < mFollowing.size(); i++) {
            final int count = i;

            Query query = reference
                    .child(getString(R.string.dbname_user_photos))
                    .child(mFollowing.get(i))
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(mFollowing.get(i));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
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
                        mPhotos.add(photo);

                    }

                    if (count >= mFollowing.size() - 1) {
                        //display photos
                        displayPhotos();
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

    }

    private void displayPhotos() {
        mPaginatedPhotos = new ArrayList<>();

        if (mPhotos != null) {
            try {
                Collections.sort(mPhotos, new Comparator<Photo>() {
                    @Override
                    public int compare(Photo photo, Photo t1) {
                        return t1.getDate_created().compareTo(photo.getDate_created());
                    }
                });
                int iterations = mPhotos.size();
                if(iterations>10){
                    iterations=10;
                }
                mResults=10;
                for(int i=0;i<iterations;i++){
                    mPaginatedPhotos.add(mPhotos.get(i));
                }
                mAdapter = new MainfeedListAdapter(getActivity(), R.layout.layout_main_feed_listitem, mPaginatedPhotos);
                mListView.setAdapter(mAdapter);
            } catch (NullPointerException e) {
                Log.d(TAG, "displayPhotos:NullPointerException " + e.getMessage());
            } catch (IndexOutOfBoundsException e) {

            }
        }
    }
    public void displayMorePhoto(){
        try {

            if(mPhotos.size()>mResults&&mPhotos.size()>0){
                int iterations;
                if(mPhotos.size()>(mResults+10)){
                    iterations=10;

                }else {
                    iterations =mPhotos.size()-mResults;
                }

                ///add the new photos
                for(int i=mResults;i<mResults+iterations;i++){
                    mPaginatedPhotos.add(mPhotos.get(i));
                }
                mResults=mResults+iterations;
                mAdapter.notifyDataSetChanged();

            }

        }  catch (NullPointerException e) {
        Log.d(TAG, "displayPhotos:NullPointerException " + e.getMessage());
    } catch (IndexOutOfBoundsException e) {
            Log.d(TAG, "displayPhotos:IndexOutOfBoundsException " + e.getMessage());

    }
    }
}
