/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.oap.server.core.alarm.provider.discord;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.oap.server.core.alarm.AlarmMessage;
import org.apache.skywalking.oap.server.core.alarm.HttpAlarmCallback;
import org.apache.skywalking.oap.server.core.alarm.provider.AlarmRulesWatcher;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Use SkyWalking alarm Discord webhook API.
 */
@Slf4j
@RequiredArgsConstructor
public class DiscordHookCallback extends HttpAlarmCallback {
    private final AlarmRulesWatcher alarmRulesWatcher;

    /**
     * Send alarm message if the settings not empty
     */
    @Override
    public void doAlarm(List<AlarmMessage> alarmMessages) throws Exception {
        final var discordSettings = alarmRulesWatcher.getDiscordSettings();
        if (discordSettings == null || discordSettings.getWebhooks().isEmpty()) {
            return;
        }
        for (final var webHookUrl : discordSettings.getWebhooks()) {
            for (final var alarmMessage : alarmMessages) {
                final var content = String.format(
                        discordSettings.getTextTemplate(),
                        alarmMessage.getAlarmMessage()
                );
                sendAlarmMessage(webHookUrl, content);
            }
        }
    }

    /**
     * Send alarm message to remote endpoint
     */
    private void sendAlarmMessage(DiscordSettings.WebHookUrl webHookUrl, String content) throws IOException, InterruptedException {
        final var body = new JsonObject();
        body.addProperty("username", webHookUrl.getUsername());
        body.addProperty("content", content);
        final var requestBody = body.toString();
        post(URI.create(webHookUrl.getUrl()), requestBody, Map.of());
    }

}
