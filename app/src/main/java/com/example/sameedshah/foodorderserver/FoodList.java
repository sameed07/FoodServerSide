package com.example.sameedshah.foodorderserver;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.sameedshah.foodorderserver.Common.Common;
import com.example.sameedshah.foodorderserver.Interface.ItemClickListener;
import com.example.sameedshah.foodorderserver.Model.Category;
import com.example.sameedshah.foodorderserver.Model.Food;
import com.example.sameedshah.foodorderserver.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    RelativeLayout rootLayout;

    FloatingActionButton fab;

    FirebaseDatabase database;
    DatabaseReference foodList;
    FirebaseStorage storage;
    StorageReference storageReference;

    String categoryId = "";

    EditText edtName,edtDescription,edtPrice,edtDiscount;
    Button btnSelect,btnUpload;

    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    Uri imageUri;

    Food newFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Foods");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        //init

        recyclerView = findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        rootLayout = findViewById(R.id.roorLayout);


        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showAddFoodDialog();

            }
        });

        if(getIntent() != null)

            categoryId = getIntent().getStringExtra("categoryId");
        if(!categoryId.isEmpty())
            loadFoodList(categoryId);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Common.IMAGE_REQUEST && resultCode == RESULT_OK && data.getData() != null
                && data != null)
        {
            imageUri = data.getData();
            btnSelect.setText("Image selected !");

        }

    }

    private void showAddFoodDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Add new Food");
        dialog.setMessage("Please full fill information");

        LayoutInflater inflater = this.getLayoutInflater();

        View menu_item = inflater.inflate(R.layout.add_new_food_layout,null);

        edtName = menu_item.findViewById(R.id.edtName);
        edtDescription = menu_item.findViewById(R.id.edtDescription);
        edtPrice = menu_item.findViewById(R.id.edtPrice);
        edtDiscount = menu_item.findViewById(R.id.edtDiscount);

        btnSelect = menu_item.findViewById(R.id.btnSelect);
        btnUpload = menu_item.findViewById(R.id.btnUpload);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadImage();
            }
        });

        dialog.setView(menu_item);
        dialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();


                if(newFood != null){
                    foodList.push().setValue(newFood);
                    Snackbar.make(rootLayout, "New Category"+newFood.getName()+" is Added", Snackbar.LENGTH_SHORT)
                            .show();
                }

            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
            }
        });
        dialog.show();

    }

    private void UploadImage() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading....");
        if(imageUri != null){


            dialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/"+ imageName);
            imageFolder.putFile(imageUri).addOnSuccessListener(
                    new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            dialog.dismiss();
                            Toast.makeText(FoodList.this, "Uploaded !!!", Toast.LENGTH_SHORT).show();

                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    newFood = new Food();
                                    newFood.setName(edtName.getText().toString());
                                    newFood.setDescription(edtDescription.getText().toString());
                                    newFood.setPrice(edtPrice.getText().toString());
                                    newFood.setDiscount(edtDiscount.getText().toString());
                                    newFood.setMenuId(categoryId);
                                    newFood.setImage(uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            dialog.dismiss();
                            Toast.makeText(FoodList.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0* taskSnapshot.getBytesTransferred()
                                    / taskSnapshot.getTotalByteCount());
                            dialog.setMessage("uploaded "+ progress +"%");
                        }
                    });
        }
    }

    private void chooseImage() {

        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i,"Select Picture"), Common.IMAGE_REQUEST);
    }

    private void loadFoodList(String categoryId) {

        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(

                Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild("menuId").equalTo(categoryId)
        ) {
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {

                viewHolder.food_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.food_image);

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int positon, boolean isLongClick) {

                    }
                });

            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.UPDATE)){
            showUpdateFoodDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));

        }else if(item.getTitle().equals(Common.DELETE)){
            deleteFood(adapter.getRef(item.getOrder()).getKey());

        }

        return super.onContextItemSelected(item);
    }

    private void deleteFood(String key) {

        foodList.child(key).removeValue();
        Snackbar.make(rootLayout, "Deleted", Snackbar.LENGTH_SHORT)
                .show();
    }

    private void showUpdateFoodDialog(final String key, final Food item) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Edit Food");
        dialog.setMessage("Please full fill information");

        LayoutInflater inflater = this.getLayoutInflater();

        View menu_item = inflater.inflate(R.layout.add_new_food_layout,null);

        edtName = menu_item.findViewById(R.id.edtName);
        edtDescription = menu_item.findViewById(R.id.edtDescription);
        edtPrice = menu_item.findViewById(R.id.edtPrice);
        edtDiscount = menu_item.findViewById(R.id.edtDiscount);

        btnSelect = menu_item.findViewById(R.id.btnSelect);
        btnUpload = menu_item.findViewById(R.id.btnUpload);

        //set default name
        edtName.setText(item.getName());
        edtDescription.setText(item.getDescription());
        edtPrice.setText(item.getPrice());
        edtDiscount.setText(item.getDiscount());

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeImage(item);
            }
        });

        dialog.setView(menu_item);
        dialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();




                    item.setName(edtName.getText().toString());
                    item.setDescription(edtDescription.getText().toString());
                    item.setPrice(edtPrice.getText().toString());
                    item.setDiscount(edtDiscount.getText().toString());

                    foodList.child(key).setValue(item);
                    Snackbar.make(rootLayout, "New Food "+item.getName()+" is Added", Snackbar.LENGTH_SHORT)
                            .show();


            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
            }
        });
        dialog.show();

    }

    private void changeImage(final Food item) {

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading....");
        if(imageUri != null){


            dialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/"+ imageName);
            imageFolder.putFile(imageUri).addOnSuccessListener(
                    new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            dialog.dismiss();
                            Toast.makeText(FoodList .this, "Uploaded !!!", Toast.LENGTH_SHORT).show();

                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    item.setImage(uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            dialog.dismiss();
                            Toast.makeText(FoodList.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0* taskSnapshot.getBytesTransferred()
                                    / taskSnapshot.getTotalByteCount());
                            dialog.setMessage("uploaded "+ progress +"%");
                        }
                    });
        }

    }
}
