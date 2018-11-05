package com.exshell.ops.activity.job.helper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import okhttp3.*;
import okhttp3.OkHttpClient.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * API client.
 *
 * @Date 2018/1/14
 * @Time 16:02
 */
@Component
public class ApiClient {

    static final int CONN_TIMEOUT = 5;
    static final int READ_TIMEOUT = 5;
    static final int WRITE_TIMEOUT = 5;


    static final String API_URL = "https://api.huobi.pro";
    static final String COINMARKETCAP_API_URL = "https://api.coinmarketcap.com/v2/ticker/?structure=array&convert=";

    static final String API_HOST = getHost();

    static final MediaType JSON = MediaType.parse("application/json");
    static final OkHttpClient client = createOkHttpClient();

    static final String accessKeyId = "1d3fa4c5-82d874e1-75c7400e-d28d0";
    static final String accessKeySecret = "a13696c4-7c7b80c7-513b19fa-8be21";
    final String assetPassword = "";

    static final String API_KEY = "1d3fa4c5-82d874e1-75c7400e-d28d0";
    static final String API_SECRET = "a13696c4-7c7b80c7-513b19fa-8be21";
    // /**
    //  * 创建一个ApiClient实例
    //  *
    //  * @param accessKeyId     AccessKeyId
    //  * @param accessKeySecret AccessKeySecret
    //  */
    // public ApiClient(String accessKeyId, String accessKeySecret) {
    //     this.accessKeyId = accessKeyId;
    //     this.accessKeySecret = accessKeySecret;
    //     this.assetPassword = null;
    // }
    //
    // /**
    //  * 创建一个ApiClient实例
    //  *
    //  * @param accessKeyId     AccessKeyId
    //  * @param accessKeySecret AccessKeySecret
    //  * @param assetPassword   AssetPassword
    //  */
    // public ApiClient(String accessKeyId, String accessKeySecret, String assetPassword) {
    //     this.accessKeyId = accessKeyId;
    //     this.accessKeySecret = accessKeySecret;
    //     this.assetPassword = assetPassword;
    // }

    /**
     * GET /market/detail 获取 Market Detail 24小时成交量数据
     *
     * @param symbol
     * @return
     */
    public DetailResponse detail(String symbol) {
        HashMap map = new HashMap();
        map.put("symbol", symbol);
        DetailResponse resp = get("/market/detail", map, new TypeReference<DetailResponse<Details>>() {
        });
        return resp;
    }

    /**
     * GET /market/detail 获取 Market Detail 24小时成交量数据
     *
     * @param symbol USDT CNY
     * @return
     */
    public String detail1(String symbol) {
        try {
            Request.Builder builder = new Request.Builder().url(COINMARKETCAP_API_URL+symbol.toUpperCase()).get();

            Request request = builder.build();
            Response response = client.newCall(request).execute();
            String s = response.body().string();
            return s;
        } catch (IOException e) {
            throw new ApiException(e);
        }


        // return resp;
    }


    // send a GET request.
    <T> T get(String uri, Map<String, String> params, TypeReference<T> ref) {
        if (params == null) {
            params = new HashMap<>();
        }
        return call("GET", uri, null, params, ref);
    }

    // send a POST request.
    <T> T post(String uri, Object object, TypeReference<T> ref) {
        return call("POST", uri, object, new HashMap<String, String>(), ref);
    }

    // call api by endpoint.
    <T> T call(String method, String uri, Object object, Map<String, String> params,
               TypeReference<T> ref) {
        ApiSignature sign = new ApiSignature();
        sign.createSignature(this.accessKeyId, this.accessKeySecret, method, API_HOST, uri, params);
        try {
            Request.Builder builder = null;
            if ("POST".equals(method)) {
                RequestBody body = RequestBody.create(JSON, JsonUtil.writeValue(object));
                builder = new Request.Builder().url(API_URL + uri + "?" + toQueryString(params)).post(body);
            } else {
                builder = new Request.Builder().url(API_URL + uri + "?" + toQueryString(params)).get();
            }
            if (this.assetPassword != null) {
                builder.addHeader("AuthData", authData());
            }
            Request request = builder.build();
            Response response = client.newCall(request).execute();
            String s = response.body().string();
            return JsonUtil.readValue(s, ref);
        } catch (IOException e) {
            throw new ApiException(e);
        }
    }

    String authData() {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        md.update(this.assetPassword.getBytes(StandardCharsets.UTF_8));
        md.update("hello, moto".getBytes(StandardCharsets.UTF_8));
        Map<String, String> map = new HashMap<>();
        map.put("assetPwd", DatatypeConverter.printHexBinary(md.digest()).toLowerCase());
        try {
            return ApiSignature.urlEncode(JsonUtil.writeValue(map));
        } catch (IOException e) {
            throw new RuntimeException("Get json failed: " + e.getMessage());
        }
    }

