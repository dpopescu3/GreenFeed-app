package diana.com.Recipe_List;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import diana.com.R;

public class RecipeViewHolder extends RecyclerView.ViewHolder {

    View mView;

    TextView textTitle,textTime;
CardView recipeCard;

    public RecipeViewHolder(@NonNull View itemView) {
        super(itemView);
        mView=itemView;
        textTitle=mView.findViewById(R.id.recipe_title);
        textTime=mView.findViewById(R.id.recipe_time);
        recipeCard=mView.findViewById(R.id.recipe_card);
    }
    public void  setNoteTitle(String title){
        textTitle.setText(title);

    }
    public void setNoteTime(String time){
        textTime.setText(time);
    }


}
