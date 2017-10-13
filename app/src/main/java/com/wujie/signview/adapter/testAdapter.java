package com.wujie.signview.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wujie.signview.R;

import java.util.List;

/**
 * Created by Troy on 2017-10-10.
 */

public class testAdapter extends RecyclerView.Adapter<testAdapter.MyViewHolder>  {

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

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_test_adapter,
                null);
        if(longItemClickListener != null) {
            convertView.setOnLongClickListener(longItemClickListener);
        }

        if(itemClicListener != null) {
            convertView.setOnClickListener(itemClicListener);
        }


       MyViewHolder myViewHolder = new MyViewHolder(convertView);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.name.setText(nameList.get(position));
    }

    @Override
    public int getItemCount() {
        return nameList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public MyViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
        }
    }


}


