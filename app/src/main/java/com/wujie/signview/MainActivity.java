package com.wujie.signview;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.wujie.signview.adapter.testAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnLongClickListener{

    private RecyclerView mRecyclerView;
    private testAdapter mAdapter;
    private List<String> nameList = new ArrayList<>();
    private boolean isOnLongClick = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        for (int i = 0; i <20 ; i++) {
            nameList.add("第"+i+"个");
        }
        mAdapter = new testAdapter(MainActivity.this, nameList);
        mAdapter.setLongItemClickListener(this);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setNestedScrollingEnabled(true);
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i("hehe", "isonLongClick"+ isOnLongClick);
                return isOnLongClick;
            }
        });


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
}
