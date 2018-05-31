package com.example.tsult.sparrow.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tsult.sparrow.Friend_req;
import com.example.tsult.sparrow.ProfileActivity;
import com.example.tsult.sparrow.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private View mView;
    private RecyclerView requestList;
    private DatabaseReference mDatabaseRequest, mDatabase, mRootRef, mFriendRequestDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUser;
    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_request, container, false);
        requestList = mView.findViewById(R.id.request_list);
        requestList.setHasFixedSize(true);
        requestList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser().getUid();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mDatabaseRequest = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrentUser);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseRequest.keepSynced(true);
        mDatabase.keepSynced(true);
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friend_req, RequestViewHolder> adapter = new FirebaseRecyclerAdapter<Friend_req, RequestViewHolder>(
                Friend_req.class,
                R.layout.request_list_row,
                RequestViewHolder.class,
                mDatabaseRequest
        ) {
            @Override
            protected void populateViewHolder(final RequestViewHolder viewHolder, Friend_req request, int position) {
                final String request_type = request.getRequest_type();
                viewHolder.setType(request_type);

                final String frndUser = getRef(position).getKey();
                mDatabase.child(frndUser).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        viewHolder.setName(userName);
                        viewHolder.setThumbImage(userThumb);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (request_type.equals("received")){
                            requestReceived(mCurrentUser, frndUser);
                        }
                    }
                });

                viewHolder.cancleBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cancleRequest(mCurrentUser, frndUser);
                    }
                });
            }
        };
        requestList.setAdapter(adapter);
    }

    private static
    class RequestViewHolder extends RecyclerView.ViewHolder{
        CircleImageView profilePic, okBtn, cancleBtn;
        TextView mName, staus;

        public RequestViewHolder(View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.request_profile_pic);
            okBtn = itemView.findViewById(R.id.request_ok_button);
            cancleBtn = itemView.findViewById(R.id.request_cancle_btn);
            mName = itemView.findViewById(R.id.request_user_single_name);
            staus = itemView.findViewById(R.id.request_user_single_status);
        }

        public void setName(String userName) {
            mName.setText(userName);
        }

        public void setThumbImage(String userThumb) {
            Picasso.get().load(userThumb).placeholder(R.drawable.ic_face_black_48dp).into(profilePic);
        }

        public void setType(String request_type) {
            if (request_type.equals("received")){
                staus.setText("You have friend request");
            }else {
                staus.setText("You send friend request");
                okBtn.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void requestReceived(String mCurrentUser, String frndUser){
        final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

        Map friendsMap = new HashMap();
        friendsMap.put("Friends/" + mCurrentUser + "/" + frndUser + "/date", currentDate);
        friendsMap.put("Friends/" + frndUser + "/"  + mCurrentUser + "/date", currentDate);


        friendsMap.put("Friend_req/" + mCurrentUser + "/" + frndUser, null);
        friendsMap.put("Friend_req/" + frndUser + "/" + mCurrentUser, null);

        mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError == null){
                    Toast.makeText(getContext(), "Friend request accepted!", Toast.LENGTH_SHORT).show();
                } else {

                    String error = databaseError.getMessage();

                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void cancleRequest(final String mCurrentUser, final String frndUser){
        mFriendRequestDatabase.child(mCurrentUser).child(frndUser).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                mFriendRequestDatabase.child(frndUser).child(mCurrentUser).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Request cancled", Toast.LENGTH_SHORT).show();

                    }
                });

            }
        });
    }
}
