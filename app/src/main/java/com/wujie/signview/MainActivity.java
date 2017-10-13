package com.wujie.signview;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.wujie.signview.adapter.testAdapter;
import com.wujie.signview.view.DefaultHeader;
import com.wujie.signview.view.SpringView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnLongClickListener, View
        .OnClickListener, SpringView.OnFreshListener{

    private RecyclerView mRecyclerView;
    private SpringView mSpringView;
    private testAdapter mAdapter;
    private List<String> nameList = new ArrayList<>();
    //private SmartRefreshLayout smartRefreshLayout;
    private boolean isOnLongClick = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //smartRefreshLayout = (SmartRefreshLayout) findViewById(R.id.refreshLayout);
        mSpringView = (SpringView)findViewById(R.id.spring_view);
        //mSpringView.setEnable(true);
       mSpringView.setHeader(new DefaultHeader(MainActivity.this));
        mSpringView.setListener(this);
        mSpringView.setType(SpringView.Type.FOLLOW);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        for (int i = 0; i <20 ; i++) {
            nameList.add("第"+i+"个");
        }
        mAdapter = new testAdapter(MainActivity.this, nameList);
//        mAdapter.setLongItemClickListener(this);
//        mAdapter.setItemClickListener(this);

        SwipeRefreshLayout ss;
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setNestedScrollingEnabled(true);
        mRecyclerView.addOnItemTouchListener(new RecyclerViewClickListener(this, mRecyclerView,
                new RecyclerViewClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Toast.makeText(MainActivity.this,"Click "+nameList.get(position),Toast
                                .LENGTH_SHORT).show();
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        Toast.makeText(MainActivity.this,"Long Click "+nameList.get(position),Toast
                                .LENGTH_SHORT).show();
                    }
                }));
    }




    @Override
    public boolean onLongClick(View v) {
        Log.i("wujie", "点击");
        isOnLongClick = true;

        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setTitle("你好")
                .setMessage("你真的很好")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        isOnLongClick = false;
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        isOnLongClick = false;
                    }
                }).setCancelable(true)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        isOnLongClick = false;
                    }
                })
                .create();
        dialog.show();
        return true;
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(MainActivity.this, Main2Activity.class));
    }

    @Override
    public void onRefresh() {
        Log.i("hehe", "刷星");
    }

    @Override
    public void onLoadmore() {

    }
}
