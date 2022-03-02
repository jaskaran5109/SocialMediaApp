package com.example.socialmediaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {


    ImageButton searchButton;
    EditText searchText;
    RecyclerView searchRecyclerView;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Search");
        auth=FirebaseAuth.getInstance();
        databaseReference=FirebaseDatabase.getInstance().getReference().child("Users");


        searchButton=(ImageButton) findViewById(R.id.search_people_Friends_button);
        searchText=(EditText) findViewById(R.id.search_box_input);

        searchRecyclerView=(RecyclerView) findViewById(R.id.search_result_list);
        searchRecyclerView.setHasFixedSize(true);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchBoxInput=searchText.getText().toString();
                SearchPeople(searchBoxInput);
            }
        });


    }

    private void SearchPeople(String searchBoxInput)
    {
        Toast.makeText(FindFriendsActivity.this, "Searching...", Toast.LENGTH_SHORT).show();
        Query searchPeople=databaseReference.orderByChild("fullName").startAt(searchBoxInput).endAt(searchBoxInput+"\uf8ff");
        FirebaseRecyclerAdapter<FindFriends,FindFriendsViewHolder> firebaseRecyclerAdapter= new
                FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(
                        FindFriends.class,
                        R.layout.all_users_display_layout,
                        FindFriendsViewHolder.class,
                        searchPeople

                        ) {
            @Override
            protected void populateViewHolder(FindFriendsViewHolder findFriendsViewHolder, FindFriends findFriends, int i) {

                findFriendsViewHolder.setFullName(findFriends.getFullName());
                findFriendsViewHolder.setStatus(findFriends.getStatus());
                findFriendsViewHolder.setProfile_picture(findFriends.getProfile_picture());

                findFriendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String visited_user_id=getRef(i).getKey();
                        Intent intent = new Intent(FindFriendsActivity.this, PersonProfileActivity.class);
                        intent.putExtra("visited_user_id",visited_user_id);
                        startActivity(intent);
                    }
                });
            }
        };
        searchRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setProfile_picture(String profile_picture)
        {
            CircleImageView myImage=(CircleImageView) mView.findViewById(R.id.all_users_profileImage);
            Picasso.get().load(profile_picture).placeholder(R.drawable.profile).into(myImage);
        }
        public void setFullName(String fullName)
        {
            TextView myName=(TextView) mView.findViewById(R.id.all_users_profileName);
            myName.setText(fullName);
        }

        public void setStatus(String status)
        {
            TextView myStatus=(TextView) mView.findViewById(R.id.all_users_profileStatus);
            myStatus.setText(status);
        }
    }
}