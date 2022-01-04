package diana.com.Fridge;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import diana.com.R;
import diana.com.Recipe_List.Recipe;
import diana.com.Shopping_List.ShoppingListActivity;

public class CreateShoppingorRecipe extends AppCompatActivity {
    private Button mButton;
    private Button ButtonRecipe;
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_shoppingor_recipe);
        mButton = findViewById(R.id.shoppinglist_button);
        ButtonRecipe = findViewById(R.id.recipe_button);


        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openShoppingList();

            }
        });

        ButtonRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRecipeList();

            }
        });
    }
            public  void openShoppingList(){
                Intent intent=new Intent(CreateShoppingorRecipe.this, ShoppingListActivity.class);
                startActivity(intent);
            }
    public  void openRecipeList(){
        Intent intent=new Intent(CreateShoppingorRecipe.this, Recipe.class);
        startActivity(intent);
    }
}

