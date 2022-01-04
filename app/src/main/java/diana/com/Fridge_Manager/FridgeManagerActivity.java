package diana.com.Fridge_Manager;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import diana.com.R;

public class FridgeManagerActivity extends AppCompatActivity {

    private static final long DAY_MS = 86400000;

    private DatabaseReference fFridgeDatabase;
//    SearchView mSearchView;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fridge__manager);
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        if (fAuth.getCurrentUser() != null) {
            fFridgeDatabase = FirebaseDatabase.getInstance().getReference().child("Products").child(fAuth.getCurrentUser().getUid());
        }
//        mSearchView = findViewById(R.id.searchView);
        RecyclerView fridgeProductsView = findViewById(R.id.recycler_fridge);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false);
        fridgeProductsView.setHasFixedSize(true);
        fridgeProductsView.setLayoutManager(gridLayoutManager);

        final FirebaseRecyclerAdapter adapter = getFridgeProductsAdapter();
        fridgeProductsView.setAdapter(adapter);

    }

    private FirebaseRecyclerAdapter getFridgeProductsAdapter() {
        return new FirebaseRecyclerAdapter<ProductFridgeModel, FridgeViewHolder>(
                ProductFridgeModel.class,
                R.layout.single_fridge_layout,
                FridgeViewHolder.class,
                fFridgeDatabase.orderByChild("expiration date")
        ) {

            @Override
            protected void populateViewHolder(final FridgeViewHolder fridgeViewHolder, ProductFridgeModel productFridgeModel, int position) {
                final String noteId = getRef(position).getKey();

                fFridgeDatabase.child(noteId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Object product = dataSnapshot.child("product").getValue();
                        if (product != null) {
                            fridgeViewHolder.setProductName(product.toString());
                        }

                        Object expirationDate = dataSnapshot.child("expiration date").getValue();
                        if (expirationDate != null && !TextUtils.isEmpty(expirationDate.toString())) {

                            setExpirationDateView(expirationDate, fridgeViewHolder);
                        }

                        Object weight = dataSnapshot.child("weight").getValue();
                        if (weight != null) {
                            fridgeViewHolder.setProductWeight(weight.toString());
                        }

                        Object quantity = dataSnapshot.child("quantity").getValue();
                        if (quantity != null) {
                            fridgeViewHolder.setProductQuantity(quantity.toString());
                        }

                        Object brand = dataSnapshot.child("brand").getValue();
                        if (brand != null) {
                            fridgeViewHolder.setProductBrand(brand.toString());
                        }

                        Object origin_country = dataSnapshot.child("origin country").getValue();
                        if (origin_country != null) {
                            fridgeViewHolder.setProductOriginCountry(origin_country.toString());
                        }
                        fridgeViewHolder.mDelete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                fFridgeDatabase.child(noteId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(FridgeManagerActivity.this, "Product Deleted", Toast.LENGTH_SHORT).show();

                                        } else {
                                            Log.e("RecipeList", task.getException().toString());
                                            Toast.makeText(FridgeManagerActivity.this, "Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });

                        fridgeViewHolder.mEdit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(FridgeManagerActivity.this, NewProductActivity.class);
                                intent.putExtra("noteId", noteId);
                                startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        new AlertDialog.Builder(FridgeManagerActivity.this)
                                .setTitle("Something went wrong")
                                .setMessage("Please try again later.")

                                .setPositiveButton(android.R.string.ok, null)
//                            .setNegativeButton(android.R.string.no, null)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                });
            }
        };
    }


    private void setExpirationDateView(Object expirationDate, FridgeViewHolder fridgeViewHolder) {
        long expirationDateLong = Long.parseLong(expirationDate.toString());

        Calendar expirationDateCalendar = Calendar.getInstance();
        expirationDateCalendar.setTimeInMillis(expirationDateLong);

        String displayableExpirationDate = expirationDateCalendar.get(Calendar.DAY_OF_MONTH) +
                "/" + (expirationDateCalendar.get(Calendar.MONTH) + 1) +
                "/" + expirationDateCalendar.get(Calendar.YEAR);

        long currentTime = System.currentTimeMillis();
        long difference = expirationDateLong - currentTime;

        if (difference < DAY_MS * 3 && difference > DAY_MS * 2) {
            fridgeViewHolder.setProductExpirationWarning(ContextCompat.getColor(FridgeManagerActivity.this, R.color.yellow_transparent));
        } else if (difference < DAY_MS * 2 && difference > DAY_MS) {
            fridgeViewHolder.setProductExpirationWarning(ContextCompat.getColor(FridgeManagerActivity.this, R.color.orange_transparent));
        } else if (difference < DAY_MS && difference > 0) {
            fridgeViewHolder.setProductExpirationWarning(ContextCompat.getColor(FridgeManagerActivity.this, R.color.red_transparent));
        } else if (difference < 0) {
            fridgeViewHolder.setProductExpirationWarning(ContextCompat.getColor(FridgeManagerActivity.this, R.color.red));

        } else {
            fridgeViewHolder.setProductExpirationWarning(ContextCompat.getColor(FridgeManagerActivity.this, R.color.transparent));
        }

        fridgeViewHolder.setProductExpDate(displayableExpirationDate);
    }

}



//    private void setUpRecycleView() {
//        Query query = fridgeRef.orderBy("expiration_date", Query.Direction.DESCENDING);
//        FirestoreRecyclerOptions<NoteFridgeProducts> options = new FirestoreRecyclerOptions.Builder<NoteFridgeProducts>()
//                .setQuery(query, NoteFridgeProducts.class)
//                .build();
//        adapter = new NoteAdapter_Products_Fridge(options);
//        RecyclerView recyclerView = findViewById(R.id.recycle_view2);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(adapter);
//
//    }
//    @Override
//    protected void onStart() {
//        super.onStart();
//        adapter.startListening();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        adapter.stopListening();
//    }

