package com.johnhao.chongding;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public Button testButton;
    private String question;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testButton = (Button) findViewById(R.id.button);

        if (!isModuleActive()){
            Toast.makeText(this, "模块未启动", LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this, "模块已启动", LENGTH_LONG).show();
        }

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                test_serach();
            }
        });
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message message){
            super.handleMessage(message);
            if (message.what == SearchThread.SEARCH_SUCCESS){
                HashMap<String, String> answers = (HashMap<String, String>) message.obj;
                List<Integer> list = new ArrayList<>();
                String showMsg = "";
                for (String key : answers.keySet()){
                    list.add(Integer.parseInt(answers.get(key)));
                    showMsg += key + "结果个数" + answers.get(key) + "\n";
                }
                Collections.sort(list);

                if(question.contains("不")) {
                    for(String key : answers.keySet()) {
                        if(Integer.parseInt(answers.get(key)) == list.get(0)) {
                            Log.w(TAG, "推荐答案： " + key);
                            showMsg += "推荐答案：" + key;
                            break;
                        }
                    }
                } else {
                    for(String key : answers.keySet()) {
                        if(Integer.parseInt(answers.get(key)) == list.get(list.size() - 1)) {
                            Log.w(TAG, "推荐答案： " + key);
                            showMsg += "推荐答案：" + key;
                            break;
                        }
                    }
                }
                Toast.makeText(getApplicationContext(), showMsg, Toast.LENGTH_SHORT).show();
            }
            else if (message.what == SearchThread.SEARCH_FAILER){
                Log.d(TAG, "搜索失败");
                Toast.makeText(getApplicationContext(), "搜索失败", Toast.LENGTH_LONG).show();
            }
        }
    };

    private boolean isModuleActive(){
        return false;
    }

    private void test_serach(){
        Log.d(TAG, "开始搜题");
        String content = "{\"answerTime\":10,\"correctOption\":1,\"desc\":\"5.等闲变却故人心”下一句是？\",\"displayOrder\":4,\"liveId\":110,\"options\":\"[\\\"比翼连枝当日愿\\\",\\\"何事秋风悲画扇\\\",\\\"却道故人心易变\\\"]\",\"questionId\":1250,\"showTime\":1515848850984,\"stats\":[6614,153688,95952],\"status\":2,\"type\":\"showAnswer\"}";

        question = Util.getQuestion(content);
        final String[] answers = Util.getAnswer(content);

        new SearchThread(handler, question, answers).start();

    }
}
