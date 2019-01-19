package com.lazyfit.app.lazyfit;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private TabsAccessorAdapter mTabsAccessorAdapter;
    private DatabaseReference RootRef;
    private String userName = "" ,userID;
    private ImageView haveMessageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeFields();

        RootRef.child("Group Add Request").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(userID)){
                    haveMessageButton.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(firebaseUser == null){
            sendUserToLogInActivity();
        }else{
            updateUserLastSeen();
            VerifyUserExistence();
        }
        getMassages();
    }

    private void getMassages() {
        RootRef.child("Group Add Request").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.child(firebaseUser.getUid()).exists()){
                        haveMessageButton.setVisibility(View.VISIBLE);
                        haveMessageButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent manage_add_request =new Intent(MainActivity.this,ManageAddRequestActivity.class);
                                startActivity(manage_add_request);
                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void VerifyUserExistence() {
        String currentUserID = mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("name").exists())) {
                    Toast.makeText(MainActivity.this,"Welcome",Toast.LENGTH_SHORT).show();
                }else{
                    sendUserSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToLogInActivity() {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()== R.id.main_logout_option){
            mAuth.signOut();
            sendUserToLogInActivity();
            finish();
        }else if(item.getItemId() == R.id.main_setting_option){
            sendUserSettingsActivity();
        }else if(item.getItemId() == R.id.main_createGroup_option){
            requestNewGroup();
        }
        return true;
    }

    private void requestNewGroup(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Enter Group Name: ");
        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("my Lazy Group");
        builder.setView(groupNameField);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName = groupNameField.getText().toString();
                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this,"Please Write Group Name",Toast.LENGTH_SHORT).show();
                }else{
                    createNewGroup(groupName);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }
    private void createNewGroup(final String groupName){
        RootRef.child("Groups").child(groupName).child("Manager").setValue(mAuth.getUid())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            RootRef.child("Groups").child(groupName).child("Members").child(mAuth.getUid()).setValue(userName);
                            Toast.makeText(MainActivity.this,groupName+" group is Created Successfully",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendUserSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void initializeFields() {
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();
        haveMessageButton = findViewById(R.id.alert_message);
        mViewPager =  findViewById(R.id.main_tabs_pager);
        mTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mTabsAccessorAdapter);
        mTabLayout =  findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mToolbar =findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        userID = mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(firebaseUser.getUid()).child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String name = dataSnapshot.getValue().toString();
                    userName = name;
                    getSupportActionBar().setTitle("LazyFit: "+userName);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void updateUserLastSeen(){
        String currentTime, currentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat date =new SimpleDateFormat("dd MM yyyy");
        currentDate =date.format(calendar.getTime());
        SimpleDateFormat time =new SimpleDateFormat("hh:mm a");
        currentTime = time.format(calendar.getTime());
        HashMap<String,Object> lastSeen = new HashMap<>();
        lastSeen.put("time",currentTime);
        lastSeen.put("date",currentDate);
        RootRef.child("Users").child(userID).child("lastSeen").updateChildren(lastSeen);
    }

}
