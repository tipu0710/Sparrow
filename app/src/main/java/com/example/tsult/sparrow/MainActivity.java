package com.example.tsult.sparrow;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.tsult.sparrow.Fragments.FriendFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar toolbar;

    private ViewPager mViewPager;
    private SectionPagerAdepter sectionPagerAdepter;

    private DatabaseReference mUserRef;

    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        toolbar = findViewById(R.id.main_app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Name of Group");

        if (mAuth.getCurrentUser() != null) {

            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        }

        mViewPager = findViewById(R.id.main_view_pager);
        sectionPagerAdepter = new SectionPagerAdepter(getSupportFragmentManager());
        mViewPager.setAdapter(sectionPagerAdepter);
        mViewPager.setCurrentItem(1);

        tabLayout = findViewById(R.id.main_tab);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            updateUI();
        }else{
            mUserRef.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null) {

            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

        }
    }

    private void updateUI() {
        Intent intent = new Intent(MainActivity.this, AuthActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_logout){
            FirebaseAuth.getInstance().signOut();
            updateUI();
        }else if (item.getItemId() == R.id.acc_setting){
            Intent settingIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingIntent);
        }else if (item.getItemId() == R.id.all_user){
            Intent allUserIntent = new Intent(MainActivity.this, UserActivity.class);
            startActivity(allUserIntent);
        }else if (item.getItemId() == R.id.create_group){
            Intent openFriendIntent = new Intent(MainActivity.this, FriendFragment.class);
            startActivity(openFriendIntent);
        }

        return true;
    }
}
