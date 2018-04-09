package com.example.tsult.sparrow;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private static final int GALARY_PIC = 1;
    private CircleImageView profilePic;
    private TextView mNameTv, statusTv;
    private Button cngProPicBtn, cngStsBtn;

    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;
    private FirebaseUser mCurrentUser;
    private String uid,status;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mNameTv = findViewById(R.id.settings_name);
        statusTv = findViewById(R.id.settings_status);
        profilePic = findViewById(R.id.settings_image);
        cngProPicBtn = findViewById(R.id.settings_img_cng_btn);
        cngStsBtn = findViewById(R.id.settings_status_cng_btn);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        uid = mCurrentUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.user)).child(uid);
        mDatabase.keepSynced(true);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String mName = dataSnapshot.child(getString(R.string.name)).getValue().toString();
                status = dataSnapshot.child(getString(R.string.status)).getValue().toString();
                final String image = dataSnapshot.child(getString(R.string.image)).getValue().toString();
                String thumbImage = dataSnapshot.child(getString(R.string.thumb_image)).getValue().toString();

                Picasso.get().load(image).fit().centerCrop().networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.ic_face_black_48dp).into(profilePic, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(image).placeholder(R.drawable.ic_face_black_48dp).into(profilePic);
                    }

                });

                mNameTv.setText(mName);
                statusTv.setText(status);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SettingsActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        cngStsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(SettingsActivity.this);
                LayoutInflater inflater = (LayoutInflater) SettingsActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialouge = inflater.inflate(R.layout.status_dialouge, null);
                final EditText statusCngEt = dialouge.findViewById(R.id.chng_stts_et);
                Button saveBtn = dialouge.findViewById(R.id.cng_stts_btn);

                statusCngEt.setText(status);

                mBuilder.setView(dialouge);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();

                saveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String s = statusCngEt.getText().toString();
                        if (s.isEmpty()){
                            s = " ";
                        }
                        mProgress = new ProgressDialog(SettingsActivity.this);
                        mProgress.setTitle("Saving Change!");
                        mProgress.setMessage("Please wait while we saving the change!");
                        mProgress.setCanceledOnTouchOutside(false);
                        mProgress.show();
                        mDatabase.child(getString(R.string.status)).setValue(s).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    mProgress.dismiss();
                                    dialog.dismiss();
                                }else {
                                    mProgress.hide();
                                    mProgress.dismiss();
                                    Toast.makeText(SettingsActivity.this, "There are some error in saving change!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        });

        cngProPicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALARY_PIC);
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
                StorageReference filePath = mStorageRef.child("profile_image").child(uid+".jpg");
                final StorageReference thumb_filepath = mStorageRef.child("profile_image").child("thumbs").child(uid + ".jpg");

                mProgress = new ProgressDialog(SettingsActivity.this);
                mProgress.setTitle("Saving Change!");
                mProgress.setMessage("Please wait while we saving the change!");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            final String imgUrl = task.getResult().getDownloadUrl().toString();
                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                    String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();
                                    if (thumb_task.isSuccessful()){
                                        Map update_hashMap = new HashMap();
                                        update_hashMap.put(getString(R.string.image), imgUrl);
                                        update_hashMap.put(getString(R.string.thumb_image), thumb_downloadUrl);

                                        mDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    mProgress.dismiss();
                                                }else {

                                                    Toast.makeText(SettingsActivity.this, "Error in uploading thumbnail.", Toast.LENGTH_LONG).show();
                                                    mProgress.dismiss();

                                                }


                                            }
                                        });
                                    }
                                }
                            });

                        }else {
                            mProgress.dismiss();
                            Toast.makeText(SettingsActivity.this, "There are some error in saving change!", Toast.LENGTH_SHORT).show();
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
