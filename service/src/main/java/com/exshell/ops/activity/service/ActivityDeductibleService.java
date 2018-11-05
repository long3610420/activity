package com.exshell.ops.activity.service;

import com.exshell.ops.activity.repository.AccountNewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActivityDeductibleService {


    @Autowired
    private AccountNewMapper accountNewMapper;


}
