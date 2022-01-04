package diana.com.Shopping_List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import diana.com.Recipe_List.RecipeList;

public class NewShoppingActivity extends AppCompatActivity {
    private Button btnCreate;
    CheckBox mCheckBox;
    private EditText etTitle, etWeight, etQuantity;
    private Toolbar mToolbar;
    private DatabaseReference fNotesDatabse;
    private FirebaseAuth fAuth;
    private String noteId = "";
    private boolean isExist;
    private FloatingActionButton mFloatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        setContentView(R.layout.activity_new_shopping);
        btnCreate = findViewById(R.id.new_note_button);
        etTitle = findViewById(R.id.new_product);
        etWeight = findViewById(R.id.new_product_weight);
        etQuantity = findViewById(R.id.new_product_quantity);
        mToolbar = findViewById(R.id.new_toolbar);
        mCheckBox = findViewById(R.id.checkBox);
        setSupportActionBar(mToolbar);
        fAuth = FirebaseAuth.getInstance();
        fNotesDatabse = FirebaseDatabase.getInstance().getReference().child("ShoppingList").child(fAuth.getCurrentUser().getUid());

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String product = etTitle.getText().toString().trim();
                String weight = etWeight.getText().toString().trim();
                String quantity = etQuantity.getText().toString().trim();
                if (!TextUtils.isEmpty(product)) {
                    addProduct(product, weight, quantity);
                    finish();
                } else {
                    Snackbar.make(view, "You have to add the name of the product", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        putData();
    }

    private void putData() {
        if (isExist) {
            fNotesDatabse.child(noteId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String product = dataSnapshot.child("product").getValue().toString();
                    String weight = dataSnapshot.child("weight").getValue().toString();
                    String quantity = dataSnapshot.child("quantity").getValue().toString();
                    etTitle.setText(product);
                    etWeight.setText(weight);
                    etQuantity.setText(quantity);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void addProduct(String product, String weight, String quantity) {
        if (fAuth.getCurrentUser() != null) {
            if (isExist) {
                //update Note
                Map updateMap = new HashMap();
                updateMap.put("product", etTitle.getText().toString().trim());
                updateMap.put("weight", etWeight.getText().toString().trim());
                updateMap.put("quantity", etQuantity.getText().toString().trim());
                updateMap.put("timestamp", ServerValue.TIMESTAMP);


                fNotesDatabse.child(noteId).updateChildren(updateMap);
            } else {
                final DatabaseReference newNoteRef = fNotesDatabse.push();
                final Map noteMap = new HashMap();
                noteMap.put("product", product);
                noteMap.put("weight", weight);
                noteMap.put("quantity", quantity);
                noteMap.put("timestamp", ServerValue.TIMESTAMP);
                Thread mainThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        newNoteRef.setValue(noteMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(NewShoppingActivity.this, "Product added to Database", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(NewShoppingActivity.this, "Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });
                mainThread.start();
            }
        } else {
            Toast.makeText(this, "User is not signed in", Toast.LENGTH_SHORT).show();
        }

    }


}
