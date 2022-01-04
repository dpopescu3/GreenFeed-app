package diana.com.Shopping_List;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import diana.com.Fridge_Manager.FridgeManagerActivity;
import diana.com.GetTimeAgo;
import diana.com.R;
import diana.com.Recipe_List.GridSpacingItemDecoration;

public class ShoppingListActivity extends AppCompatActivity {

    private RecyclerView mShoppingList;
    private DatabaseReference fProductDatabase;
    private DatabaseReference fNotesDatabase;
    private DatabaseReference fFridgeDatabase;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);
        mShoppingList = findViewById(R.id.shopping_list2);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false);
        mShoppingList.setHasFixedSize(true);
        mShoppingList.setLayoutManager(gridLayoutManager);
        mShoppingList.addItemDecoration(new GridSpacingItemDecoration(1, dpTopPx(10), true));
        FirebaseAuth fAuth = FirebaseAuth.getInstance();

        if (fAuth.getCurrentUser() != null) {
            fNotesDatabase = FirebaseDatabase.getInstance().getReference().child("ShoppingList").child(fAuth.getCurrentUser().getUid());
        }

        if (fAuth.getCurrentUser() != null) {
            fProductDatabase = FirebaseDatabase.getInstance().getReference().child("ShoppingList").child(fAuth.getCurrentUser().getUid());
        }

        if (fAuth.getCurrentUser() != null) {
            fFridgeDatabase = FirebaseDatabase.getInstance().getReference().child("Products").child(fAuth.getCurrentUser().getUid());
        }

        FloatingActionButton mFloatingActionButton = findViewById(R.id.button_add_note);

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                addNotification();
                openNewProduct();

            }
        });

        loadData();
    }

    @Override
    public void onStart() {
        super.onStart();

    }
//    private void addNotification(){
//        NotificationCompat.Builder builder=new NotificationCompat.Builder( this)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentTitle("Your produs has expired")
//                .setContentText("Go to your fridge and delete it");
//        Intent notificationIntent = new Intent(this, ShoppingListActivity.class);
//        PendingIntent contentIntent =PendingIntent.getActivity(this,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
//        builder.setContentIntent(contentIntent);
//        NotificationManager manager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        manager.notify(0,builder.build());
//
//    }

    public void loadData() {
        Query query = fProductDatabase.orderByChild("product");
        FirebaseRecyclerAdapter<ProductModel, ProductViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ProductModel, ProductViewHolder>(
                ProductModel.class,
                R.layout.single_product_layout,
                ProductViewHolder.class,
                query
        ) {
            @Override
            protected void populateViewHolder(final ProductViewHolder productViewHolder, ProductModel productModel, int i) {
                final String noteId = getRef(i).getKey();
                fProductDatabase.child(noteId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final Object product = dataSnapshot.child("product").getValue();
                        if (product != null) {
                            productViewHolder.setProductName(product.toString());
                        }

                        Object weight = dataSnapshot.child("weight").getValue();
                        if (weight != null) {
                            productViewHolder.setProductWeight(weight.toString());
                        }

                        Object quantity = dataSnapshot.child("quantity").getValue();
                        if (quantity != null) {
                            productViewHolder.setProductQuantity(quantity.toString());
                        }

                        Object timestamp = dataSnapshot.child("timestamp").getValue();
                        if (timestamp != null) {
                            GetTimeAgo getTimeAgo = new GetTimeAgo();
                            productViewHolder.setProductime(getTimeAgo.getTimeAgo(Long.parseLong(timestamp.toString()), ShoppingListActivity.this));
                            productViewHolder.mCheckBox.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    fNotesDatabase.child(noteId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(ShoppingListActivity.this, "Product Deleted", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Log.e("RecipeList", task.getException().toString());
                                                Toast.makeText(ShoppingListActivity.this, "Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }

                        productViewHolder.Edit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(ShoppingListActivity.this, NewShoppingActivity.class);
                                intent.putExtra("noteId", noteId);
                                startActivity(intent);
                            }
                        });

                        productViewHolder.Search.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (product == null) {
                                    return;
                                }

                                queryFridgeDatabase(product.toString(), noteId);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        mShoppingList.setAdapter(firebaseRecyclerAdapter);
    }

    private void queryFridgeDatabase(final String desiredProduct, final String noteId) {
        fFridgeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                boolean exists = false;

                for (DataSnapshot product : dataSnapshot.getChildren()) {
                    if (product.hasChild("product")) {

                        if (desiredProduct.equals(product.child("product").getValue())) {
                            exists = true;
                            new AlertDialog.Builder(ShoppingListActivity.this)
                                    .setTitle("Already owned")
                                    .setMessage("You already have " + desiredProduct + " in your fridge.")
                                    .setPositiveButton(android.R.string.ok, null)
                                    .setNegativeButton("Delete Product", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                         fProductDatabase.child(noteId).removeValue();
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    }
                }

                if (!exists) {
                    new AlertDialog.Builder(ShoppingListActivity.this)
                            .setTitle("Not owned")
                            .setMessage("You can buy " + desiredProduct + ".")
                            .setPositiveButton(android.R.string.ok, null)
                            .setIcon(android.R.drawable.ic_input_add)
                            .show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                new AlertDialog.Builder(ShoppingListActivity.this)
                        .setTitle("Something went wrong")
                        .setMessage("Please try again later.")

                        .setPositiveButton(android.R.string.ok, null)
//                            .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }


    public void openNewProduct() {
        Intent intent = new Intent(this, NewShoppingActivity.class);
        startActivity(intent);
    }

    private int dpTopPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
}







