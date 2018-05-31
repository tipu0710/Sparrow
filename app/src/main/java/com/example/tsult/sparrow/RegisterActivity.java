package com.example.tsult.sparrow;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout nameEt;
    private TextInputLayout emailEt;
    private TextInputLayout passwordEt;
    private Button createBtn;

    private FirebaseAuth mAuth;
    private ProgressDialog mRegisterProgress;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mRegisterProgress = new ProgressDialog(this);

        nameEt = findViewById(R.id.name_et);
        emailEt = findViewById(R.id.email_et);
        passwordEt = findViewById(R.id.password_et);
        createBtn = findViewById(R.id.register_btn);

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int a = 0;
                String mName = nameEt.getEditText().getText().toString();
                if (mName.isEmpty()){
                    nameEt.getEditText().setError(getString(R.string.error_msg));
                    a++;
                }
                String mEmail = emailEt.getEditText().getText().toString();
                if (mEmail.isEmpty()){
                    emailEt.getEditText().setError(getString(R.string.error_msg));
                    a++;
                }
                String mPassword = passwordEt.getEditText().getText().toString();
                if (mPassword.isEmpty()){
                    passwordEt.getEditText().setError(getString(R.string.error_msg));
                    a++;
                }
                if (a == 0){
                    mRegisterProgress.setTitle("Creating Account");
                    mRegisterProgress.setMessage("Please wait while we creating your Account!");
                    mRegisterProgress.setCanceledOnTouchOutside(false);
                    mRegisterProgress.show();
                    createAccount(mName, mEmail, mPassword);
                }
            }
        });
    }

    private void createAccount(final String mName, String mEmail, String mPassword) {
        mAuth.createUserWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    String UID = firebaseUser.getUid();

                    databaseReference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.user)).child(UID);

                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put(getString(R.string.name), mName);
                    userMap.put(getString(R.string.status), getString(R.string.default_status));
                    userMap.put(getString(R.string.image), "default");
                    userMap.put(getString(R.string.thumb_image), "default");
                    userMap.put("device_token", deviceToken);
                    userMap.put("type", "single");

                    databaseReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                mRegisterProgress.dismiss();
                                Intent main_intent = new Intent(RegisterActivity.this,MainActivity.class);
                                main_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(main_intent);
                                finish();

                            }
                        }
                    });

                }else {
                    mRegisterProgress.hide();
                    Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
