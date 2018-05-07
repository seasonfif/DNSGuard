DNSGuard
========

一个通过预置安全ip和安全规则来检测和预防DNS劫持的方案

## **原理**
该方案通过Android系统DNS服务InetAddress.getAllByName()函数获取域名对应的ip，然后将ip和预先设置的安全ip和安全规则进行匹配，
从而判断网络请求的ip是否被劫持，如果发生劫持则换用预置的安全ip发起网络请求。

## **设置Gradle依赖**
1、在工程build.gradle文件中添加maven仓库地址 
```
allprojects {
    repositories {
        ...
        maven {
            url "http://maven.sogou/nexus/content/repositories/releases/"
        }

        maven {
            url "http://maven.sogou/nexus/content/repositories/snapshots/"
        }
    }
}
```

2、在使用的module中添加依赖
```
dependencies {
    compile 'com.sogou.commonlib:DNSGuard:1.0.0@aar''
}
```
## **接入方法**
#### **1、初始化**
在网络请求之前需要使用预置规则进行初始化，建议在Application中执行。
示例如下：
```java
 DNSGuard.guard(new GuardianMap()
                 .put("www.sogou.com",
                         new Guardian(new String[]{"111.202.102", "111.202.100.49", "111.202.102.51"}, "111.202.\\d{1,3}.\\d{1,3}"))
                 .put("www.baidu.com",
                         new Guardian(new String[]{"61.135.19.121"}, "61.135.19.\\d{1,3}")));
```
GuardianMap内部使用HashMap存储预置的安全规则，key为域名，value为Guardian对象。
Guardian对象描述了具体的安全规则，包括一组ip地址和一个正则匹配规则，构造函数如下：
```java
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
```

#### **2、OkHttp接入使用**
由于OkHttp支持自定义DNS，通过实现其Dns接口就可以快速集成且没有任何使用限制。
具体做法是在初始化OkHttp时通过setDns方法设置一个Dns的实现类。
在实现类内部调用DNSGuard.getsInstance().lookup()即可。
示例如下：
```java
OkHttpClient client = new OkHttpClient();
client.setDns(new Dns() {
    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        return DNSGuard.getsInstance().lookup(hostname);
    }
});
```
通过以上代码可以看到在OkHttp中接入DNSGuard实现简单，只需通过实现Dns接口即可接入。
而且通用性强，在HTTPS,SNI以及设置Cookie等场景均适用。规避了证书校验，域名检查等环节
#### **3、HttpURLConnection接入使用**
在HttpURLConnection接入DNSGuard会稍显繁琐且针对HTTPS、SNI、Cookie等场景需要单独处理。

1、普通使用场景
在原有URL的基础上，将域名替换为IP，然后用新的URL发起HTTP请求。
然而，标准的HTTP协议中服务端会将HTTP请求头中HOST字段的值作为请求的域名，在我们没有主动设置HOST字段的值时，
网络库也会自动地从URL中提取域名，并为请求做设置。但使用HttpDns后，URL中的域名信息丢失，
会导致默认情况下请求的HOST 头部字段无法被正确设置，进而导致服务端的异常。为了解决这个问题，需要主动地为请求设置HOST字段值。
示例如下：
```java
String originalUrl = "http://www.baidu.com/";
URL url = new URL(originalURL);
String originalHost = url.getHost(); 
// 同步接口获取IP 
String ip = DNSGuard.getsInstance().getIpByHost(originalHost); 
HttpURLConnection conn; 
if (ip != null) { 
    // 通过HTTPDNS获取IP成功，进行URL替换和HOST头设置 
    url = new URL(originalUrl.replaceFirst(originalHost, ip)); 
    conn = (HttpURLConnection) url.openConnection(); 
    // 设置请求HOST字段 
    conn.setRequestProperty("Host", originHost); 
} else { 
    conn = (HttpURLConnection) url.openConnection(); 
}
```

2、针对HTTPS（不含SNI）业务场景得接入说明：
```java
try {
    String originUrl = "https://www.baidu.com/";
    URL url = new URL(originUrl);
    final String originHost = url.getHost();
    // 同步接口获取IP
    String ip = DNSGuard.getsInstance().getIpByHost(originHost);
    HttpsURLConnection conn;
    if (ip != null) {
        // 通过HTTPDNS获取IP成功，进行URL替换和HOST头设置
        url = new URL(originUrl.replaceFirst(originHost, ip));
        conn = (HttpsURLConnection) url.openConnection();
        // 设置请求HOST字段
        conn.setRequestProperty("Host", originHost);
        conn.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return HttpsURLConnection.getDefaultHostnameVerifier().verify(originHost, session);
            }
        });
    } else {
        conn = (HttpsURLConnection) url.openConnection();
    }
} catch (IOException e) {
    e.printStackTrace();
}
```

3、针对含SNI的HTTPS业务场景得接入参见https://help.aliyun.com/document_detail/30143.html/

4、使用Cookie的业务场景接入参见https://yq.aliyun.com/articles/64356

## **反馈**
接入和使用过程中遇到的任何问题可以反馈到seasonfif@gmail.com