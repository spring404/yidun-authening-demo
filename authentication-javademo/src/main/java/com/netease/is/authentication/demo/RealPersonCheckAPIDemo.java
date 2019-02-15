/*
 * @(#) ImageCheckAPIDemo.java 2016年3月15日
 *
 * Copyright 2010 NetEase.com, Inc. All rights reserved.
 */
package com.netease.is.authentication.demo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.is.authentication.demo.utils.HttpClient4Utils;
import com.netease.is.authentication.demo.utils.SignatureUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 1. httpclient，用于发送http请求
 * 2. commons-codec，使用md5算法生成签名信息，详细见SignatureUtils.java
 * 3. gson，用于做json解析
 *
 */
public class RealPersonCheckAPIDemo {
    /** 产品密钥ID，产品标识 */
    private final static String SECRETID = "ff31feb1b3ec65b60ca20f7db6d91594";
    /** 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露 */
    private final static String SECRETKEY = "10a97c3144d300f8676d7482faead93c";
    /** 业务ID，易盾根据产品业务特点分配 */
    private final static String BUSINESSID = "dd6bd0f0c1c24605bde49a612ff51628";
    /** 接口地址 */
    private final static String API_URL = "httpS://verify.dun.163yun.com/v1/rp/check";
    /** 实例化HttpClient，发送http请求使用，可根据需要自行调参 */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 10000, 2000, 2000);

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> params = buildParam();
        String response = HttpClient4Utils.sendPost(httpClient, API_URL, params, Consts.UTF_8);
        JsonObject jObject = new JsonParser().parse(response).getAsJsonObject();
        int code = jObject.get("code").getAsInt();
        if (code == 200) {
            JsonObject resultObject = jObject.getAsJsonObject("result");
            int status = resultObject.get("status").getAsInt();
            String taskId = resultObject.get("taskId").getAsString();
            if (status == 1) {
                double similarityScore =  resultObject.get("similarityScore").getAsDouble();
                System.out.println(String.format("taskId=%s，姓名身份证认证通过, 头像相似度得分 = %s", taskId, similarityScore));
            } else if (status == 2) {
                int reasonType = resultObject.get("reasonType").getAsInt();
                System.out.println(String.format("taskId=%s，姓名身份证认证不通过，不通过原因：%s", taskId, reasonType));
            }
        } else {
            System.out.println(String.format("ERROR: code=%s", code));
        }
    }

    private static Map<String, String> buildParam() throws IOException {
        final Map<String, String> params =  new HashMap<String, String>();
        params.put("secretId", SECRETID);
        params.put("businessId", BUSINESSID);
        params.put("nonce", String.valueOf(RandomUtils.nextLong(10000, 10000000)));
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("version", "v1");
        // 2.设置私有参数
        params.put("name", "张三");
        params.put("cardNo", "36042519880801493X");
        params.put("picType", "1");
        params.put("avatar", "http://xxx.com/xxx.jpg");
        params.put("callback", "ebfcad1c-dba1-490c-b4de-e784c2691768");
        String signature = SignatureUtils.genSignature(SECRETKEY, params);
        params.put("signature", signature);
        return params;
    }
}
