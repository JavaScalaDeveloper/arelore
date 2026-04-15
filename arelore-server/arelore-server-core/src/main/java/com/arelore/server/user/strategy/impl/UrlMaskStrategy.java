package com.arelore.server.user.strategy.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.arelore.server.user.strategy.SensitiveMaskStrategy;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * URL 链接脱敏策略
 */
@Component
public class UrlMaskStrategy implements SensitiveMaskStrategy {
    
    private static final Pattern URL_PATTERN = Pattern.compile(
        "\\b(?:https?://|ftp://|www\\.)[^\\s<>\"']+\\b"
    );
    
    private String matchedExample;
    
    @Override
    public String mask(String text) {
        if (CharSequenceUtil.isBlank(text)) {
            matchedExample = null;
            return text;
        }
        
        matchedExample = null;
        // 使用正则匹配URL，保留协议和域名部分
        return URL_PATTERN.matcher(text).replaceAll(match -> {
            String url = match.group();
            if (matchedExample == null) {
                matchedExample = url;
            }
            
            try {
                // 尝试提取协议和域名部分
                String protocol = "";
                String domain = "";
                String rest = "";
                
                if (url.startsWith("http://")) {
                    protocol = "http://";
                    String withoutProtocol = url.substring(7);
                    int domainEnd = withoutProtocol.indexOf('/');
                    if (domainEnd == -1) domainEnd = withoutProtocol.length();
                    domain = withoutProtocol.substring(0, domainEnd);
                    rest = withoutProtocol.substring(domainEnd);
                } else if (url.startsWith("https://")) {
                    protocol = "https://";
                    String withoutProtocol = url.substring(8);
                    int domainEnd = withoutProtocol.indexOf('/');
                    if (domainEnd == -1) domainEnd = withoutProtocol.length();
                    domain = withoutProtocol.substring(0, domainEnd);
                    rest = withoutProtocol.substring(domainEnd);
                } else if (url.startsWith("ftp://")) {
                    protocol = "ftp://";
                    String withoutProtocol = url.substring(6);
                    int domainEnd = withoutProtocol.indexOf('/');
                    if (domainEnd == -1) domainEnd = withoutProtocol.length();
                    domain = withoutProtocol.substring(0, domainEnd);
                    rest = withoutProtocol.substring(domainEnd);
                } else if (url.startsWith("www.")) {
                    protocol = "www.";
                    String withoutProtocol = url.substring(4);
                    int domainEnd = withoutProtocol.indexOf('/');
                    if (domainEnd == -1) domainEnd = withoutProtocol.length();
                    domain = withoutProtocol.substring(0, domainEnd);
                    rest = withoutProtocol.substring(domainEnd);
                } else {
                    return url.substring(0, Math.min(10, url.length())) + "...";
                }
                
                // 脱敏域名，只保留前3个字符
                if (domain.length() > 3) {
                    domain = domain.substring(0, 3) + "***";
                }
                
                // 脱敏路径部分
                if (!rest.isEmpty() && rest.length() > 3) {
                    rest = "***";
                }
                
                return protocol + domain + rest;
            } catch (Exception e) {
                // 如果解析失败，只保留前10个字符
                return url.substring(0, Math.min(10, url.length())) + "...";
            }
        });
    }
    
    @Override
    public String getType() {
        return "url";
    }
    
    @Override
    public String getExample() {
        return matchedExample;
    }
}
