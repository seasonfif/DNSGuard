package com.sogou.dnsguard;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

interface IDNS {

    List<InetAddress> lookup(String hostname) throws UnknownHostException;
}
