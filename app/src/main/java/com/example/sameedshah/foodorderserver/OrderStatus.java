package com.example.sameedshah.foodorderserver;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;

import com.example.sameedshah.foodorderserver.Common.Common;
import com.example.sameedshah.foodorderserver.Interface.ItemClickListener;
import com.example.sameedshah.foodorderserver.Model.Request;
import com.example.sameedshah.foodorderserver.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.jaredrummler.materialspinner.MaterialSpinner;

public class OrderStatus extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    MaterialSpinner spinner;


    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        //firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        //init
        recyclerView = findViewById(R.id.list_Order);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        loadOrders();


    }

    private void loadOrders() {

        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                requests
        ) {
            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, final Request model, int position) {

                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderPhone.setText(model.getPhone());

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int positon, boolean isLongClick) {

                        Intent i = new Intent(OrderStatus.this,TrackingOrder.class);
                        Common.currentRequest = model;
                        startActivity(i);

                    }
                });
            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getTitle().equals(Common.UPDATE)) {
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
        } else if (item.getTitle().equals(Common.DELETE)) {
            deleteOrder(adapter.getRef(item.getOrder()).getKey());

        }

        return super.onContextItemSelected(item);

    }

    private void deleteOrder(String key) {

        requests.child(key).removeValue();
    }

    private void showUpdateDialog(String key, final Request item) {


        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Update Status");
        dialog.setMessage("choose status");

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.update_order_layout,null);

        spinner = view.findViewById(R.id.statusSpinner);
        spinner.setItems("Placed", "On my way", "Shipped");

        dialog.setView(view);

        final  String localKey = key;

        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));

                requests.child(localKey).setValue(item);
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();;

            }
        });

        dialog.show();

    }
}
