package com.wujie.signview.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Troy on 2017-11-1.
 */

public class SpeechAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> listName;
    private Context context;
    public SpeechAdapter(List<String> mListName, Context mContext) {
        this.listName = mListName;
        this.context = mContext;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == 0) {
            //View convertView = LayoutInflater.from();
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    public class HirontalListViewHolder extends RecyclerView.ViewHolder  {

        public HirontalListViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class LeftTextViewHolder extends RecyclerView.ViewHolder {

        public LeftTextViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class RightTextViewHolder extends RecyclerView.ViewHolder {

        public RightTextViewHolder(View itemView) {
            super(itemView);
        }
    }
}
