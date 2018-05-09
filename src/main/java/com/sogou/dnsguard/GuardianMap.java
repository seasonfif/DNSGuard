package com.sogou.dnsguard;

import java.util.HashMap;

class GuardianMap {

    private HashMap<String, Guardian> map;

    public GuardianMap(){
        map = new HashMap<>();
    }

    void put(String domain, Guardian guardian){
        if (map != null){
            map.put(domain, guardian);
        }
    }

    Guardian get(String domain) {
        if (map != null && map.containsKey(domain)){
            return map.get(domain);
        }
        return null;
    }
}
