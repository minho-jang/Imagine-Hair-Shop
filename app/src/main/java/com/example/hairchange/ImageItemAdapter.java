package com.example.hairchange;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ImageItemAdapter extends RecyclerView.Adapter<ImageItemAdapter.ItemViewHolder>{
//    private ArrayList<String> mData;
    private ArrayList<Integer> mData;


    public class ItemViewHolder extends RecyclerView.ViewHolder {
        protected ImageView imageItem1;
 //       protected ImageView imageItem2;
//        protected ImageView imageItem3;
//        protected ImageView imageItem4;
        View view;
        public ItemViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;

            imageItem1 = view.findViewById(R.id.image_item1);
//            imageItem2 = view.findViewById(R.id.image_item2);
//            imageItem3 = view.findViewById(R.id.image_item3);
//            imageItem4 = view.findViewById(R.id.image_item4);

        }

        void onBind(String data) {
//            Glide.with(view).load(data).into(imageItem);

        }
    }

    public ImageItemAdapter() {
      mData = new ArrayList<>();

    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.scroll_item, viewGroup, false);
        /*
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.scroll_item, parent, false);
        ImageItemAdapter.ViewHolder vh = new ImageItemAdapter.ViewHolder(view);


         */
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        //Glide.with(holder.itemView.getContext()).load(getItem(position)).into(holder.imageItem);
//        holder.onBind(mData.get(position));
        holder.imageItem1.setImageResource(mData.get(position));
//        holder.imageItem2.setImageResource(mData.get(position));
//        holder.imageItem3.setImageResource(mData.get(position));
//        holder.imageItem4.setImageResource(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();

    }

    public void addItem(int data) {
        mData.add(data);
    }

    public int getItem(int pos) { return mData.get(pos); }
}
