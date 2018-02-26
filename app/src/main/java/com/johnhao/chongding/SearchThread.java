package com.johnhao.chongding;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by johnhao on 2018/2/1.
 */

public class SearchThread extends Thread{

    public static final int SEARCH_SUCCESS = 0;
    public static final int SEARCH_FAILER = 1;

    private static final String TAG = "SearchThread";
    private static final String baseUrl = "http://baidu.com/s?wd=";

    private Handler handler;
    private String question;
    private String[] answers;

    public SearchThread(Handler handler, String question, String[] answers) {
        this.handler = handler;
        this.question = question;
        this.answers = answers;
    }

    public void run(){
        Map<String, String> answmaps = new HashMap<>();
        try {

            for(String itemans : answers) {
                String strurl = baseUrl + URLEncoder.encode(question, "utf-8") +
                        "+" + URLEncoder.encode(itemans, "utf-8");
//                Log.i(Main.TAG, strurl);
                URL url = new URL(strurl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-agent", "Mozilla/4.0");
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                int code = connection.getResponseCode();
                if (code == 200) {
                    InputStreamReader is = new InputStreamReader(connection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(is);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        Pattern pattern = Pattern.compile("百度为您找到相关结果约.*?个<");
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            String num = matcher.group();
                            num = num.replace("百度为您找到相关结果约", "").
                                    replace("个<", "").replace(",", "");
                            Log.d(TAG, itemans + ": " + num);
                            answmaps.put(itemans, num);
                            break;
                        }
                    }
                    is.close();
                }
                connection.disconnect();
            }
            Message message = new Message();
            message.what = SEARCH_SUCCESS;
            message.obj = answmaps;
            handler.sendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
            Message message = new Message();
            message.what = SEARCH_FAILER;
            handler.sendMessage(message);
        }
    }

}
