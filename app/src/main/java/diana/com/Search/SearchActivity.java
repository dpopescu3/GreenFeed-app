package diana.com.Search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import diana.com.Profile.ProfileActivity;
import diana.com.R;
import diana.com.Utilis.Bottom_Navigation_View_Helper;
import diana.com.Utilis.UserListAdapter;
import diana.com.models.User;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";
    private static final int ACTIVITY_NUM=2;

    //widgets
    private EditText mSearchParam;
    private ListView mListView;
    private List<User> mUserList;
    private UserListAdapter mAdapter;
    private Context mContext=SearchActivity.this;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mSearchParam=(EditText)findViewById(R.id.search);
        mListView=findViewById(R.id.listView);
        Log.d(TAG, "onCreate: started");
        hideSoftKeyBoard();
        setupBottonNavigationView();
        initTextListener();

    }
    private void initTextListener(){
        mUserList=new ArrayList<>();
        mSearchParam.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text=mSearchParam.getText().toString().toLowerCase(Locale.getDefault());
                SearchForMatch(text);
            }
        });
    }

    private void SearchForMatch(String keyword){
        mUserList.clear();
        if(keyword.length()==0){
        }else{
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query=reference.child(getString(R.string.dbname_users)).orderByChild(getString(R.string.field_username)).equalTo(keyword);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        mUserList.add(singleSnapshot.getValue(User.class));
                        updateUserList();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
    }}
    private void updateUserList(){

        mAdapter=new UserListAdapter(SearchActivity.this,R.layout.layout_user_listitem,mUserList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Intent intent =new Intent(SearchActivity.this, ProfileActivity.class);
                intent.putExtra(getString(R.string.calling_activity),getString(R.string.search_activity));
                intent.putExtra(getString(R.string.intent_user),mUserList.get(position));
                startActivity(intent);

            }
        });
    }
    private void hideSoftKeyBoard(){
        if(getCurrentFocus()!=null){
            InputMethodManager imm =(InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
        }
    }
    private void setupBottonNavigationView(){
        Log.d(TAG, "setupBottonNavigationView: set up BottonNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx=(BottomNavigationViewEx) findViewById(R.id.bottomNavView);
        Bottom_Navigation_View_Helper.setupBottomNavigationView(bottomNavigationViewEx);
        Bottom_Navigation_View_Helper.enableNavigation(mContext,this,bottomNavigationViewEx);
        Menu menu=bottomNavigationViewEx.getMenu();
        MenuItem menuItem=menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