    // Encode as "a=1&b=%20&c=&d=AAA"
    String toQueryString(Map<String, String> params) {
        return String.join("&", params.entrySet().stream().map((entry) -> {
            return entry.getKey() + "=" + ApiSignature.urlEncode(entry.getValue());
        }).collect(Collectors.toList()));
    }

    // create OkHttpClient:
    static OkHttpClient createOkHttpClient() {
        return new Builder().connectTimeout(CONN_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS).writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .build();
    }

    static String getHost() {
        String host = null;
        try {
            host = new URL(API_URL).getHost();
        } catch (MalformedURLException e) {
            System.err.println("parse API_URL error,system exit!,please check API_URL:" + API_URL);
            System.exit(0);
        }
        return host;
    }

}


// /**
//  * API签名，签名规范：
//  * <p>
//  * http://docs.aws.amazon.com/zh_cn/general/latest/gr/signature-version-2.html
//  *
//  * @Date 2018/1/14
//  * @Time 16:02
//  */
// class ApiSignature {
//
//     final Logger log = LoggerFactory.getLogger(getClass());
//
//     static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
//     static final ZoneId ZONE_GMT = ZoneId.of("Z");
//
//     /**
//      * 创建一个有效的签名。该方法为客户端调用，将在传入的params中添加AccessKeyId、Timestamp、SignatureVersion、SignatureMethod、Signature参数。
//      *
//      * @param appKey       AppKeyId.
//      * @param appSecretKey AppKeySecret.
//      * @param method       请求方法，"GET"或"POST"
//      * @param host         请求域名，例如"be.huobi.com"
//      * @param uri          请求路径，注意不含?以及后的参数，例如"/v1/api/info"
//      * @param params       原始请求参数，以Key-Value存储，注意Value不要编码
//      */
//     public void createSignature(String appKey, String appSecretKey, String method, String host,
//                                 String uri, Map<String, String> params) {
//         StringBuilder sb = new StringBuilder(1024);
//         sb.append(method.toUpperCase()).append('\n') // GET
//                 .append(host.toLowerCase()).append('\n') // Host
//                 .append(uri).append('\n'); // /path
//         params.remove("Signature");
//         params.put("AccessKeyId", appKey);
//         params.put("SignatureVersion", "2");
//         params.put("SignatureMethod", "HmacSHA256");
//         params.put("Timestamp", gmtNow());
//         // build signature:
//         SortedMap<String, String> map = new TreeMap<>(params);
//         for (Map.Entry<String, String> entry : map.entrySet()) {
//             String key = entry.getKey();
//             String value = entry.getValue();
//             sb.append(key).append('=').append(urlEncode(value)).append('&');
//         }
//         // remove last '&':
//         sb.deleteCharAt(sb.length() - 1);
//         // sign:
//         Mac hmacSha256 = null;
//         try {
//             hmacSha256 = Mac.getInstance("HmacSHA256");
//             SecretKeySpec secKey =
//                     new SecretKeySpec(appSecretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
//             hmacSha256.init(secKey);
//         } catch (NoSuchAlgorithmException e) {
//             throw new RuntimeException("No such algorithm: " + e.getMessage());
//         } catch (InvalidKeyException e) {
//             throw new RuntimeException("Invalid key: " + e.getMessage());
//         }
//         String payload = sb.toString();
//         byte[] hash = hmacSha256.doFinal(payload.getBytes(StandardCharsets.UTF_8));
//         String actualSign = Base64.getEncoder().encodeToString(hash);
//         params.put("Signature", actualSign);
//
//
//         if (log.isDebugEnabled()) {
//             log.debug("Dump parameters:");
//             for (Map.Entry<String, String> entry : params.entrySet()) {
//                 log.debug("  key: " + entry.getKey() + ", value: " + entry.getValue());
//             }
//         }
//     }
//
//
//     /**
//      * 使用标准URL Encode编码。注意和JDK默认的不同，空格被编码为%20而不是+。
//      *
//      * @param s String字符串
//      * @return URL编码后的字符串
//      */
//     public static String urlEncode(String s) {
//         try {
//             return URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20");
//         } catch (UnsupportedEncodingException e) {
//             throw new IllegalArgumentException("UTF-8 encoding not supported!");
//         }
//     }
//
//     /**
//      * Return epoch seconds
//      */
//     long epochNow() {
//         return Instant.now().getEpochSecond();
//     }
//
//     String gmtNow() {
//         return Instant.ofEpochSecond(epochNow()).atZone(ZONE_GMT).format(DT_FORMAT);
//     }
// }


class JsonUtil {

    public static String writeValue(Object obj) throws IOException {
        return objectMapper.writeValueAsString(obj);
    }

    public static <T> T readValue(String s, TypeReference<T> ref) throws IOException {
        return objectMapper.readValue(s, ref);
    }

    static final ObjectMapper objectMapper = createObjectMapper();

    static ObjectMapper createObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        // disabled features:
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

}
