package diana.com.Utilis;

import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import diana.com.AdmirersActivity;
import diana.com.R;
import diana.com.models.User;
import diana.com.models.UserAccountSettings;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class UserListAdapter extends ArrayAdapter<User>{

    private LayoutInflater mInflater;
    private List<User> mUsers=null;
    private int layoutResource;
    private Context mContext;

    public UserListAdapter(Context context, int resource, List<User> objects) {
        super(context, resource, objects);
        mContext=context;
        mInflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource=resource;
        this.mUsers=objects;
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(mContext));

    }


    private static class ViewHolder{
        TextView username,email;
        CircleImageView profileImage;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if(convertView==null){
            convertView=mInflater.inflate(layoutResource,parent,false);
            holder=new ViewHolder();

            holder.username=(TextView) convertView.findViewById(R.id.username);
            holder.profileImage=(CircleImageView) convertView.findViewById(R.id.profile_image);

            convertView.setTag(holder);

        }else {
            holder=(ViewHolder) convertView.getTag();
        }

        holder.username.setText(getItem(position).getUsername());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query =reference.child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: "+
                    singleSnapshot.getValue(UserAccountSettings.class).toString());

                    ImageLoader imageLoader=ImageLoader.getInstance();
                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),holder.profileImage);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return convertView;
    }
}
