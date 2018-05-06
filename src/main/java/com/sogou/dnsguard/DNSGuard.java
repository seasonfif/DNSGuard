package com.sogou.dnsguard;

import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

public class DNSGuard {

    private static volatile DNSGuard sInstance;

    private static GuardianMap sGuardians;
    private SafeDNS safeDNS;

    private DNSGuard(){
        safeDNS = new SafeDNS();
    }

    public static DNSGuard getsInstance() {
        if (sInstance == null){
            synchronized (DNSGuard.class){
                if (sInstance == null){
                    sInstance = new DNSGuard();
                }
            }
        }
        return sInstance;
    }

    /**
     * 设置域名对应的ip以及匹配规则用以判断是否发生DNS拦截
     * 必须在网络请求之前进行设置，建议Application中
     * @param guardians
     */
    public static void guard(GuardianMap guardians){
        sGuardians = guardians;
    }

    /**
     * HttpClient/HttpURLConnection接入时通过域名获取直连ip
     * 如果发生劫持将会返回配置的ip，如果没有预先配置将会返回null
     * 否则返回原域名
     * @param domain
     * @return
     */
    public String getIpByHost(String domain){
        try {
            InetAddress inetAddresses = InetAddress.getByName(domain);
            if (safeDNS.isHijack(new InetAddress[]{inetAddresses}, domain)){
                return getRandomIpFromGuardian(domain);
            }
        } catch (UnknownHostException e) {
            return getRandomIpFromGuardian(domain);
        }
        return domain;
    }

    /**
     * OkHttp接入时在自定义的DNS里面调用此方法即可
     * @param domain
     * @return
     * @throws UnknownHostException
     */
    public List<InetAddress> lookup(String domain) throws UnknownHostException{
        return safeDNS.lookup(domain);
    }

    /**
     * 发生劫持时返回配置的ip
     * @param domain
     * @return
     * @throws UnknownHostException
     */
    InetAddress[] resolveHijack(String domain) throws UnknownHostException {
        String ip = getRandomIpFromGuardian(domain);
        if (ip != null){
            return InetAddress.getAllByName(ip);
        }else{
            throw new UnknownHostException(String.format("DNSGuard未找到%s配置的ips", domain));
        }
    }

    /**
     * 查找配置的规则
     * @param domain
     * @return
     */
    Guardian lookfor(String domain) {
        if (sGuardians != null){
            return sGuardians.get(domain);
        }
        return null;
    }

    /**
     * 随机获取一个Guardian配置的ip
     * @param domain
     * @return
     */
    private String getRandomIpFromGuardian(String domain) {
        Guardian guardian = lookfor(domain);
        if (guardian != null){
            String[] ips = guardian.ips;
            if (ips != null && ips.length > 0){
                return getDirectIpForHttp(ips);
            }
        }
        return null;
    }

    /**
     * 随机选择一个配置里的ip
     * @param ips
     * @return
     */
    private String getDirectIpForHttp(String[] ips) {
        String directIp;
        int ipCount = ips.length;
        if (ipCount > 1){
            int index = new Random().nextInt(ipCount);
            directIp = ips[index];
        }else{
            directIp = ips[0];
        }
        Log.e("DNSGuard","尝试使用ip: "+directIp);
        return directIp;
    }
}
