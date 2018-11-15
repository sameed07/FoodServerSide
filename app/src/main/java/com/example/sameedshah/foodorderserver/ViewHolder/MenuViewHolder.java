package com.example.sameedshah.foodorderserver.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sameedshah.foodorderserver.Common.Common;
import com.example.sameedshah.foodorderserver.Interface.ItemClickListener;
import com.example.sameedshah.foodorderserver.R;

public class MenuViewHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener,
        View.OnCreateContextMenuListener

{

    public TextView txtMenuName;
    public ImageView itemImage;

    private ItemClickListener itemClickListener;

    public MenuViewHolder(@NonNull View itemView) {
        super(itemView);

        txtMenuName = itemView.findViewById(R.id.txtMenuName);
        itemImage = itemView.findViewById(R.id.itemImage);

        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);

    }

    public ItemClickListener getItemClickListener() {
        return itemClickListener;
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {

        itemClickListener.onClick(view,getAdapterPosition(),false);


    }


    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {

        contextMenu.setHeaderTitle("Select the action");
        contextMenu.add(0,0,getAdapterPosition(), Common.UPDATE);
        contextMenu.add(0,1,getAdapterPosition(), Common.DELETE);

    }
}
