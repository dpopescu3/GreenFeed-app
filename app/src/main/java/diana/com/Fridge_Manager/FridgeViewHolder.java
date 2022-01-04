package diana.com.Fridge_Manager;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import diana.com.R;

public class FridgeViewHolder extends RecyclerView.ViewHolder {

    ImageButton mDelete, mEdit;

    private TextView textProduct, textExpDate, textWeight, textQuantity, textBrand, textOrigin;
    private CardView productContainer;

    public FridgeViewHolder(@NonNull View itemView) {
        super(itemView);

        productContainer = itemView.findViewById(R.id.product_card);
        textProduct = itemView.findViewById(R.id.text_view_product_fridge);
        textExpDate = itemView.findViewById(R.id.text_view_expirationdate);
        textWeight = itemView.findViewById(R.id.text_view_weight);
        textQuantity = itemView.findViewById(R.id.text_view_quantity_fridge);
        textBrand = itemView.findViewById(R.id.text_view_brand);
        textOrigin = itemView.findViewById(R.id.text_origin_country);
        mDelete = itemView.findViewById(R.id.delete_red);
        mEdit = itemView.findViewById(R.id.edit);
    }

    void setProductName(String product) {
        textProduct.setText(product);
    }

    void setProductExpDate(String expDate) {
        textExpDate.setText(expDate);
    }

    void setProductWeight(String productWeight) {
        textWeight.setText(productWeight);
    }

    void setProductQuantity(String productQuantity) {
        textQuantity.setText(productQuantity);
    }

    void setProductBrand(String productBrand) {
        textBrand.setText(productBrand);
    }

    void setProductOriginCountry(String productOriginCountry) {
        textOrigin.setText(productOriginCountry);
    }

    void setProductExpirationWarning(int color) {
        productContainer.setBackgroundColor(color);
    }
}
