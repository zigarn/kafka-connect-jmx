/*
 * Copyright 2018 Alexandre Garnier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zigarn.kafka.connect.jmx;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.apache.kafka.common.config.types.Password;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JmxSourceConnector extends SourceConnector {

    public static final String TOPIC_CONFIG = "topic";
    public static final String JMX_URL_CONF = "jmx.url";
    public static final String JMX_USERNAME_CONF = "jmx.username";
    public static final String JMX_PASSWORD_CONF = "jmx.password";
    public static final String CONNECTION_ATTEMPTS_CONF = "connection.attempts";
    public static final String CONNECTION_BACKOFF_CONF = "connection.backoff.ms";

    private static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(TOPIC_CONFIG, Type.STRING, Importance.HIGH, "The topic to publish data to")
            .define(JMX_URL_CONF, Type.STRING, Importance.HIGH, "The JMX URL to fetch data from")
            .define(JMX_USERNAME_CONF, Type.STRING, "", Importance.MEDIUM, "The username to connect to JMX")
            .define(JMX_PASSWORD_CONF, Type.PASSWORD, "", Importance.MEDIUM, "The password to connect to JMX")
            .define(CONNECTION_ATTEMPTS_CONF, Type.INT, 3, ConfigDef.Range.atLeast(0), Importance.LOW, "Maximum number of attempts to retrieve a JMX connection")
            .define(CONNECTION_BACKOFF_CONF, Type.LONG, 10000L, ConfigDef.Range.atLeast(0L), Importance.LOW, "Backoff time in milliseconds between connection attempts");

    private String topic;
    private String jmxUrl;
    private String jmxUsername;
    private Password jmxPassword;
    private Integer maxConnectionAttempts;
    private Long connectionRetryBackoff;

    @Override
    public String version() {
        return JmxSourceConnector.class.getPackage().getImplementationVersion();
    }

    @Override
    public void start(Map<String, String> props) {
        Map<String, Object> parsedProps = CONFIG_DEF.parse(props);
        topic = (String) parsedProps.get(TOPIC_CONFIG);
        jmxUrl = (String) parsedProps.get(JMX_URL_CONF);
        jmxUsername = (String) parsedProps.get(JMX_USERNAME_CONF);
        jmxPassword = (Password) parsedProps.get(JMX_PASSWORD_CONF);
        maxConnectionAttempts = (Integer) parsedProps.get(CONNECTION_ATTEMPTS_CONF);
        connectionRetryBackoff = (Long) parsedProps.get(CONNECTION_BACKOFF_CONF);
        if (topic.contains(","))
            throw new ConfigException("JmxSourceConnector should only have a single topic.");
    }

    @Override
    public Class<? extends Task> taskClass() {
        return JmxSourceTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        ArrayList<Map<String, String>> configs = new ArrayList<>(1);
        // Only one JMX connection makes sense.
        Map<String, String> config = new HashMap<>();
        config.put(TOPIC_CONFIG, topic);
        config.put(JMX_URL_CONF, jmxUrl);
        config.put(JMX_USERNAME_CONF, jmxUsername);
        config.put(JMX_PASSWORD_CONF, jmxPassword.value());
        config.put(CONNECTION_ATTEMPTS_CONF, maxConnectionAttempts.toString());
        config.put(CONNECTION_BACKOFF_CONF, connectionRetryBackoff.toString());

        configs.add(config);
        return configs;
    }

    @Override
    public void stop() {
        // Nothing to do since JmxSourceConnector has no background monitoring.
    }

    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }

}
