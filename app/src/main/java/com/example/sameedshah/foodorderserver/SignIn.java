package com.example.sameedshah.foodorderserver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.example.sameedshah.foodorderserver.Common.Common;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.sameedshah.foodorderserver.Model.User;

public class SignIn extends AppCompatActivity {

    EditText edtPhone,edtPassword;
    Button btnSignIn;

    FirebaseDatabase db;
    DatabaseReference users;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtPassword = findViewById(R.id.edtPassword);
        edtPhone = findViewById(R.id.edtPhone);
        btnSignIn = findViewById(R.id.btnLogin);

        //firebase

        db = FirebaseDatabase.getInstance();
        users = db.getReference("User");

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInUser(edtPhone.getText().toString(),edtPassword.getText().toString());
            }
        });

    }

    private void signInUser(String phone, final String password) {

        final ProgressDialog mProgress = new ProgressDialog(this);
        mProgress.setMessage("Please wait....");
        mProgress.setCancelable(false);
        mProgress.show();

        final  String localPhone  = phone;
        final  String localPassword  = password;

        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(localPhone).exists())
                {
                    mProgress.dismiss();;
                    User user =   dataSnapshot.child(localPhone).getValue(User.class);
                    user.setPhone(localPhone);
                    if(Boolean.parseBoolean(user.getIsStaff()))
                    {
                        if(user.getPassword().equals(localPassword)){
                            //login
                            Toast.makeText(SignIn.this, "Login successfull", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(SignIn.this, Home.class);
                            Common.currentUser = user;
                            startActivity(i);
                        }else{

                            Toast.makeText(SignIn.this, "Wrong password!!", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(SignIn.this, "Please check your staff account !!!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    mProgress.dismiss();
                    Toast.makeText(SignIn.this, "User not exists in Database", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
