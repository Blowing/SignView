package com.wujie.signview.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wujie.signview.R;
import com.wujie.signview.util.NextClickableSpan;

import java.util.List;

/**
 * Created by Troy on 2017-10-10.
 */

public class testAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private List<String> nameList;
    private Context mContext;

    public void setLongItemClickListener(View.OnLongClickListener longItemClickListener1) {
        this.longItemClickListener = longItemClickListener1;
    }

    private View.OnLongClickListener longItemClickListener;

    private View.OnClickListener itemClicListener;
    public void setItemClickListener (View.OnClickListener itemClickListener) {
        this.itemClicListener = itemClickListener;
    }

    public testAdapter(Context context, List<String> nameList) {
        this.mContext = context;
        this.nameList = nameList;
    }

    public void setNameList(List<String> nameList) {
        this.nameList = nameList;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == 0 ) {
            View convertView  = LayoutInflater.from(mContext).inflate(R.layout
                    .item_speech_hirontall, null);
            return new HirontalListViewHolder(convertView);
        } else {
            View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_test_adapter,
                    null);
            if(longItemClickListener != null) {
                convertView.setOnLongClickListener(longItemClickListener);
            }

//            if(itemClicListener != null) {
//                convertView.setOnClickListener(itemClicListener);
//            }


            MyViewHolder myViewHolder = new MyViewHolder(convertView);
            return myViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof MyViewHolder) {
            MyViewHolder myViewHolder = (MyViewHolder) holder;
            String ss = nameList.get(position) + "下一步";
            SpannableString str = new SpannableString(ss);

            str.setSpan(new NextClickableSpan(mContext), 4, str.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            myViewHolder.name.setText(str);
            myViewHolder.name.setMovementMethod(LinkMovementMethod.getInstance());
            myViewHolder.name.setHighlightColor(Color.TRANSPARENT);
        }

    }

    @Override
    public int getItemCount() {
        return nameList.size();
    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public MyViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
        }
    }

    public class HirontalListViewHolder extends RecyclerView.ViewHolder  {

        public HirontalListViewHolder(View itemView) {
            super(itemView);
        }
    }


}


