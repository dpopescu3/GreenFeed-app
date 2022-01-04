package diana.com.Dialogs;

import android.content.Context;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.w3c.dom.Text;

import diana.com.R;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class ConfirmPassworsDialog extends DialogFragment {
    public interface  OnConfirmPasswordListener{
        public void onConfirmPassword(String password);

    }
    OnConfirmPasswordListener mOnConfirmPasswordListener;
    TextView mPassword;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_confirm_password, container, false);
mPassword=view.findViewById(R.id.confirm_password);
        TextView confirmDialog=view.findViewById(R.id.dialogConfirm);
        confirmDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password=mPassword.getText().toString();
                if(!password.equals("")) {
                    mOnConfirmPasswordListener.onConfirmPassword(password);
                    getDialog().dismiss();
                }else {
                    Toast.makeText(getActivity(), "You must enter a password", Toast.LENGTH_SHORT).show();
                }
            }
        });
        TextView cancelDialog=view.findViewById(R.id.dialogCancel);
        cancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });


        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mOnConfirmPasswordListener=(OnConfirmPasswordListener)getTargetFragment();
            
        }catch (ClassCastException e){
            Log.e(TAG, "onAttach:ClassException"+e.getMessage());
        }
    }
}
