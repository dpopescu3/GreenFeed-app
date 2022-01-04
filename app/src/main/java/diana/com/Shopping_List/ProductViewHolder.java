package diana.com.Shopping_List;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import diana.com.R;

public class ProductViewHolder extends RecyclerView.ViewHolder {
    View mView;
TextView textName,textTime,textWeight,textQuantity;
    CheckBox mCheckBox;
    ImageButton Search,Edit;
    public ProductViewHolder(@NonNull View itemView) {
        super(itemView);
        mView=itemView;
        textName=mView.findViewById(R.id.product_name);
        textTime=mView.findViewById(R.id.product_time);
        textWeight=mView.findViewById(R.id.product_weight);
        textQuantity=mView.findViewById(R.id.product_quantity);
        mCheckBox=mView.findViewById(R.id.checkBox);
        Search=mView.findViewById(R.id.checkavability);
        Edit=mView.findViewById(R.id.editproduct);

    }

    public void setProductName(String name){
        textName.setText(name);

    }
    public void setProductime(String time){
        textTime.setText(time);

    }
    public void setProductQuantity(String quantity){
        textQuantity.setText(quantity);

    }  public void setProductWeight(String weight){
        textWeight.setText(weight);

    }
}
