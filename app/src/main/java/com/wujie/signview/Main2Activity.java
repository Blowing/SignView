package com.wujie.signview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends AppCompatActivity {

    private ListView mListView;
    private listAdapter mAdapter;
    private List<String> nameList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mListView = (ListView) findViewById(R.id.listView);
        for (int i = 0; i <20 ; i++) {
            nameList.add("第"+i+"个");
        }
        mAdapter = new listAdapter(Main2Activity.this, nameList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog dialog = new AlertDialog.Builder(Main2Activity.this).setTitle("你好")
                        .setMessage("第二个界面")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                            }
                        }).setCancelable(true)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {

                            }
                        })
                        .create();
                dialog.show();
            }
        });
    }

    private class listAdapter extends BaseAdapter {

        private List<String> nameList;
        private Context mContext;

        public listAdapter(Context context, List<String> nameList1) {
            this.mContext = context;
            this.nameList = nameList1;
        }
        @Override
        public int getCount() {
            return nameList.size();
        }

        @Override
        public Object getItem(int position) {
            return nameList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MyHoler myHoler;
            if(convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_test_adapter,
                        null);
                myHoler = new MyHoler();
                myHoler.name = (TextView) convertView.findViewById(R.id.name);
                convertView.setTag(myHoler);

            }
            myHoler = (MyHoler) convertView.getTag();
            myHoler.name.setText(nameList.get(position));
            return convertView;
        }

        class MyHoler {
            public TextView name;
        }
    }
}
