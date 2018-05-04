package com.sogou.dnsguard;

import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DNSGuard {

    private static volatile DNSGuard sInstance;

    private static GuardianMap sGuardians;
    private static Map<String, SafeDNS> sDNSCache;

    private DNSGuard(){
        sDNSCache = new HashMap<>();
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

    public static void guard(GuardianMap guardians){
        sGuardians = guardians;
    }

    public List<InetAddress> lookup(String domain) throws UnknownHostException{
        SafeDNS safeDNS = sDNSCache.get(domain);
        if (safeDNS == null){
            safeDNS = new SafeDNS();
            sDNSCache.put(domain, safeDNS);
        }
        return safeDNS.lookup(domain);
    }

    /**
     * 发生劫持时返回配置的ip
     * @param domain
     * @return
     * @throws UnknownHostException
     */
    public InetAddress[] resolveHijack(String domain) throws UnknownHostException {
        Log.e("DNSGuard","发生DNS劫持: "+domain);
        String[] ips = lookfor(domain);
        if (ips != null && ips.length > 0){
            return InetAddress.getAllByName(getDirectIpForHttp(ips));
        }else{
            throw new UnknownHostException(String.format("DNSGuard未找到%s配置的ips", domain));
        }
    }

    /**
     * 查找配置的规则
     * @param domain
     * @return
     */
    public String[] lookfor(String domain) {
        if (sGuardians != null){
            return sGuardians.get(domain);
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
        return directIp;
    }
}
