package com.exshell.ops.activity.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActivityConstants {
    public static final int INT_0 = 0;
    public static final int INT_1 = 1;
    public static final int INT_2 = 2;
    public static final String PWD = "654321";

    // dawn.broker.host=http://dawn-broker-bitex.devtest.exshell-dev.com
    private static String BROKER_HOST;
    private static String PRO_HOST;
    private static String BITEX_HOST;

    public static String getBrokerHost() {
        return BROKER_HOST;
    }

    @Value("${dawn.broker.host}")
    public void setBrokerHost(String brokerHost) {
        ActivityConstants.BROKER_HOST = brokerHost;
    }

    public static String getBitexHost() {
        return BITEX_HOST;
    }

    @Value("${bitex.general.host}")
    public void setBitexHost(String bitexHost) {
        ActivityConstants.BITEX_HOST = bitexHost;
    }


    public static String getProHost() {
        return PRO_HOST;
    }

    @Value("${pro.web.host}")
    public void setProHost(String proHost) {
        ActivityConstants.PRO_HOST = proHost;
    }
}
