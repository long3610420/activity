package com.exshell.ops.activity.service;

import com.exshell.dawn.exception.ApiException;
import com.exshell.ops.activity.enums.AccountErrorEnum;
import com.exshell.ops.activity.model.AccountNew;
import com.exshell.ops.activity.repository.AccountNewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountDemoService {


  @Autowired
  private AccountNewMapper accountNewMapper;

  public AccountNew getBalance(final Long userId, final Long accountId) {
    // 业务校验
    AccountNew account = findAccount(accountId);

    if (account == null || !account.getUserId().equals(userId)) {
      throw new ApiException(AccountErrorEnum.ACCOUNT_GET_BALANCE_ACCOUNT_INEXISTENT_ERROR,
          accountId.toString(), userId.toString());
    }

    return account;
  }


  public AccountNew findAccount(final Long accountId) {
    AccountNew account = accountNewMapper.selectByPrimaryKey(accountId);
    if (account == null) {
      throw new ApiException(AccountErrorEnum.ACCOUNT_GET_ACCOUNTS_INEXISTENT_ERROR);
    }

    return account;
  }

}
