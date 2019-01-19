package com.lazyfit.app.lazyfit;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ManageAddRequestActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView joinGroupRecyclerView;
    private DatabaseReference groupAddRef ,groupsRef,usersRef;
    private FirebaseAuth mAuth;
    private String currentUserID,currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_add_request);

        initialize();
    }

    protected static class ManageAddRequestViewHolder extends RecyclerView.ViewHolder{

        TextView groupName;
        CircleImageView groupImage;
        Button accept;
        Button decline;

        public ManageAddRequestViewHolder(@NonNull View itemView) {// object the holds contacts items
            super(itemView);
             accept = itemView.findViewById(R.id.accept_add_request);
             decline = itemView.findViewById(R.id.decline_add_request);
             groupName = itemView.findViewById(R.id.group_profile_name);
             groupImage = itemView.findViewById(R.id.group_profile_image);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<GroupID> options =
                new FirebaseRecyclerOptions.Builder<GroupID>()
                .setQuery(groupAddRef,GroupID.class)
                .build();

        FirebaseRecyclerAdapter<GroupID,ManageAddRequestViewHolder> adapter =
                new FirebaseRecyclerAdapter<GroupID, ManageAddRequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ManageAddRequestViewHolder holder, int position, @NonNull final GroupID model) {
                        holder.groupName.setText(model.getName());
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.group_profile).into(holder.groupImage);
                        holder.accept.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                groupsRef.child(model.getName()).child("Members").child(currentUserID).setValue(currentUserName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(ManageAddRequestActivity.this, "Joined to "+model.getName(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                cancelRequest(model.getName());
                            }
                        });
                        holder.decline.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelRequest(model.getName());
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ManageAddRequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view =LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.add_group_display_layout,viewGroup,false);
                        ManageAddRequestViewHolder viewHolder =new ManageAddRequestViewHolder(view);
                        return viewHolder;
                    }
                };
        joinGroupRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void cancelRequest(final String name){
        groupAddRef.child(name).removeValue();
    }


    private void initialize() {
        mToolbar = findViewById(R.id.manage_add_bar);
        mAuth =FirebaseAuth.getInstance();
        currentUserID =mAuth.getCurrentUser().getUid();
        setSupportActionBar(mToolbar);

        joinGroupRecyclerView =  findViewById(R.id.manage_add_recyclerView);
        joinGroupRecyclerView.setLayoutManager((new LinearLayoutManager(this)));
        groupAddRef =FirebaseDatabase.getInstance().getReference().child("Group Add Request").child(currentUserID);
        groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUserName = dataSnapshot.child(currentUserID).child("name").getValue().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Group request");
    }
}
