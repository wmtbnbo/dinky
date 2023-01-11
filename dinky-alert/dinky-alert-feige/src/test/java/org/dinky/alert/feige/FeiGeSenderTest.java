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

import org.dinky.alert.AlertResult;
import org.dinky.alert.ShowType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * FeiGeSenderTest
 */
public class FeiGeSenderTest {

    private static Map<String, String> weChatConfig = new HashMap<>();

    private String contentTest = "[{\"id\":\"70\","
            +
            "\"name\":\"UserBehavior-0--1193959466\","
            +
            "\"Job name\":\"Start workflow\","
            +
            "\"State\":\"SUCCESS\","
            +
            "\"Recovery\":\"NO\","
            +
            "\"Run time\":\"1\","
            +
            "\"Start time\": \"2018-08-06 10:31:34.0\","
            +
            "\"End time\": \"2018-08-06 10:31:49.0\","
            +
            "\"Host\": \"192.168.xx.xx\","
            +
            "\"Notify group\" :\"4\"}]";

    /**
     * init FeiGeConfig
     */
    @Before
    public void initWeChatConfig() {
        // Just for this test, I will delete these configurations before this PR is merged
        weChatConfig.put(FeiGeConstants.WEBHOOK, "https://xxxxxxxxxxxx/xxxxxx/xxxxxx/send");
        weChatConfig.put(FeiGeConstants.CHATID, "xxxxxxxxxxxxxxxxxx");
        weChatConfig.put(FeiGeConstants.APPKEY, "xxxxxxxxxxxxx");
        weChatConfig.put(FeiGeConstants.APISECRET, "xxxxxxxxxxxxxx");
        weChatConfig.put(FeiGeConstants.SHOW_TYPE, ShowType.TEXT.getValue());// default is "table"
        weChatConfig.put(FeiGeConstants.KEYWORD, "企微WEBHOOK  TEXT方式 告警测试");

    }

    @Test
    public void testSend() throws IOException {
        FeiGeSender weChatSender = new FeiGeSender(weChatConfig);
        AlertResult alertResult = weChatSender.send("TEXT-TEST", contentTest);
        Assert.assertEquals(true, alertResult.getSuccess());
    }

}
