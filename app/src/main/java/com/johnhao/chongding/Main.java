package com.johnhao.chongding;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by johnhao on 2018/1/31.
 */

public class Main implements IXposedHookLoadPackage{

    private static final String TAG = "my_hook";
    private Context context;
    private String question;
    private Handler handler;
    private String[] answers;


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        // Xposed模块自检测
        if (loadPackageParam.packageName.equals("com.johnhao.chongding")){
            XposedHelpers.findAndHookMethod("com.johnhao.chongding.MainActivity", loadPackageParam.classLoader, "isModuleActive", XC_MethodReplacement.returnConstant(true));
        }

        // 被hook对象
        if (loadPackageParam.packageName.equals("com.chongdingdahui.app")){
            Log.d(TAG, "Ready to hook 冲顶大会");

            // 应用被加壳，采用这种方式加载类
            try {
                XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        context = (Context) param.args[0];
                        ClassLoader loader = context.getClassLoader();

                        // 答题时间 set 10 -> 15s
                        try {
                            Class clazz = loader.loadClass("com.chongdingdahui.app.model.Question");
                            if (clazz != null){
                                XposedHelpers.findAndHookMethod(clazz, "getAnswerTime", new XC_MethodHook() {
                                    @Override
                                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                        param.setResult(15);
                                    }
                                });
                            }

                        }catch (Exception e){
                            Log.d(TAG, "model.Question not found" + Log.getStackTraceString(e));
                        }

                        // 获取题目和答案
                        try {
                            Class clazz = loader.loadClass("com.chongdingdahui.app.socket.MessageManager$7");
                            if (clazz != null){

                                XposedHelpers.findAndHookMethod(clazz, "call", Object[].class, new XC_MethodHook() {
                                    @Override
                                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                        super.beforeHookedMethod(param);
                                        Object[] obj = (Object[]) param.args[0];
                                        String content = obj[0].toString();
                                        Log.d(TAG, content);
                                        question = Util.getQuestion(content);
                                        answers = Util.getAnswer(content);
                                        String crop_qus = Util.resetQus(question);
                                        new SearchThread(handler, crop_qus, answers).start();

                                    }
                                });
                            }

                        }catch (Exception e){
                            Log.e(TAG, "socket.MessageManager$7 clazz not found" + Log.getStackTraceString(e));
                        }

                        //这里做搜索计算
                        Log.d(TAG, "开始解题");

                        handler = new Handler(){
                            @Override
                            public void handleMessage(Message msg) {
                                super.handleMessage(msg);
                                if(msg.what == SearchThread.SEARCH_SUCCESS) {
                                    HashMap<String, String> answers = (HashMap<String, String>) msg.obj;
                                    List<Integer> as = new ArrayList<>();
                                    String showMsg = question + "\n";
                                    String simpleAns = "";
                                    for(String key : answers.keySet()) {
                                        as.add(Integer.parseInt(answers.get(key)));
                                        showMsg += key + "（结果个数）:" + answers.get(key) + "\n";
                                    }
                                    Collections.sort(as);

                                    if(question.contains("不") || question.contains("没有")) {
                                        for(String key : answers.keySet()) {
                                            if(Integer.parseInt(answers.get(key)) == as.get(0)) {
                                                Log.w(TAG, "推荐***否定***答案： " + key);
                                                showMsg += "推荐***否定***答案：" + key;
                                                simpleAns = "推荐***否定***答案：" + key;
                                                break;
                                            }
                                        }
                                    } else {
                                        for(String key : answers.keySet()) {
                                            if(Integer.parseInt(answers.get(key)) == as.get(as.size() - 1)) {
                                                Log.w(TAG, "推荐答案： " + key);
                                                showMsg += "推荐答案：" + key;
                                                simpleAns = "推荐答案：" + key;
                                                break;
                                            }
                                        }
                                    }

                                    Toast.makeText(context.getApplicationContext(), simpleAns, Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, showMsg);

                                } else if(msg.what == SearchThread.SEARCH_FAILER) {
                                    Log.d(TAG, "failer");
                                }
                            }
                        };
                    }
                });

            }catch (Exception e){
                Log.e(TAG, "error" + Log.getStackTraceString(e));

            }
        }

    }
}
