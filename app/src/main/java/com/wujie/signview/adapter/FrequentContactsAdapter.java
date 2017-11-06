package com.wujie.signview.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.wujie.signview.R;

import java.util.List;

/**
 * Created by wujie on 2017/11/6/006.
 */

public class FrequentContactsAdapter extends RecyclerView.Adapter<FrequentContactsAdapter.MyHolder>{

    private Context mContext;
    private List<String> userNameList;

    public FrequentContactsAdapter(Context mContext, List<String> userNameList) {
        this.mContext = mContext;
        this.userNameList = userNameList;
    }

    @Override

    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(mContext).inflate(R.layout
                .item_speech_select_frequent_people, null);
        MyHolder myHolder = new MyHolder(convertView);
        return myHolder;
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        holder.btnUserName.setText(userNameList.get(position));
    }

    @Override
    public int getItemCount() {
        return userNameList.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder{

        private Button btnUserName;
        public MyHolder(View itemView) {
            super(itemView);
            btnUserName = (Button) itemView.findViewById(R.id.btn_speech_select_username);
        }
    }
}
