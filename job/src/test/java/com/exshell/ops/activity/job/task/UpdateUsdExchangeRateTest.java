//package com.exshell.bitex.general.task;
//
//import com.alibaba.fastjson.JSON;
//import com.exshell.start.Main;
//import javax.annotation.Resource;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringRunner;
//
///**
// * 更新usd更新汇率
// */
//@RunWith(SpringRunner.class)
//@ContextConfiguration(classes = {Main.class})
//public class UpdateUsdExchangeRateTest {
//
//    @Resource
//    private UpdateUsdExchangeRate updateUsdExchangeRate;
//
//    @Test
//    public void test1() throws Exception {
//        UpdateUsdExchangeRate.UsdExchangeRate sina = updateUsdExchangeRate.getSinaData();
//        UpdateUsdExchangeRate.UsdExchangeRate eastMoney = updateUsdExchangeRate.getEastMoneyData();
//        UpdateUsdExchangeRate.UsdExchangeRate heXun = updateUsdExchangeRate.getHeXunData();
//        UpdateUsdExchangeRate.UsdExchangeRate showApi = updateUsdExchangeRate.getShowApiData();
//        UpdateUsdExchangeRate.UsdExchangeRate huiTong = updateUsdExchangeRate.getHuiTongData();
//
//        System.out.println(JSON.toJSONString(sina));
//        System.out.println(JSON.toJSONString(eastMoney));
//        System.out.println(JSON.toJSONString(heXun));
//        System.out.println(JSON.toJSONString(showApi));
//        System.out.println(JSON.toJSONString(huiTong));
//    }
//}