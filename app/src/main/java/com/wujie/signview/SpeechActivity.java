package com.wujie.signview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.wujie.signview.adapter.FrequentContactsAdapter;

import java.util.ArrayList;
import java.util.List;

public class SpeechActivity extends AppCompatActivity {


    private RecyclerView mFrequentContactsRv;
    private FrequentContactsAdapter frequentContactsAdapter;
    private List<String> nameList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);
        mFrequentContactsRv = (RecyclerView) findViewById(R.id.rv_speech_frequent_select);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mFrequentContactsRv.setLayoutManager(linearLayoutManager);
        nameList.add("林浪");
        nameList.add("黄雄");
        nameList.add("吴建生");
        nameList.add("吴杰");
        nameList.add("汪成平");
        nameList.add("邹俊");
        nameList.add("邓晓蓉");
        nameList.add("李时齐");
        nameList.add("丁文静");
        nameList.add("陈武");
        nameList.add("吴澜");
        nameList.add("王显康");
        nameList.add("廖燕");
        nameList.add("更多");

        frequentContactsAdapter = new FrequentContactsAdapter(this, nameList);
        mFrequentContactsRv.setAdapter(frequentContactsAdapter);

    }
}
