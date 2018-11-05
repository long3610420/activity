package com.exshell.ops.activity.job.helper;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.exshell.bit.common.HttpHelper;
import net.logstash.logback.encoder.org.apache.commons.lang.exception.ExceptionUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
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

import static com.alibaba.dubbo.common.utils.StringUtils.toQueryString;

public class TradeApiClient {
    public static final Logger logger = LoggerFactory.getLogger(TradeApiClient.class);

    static final int CONN_TIMEOUT = 5;
    static final int READ_TIMEOUT = 5;
    static final int WRITE_TIMEOUT = 5;

    private String accessKey;
    private String secretKey;
    private String assetPassword;
    private String apiTradeHost;
    private String apiPrefix;

    static final MediaType JSON = MediaType.parse("application/json");
    static final OkHttpClient client = createOkHttpClient();

    private static OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder().connectTimeout(CONN_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS).writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .build();
    }

    public TradeApiClient(String accessKey, String secretKey, String assetPassword, String apiTradeHost, Boolean hadax) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.assetPassword = assetPassword;
        this.apiTradeHost = apiTradeHost;
        this.apiPrefix = hadax?"/hadax":"";
    }

    // send a GET request.
    public String get(String uri, Map<String, String> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        return call("GET", uri, params, null);
    }

    // send a POST request.
    public String post(String uri, Map<String, String> params, Object postData) {
        return call("POST", uri, params, postData);
    }

    // call api by endpoint.
    private String call(String method, String uri, Map<String, String> params, Object postData) {
        try {
            ApiSignature sign = new ApiSignature();
            URL aURL = new URL(this.apiTradeHost);
            params.put("random", String.valueOf(System.currentTimeMillis()));
            sign.createSignature(this.accessKey, this.secretKey, method, aURL.getHost(), uri, params);

            String response = null;
            if ("POST".equals(method)) {
                if (this.assetPassword != null) {
                    Map<String, Object> headerMap = new HashMap<>();
                    headerMap.put("AuthData", authData());
                    response = new HttpHelper().headers(headerMap).doPost(this.apiTradeHost + uri + "?" + toQueryString(params), JSONObject.toJSONString(postData));
                }else{
                    response = new HttpHelper().doPost(this.apiTradeHost + uri + "?" + toQueryString(params), JSONObject.toJSONString(postData));
                }
            } else {
                if (this.assetPassword != null) {
                    Map<String, Object> headerMap = new HashMap<>();
                    headerMap.put("AuthData", authData());
                    response = new HttpHelper().headers(headerMap).doGet(this.apiTradeHost + uri + "?" + toQueryString(params));
                }else{
                    response = new HttpHelper().doGet(this.apiTradeHost + uri + "?" + toQueryString(params));
                }
            }
            ApiResponse apiResponse = JSONObject.parseObject(response, ApiResponse.class);
            return apiResponse.checkAndReturn();
        } catch (IOException e) {
            logger.error("api res:{}", ExceptionUtils.getFullStackTrace(e));
            return null;
        }
    }

    private String authData() {
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
        return ApiSignature.urlEncode(com.exshell.bit.common.JsonUtil.writeValue(map));
    }

    public String getAccountId(){
        String res = this.get("/v1/account/accounts", new HashMap<>());

        Integer accountId = 0;

        JSONArray accounts = JSONObject.parseArray(res);
        for (int i = 0;i<accounts.size();i++){
            JSONObject account = accounts.getJSONObject(i);
            if(account.getString("type").equals("spot") && account.getString("state").equals("working")){
                accountId = account.getInteger("id");
            }
        }

        if(accountId == 0){
            throw new ApiException("error", "获取accountId失败");
        }
        return String.valueOf(accountId);
    }

    public Map<String, Map<String, String>> getBalance(String accountId){
        Map<String, Map<String, String>> result = new HashMap<>();
        String[] balanceUrls = new String[]{"/v1/account/accounts/"+accountId+"/balance", "/v1/hadax/account/accounts/"+accountId+"/balance"};

        for (String balanceUrl:balanceUrls){
            try{
                String res = this.get(balanceUrl, new HashMap<>());
                List<Map> balance = JSONObject.parseArray(JSONObject.parseObject(res).getString("list"), Map.class);
                balance.forEach(s->{
                    if(result.containsKey(s.get("currency").toString())){
                        result.get(s.get("currency").toString()).put(s.get("type").toString(), s.get("balance").toString());
                    }else{
                        Map<String, String> subRes = new HashMap<>();
                        subRes.put(s.get("type").toString(), s.get("balance").toString());
                        result.put(s.get("currency").toString(), subRes);
                    }
                });
            }catch(Exception ignored){}
        }
        return result;
    }

    public Map<String, String> getOrder(String orderId){
        String res = this.get("/v1/order/orders/" + orderId, new HashMap<>());
        return JSONObject.parseObject(res, Map.class);
    }

    public void cancelOrder(String orderId){
        this.post("/v1/order/orders/" + orderId + "/submitcancel", new HashMap<>(), null);
    }

    public String createOrder(String accountId, String symbol, String type, String price, String amount){
        Map<String, String> params = new HashMap<>();
        params.put("account-id", accountId);
        params.put("symbol", symbol);
        params.put("type", type);
        params.put("source", "api");
        params.put("amount", amount);
        params.put("price", price);

        return this.post("/v1"+this.apiPrefix+"/order/orders/place", new HashMap<>(), params);
    }

    public List<Long> getAllOrder(String symbol){

        Map<String, String> params = new HashMap<>();
        params.put("symbol", symbol);
        params.put("size", "50");
        params.put("states", "submitted,partial-filled");

        String data = this.get("/v1/order/orders", params);

        List<OrderDetail> orderDetailList = JSONObject.parseArray(data, OrderDetail.class);

        List<Long> result = new ArrayList<>();

        orderDetailList.forEach(s-> result.add(s.getId()));

        return result;
    }

    public String batchCancel(List<Long> orderIds){
        Map<String, List<Long>> params = new HashMap<>();
        params.put("order-ids", orderIds);
        return this.post("/v1/order/orders/batchcancel", new HashMap<>(), params);
    }
}

