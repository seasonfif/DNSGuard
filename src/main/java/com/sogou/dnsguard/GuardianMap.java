package com.sogou.dnsguard;

import java.util.HashMap;

public class GuardianMap {

    private HashMap<String, String[]> map;

    public GuardianMap(){
        map = new HashMap<>();
    }

    public GuardianMap put(String domain, String[] guardian){
        if (map != null){
            map.put(domain, guardian);
        }
        return this;
    }

    public String[] get(String domain) {
        if (map != null && map.containsKey(domain)){
            return map.get(domain);
        }
        return null;
    }
}
