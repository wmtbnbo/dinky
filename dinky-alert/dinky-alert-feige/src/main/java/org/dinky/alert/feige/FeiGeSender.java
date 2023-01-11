/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.dinky.alert.feige;

import static java.util.Objects.requireNonNull;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.dinky.alert.AlertResult;
import org.dinky.utils.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FeiGeSender
 *
 * @author wangmu
 * @since 2022/9/21 09:58
 **/
public class FeiGeSender {

    private static final Logger logger = LoggerFactory.getLogger(FeiGeSender.class);
    private static String appkey = "xxxxxxxxxx";
    private static String apisecret = "xxxxxxxxxxx";
    private static String webhookUrl = "https://xxxxxxxxx/xxxxxx/xxxx/send";
    private final String chatid;
    private final String keyWord;

    FeiGeSender(Map<String, String> config) {
        webhookUrl = (config.get(FeiGeConstants.WEBHOOK) == null || config.get(FeiGeConstants.WEBHOOK).isEmpty())
                ? webhookUrl
                : config.get(FeiGeConstants.WEBHOOK);// url
        chatid = config.get(FeiGeConstants.CHATID);
        appkey = (config.get(FeiGeConstants.APPKEY) == null || config.get(FeiGeConstants.APPKEY).isEmpty())
                ? appkey
                : config.get(FeiGeConstants.APPKEY);
        apisecret = (config.get(FeiGeConstants.APISECRET) == null || config.get(FeiGeConstants.APISECRET).isEmpty())
                ? apisecret
                : config.get(FeiGeConstants.APISECRET);
        keyWord = config.get(FeiGeConstants.KEYWORD);
        requireNonNull(keyWord, FeiGeConstants.KEYWORD + " must not null");
    }

    public AlertResult send(String title, String content) {
        AlertResult alertResult = new AlertResult();
        String val = keyWord + FeiGeConstants.SPLIT + title + FeiGeConstants.SPLIT
                + content.substring(2, content.length() - 2).trim()
                        .replaceAll("\"", "")
                        .replaceAll(",", FeiGeConstants.SPLIT);
        HashMap<String, String> map = new HashMap<>();
        map.put("content", val);
        map.put("chatid", chatid);
        String msg = JSONUtil.toJsonString(map);
        try {
            return checkUserApiSendMsgResult(post(webhookUrl, msg));
        } catch (Exception e) {
            logger.info("send Fei Ge alert msg  exception : {}", e.getMessage());
            alertResult.setMessage("send Fei Ge alert fail");
            alertResult.setSuccess(false);
        }
        return alertResult;
    }

    private static String post(String url, String data) throws IOException {
        long notice = System.currentTimeMillis() / 1000;
        String mw = "appkey" + appkey + "notice" + notice + "timestamp" + notice + apisecret;
        String md5 = DigestUtils.md5Hex(mw).toUpperCase();
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Content-type", "application/json");
            httpPost.addHeader("appkey", appkey);
            httpPost.addHeader("signature", md5);
            httpPost.addHeader("timestamp", String.valueOf(notice));
            httpPost.addHeader("notice", String.valueOf(notice));
            httpPost.setEntity(new StringEntity(data, FeiGeConstants.CHARSET));
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String resp;
            try {
                HttpEntity entity = response.getEntity();
                resp = EntityUtils.toString(entity, FeiGeConstants.CHARSET);
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
            return resp;
        }
    }

    private static AlertResult checkUserApiSendMsgResult(String result) {
        AlertResult alertResult = new AlertResult();
        alertResult.setSuccess(false);
        if (null == result) {
            alertResult.setMessage("Fei Ge send fail");
            logger.info("send Fei Ge msg error,resp is null");
            return alertResult;
        }
        Map<String, String> sendMsgResponse = JSONUtil.toMap(result);
        if (null == sendMsgResponse) {
            alertResult.setMessage("Fei Ge send fail");
            logger.info("send Fei Ge msg error,resp error");
            return alertResult;
        }
        if (sendMsgResponse.get("code").equals("0000")) {
            alertResult.setSuccess(true);
            alertResult.setMessage("Fei Ge alert send success");
            return alertResult;
        }
        alertResult.setSuccess(false);
        alertResult.setMessage(sendMsgResponse.get("desc"));
        return alertResult;
    }

}
