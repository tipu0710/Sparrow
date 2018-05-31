package com.example.tsult.sparrow;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by tsult on 30/3/2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{


    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;
    private Context context;
    public MessageAdapter(List<Messages> mMessageList, Context context) {

        this.mMessageList = mMessageList;
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout ,parent, false);

        return new MessageViewHolder(v);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText, timeText,messageText1, timeText1,nametv, nametv1;
        public CircleImageView profileImage,profileImage1;
        public ImageView messageImage,messageImage1;

        public MessageViewHolder(View view) {
            super(view);
            messageText = view.findViewById(R.id.message_text_layout);
            profileImage = view.findViewById(R.id.message_profile_layout);
            messageImage = view.findViewById(R.id.message_image_layout);
            timeText = view.findViewById(R.id.time_text_layout);
            nametv = itemView.findViewById(R.id.message_name);

            messageText1 = view.findViewById(R.id.message_text_layout1);
            profileImage1 = view.findViewById(R.id.message_profile_layout1);
            messageImage1 = view.findViewById(R.id.message_image_layout1);
            timeText1 = view.findViewById(R.id.time_text_layout1);
            nametv1 = itemView.findViewById(R.id.message_name1);

        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {

        viewHolder.setIsRecyclable(false);
        Messages c = mMessageList.get(i);

        final String current_userId = mAuth.getCurrentUser().getUid();
        final String from_user = c.getFrom();
        final String message_type = c.getType();

        if (from_user.equals(current_userId)){
            
            viewHolder.messageText1.setBackgroundResource(R.drawable.message_text_background);
            viewHolder.messageText1.setTextColor(Color.WHITE);
            viewHolder.messageText.setVisibility(View.GONE);
            viewHolder.profileImage.setVisibility(View.GONE);
            viewHolder.timeText.setVisibility(View.GONE);
            viewHolder.nametv.setVisibility(View.GONE);
        }else {
            viewHolder.messageText.setBackgroundResource(R.drawable.message_text_background1);
            viewHolder.messageText.setTextColor(Color.BLACK);
            viewHolder.messageText1.setVisibility(View.GONE);
            viewHolder.profileImage1.setVisibility(View.GONE);
            viewHolder.timeText1.setVisibility(View.GONE);
            viewHolder.nametv1.setVisibility(View.GONE);
        }

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                if (from_user.equals(current_userId)){
                    Picasso.get().load(image).fit().centerCrop()
                            .placeholder(R.drawable.ic_face_black_48dp).into(viewHolder.profileImage1);

                }else {
                    Picasso.get().load(image).fit().centerCrop()
                            .placeholder(R.drawable.ic_face_black_48dp).into(viewHolder.profileImage);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(message_type.equals("text")) {
            if (from_user.equals(current_userId)){
                viewHolder.messageText1.setText(c.getMessage());
                viewHolder.nametv1.setText("You");
                long time = c.getTime();
                GetTimeAgo getTimeAgo = new GetTimeAgo();
                long lastTime = Long.parseLong(String.valueOf(time));
                String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, context);
                viewHolder.timeText1.setText(lastSeenTime);
            }else {
                viewHolder.messageText.setText(c.getMessage());
                viewHolder.nametv.setText(c.getName());
                long time = c.getTime();
                GetTimeAgo getTimeAgo = new GetTimeAgo();
                long lastTime = Long.parseLong(String.valueOf(time));
                String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, context);
                viewHolder.timeText.setText(lastSeenTime);
            }


        } else {

            viewHolder.messageText.setVisibility(View.INVISIBLE);
            /*Picasso.get().load(c.getMessage()).resize(100,100)
                    .placeholder(R.drawable.ic_face_black_48dp).into(viewHolder.messageImage);
*/
        }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }








}
