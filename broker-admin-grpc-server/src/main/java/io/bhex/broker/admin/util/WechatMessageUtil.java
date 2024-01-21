package io.bhex.broker.admin.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class WechatMessageUtil {

    private final static String token_url="https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=ID&corpsecret=SECRET";
    private final static String put_url="https://qyapi.weixin.qq.com/cgi-bin/appchat/send?access_token=ACCESS_TOKEN";

    private final static String agentId="1000028";
    private final static String secret="hlsfDj7b5PThiwaadrsZSzemxwLExcg1BopEHKRckn8";
    private final static String corp_id="ww86103d22c9a012e2";
    private final static String chatid="14461621355219127248";


    private static Cache<String,String> tokenCache=CacheBuilder.newBuilder()
            .expireAfterWrite(7200, TimeUnit.SECONDS).build();


    public String getToken() throws ExecutionException {
        return tokenCache.get("token", new Callable<String>(){

            @Override
            public String call() throws Exception {

                String url=token_url.replace("ID",corp_id).replace("SECRET",secret);
                OkHttpClient httpClient = new OkHttpClient();
                final Request request = new Request.Builder()
                        .url(url)
                        .build();

                Call call=httpClient.newCall(request);
                Response response=call.execute();
                JSONObject obj=JSON.parseObject(response.body().string());
                int errorCode=obj.getInteger("errcode");
                if(errorCode==0){
                    return obj.getString("access_token");
                }

                throw new IllegalStateException("None token");
            }
        });
    };

    @Async
    public void sendWechatMsg(String msg) throws Exception {

        String token= getToken();
        if(StringUtils.isEmpty(token)){
            return;
        }
        String url=put_url.replace("ACCESS_TOKEN",token);

        Map<String,String> contentMap=Maps.newHashMap();
        contentMap.put("content",msg);

        Map<String,Object> message=Maps.newHashMap();
        message.put("chatid",chatid);
        message.put("msgtype","text");
        message.put("text",contentMap);
        message.put("safe",0);

        OkHttpClient httpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url).post(RequestBody.create(MediaType.parse("application/json;charset=utf-8"),JSON.toJSONString(message)))
                .build();

        Call call=httpClient.newCall(request);
        Response response=call.execute();

        if(response.code()==200){
            String body=response.body().string();
            JSONObject obj=JSON.parseObject(body);
            int code=obj.getInteger("errcode");
            if(code==0){
                return;
            }

            log.error("send wx message fail,response={}",body);
        }else{
            log.error("send wx message fail,response={} ",JSON.toJSONString(response.body().string()));
        }
    }


    public static void main(String[] args) throws Exception {

        WechatMessageUtil wmsg=new WechatMessageUtil();
        String token=wmsg.getToken();
        System.out.println(token);
        wmsg.sendWechatMsg("test message");


    }
}
