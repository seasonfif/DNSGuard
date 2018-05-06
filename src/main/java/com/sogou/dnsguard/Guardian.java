package com.sogou.dnsguard;

/**
 * ip匹配规则
 */
public class Guardian {

    /**
     * 域名
     */
    public String domain;

    /**
     * 域名对应的ips
     */
    public String[] ips;

    /**
     * 域名规则
     */
    public String pattern;

    public Guardian(String[] ips, String pattern){
        this.ips = ips;
        this.pattern = pattern;

    }
}
