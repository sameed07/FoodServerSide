package com.example.sameedshah.foodorderserver;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sameedshah.foodorderserver.Common.Common;
import com.example.sameedshah.foodorderserver.Interface.ItemClickListener;
import com.example.sameedshah.foodorderserver.Model.Category;
import com.example.sameedshah.foodorderserver.Model.User;
import com.example.sameedshah.foodorderserver.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.util.UUID;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    TextView txtFullname;

    //firebas
    FirebaseDatabase database;
    DatabaseReference categories;
    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;
    FirebaseStorage storage;
    StorageReference storageReference;


    TextView edtName;
    Button btnSelect;
    Button btnUpload;

    //view
    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;

    Category newCategory;
    Uri imageUri;


    DrawerLayout drawer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu Management");
        setSupportActionBar(toolbar);

        //init firebase
        database  = FirebaseDatabase.getInstance();
        categories = database.getReference("Category");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        recycler_menu = findViewById(R.id.recyler_menu);
        recycler_menu.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recycler_menu.setLayoutManager(layoutManager);



        loadMenu();



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

                showDialog();
            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //set name for user
        View headView = navigationView.getHeaderView(0);
        txtFullname = headView.findViewById(R.id.txtFullName);
        User user = new User();
        txtFullname.setText(Common.currentUser.getName());
        //Toast.makeText(this, Common.currentUser.getName(), Toast.LENGTH_SHORT).show();

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

    private void showDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Add new Category");
        dialog.setMessage("Please full fill information");

        LayoutInflater inflater = this.getLayoutInflater();

        View menu_item = inflater.inflate(R.layout.add_new_menu,null);

        edtName = menu_item.findViewById(R.id.edtName);
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

                if(newCategory != null){
                    categories.push().setValue(newCategory);
                    Snackbar.make(drawer, "New Category"+newCategory.getName()+" is Added", Snackbar.LENGTH_SHORT)
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
                    Toast.makeText(Home.this, "Uploaded !!!", Toast.LENGTH_SHORT).show();

                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            newCategory = new Category(edtName.getText().toString() , uri.toString());
                        }
                    });
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            dialog.dismiss();
                            Toast.makeText(Home.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void loadMenu() {

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(
                Category.class,
                R.layout.menu_item,
                MenuViewHolder.class,
                categories

        ) {
            @Override
            protected void populateViewHolder(MenuViewHolder viewHolder, Category model, int position) {

                viewHolder.txtMenuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.itemImage);

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int positon, boolean isLongClick) {

                        Intent foodIntent = new Intent(Home.this, FoodList.class);
                        foodIntent.putExtra("categoryId",adapter.getRef(positon).getKey());
                        startActivity(foodIntent);
                    }
                });


            }
        };
        adapter.notifyDataSetChanged();
        recycler_menu.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {

        } else if (id == R.id.nav_cart) {



        } else if (id == R.id.nav_ordesr) {

            Intent i = new Intent(Home.this,OrderStatus.class);
            startActivity(i);

        } else if (id == R.id.nav_logout) {



        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //update delete

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.UPDATE)){
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));

        }else if(item.getTitle().equals(Common.DELETE)){
            deleteCategory(adapter.getRef(item.getOrder()).getKey());

        }

        return super.onContextItemSelected(item);
    }

    private void deleteCategory(String key ) {

        categories.child(key).removeValue();
        Toast.makeText(this, "Item Removed", Toast.LENGTH_SHORT).show();
    }

    private void showUpdateDialog(final String key, final Category item) {


        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Update Category");
        dialog.setMessage("Please full fill information");

        LayoutInflater inflater = this.getLayoutInflater();

        View menu_item = inflater.inflate(R.layout.add_new_menu,null);

        edtName = menu_item.findViewById(R.id.edtName);
        btnSelect = menu_item.findViewById(R.id.btnSelect);
        btnUpload = menu_item.findViewById(R.id.btnUpload);

        edtName.setText(item.getName());

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

                //updated informaation

                item.setName(edtName.getText().toString());
                categories.child(key).setValue(item);
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

    private void changeImage(final Category item) {

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
                            Toast.makeText(Home.this, "Uploaded !!!", Toast.LENGTH_SHORT).show();

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
                            Toast.makeText(Home.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
