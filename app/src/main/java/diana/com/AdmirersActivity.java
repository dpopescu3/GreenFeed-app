package diana.com;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import diana.com.Utilis.UserListAdapter;
import diana.com.models.User;

public class AdmirersActivity extends AppCompatActivity {
 String id;
 String title;
 List<String> idList;
 RecyclerView recyclerView;
 UserListAdapter userListAdapter;
 List<User> userlist;
 int resources;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admirers);
        Intent intent=getIntent();
        id=intent.getStringExtra("id");
        title=intent.getStringExtra("title");
        Toolbar toolbar=findViewById(R.id.toolbar4);
       setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        recyclerView=findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userlist=new ArrayList<>();
        userListAdapter=new UserListAdapter(this,resources,userlist);
//        recyclerView.setAdapter(userListAdapter);
        idList=new ArrayList<>();
        switch (title){
            case "likes":
                getLikes();
                break;
            case "following":
                getFollowing();
            case "followers":
                getFollowers();
                break;
        }




    }

    private void getLikes() {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("user_photos")
                .child(id).child("likes");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                idList.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    idList.add(snapshot.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getFollowing() {

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("following")
                .child(id).child("userid");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                idList.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    idList.add(snapshot.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getFollowers() {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("followers")
                .child(id).child("userid");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                idList.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    idList.add(snapshot.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setSupportActionBar(Toolbar toolbar) {
    }
 private void showUsers(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userlist.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    User user=snapshot.getValue(User.class);
                    for(String id: idList){
                        if(user.getUser_id().equals(id)){
                            userlist.add(user);
                        }
                    }
                }
                userListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
 }
}
