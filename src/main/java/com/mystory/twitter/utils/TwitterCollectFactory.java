package com.mystory.twitter.utils;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

@Component
@Log4j
public class TwitterCollectFactory {
    @Value("${blocked}")
    private Boolean blocked;
    @Value("${proxy.type}")
    private String proxyType;
    @Value("${proxy.host}")
    private String host;
    @Value("${proxy.port}")
    private Integer port;
    @Value("${proxy.user}")
    private String user;
    @Value("${proxy.password}")
    private String password;

    @Bean
    public Twitter getTwitter(@Value("${twitter4j.oauth.consumerKey}") String ck,
                              @Value("${twitter4j.oauth.consumerSecret}") String cc,
                              @Value("${twitter4j.oauth.accessToken}") String at,
                              @Value("${twitter4j.oauth.accessTokenSecret}") String ats) {
        log.error("-------------blocked-------------------" + this.toString());
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(ck)
                .setOAuthConsumerSecret(cc)
                .setOAuthAccessToken(at)
                .setOAuthAccessTokenSecret(ats);
        if (blocked) {
            if (proxyType == "socks5" || proxyType == "socks" || proxyType == "socks4") {
                cb.setHttpProxySocks(true);
            }
            cb.setHttpProxyHost(host).setHttpProxyPort(port);
            if (user != null && password != null) {
                cb.setHttpProxyUser(user).setHttpProxyPassword(password);
            }
            log.info(String.format("Proxy Settled, Type = %s, Host = %s, Port = %d, user = %s, password = %s",
                    proxyType, host, port, user, password));
        }
        TwitterFactory tf = new TwitterFactory(cb.build());
        return tf.getInstance();
    }
}