/**
 * API签名，签名规范：
 *
 * http://docs.aws.amazon.com/zh_cn/general/latest/gr/signature-version-2.html
 *
 * @author liaoxuefeng
 */
class ApiSignature {
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
    private static final ZoneId ZONE_GMT = ZoneId.of("Z");

    /**
     * 创建一个有效的签名。该方法为客户端调用，将在传入的params中添加AccessKeyId、Timestamp、SignatureVersion、SignatureMethod、Signature参数。
     *
     * @param appKey AppKeyId.
     * @param appSecretKey AppKeySecret.
     * @param method 请求方法，"GET"或"POST"
     * @param host 请求域名，例如"be.exshell.com"
     * @param uri 请求路径，注意不含?以及后的参数，例如"/v1/api/info"
     * @param params 原始请求参数，以Key-Value存储，注意Value不要编码
     */
    public void createSignature(String appKey, String appSecretKey, String method, String host,
                                String uri, Map<String, String> params) {
        StringBuilder sb = new StringBuilder(1024);
        sb.append(method.toUpperCase()).append('\n') // GET
                .append(host.toLowerCase()).append('\n') // Host
                .append(uri).append('\n'); // /path
        params.remove("Signature");
        params.put("AccessKeyId", appKey);
        params.put("SignatureVersion", "2");
        params.put("SignatureMethod", "HmacSHA256");
        params.put("Timestamp", gmtNow());
        // build signature:
        SortedMap<String, String> map = new TreeMap<>(params);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append('=').append(urlEncode(value)).append('&');
        }
        // remove last '&':
        sb.deleteCharAt(sb.length() - 1);
        // sign:
        Mac hmacSha256 = null;
        try {
            hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secKey = new SecretKeySpec(appSecretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSha256.init(secKey);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No such algorithm: " + e.getMessage());
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid key: " + e.getMessage());
        }
        String payload = sb.toString();
        byte[] hash = hmacSha256.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        String actualSign = urlEncode(Base64.getEncoder().encodeToString(hash));
        params.put("Signature", actualSign);
    }

    /**
     * 使用标准URL Encode编码。注意和JDK默认的不同，空格被编码为%20而不是+。
     */
    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("UTF-8 encoding not supported!");
        }
    }

    private long epochNow() {
        return Instant.now().getEpochSecond();
    }

    private String gmtNow() {
        return Instant.ofEpochSecond(epochNow()).atZone(ZONE_GMT).format(DT_FORMAT);
    }
}

class ApiException extends RuntimeException {

    final String errCode;

    public ApiException(String errCode, String errMsg) {
        super(errMsg);
        this.errCode = errCode;
    }

    public ApiException(Exception e) {
        super(e);
        this.errCode = e.getClass().getName();
    }

    public String getErrCode() {
        return this.errCode;
    }

}

class ApiResponse {
    public String status;
    public String errCode;
    public String errMsg;
    public String data;

    public String checkAndReturn() {
        if ("ok".equals(status)) {
            return data;
        }
        throw new ApiException(errCode, errMsg);
    }
}

class OrderDetail implements Serializable{
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}