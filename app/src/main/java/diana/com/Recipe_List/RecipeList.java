package diana.com.Recipe_List;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import diana.com.R;

//import android.widget.Toolbar;

public class RecipeList extends AppCompatActivity {
    private Button btnCreate;
    private ImageButton btnDelete;
    private EditText etTitle, etDescription;
    private FirebaseAuth fAuth;
    private DatabaseReference fRecipeDatabase;
    private Menu mainMenu;
    private String noteId="";
    private Toolbar mToolbar;
    private boolean isExist;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);
        try {
            noteId = getIntent().getStringExtra("noteId");
            //Toast.makeText(this, noteID, Toast.LENGTH_SHORT).show();

            if (!noteId.trim().equals("")) {
                isExist = true;
            } else {
                isExist = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        btnCreate = findViewById(R.id.new_recipe);
        etTitle = findViewById(R.id.new_recipe_title);
        etDescription = findViewById(R.id.new_recipe_description);
        mToolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fAuth = FirebaseAuth.getInstance();

        if (fAuth.getCurrentUser() != null) {
            fRecipeDatabase = FirebaseDatabase.getInstance().getReference().child("Recipe").child(fAuth.getCurrentUser().getUid());
        }

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = etTitle.getText().toString().trim();
                String description = etDescription.getText().toString().trim();
                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(description)) {
                    createRecipe(title, description);

                } else {
                    Snackbar.make(view, "Fill empty fields", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
putData();

    }
private  void putData(){
        if(isExist) {
            fRecipeDatabase.child(noteId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("title") && dataSnapshot.hasChild("description")) {
                        String title = dataSnapshot.child("title").getValue().toString();
                        String description = dataSnapshot.child("description").getValue().toString();
                        etTitle.setText(title);
                        etDescription.setText(description);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
}

    private void createRecipe(String title, String description) {
        if (fAuth.getCurrentUser() != null) {
            if (isExist) {
                ///Update Note
                Map updateMap = new HashMap();

                updateMap.put("title",etTitle.getText().toString().trim());
                updateMap.put("description",etDescription.getText().toString().trim());
                updateMap.put("timestamp",ServerValue.TIMESTAMP);
                fRecipeDatabase.child(noteId).updateChildren(updateMap);
                Toast.makeText(this, "Recipe updated", Toast.LENGTH_SHORT).show();
            } else {
                ///create a new note


                final DatabaseReference newRecipeRef = fRecipeDatabase.push();
                final Map recipeMap = new HashMap();
                recipeMap.put("title", title);
                recipeMap.put("description", description);
                recipeMap.put("timestamp", ServerValue.TIMESTAMP);
                Thread mainThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        newRecipeRef.setValue(recipeMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    Toast.makeText(RecipeList.this, "Recipe added", Toast.LENGTH_SHORT).show();

                                } else {
                                    Toast.makeText(RecipeList.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            }
                        });

                    }
                });
                mainThread.start();
            }

        } else {
            Toast.makeText(this, "User is not Signed In", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.new_recipe_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.new_note_delete_buuton:
                if (isExist) {
                    deleteRecipe();
                }else {
                    Toast.makeText(this, "Nothing to delete", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return true;
    }

    private void deleteRecipe() {
        fRecipeDatabase.child(noteId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(RecipeList.this, "Recipe Deleted", Toast.LENGTH_SHORT).show();
                    noteId = "no";
                    finish();
                } else {
                    Log.e("RecipeList", task.getException().toString());
                    Toast.makeText(RecipeList.this, "Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
}
