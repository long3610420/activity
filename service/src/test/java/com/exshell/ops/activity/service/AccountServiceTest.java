package com.exshell.ops.activity.service;

import com.exshell.ops.activity.TestConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@Sql({"classpath:/import-account.sql"})
@TestPropertySource("classpath:/test.properties")
@SpringBootTest(
        classes = {TestConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
public class AccountServiceTest {

    @Test
    public void getBalance() {
    }
}