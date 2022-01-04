package diana.com.Utilis;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import diana.com.R;
import diana.com.models.Comment;
import diana.com.models.UserAccountSettings;

public class CommentListAdapter extends ArrayAdapter<Comment> {
    private static final String TAG = "CommentListAdapter";

    private LayoutInflater mInflater;
    private int layoutResource;
    private Context mContext;
    public CommentListAdapter(Context context,
                              int resource, List<Comment> objects) {
        super(context, resource, objects);
        mInflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext=context;
        layoutResource=resource;

    }
    private static class ViewHolder{
        TextView comment,username,timestamp,reply,likes;
        CircleImageView profileImage;
        ImageView like;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if(convertView==null){
            convertView=mInflater.inflate(layoutResource,parent,false);
            holder=new ViewHolder();
            holder.comment=convertView.findViewById(R.id.comment);
            holder.username=convertView.findViewById(R.id.commenT_username);
            holder.timestamp=convertView.findViewById(R.id.comment_time_posted);
            holder.like=convertView.findViewById(R.id.comment_like);
            holder.likes=convertView.findViewById(R.id.comment_likes);
            holder.profileImage=convertView.findViewById(R.id.comment_profile_image);
            convertView.setTag(holder);
        }else {
            holder=(ViewHolder)convertView.getTag();
        }
        ///set the comment
        holder.comment.setText(getItem(position).getComment());
        ///set the timestamp difference
        String timestampDifference =getTimeStampDifference(getItem(position));
        if(!timestampDifference.equals("0")){
            holder.timestamp.setText(timestampDifference + " days");
        }else {
            holder.timestamp.setText("today");
        }
        //set the username
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    ImageLoader imageLoader=ImageLoader.getInstance();
                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo()
                    ,holder.profileImage);

                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        try {
            if(position==0){
                holder.like.setVisibility(View.GONE);
                holder.likes.setVisibility(View.GONE);
                holder.reply.setVisibility(View.GONE);
            }
        }catch (NullPointerException e){
            Log.d(TAG, "getView: ex"+e);
        }



        return convertView;
    }
        private String getTimeStampDifference(Comment comment) {
            String difference = "";
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getDefault());
            Date today = c.getTime();
            sdf.format(today);
            Date timestamp;
            final String phototimestamp = comment.getDate_created();
            try {
                timestamp = sdf.parse(phototimestamp);
                difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24)));
            } catch (ParseException e) {
                Log.d(TAG, "getTimeStampDifference:ParseException " + e.getMessage());
                difference = "0";
            }
            return difference;
        }


}
