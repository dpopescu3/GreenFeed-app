package diana.com.Fridge_Manager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import diana.com.R;
import diana.com.Shopping_List.NewShoppingActivity;

public class NewProductActivity extends AppCompatActivity {
    private Button buttonadd;
    protected static TextView displayCurrentTime;
    private EditText etProduct, etWeight, etQuantity, etBrand, etOriginCountry;
    DatePickerDialog dpd;
    Calendar c;
    private DatabaseReference fProductDatabase;
    private FirebaseAuth fAuth;
    private String noteId = "";
    private boolean isExist;

    private long expirationDateInMs;

    @SuppressLint("WrongViewCast")
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
        setContentView(R.layout.activity_new_product);
        buttonadd = findViewById(R.id.addfridge);
        etProduct = findViewById(R.id.productfridge);
        etWeight = findViewById(R.id.weightfridge);
        etQuantity = findViewById(R.id.quantityfridge);
        etBrand = findViewById(R.id.Brandfridge);
        etOriginCountry = findViewById(R.id.OriginCountryfridge);
        fAuth = FirebaseAuth.getInstance();
        fProductDatabase = FirebaseDatabase.getInstance().getReference().child("Products").child(fAuth.getCurrentUser().getUid());
        displayCurrentTime = findViewById(R.id.expirationdate_fridge);
        Button displayTimeButton = findViewById(R.id.datebutton);
        displayTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c = Calendar.getInstance();
                int day = c.get(Calendar.DAY_OF_MONTH);
                int month = c.get(Calendar.MONTH);
                int year = c.get(Calendar.YEAR);
                dpd = new DatePickerDialog(NewProductActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDateSet(DatePicker datePicker, int mYear, int mMonth, int mDay) {
                        Calendar expirationDate = Calendar.getInstance();
                        expirationDate.set(mYear, mMonth, mDay);
                        expirationDateInMs = expirationDate.getTimeInMillis();
                        displayCurrentTime.setText(mDay + "/" + (mMonth + 1) + "/" + mYear);

                    }
                }, day, month, year);
                dpd.show();


            }
        });

        buttonadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String product = etProduct.getText().toString().trim();
                String weight = etWeight.getText().toString().trim();
                String quantity = etQuantity.getText().toString().trim();
                String brand = etBrand.getText().toString().trim();
                String origin = etOriginCountry.getText().toString().trim();

                if (!TextUtils.isEmpty(product)) {
                    addProduct(product, weight, quantity, brand, origin);
                } else {
                    Snackbar.make(view, "Fill empty fields", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
putData();
    }
    private void putData() {
        if (isExist) {
            fProductDatabase.child(noteId).addValueEventListener(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String product = Objects.requireNonNull(dataSnapshot.child("product").getValue()).toString();
                    String weight = dataSnapshot.child("weight").getValue().toString();
                    String quantity = dataSnapshot.child("quantity").getValue().toString();
                    String brand = dataSnapshot.child("brand").getValue().toString();
                    String origin_country = dataSnapshot.child("origin country").getValue().toString();
                    String exp = dataSnapshot.child("expiration date").getValue().toString();
                    etProduct.setText(product);
                    etWeight.setText(weight);
                    etQuantity.setText(quantity);
                    etBrand.setText(brand);
                    etOriginCountry.setText(origin_country);
                    displayCurrentTime.setText(exp);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
    private void addProduct(String product, String weight, String quantity, String brand, String origin) {
        if (fAuth.getCurrentUser() != null) {
            if (isExist) {
                //update Note
                Map updateMap = new HashMap();
                updateMap.put("product", etProduct.getText().toString().trim());
                updateMap.put("expiration date", expirationDateInMs);
                updateMap.put("weight", etWeight.getText().toString().trim());
                updateMap.put("quantity", etQuantity.getText().toString().trim());
                updateMap.put("brand", etBrand.getText().toString().trim());
                updateMap.put("origin country", etOriginCountry.getText().toString().trim());

                fProductDatabase.child(noteId).updateChildren(updateMap);
                Toast.makeText(this, "Product updated", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                final DatabaseReference newNoteRef = fProductDatabase.push();
                final HashMap<String, String> prodMap = new HashMap<>();
                prodMap.put("product", product);
                prodMap.put("expiration date", String.valueOf(expirationDateInMs));
                prodMap.put("weight", weight);
                prodMap.put("quantity", quantity);
                prodMap.put("brand", brand);
                prodMap.put("origin country", origin);
                newNoteRef.setValue(prodMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(NewProductActivity.this, "Product added to Database", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(NewProductActivity.this, "Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } else {
            Toast.makeText(this, "User is not signed in", Toast.LENGTH_SHORT).show();
        }

    }

//    private void createProduct(String product, String expirationdate, String weight, String quantity, String brand, String origin) {
//
//        if (fAuth.getCurrentUser() != null) {
//            final DatabaseReference newProdRef = fProductDatabase.push();
//            final Map prodMap = new HashMap();
//            prodMap.put("product", product);
//            prodMap.put("expiration date", expirationdate);
//            prodMap.put("weight", weight);
//            prodMap.put("quantity", quantity);
//            prodMap.put("brand", brand);
//            prodMap.put("origin country", origin);
//            Thread mainThread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    newProdRef.setValue(prodMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//
//                            if (task.isSuccessful()) {
//                                Toast.makeText(NewProductActivity.this, "Note added to Database", Toast.LENGTH_SHORT).show();
//                            } else {
//                                Toast.makeText(NewProductActivity.this, "Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
//                }
//            });
//            mainThread.start();
//
//        } else {
//            Toast.makeText(this, "User is not signed in", Toast.LENGTH_SHORT).show();
//        }
//    }


}




