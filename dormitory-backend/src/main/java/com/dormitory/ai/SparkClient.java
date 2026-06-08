package com.dormitory.ai;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dormitory.config.SparkProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 讯飞星火 OpenAI 兼容 HTTP 接口客户端。
 * 统一封装一次 chat completions 调用。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SparkClient {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final SparkProperties props;

    private OkHttpClient httpClient;

    private OkHttpClient client() {
        if (httpClient == null) {
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(props.getTimeout(), TimeUnit.SECONDS)
                    .writeTimeout(props.getTimeout(), TimeUnit.SECONDS)
                    .build();
        }
        return httpClient;
    }

    /**
     * 单轮对话。
     *
     * @param systemPrompt 系统提示（角色设定），可空
     * @param userPrompt   用户输入
     * @param jsonMode     是否要求模型返回 JSON
     * @return 模型回复文本
     */
    public String chat(String systemPrompt, String userPrompt, boolean jsonMode) {
        if (props.getApiPassword() == null || props.getApiPassword().trim().isEmpty()
                || props.getApiPassword().contains("请替换")) {
            throw new SparkException("未配置讯飞星火 APIPassword，请在 application.yml 或环境变量 SPARK_API_PASSWORD 中设置");
        }

        // 构造 messages
        JSONArray messages = new JSONArray();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(new JSONObject().set("role", "system").set("content", systemPrompt));
        }
        messages.add(new JSONObject().set("role", "user").set("content", userPrompt));

        JSONObject body = new JSONObject();
        body.set("model", props.getModel());
        body.set("messages", messages);
        body.set("temperature", 0.5);
        body.set("stream", false);
        if (jsonMode) {
            body.set("response_format", new JSONObject().set("type", "json_object"));
        }

        Request request = new Request.Builder()
                .url(props.getUrl())
                .addHeader("Authorization", "Bearer " + props.getApiPassword())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        try (Response response = client().newCall(request).execute()) {
            String respBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                log.error("星火接口调用失败 http={} body={}", response.code(), respBody);
                throw new SparkException("星火接口调用失败：HTTP " + response.code());
            }
            JSONObject json = JSONUtil.parseObj(respBody);
            // 业务错误码
            Integer code = json.getInt("code");
            if (code != null && code != 0) {
                throw new SparkException("星火返回错误：" + json.getStr("message"));
            }
            JSONArray choices = json.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                throw new SparkException("星火返回内容为空");
            }
            return choices.getJSONObject(0).getJSONObject("message").getStr("content");
        } catch (SparkException e) {
            throw e;
        } catch (Exception e) {
            log.error("星火接口异常", e);
            throw new SparkException("星火接口调用异常：" + e.getMessage());
        }
    }

    /** 星火调用异常 */
    public static class SparkException extends RuntimeException {
        public SparkException(String message) {
            super(message);
        }
    }
}
