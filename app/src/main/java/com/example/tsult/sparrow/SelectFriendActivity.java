package com.example.tsult.sparrow;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SelectFriendActivity extends AppCompatActivity {

    private Toolbar friendToolBar;
    private RecyclerView friendList;

    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private ArrayList<checkItem> checkItems;
    private FriendSelectAdapter adapter;

    private FloatingActionButton nextBtn;
    private boolean fromGroupChat = false;
    private String mGroupId, groupName;

    private DatabaseReference mRootRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friend);

        friendToolBar = findViewById(R.id.select_friend_app_bar);
        nextBtn = findViewById(R.id.nextbtn);
        setSupportActionBar(friendToolBar);
        getSupportActionBar().setTitle("Select Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fromGroupChat = getIntent().getBooleanExtra("groupChat", false);
        if (fromGroupChat){
            mGroupId = getIntent().getStringExtra("groupId");
            mRootRef = FirebaseDatabase.getInstance().getReference();
            groupName = getIntent().getStringExtra("user_name");
        }


        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        checkItems = new ArrayList<>();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        Query messageQuery = mFriendsDatabase;
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                checkItems.add(new checkItem(false, dataSnapshot.getKey().toString()));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        friendList = findViewById(R.id.friend_list_select);
        friendList.setHasFixedSize(true);
        friendList.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FriendSelectAdapter(this);
        friendList.setAdapter(adapter);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkItems.size()!=0){
                    if (!fromGroupChat){
                        checkItems.add(new checkItem(true, mCurrent_user_id));
                        Intent intent = new Intent(SelectFriendActivity.this, CreateGroup.class);
                        intent.putExtra("list", checkItems);
                        startActivity(intent);
                    }else {
                        addToGroup(checkItems);

                        Intent intent = new Intent(SelectFriendActivity.this, GroupChat.class);
                        intent.putExtra("user_id", mGroupId);
                        intent.putExtra("user_name", groupName);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }else {
                    Toast.makeText(SelectFriendActivity.this, "Select at least 1 friend!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addToGroup(ArrayList<checkItem> checkItems) {

        for (int i = 0; i<checkItems.size(); i++){
            if (checkItems.get(i).isChecked()){
                addInChat(checkItems.get(i).getUserId());
            }
        }

    }

    public class FriendSelectAdapter extends RecyclerView.Adapter<FriendSelectAdapter.ViewHolder>{
        private Context context;
        private DatabaseReference mUsersDatabase;

        public FriendSelectAdapter(Context context) {
            this.context = context;

            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(context).inflate(R.layout.single_friend_select, parent, false);
            ViewHolder view = new ViewHolder(v);
            return view;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            mUsersDatabase.child(checkItems.get(position).getUserId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();

                    holder.nameTv.setText(userName);
                    holder.statusTv.setText(status);
                    Picasso.get().load(userThumb).placeholder(R.drawable.ic_face_black_48dp).into(holder.userImageView);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            if (checkItems.get(position).isChecked()){
                holder.itemView.setBackgroundColor(Color.parseColor("#7b7dcdf2"));
            }else {
                holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (checkItems.get(position).isChecked()){
                        holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));
                        checkItems.get(position).setChecked(false);
                    }else {
                        holder.itemView.setBackgroundColor(Color.parseColor("#7b7dcdf2"));
                        checkItems.get(position).setChecked(true);
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return checkItems.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            CircleImageView userImageView;
            TextView nameTv, statusTv;
            public ViewHolder(View itemView) {
                super(itemView);
                userImageView = itemView.findViewById(R.id.profile_pic_single_select);
                nameTv = itemView.findViewById(R.id.user_single_name_single_select);
                statusTv = itemView.findViewById(R.id.user_single_status_single_select);
            }
        }
    }

    private void addInChat(final String UserId){
        mRootRef.child("Chat").child(UserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(mGroupId)){

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);
                    chatAddMap.put("type", "group");
                    chatAddMap.put("access", true);
                    chatAddMap.put("LeaveTime", 0);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + UserId + "/" + mGroupId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){

                                Log.d("CHAT_LOG", databaseError.getMessage().toString());

                            }

                        }
                    });

                }else {
                    mRootRef.child("Chat").child(UserId).child(mGroupId).child("access").setValue(true);
                    mRootRef.child("Chat").child(UserId).child(mGroupId).child("LeaveTime").setValue(0);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
