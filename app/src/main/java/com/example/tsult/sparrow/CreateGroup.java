package com.example.tsult.sparrow;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class CreateGroup extends AppCompatActivity {

    private ArrayList<checkItem> checkItems;
    private Toolbar mToolbar;

    private TextInputLayout groupNameTv;
    private CircleImageView imageView;
    private Button createbtn;


    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;
    private String groupId;
    private DatabaseReference databaseReference, mRootRef;

    private static final int GALARY_PIC = 1;
    private ProgressDialog mProgress;

    private String imgUrl = "default", thumb_downloadUrl= "default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        mToolbar = findViewById(R.id.create_group_app_bar);
        groupNameTv = findViewById(R.id.group_name);
        imageView = findViewById(R.id.group_image);
        createbtn = findViewById(R.id.finish);

        setSupportActionBar(mToolbar);

        getSupportActionBar().setTitle("Create Group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        databaseReference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.user)).push();
        groupId = databaseReference.getKey();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.user)).child(groupId);

        checkItems = (ArrayList<checkItem>) getIntent().getSerializableExtra("list");

        createbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String groupName = groupNameTv.getEditText().getText().toString();
                if(!groupName.isEmpty()){
                    addInUser(groupName);
                }

                for (int i = 0; i<checkItems.size(); i++){
                    if (checkItems.get(i).isChecked()){
                        addInChat(checkItems.get(i).getUserId());
                    }
                }
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALARY_PIC);
            }
        });

    }

    private void addInUser(String mName) {

            databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(groupId);
            HashMap<String, String> userMap = new HashMap<>();
            userMap.put(getString(R.string.name), mName);
            userMap.put(getString(R.string.image), imgUrl);
            userMap.put(getString(R.string.thumb_image), thumb_downloadUrl);
            userMap.put("type", "group");
            userMap.put(getString(R.string.status), "Group");
            userMap.put("online", "true");

            databaseReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Intent main_intent = new Intent(CreateGroup.this,MainActivity.class);
                        startActivity(main_intent);
                        finish();

                    }
                }
            });
    }

    private void addInChat(final String UserId){
        mRootRef.child("Chat").child(UserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(groupId)){

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);
                    chatAddMap.put("type", "group");
                    chatAddMap.put("access", true);
                    chatAddMap.put("LeaveTime", 0);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + UserId + "/" + groupId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){

                                Log.d("CHAT_LOG", databaseError.getMessage().toString());

                            }

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALARY_PIC && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                File thumb_filePath = new File(resultUri.getPath());
                Bitmap thumb_img = null;
                try {
                    thumb_img = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_img.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();
                StorageReference filePath = mStorageRef.child("profile_image").child(groupId+".jpg");
                final StorageReference thumb_filepath = mStorageRef.child("profile_image").child("thumbs").child(groupId + ".jpg");

                mProgress = new ProgressDialog(CreateGroup.this);
                mProgress.setTitle("Saving Change!");
                mProgress.setMessage("Please wait while we saving the change!");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            imgUrl = task.getResult().getDownloadUrl().toString();
                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                    thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();
                                    Picasso.get().load(thumb_downloadUrl).placeholder(R.drawable.camera).into(imageView);
                                    if (thumb_task.isSuccessful()){
                                        Map update_hashMap = new HashMap();
                                        update_hashMap.put(getString(R.string.image), imgUrl);
                                        update_hashMap.put(getString(R.string.thumb_image), thumb_downloadUrl);
                                        mProgress.dismiss();
                                        /*mDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    mProgress.dismiss();
                                                }else {

                                                    Toast.makeText(CreateGroup.this, "Error in uploading thumbnail.", Toast.LENGTH_LONG).show();
                                                    mProgress.dismiss();

                                                }


                                            }
                                        });*/
                                    }
                                }
                            });

                        }else {
                            mProgress.dismiss();
                            Toast.makeText(CreateGroup.this, "There are some error in saving change!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
