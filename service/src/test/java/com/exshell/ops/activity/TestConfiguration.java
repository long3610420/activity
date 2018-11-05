package com.exshell.ops.activity;

import java.util.ArrayList;
import java.util.List;

import com.exshell.common.service.CurrencyConfiguration;
import com.exshell.common.service.AddressTypeConfiguration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("accountTestConfiguration")
public class TestConfiguration {

  @Bean
  public CurrencyConfiguration currencyConfiguration() {
    AddressTypeConfiguration addressTypeConfiguration = new AddressTypeConfiguration();
    List<AddressTypeConfiguration.CurrencyMap> list = new ArrayList<>();

    AddressTypeConfiguration.CurrencyMap currencyMap = new AddressTypeConfiguration.CurrencyMap();
    currencyMap.setSourceCurrency("bcc");
    currencyMap.setTargetCurrency("btc");
    list.add(currencyMap);

    addressTypeConfiguration.setCurrencyMap(list);
    return new CurrencyConfiguration(addressTypeConfiguration);
  }

}
