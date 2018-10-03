package com.mystory.twitter.utils;

import com.google.common.base.Strings;
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
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(ck)
                .setOAuthConsumerSecret(cc)
                .setOAuthAccessToken(at)
                .setOAuthAccessTokenSecret(ats);
        if (blocked) {
            if (proxyType.equals("socks5") || proxyType.equals("socks") || proxyType.equals("socks4")) {
                cb.setHttpProxySocks(true);
            }
            cb.setHttpProxyHost(host).setHttpProxyPort(port);
            if (!Strings.isNullOrEmpty(user) && !Strings.isNullOrEmpty(password)) {
                cb.setHttpProxyUser(user).setHttpProxyPassword(password);
            }
            log.info(String.format("Proxy Settled, Type = %s, Host = %s, Port = %d, user = %s, password = %s",
                    proxyType, host, port, user, password));
        }
        TwitterFactory tf = new TwitterFactory(cb.build());
        return tf.getInstance();
    }
}
