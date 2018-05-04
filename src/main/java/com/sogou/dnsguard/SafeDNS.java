package com.sogou.dnsguard;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SafeDNS implements IDNS{

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {

        InetAddress[] inetAddresses;
        try{
            inetAddresses = InetAddress.getAllByName(hostname);
            if (isHijack(inetAddresses, hostname)){
                inetAddresses = DNSGuard.getsInstance().resolveHijack(hostname);
            }

        } catch (UnknownHostException e){

            inetAddresses = DNSGuard.getsInstance().resolveHijack(hostname);
        }

        return Arrays.asList(inetAddresses);
    }

    /**
     * DNS是否被劫持
     * @param inetAddresses
     * @param hostname
     * @return
     */
    private boolean isHijack(InetAddress[] inetAddresses, String hostname){
        boolean isHijack = false;
        if (inetAddresses != null && inetAddresses.length > 0){
            String inetAddress = inetAddresses[0].getHostAddress();
            String[] ips = DNSGuard.getsInstance().lookfor(hostname);
            if (ips != null){
                // 检测ip是否在设置的白名单里面
                if (!Arrays.asList(ips).contains(inetAddress)){
                    isHijack = true;


                    boolean match = Pattern.matches("(111.202.)*", inetAddress);
                    // 如果不在白名单
                    if (match){
                        isHijack = false;
                    }
                }
            }
        }
        return isHijack;
    }

    public static void main(String[] args){
        boolean match = Pattern.matches("111.202.\\d{1,3}.\\d{1,3}", "111.202.111.01");
        System.out.print(match);
    }
}


