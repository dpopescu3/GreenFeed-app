package diana.com.Recipe_List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import diana.com.GetTimeAgo;
import diana.com.R;

import static diana.com.R.id.main_new_recipe_button;
import static diana.com.R.id.title;

public class Recipe extends AppCompatActivity {
    private RecyclerView mRecipeList;
    private GridLayoutManager gridLayoutManager;
    private DatabaseReference fRecipeDatabase;
    private FirebaseAuth fAuth;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
        mRecipeList =(RecyclerView)findViewById(R.id.recipe_list);
        gridLayoutManager = new GridLayoutManager(this, 2,GridLayoutManager.VERTICAL, false);
        mRecipeList.setHasFixedSize(true);
        mRecipeList.setLayoutManager(gridLayoutManager);
       // gridLayoutManager.setReverseLayout(true);
       // gridLayoutManager.setStackFromEnd(true);
mRecipeList.addItemDecoration(new GridSpacingItemDecoration(2,dpTopPx(10),true));
        fAuth = FirebaseAuth.getInstance();



        if (fAuth.getCurrentUser() != null) {
            fRecipeDatabase = FirebaseDatabase.getInstance().getReference().child("Recipe").child(fAuth.getCurrentUser().getUid());
        }
        loadData();
    }

    @Override
    public void onStart() {
        super.onStart();
    }
private void loadData(){
    Query query=fRecipeDatabase.orderByChild("title");
    final FirebaseRecyclerAdapter<RecipeModel, RecipeViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<RecipeModel, RecipeViewHolder>(
            RecipeModel.class,
            R.layout.single_recipe_layout,
            RecipeViewHolder.class,
            query
    ) {
        @Override
        protected void populateViewHolder(final RecipeViewHolder recipeViewHolder, RecipeModel recipeModel, int position) {
            final String recipeId = getRef(position).getKey();
            fRecipeDatabase.child(recipeId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("title") && dataSnapshot.hasChild("timestamp")) {
                        String title = dataSnapshot.child("title").getValue().toString();
                        String timestamp = dataSnapshot.child("timestamp").getValue().toString();
                        recipeViewHolder.setNoteTitle(title);
                        //recipeViewHolder.setNoteTime(timestamp);
                        GetTimeAgo getTimeAgo=new GetTimeAgo();
                        recipeViewHolder.setNoteTime(getTimeAgo.getTimeAgo(Long.parseLong(timestamp),getApplicationContext()));
                        recipeViewHolder.recipeCard.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Recipe.this, RecipeList.class);
                                intent.putExtra("noteId", recipeId);
                                startActivity(intent);
                            }
                        });

                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

    };
    mRecipeList.setAdapter(firebaseRecyclerAdapter);


}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_recipe, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case main_new_recipe_button:
                Intent newIntent = new Intent(Recipe.this, RecipeList.class);
                startActivity(newIntent);
                break;
        }
        return true;
    }


    private  int dpTopPx(int dp){
        Resources r=getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,r.getDisplayMetrics()));
    }


}
