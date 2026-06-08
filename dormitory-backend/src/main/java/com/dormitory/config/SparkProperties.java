package com.dormitory.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 讯飞星火大模型配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "spark")
public class SparkProperties {

    /** OpenAI 兼容接口地址 */
    private String url;

    /** 控制台获取的 APIPassword */
    private String apiPassword;

    /** 模型版本，如 lite / generalv3.5 / 4.0Ultra */
    private String model;

    /** 请求超时（秒） */
    private Integer timeout = 30;
}
