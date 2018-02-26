package com.johnhao.chongding;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by johnhao on 2018/2/1.
 */

public class Util {

    public static String getQuestion(String string){
        String question = null;
        try {
            JSONObject jsonObject = new JSONObject(string);
            question = jsonObject.get("desc").toString().trim();
        } catch (Exception e) {
            Log.e("question", "json error on question");
            e.printStackTrace();
        }

        return question;
    }

    public static String[] getAnswer(String string){
        List<String> answers = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(string);
            JSONArray jsonArray = new JSONArray(jsonObject.get("options").toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                answers.add(jsonArray.get(i).toString().trim());
            }
        } catch (Exception e) {
            Log.e("question", "json error on answer");
            e.printStackTrace();
        }

        return answers.toArray(new String[answers.size()]);

    }

    public static String resetQus(String string){
        String[] word_del = {"以下","下列", "哪个是", "哪部", "哪个", "哪一种", "哪位",
                "是谁","哪一部","哪一个","《","》","?","？","诗句","<",">","“","”","。","，"};
        for (String del : word_del){
            if (string.contains(del)) {
                string = string.replace(del, "");
            }
        }
        if (string.contains("不是"))
            string = string.replace("不是", "是");
        if (string.contains("不具有"))
            string = string.replace("不具有", "有");

        return string;
    }
}
