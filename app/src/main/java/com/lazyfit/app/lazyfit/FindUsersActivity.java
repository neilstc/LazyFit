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
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindUsersActivity extends AppCompatActivity{

    private Toolbar mToolbar;
    private RecyclerView FindUsersRecyclerView;
    private DatabaseReference usersRer;
    private String group_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);=
        setContentView(R.layout.activity_find_users);

        usersRer = FirebaseDatabase.getInstance().getReference().child("Users");
        FindUsersRecyclerView =  findViewById(R.id.find_users_recyclerView);
        FindUsersRecyclerView.setLayoutManager((new LinearLayoutManager(this)));
        group_name  = getIntent().getExtras().get("group_name").toString();
        mToolbar = findViewById(R.id.find_user_toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Users");

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(usersRer,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,findUsersViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, findUsersViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull findUsersViewHolder holder, final int position, @NonNull Contacts model) {
                        holder.userName.setText(model.getName());
                        holder.userAboutMe.setText(model.getAboutMe());
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String visit_user_id =getRef(position).getKey();
                                Intent profileIntent =new Intent(FindUsersActivity.this,ProfileActivity.class);

                                profileIntent.putExtra("visit_user_id",visit_user_id);
                                profileIntent.putExtra("group_name",group_name);
                                startActivity(profileIntent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public findUsersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                        findUsersViewHolder viewHolder = new findUsersViewHolder(view);
                        return viewHolder;
                    }
                };
        FindUsersRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    protected static class  findUsersViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName , userAboutMe;
        CircleImageView profileImage;

        public findUsersViewHolder(@NonNull View itemView) {
            super(itemView);

            userName =itemView.findViewById(R.id.users_profile_name);
            userAboutMe =itemView.findViewById(R.id.user_about_me);
            profileImage =itemView.findViewById(R.id.user_profile_image);
         }
    }
}
