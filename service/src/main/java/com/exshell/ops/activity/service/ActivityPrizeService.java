package com.exshell.ops.activity.service;

import com.exshell.ops.activity.model.ActivityPrize;
import com.exshell.ops.activity.model.ActivityUser;
import com.exshell.ops.activity.repository.ActivityPrizeMapper;
import com.exshell.ops.activity.repository.ActivityUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class ActivityPrizeService {

    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;
    @Autowired
    private ActivityUserMapper activityUserMapper;
    @Autowired
    private ActivityPrizeService activityPrizeService;

    //保存中奖纪录
    @Transactional
    public void saveActivityPrize(List <ActivityPrize> activityPrizes) {
        Long nowTime = System.currentTimeMillis();
        for (ActivityPrize prize  : activityPrizes) {
            if(prize.getState() !=null && prize.getState() == 1) {//如果为真数据
                ActivityUser user = activityUserMapper.selectByUid(prize.getUid());
                if (null != user) {
                    prize.setType(activityPrizeService.getTypeForEt(user.getReward()));
                } else {
                    log.error("saveActivityPrize error The user is not exit");
                    throw new RuntimeException();
                }
            }
            prize.setPrizeDate(nowTime);
            prize.setCreatedAt(nowTime);
            prize.setUpdatedAt(nowTime);
            int save = activityPrizeMapper.insert(prize);
            if (save <= 0) {
                throw new RuntimeException();
            }
        }
    }

    //保存中奖纪录
    @Transactional
    public void updateActivityPrize(List<ActivityPrize> activityPrizes) {
        Long nowTime = System.currentTimeMillis();
        for (ActivityPrize prize : activityPrizes) {
            if (prize.getState() != null && prize.getState() == 1) {//如果为真数据

                prize.setUpdatedAt(nowTime);
                int save = activityPrizeMapper.update(prize);
                if (save <= 0) {
                    // throw new RuntimeException();
                }
            }
        }
    }
    public List<ActivityPrize> queryActivityPrize(ActivityPrize prize) {
        return activityPrizeMapper.queryActivityPrize(prize);
    }

    public BigDecimal getEtForType(Integer type) {
        if (type == null) {
            return new BigDecimal(0);
        }
        if (type == 1) {
            return new BigDecimal(1000000);
        }
        if (type == 2) {
            return new BigDecimal(100000);
        }
        if (type == 3) {
            return new BigDecimal(8000);
        }
        if (type == 4) {
            return new BigDecimal(1000);
        }

        return new BigDecimal(0);
    }

    public Integer getTypeForEt(BigDecimal et) {
        BigDecimal[] compare = et.divideAndRemainder(new BigDecimal(1000000));
        if (compare[0].compareTo(new BigDecimal(0)) != 1) {
            compare = et.divideAndRemainder(new BigDecimal(100000));
            if (compare[0].compareTo(new BigDecimal(0)) != 1) {
                compare = et.divideAndRemainder(new BigDecimal(10000));
                if (compare[0].compareTo(new BigDecimal(0)) != 1) {
                    compare = et.divideAndRemainder(new BigDecimal(1000));
                    if (compare[0].compareTo(new BigDecimal(0)) != 1) {
                        return null;
                    } else {
                        return  4;
                    }
                } else {
                    return  3;
                }
            } else {
                return  2;
            }
        } else {
            return  1;
        }
    }
}
