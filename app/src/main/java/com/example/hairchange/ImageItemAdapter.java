package com.example.hairchange;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;

public class ImageItemAdapter extends RecyclerView.Adapter<ImageItemAdapter.ItemViewHolder>{
//    private ArrayList<String> mData;
    private ArrayList<Integer> mData;
    int tag;

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        protected ImageView imageItem1;
        private static final String IMAGEVIEW_TAG = "icon bitmap";
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


            View.OnTouchListener mTouchListener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
                        ClipData clip = ClipData.newPlainText("","");

                        view.startDrag(clip, new CanvasShadow(view, (int)event.getX(), (int)event.getY()), view, 0);

//                        view.setVisibility(View.INVISIBLE);

                        return true;
                    }
                    return false;
                }
            };

            View.OnDragListener mDragListener = new View.OnDragListener() {

                @Override
                public boolean onDrag(View v, DragEvent event) {
                    v.invalidate();
                    switch(event.getAction()) {
                        case DragEvent.ACTION_DRAG_STARTED:

                            return true;
                        case DragEvent.ACTION_DRAG_ENTERED:

                            return true;
                        case DragEvent.ACTION_DRAG_EXITED:
                            tag = (int)v.getTag();

                            Log.i("Tag", tag+"");
                            Log.i("pos", getAdapterPosition()+"");
                            return true;
                        case DragEvent.ACTION_DROP:
                            /*
                            View view = (View)event.getLocalState();
                            Log.i("localstate", event.getLocalState().toString());
                            ViewGroup viewGroup = (ViewGroup) view.getParent();
                            viewGroup.removeView(view);

                            FrameLayout containView;

                            containView = (FrameLayout) v;
                            containView.addView(view);
                            view.setVisibility(View.VISIBLE);
                            */

                            return true;
                        case DragEvent.ACTION_DRAG_ENDED:
                            if(event.getResult() == false) {
                                ((View)(event.getLocalState())).setVisibility(View.VISIBLE);
                            }
                            View view2 = (View)event.getLocalState();
                            ViewGroup viewGroup2 = (ViewGroup) view2.getParent().getParent().getParent();
                            FrameLayout frame = viewGroup2.findViewById(R.id.frame);

                            ImageView sticker = (ImageView) viewGroup2.findViewById(R.id.sticker);
//                            sticker.setImageResource(R.drawable.man_raised1test);
//                            sticker.setImageResource((int)v.getTag());
                            Log.i("Tag", tag+"");
                            sticker.setImageResource(tag);


//                            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) sticker.getLayoutParams();
//                            params.width = 300;
//                            params.height = 300;
//                            sticker.setLayoutParams(params);
                            int parentWidth = ((ViewGroup)v.getParent()).getWidth();
                            int parentHeight = ((ViewGroup)v.getParent()).getHeight();
                            sticker.setX(event.getX() - (frame.getWidth()/2));
                            sticker.setY(event.getY() - (frame.getHeight()/2));
                            ImageView photo = viewGroup2.findViewById(R.id.photo);
                            Log.i("v.getX", v.getX()+"");
                            Log.i("event.getX", event.getX()+"");
                            Log.i("event.getY", event.getY()+"");
                            Log.i("frame.getWidth", frame.getWidth()+"");
                            Log.i("frame.getHeight", frame.getHeight()+"");
                            Log.i("frame.getX", frame.getX()+"");
                            Log.i("frame.getY", frame.getY()+"");
                            Log.i("photo.getY", photo.getY()+"");
                            Log.i("sticker.getX", sticker.getX()+"");
                            Log.i("sticker.getY", sticker.getY()+"");
                            sticker.setVisibility(View.VISIBLE);
                            return true;
                    }
                    return true;
                }
            };


            imageItem1.setOnTouchListener(mTouchListener);
            imageItem1.setOnDragListener(mDragListener);
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
        holder.imageItem1.setTag(mData.get(position));
        Log.i("setTag", mData.get(position).toString());
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


    class CanvasShadow extends View.DragShadowBuilder {
        int mWidth, mHeight;
        int mX, mY;

        public CanvasShadow(View v, int x, int y){
            super(v);

            //좌표를 저장해둠
            mWidth = v.getWidth();
            mHeight = v.getHeight();
            mX = x;
            mY = y;
        }
        public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint){
            shadowSize.set(mWidth, mHeight);//섀도우 이미지 크기 지정함
            shadowTouchPoint.set(mX, mY);//섀도우 이미지 중심점을 지정함.
        }
        public void onDrawShadow(Canvas canvas){
            super.onDrawShadow(canvas);//이미지 복사
        }
    }

}


