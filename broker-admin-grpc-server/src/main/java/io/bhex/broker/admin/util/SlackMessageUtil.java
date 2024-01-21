package io.bhex.broker.admin.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class SlackMessageUtil {

    private static final String webhookUrl = "https://hooks.slack.com/services/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";

    @Async
    public void sendSlackMsg(String msg) throws Exception {
        Map<String, String> contentMap = Maps.newHashMap();
        contentMap.put("text", msg);

        OkHttpClient httpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(webhookUrl)
                .post(RequestBody.create(MediaType.parse("application/json;charset=utf-8"), JSON.toJSONString(contentMap)))
                .build();

        Call call = httpClient.newCall(request);
        @Cleanup
        Response response = call.execute();
        log.info("{} {}", msg, response.code());
    }

    public static void main(String[] args) throws Exception {
        new SlackMessageUtil().sendSlackMsg("test");
    }
}
