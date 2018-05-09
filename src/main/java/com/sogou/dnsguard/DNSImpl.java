package com.sogou.dnsguard;

import android.text.TextUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

class DNSImpl implements IDNS{

    private static String[] BLACK_DOMAIN = {"127.0.0.1"};

    private static List<String> BLACKLIST = Collections.EMPTY_LIST;

    static {
        BLACKLIST = Arrays.asList(BLACK_DOMAIN);
    }

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {

        InetAddress[] inetAddresses = null;
        try{
            long start = System.currentTimeMillis();
            inetAddresses = InetAddress.getAllByName(hostname);
            long end = System.currentTimeMillis();
            LogKit.log("InetAddress.getAllByName耗时："+(end-start));

            if (isHijack(inetAddresses, hostname)){

                inetAddresses = DNSGuard.getInstance().resolveHijack(hostname);

            }

        } catch (UnknownHostException e){

            inetAddresses = DNSGuard.getInstance().resolveHijack(hostname);

        } finally {

            if (inetAddresses == null) throw new UnknownHostException();

        }

        return Arrays.asList(inetAddresses);
    }

    /**
     * DNS是否被劫持
     * @param inetAddresses
     * @param hostname
     * @return
     */
    boolean isHijack(InetAddress[] inetAddresses, String hostname){
        boolean isHijack = false;
        if (inetAddresses != null && inetAddresses.length > 0){
            String inetAddress = inetAddresses[0].getHostAddress();
            LogKit.log("使用的ip为："+inetAddress);
            if(ipInBlack(inetAddress)){
                isHijack = true;
            }else{
                isHijack = judgeByGuardian(inetAddress, hostname);
            }
            LogKit.log("DNS"+(isHijack?"已被":"未被")+"劫持");
        }
        return isHijack;
    }

    /**
     * 根据配置的ip白名单和ip匹配规则判断是否劫持
     * 如果不在白名单且不匹配规则则判定为劫持
     * @param inetAddress
     * @param hostname
     * @return
     */
    private boolean judgeByGuardian(String inetAddress, String hostname) {
        boolean isHijack = false;
        Guardian guardian = DNSGuard.getInstance().lookfor(hostname);
        if (guardian != null){
            String[] ips = guardian.ips;
            String pattern = guardian.pattern;
            if (ips == null && pattern == null){
                LogKit.log("DNSGuard","未设置白名单和匹配规则" + ": " + inetAddress);
                isHijack = false;
            } else{
                if (ips != null && ips.length > 0){
                    // 检测ip是否在设置的白名单里面
                    if (!Arrays.asList(ips).contains(inetAddress)){
                        LogKit.log("DNSGuard","ip不在白名单："+inetAddress);
                        isHijack = true;

                        if (!TextUtils.isEmpty(pattern)){

                            boolean match = Pattern.matches(pattern, inetAddress);
                            // 是否匹配合法的ip规则
                            if (match){
                                LogKit.log("DNSGuard","ip符合规则："+pattern + ": " + inetAddress);
                                isHijack = false;
                            }else{
                                LogKit.log("DNSGuard","ip不符合规则："+pattern + ": " + inetAddress);
                            }
                        }
                    } else {
                        LogKit.log("DNSGuard","ip在白名单："+inetAddress);
                    }
                } else {
                    if (!TextUtils.isEmpty(pattern)){

                        boolean match = Pattern.matches(pattern, inetAddress);
                        // 是否匹配合法的ip规则
                        if (match){
                            LogKit.log("DNSGuard","未配置白名单但是ip符合规则："+pattern + ": " + inetAddress);
                        }else{
                            LogKit.log("DNSGuard","未配置白名单且ip不符合规则："+pattern + ": " + inetAddress);
                            isHijack = true;
                        }
                    }else{
                        LogKit.log("DNSGuard","未设置有效的白名单和匹配规则" + ": " + inetAddress);
                    }
                }
            }
        }
        return isHijack;
    }

    /**
     * 访问ip是否在黑名单中
     * 如果在黑名单则判定为劫持
     * @param inetAddress
     * @return
     */
    private boolean ipInBlack(String inetAddress) {
        return BLACKLIST.contains(inetAddress);
    }
}


