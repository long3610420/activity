package com.exshell.ops.activity.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.exshell.dawn.exception.ApiException;
import com.exshell.dawn.rest.ApiResponse;
import com.exshell.gateway.ValidationErrorEnum;
import com.exshell.ops.activity.constants.ActivityConstants;
import com.exshell.ops.activity.model.*;
import com.exshell.ops.activity.param.AddActivityUserReq;
import com.exshell.ops.activity.param.AddFirstChargeReq;
import com.exshell.ops.activity.service.*;
import com.exshell.ops.activity.vo.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/act")
@Validated
public class ActivityApiController {
    // private static final Logger log = LoggerFactory.getLogger(ActivityApiController.class);
    // @Autowired
    // private AccountDemoService accountDemoService;
    @Autowired
    private ActivityUserService activityUserService;
    @Autowired
    private ActivityFirstChargeService activityFirstChargeService;
    @Autowired
    private ActivityRewardService activityRewardService;
    @Autowired
    private ActivityTempService activityTempService;
    @Autowired
    private ActivityPrizeService activityPrizeService;

/*  @ApiOperation("查询用户账号信息")
  @GetMapping("/user/account")
  public ApiResponse<AccountNew> queryBalance(
          @ApiParam(value = "账号id", required = true)
          @NotNull(message = "REQUIRE_ACCOUNT_ID")
          @RequestParam("accountId") String accountId) {
    if(StringUtils.isEmpty(accountId)){
      log.error("INVALID_ACCOUNT_ID:"+accountId);
      throw new ApiException(ValidationErrorEnum.INVALID_ACCOUNT_ID);
    }
    Long userId = GatewayContext.getRequiredUserId();  //根据token判断是否登录，没有登录抛出异常
    AccountNew account = accountDemoService.getBalance(userId,Long.parseLong(accountId));
    if(account == null){
      log.error("account not exist,accountId="+accountId);
      throw new ApiException(ValidationErrorEnum.INVALID_ACCOUNT_ID);
    }
    return ApiResponse.ok(account);
  }*/

    @ApiOperation("查询活动总用户充值数")
    @GetMapping("/charge/search")
    public ApiResponse<ActivityUserTotalVo> queryCharge() {

        // log.info("GET /charge/search ......start......");
        ActivityUserTotalVo activityUserTotalVo = new ActivityUserTotalVo();
        Integer total = activityUserService.queryChargeTotal();
        activityUserTotalVo.setTotal(total == null ? 0 : total);
        // activityUserTotalVo.setState();
        // log.info("GET /charge/search ......end......" + activityUserTotalVo.getTotal());
        return ApiResponse.ok(activityUserTotalVo);
    }

    @ApiOperation("查询用户奖励总数及明细")
    @GetMapping("/reward/search")
    public ApiResponse<ActivityRewardVo> queryReward(
            @ApiParam(value = "账号id", required = true)
            @NotNull(message = "REQUIRE_USER_ID")
            @RequestParam("user-id") String uid) {
        // log.info("GET /reward/search ......start......");
        if (StringUtils.isEmpty(uid)) {
            log.error("INVALID_UID:" + uid);
            throw new ApiException(ValidationErrorEnum.INVALID_UID);
        }
        Long userId = Long.parseLong(uid);
        List<ActivityReward> account = activityRewardService.searchRewards(userId);
        if (account == null) {
            log.error("rewards not exist,uid=" + userId);
            throw new ApiException(ValidationErrorEnum.INVALID_UID);
        }
        ActivityRewardVo activityRewardVo = new ActivityRewardVo();
        StringBuffer sb = new StringBuffer();
        Map<String, BigDecimal> map = new HashMap<>();
        // 遍历统计
        for (ActivityReward activityReward : account) {
            String currency = activityReward.getCurrency();
            BigDecimal amount = activityReward.getAmount();
            if (!map.containsKey(currency)) {
                map.put(currency, amount);
            } else {
                map.put(currency, map.get(currency).add(amount));
            }
        }
        map.forEach((k, v) -> {
            if (v != null) {
                double d = v.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
                sb.append(d).append(" ").append(k.toUpperCase()).append(",");
            }
        });
        activityRewardVo.setTotal(sb.length() > 1 ? sb.substring(0, sb.length() - 1) : "");
        activityRewardVo.setList(account);
        log.info("GET /reward/search ......end......" + activityRewardVo.getTotal());
        return ApiResponse.ok(activityRewardVo);
    }

    //新增活动用户user/save
    @ApiOperation("新增活动用户")
    @PostMapping("/user/save")
    public ApiResponse<ActivityUser> saveUser(@RequestBody @Valid AddActivityUserReq addActivityUserReq) {
        log.info("POST /user/save ......start......");
        Long userId = Long.parseLong(addActivityUserReq.getUid());
        Long invUid = 0L;
        String inviterUid = addActivityUserReq.getInviterUid();
        if (!StringUtils.isEmpty(inviterUid)) {
            invUid = Long.parseLong(inviterUid);
        }
        ActivityUser activityUser = activityUserService.saveActUser(userId, addActivityUserReq.getInviterCode(), invUid);

        log.info("POST /user/save ......end......" + activityUser.getId());
        return ApiResponse.ok(activityUser);
    }

    //新增活动用户充值记录charge/save
    @ApiOperation("新增活动用户充值记录")
    @PostMapping("/charge/save")
    public ApiResponse<ActivityStateVo> saveCharge(@RequestBody @Valid AddFirstChargeReq addFirstChargeReq) {
        log.info("POST /charge/save ......start......");
        Long userId = Long.parseLong(addFirstChargeReq.getUid());

        // Boolean aBoolean = activityFirstChargeService.saveFirst(userId, addFirstChargeReq.getActivityId(),
        //         addFirstChargeReq.getRechargeDate(), addFirstChargeReq.getCurrency(), addFirstChargeReq.getAmount());
        ActivityStateVo activityStateVo = new ActivityStateVo();
        // activityStateVo.setState(aBoolean ? "success" : "failed");
        log.info("POST /charge/save ......end......" + activityStateVo.getState());
        return ApiResponse.ok(activityStateVo);
    }

    //增加活动总用户充值数charge/update
    @ApiOperation("增加活动总用户充值数")
    @PostMapping("/charge/update")
    public ApiResponse<ActivityUserTotalVo> updateCharge(
            @ApiParam(value = "需要增加的数量", required = true)
            @NotNull(message = "REQUIRE_AMOUNT")
            @RequestParam("amount") Long amount,
            @ApiParam(value = "密码", required = true)
            @NotNull(message = "REQUIRE_PWD")
            @RequestParam("pwd") String pwd) {
        log.info("POST /charge/update ......start......" + amount);
        ActivityUserTotalVo activityUserTotalVo = new ActivityUserTotalVo();
        if (ActivityConstants.PWD.equals(pwd)) {
            activityUserService.incrChargeTotal(amount);
        }
        log.info("POST /charge/update ......end......" + activityUserTotalVo.getTotal());
        return ApiResponse.ok(activityUserTotalVo);
    }

    //活动首冲统计
    @ApiOperation("活动首冲统计")
    @PostMapping("/charge/statustics")
    public ApiResponse<ActivityFirstChargeStatistics> queryActivityFirstChargeStatistics(
            @ApiParam(value = "开始时间", required = false)
            @RequestParam(value = "beginTime", defaultValue = "")
                    Long beginTime,
            @ApiParam(value = "结束时间", required = false)
            @RequestParam(value = "endTime", defaultValue = "")
                    Long endTime,
            @ApiParam(value = "币种类型", required = true)
            @RequestParam(value = "currencys")
                    String currencys) {

        List<Map> param = new ArrayList<Map>();
        String[] cyString = null;
        try {
            cyString = currencys.split(",");
        } catch (Exception e) {
            log.error("currencys is NULL", e);
        }
        try {
            param = activityFirstChargeService.findActivityFirstChargeStatistics(beginTime, endTime, cyString);
        } catch (Exception e) {
            log.error("activityFirstChargeService method findActivityFirstChargeStatistics erro", e);
        }
        ActivityFirstChargeStatistics rep = new ActivityFirstChargeStatistics();
        rep.setRep(param);
        return ApiResponse.ok(rep);
    }

    @ApiOperation("补用户奖励")
    @GetMapping("/reward/repairReward")
    public ApiResponse<ActivityStateVo> repairReward(
            @ApiParam(value = "账号id", required = true)
            @NotNull(message = "REQUIRE_USER_ID")
            @RequestParam("user-id") String uid) {
        log.info("GET /reward/repairReward ......start......uid" + uid);
        if (StringUtils.isEmpty(uid)) {
            log.error("INVALID_UID:" + uid);
            throw new ApiException(ValidationErrorEnum.INVALID_UID);
        }
        Long userId = Long.parseLong(uid);
        Integer integer = activityRewardService.saveFirst(userId);
        ActivityStateVo activityStateVo = new ActivityStateVo();
        if (integer > 0)
            activityStateVo.setState("success");
        return ApiResponse.ok(activityStateVo);
    }

    @ApiOperation("查询用户锁仓资产")
    @GetMapping("lock/assets")
    public ApiResponse<ActivityRewardLockVo> querylockAssets(
            @ApiParam(value = "账号id", required = true)
            @NotNull(message = "REQUIRE_USER_ID")
            @RequestParam("user-id") String uid,
            @ApiParam(value = "账号id", required = true)
            @NotNull(message = "REQUIRE_CURRENCY")
            @RequestParam("currency") String currency) {
        log.info("GET /lock/assets ......start......uid" + uid);
        if (StringUtils.isEmpty(uid)) {
            log.error("INVALID_UID:" + uid);
            throw new ApiException(ValidationErrorEnum.INVALID_UID);
        }
        Long userId = Long.parseLong(uid);
        ActivityRewardLockVo activityRewardLockVo = new ActivityRewardLockVo();
        Map rsp = activityRewardService.queryUserlockAssets(userId, currency);
        if (null != rsp) {
            activityRewardLockVo.setRewardLocks((List) rsp.get("activityRewardLocks"));
            activityRewardLockVo.setLock((BigDecimal) rsp.get("lock"));
            activityRewardLockVo.setUnlock((BigDecimal) rsp.get("unLock"));
            activityRewardLockVo.setIsPrize(rsp.get("isPrize") != null ? Integer.parseInt(rsp.get("isPrize").toString()) : 0);
            activityRewardLockVo.setType(rsp.get("type") != null ? Integer.parseInt(rsp.get("type").toString()) : 0);
            activityRewardLockVo.setTime(rsp.get("time") + "");
        }
        activityRewardLockVo.setState("success");
        return ApiResponse.ok(activityRewardLockVo);
    }

    @ApiOperation("查询用户充值返奖记录")
    @GetMapping("recharge/record")
    public ApiResponse<ActivityUserTotalVo> rechargeRecord(@RequestParam(name = "minReward", required = false)
                                                                   Long minReward,
                                                           @RequestParam(name = "maxReward", required = false)
                                                                   Long maxReward) {
        ActivityUserTotalVo activityUserTotalVo = new ActivityUserTotalVo();
        activityUserTotalVo.setState("fail");
        try {
            activityUserTotalVo.setActivityUsers(activityUserService.queryRechargeRecord(minReward, maxReward));
            activityUserTotalVo.setState("success");
        } catch (Exception e) {
            System.out.println(e);
            log.error("/v1/act/recharge/Record queryRechargeRecord method is error");
            return ApiResponse.ok(activityUserTotalVo);
        }
        return ApiResponse.ok(activityUserTotalVo);
    }

    @ApiOperation("查询活动开始之前的用户资产")
    @GetMapping("/snapshot/query")
    public ApiResponse<List<ActivityTemp>> querySnapshot(
            @ApiParam(value = "offset", required = true)
            @NotNull(message = "REQUIRE_OFFSET")
            @RequestParam("offset") Integer offset,
            @ApiParam(value = "limit", required = true)
            @NotNull(message = "REQUIRE_LIMIT")
            @RequestParam("limit") Integer limit) {
        log.info("GET /reward/repairReward ......start... offset={}...limit={},", offset, limit);


        List<ActivityTemp> query = activityTempService.query(offset, limit);

        return ApiResponse.ok(query);
    }

    @ApiOperation("保存活动用户返奖")
    @PostMapping("/reward/lock/save")
    public ApiResponse<Boolean> saveLock(
            @ApiParam(value = "data", required = true)
            @NotNull(message = "REQUIRE_DATA")
            @RequestParam("data") String data) {
        log.info("POST /reward/lock/save ......start... list={}", data);
        JSONArray objects = JSON.parseArray(data);
        objects.forEach(n -> {
            JSONObject jsonObject = JSON.parseObject(n.toString());
            // Map<Long, String> map = (Map) n;
            jsonObject.forEach((k, v) -> {
                if (k != null && org.apache.commons.lang3.StringUtils.isNotEmpty(k) && Long.parseLong(k) != 0) {

                    //将要发放的ET
                    BigDecimal reward = new BigDecimal(0);
                    //查看v是否为0，如果不为0，则查看用户是否中奖
                    String val = v.toString();
                    if (v != null) {
                        Double i = Double.parseDouble(val);
                        if (i != 0d) {
                            reward = new BigDecimal(val);
                            ActivityPrize activityPrize = new ActivityPrize();
                            activityPrize.setUid(Long.parseLong(k));
                            activityPrize.setState(1);
                            List<ActivityPrize> activityPrizes = activityPrizeService.queryActivityPrize(activityPrize);
                            if (!CollectionUtils.isEmpty(activityPrizes)) {//如果中奖
                                reward = reward.add(activityPrizeService.getEtForType(activityPrizes.get(0).getType()));
                            }
                        } else {

                        }
                    }

                    List list = activityRewardService.queryUserlockByUserId(Long.parseLong(k), "ET");

                    if (list != null && list.size() > 0) {
                        activityRewardService.updateRewardLock(Long.parseLong(k), reward.toString());
                    } else {
                        activityRewardService.saveRewardLock(Long.parseLong(k), reward.toString());

                    }
                    //查询用户是否存在，不存在则插入
                    ActivityUser hasUser = activityUserService.selectByUid(Long.parseLong(k));
                    if (hasUser == null) {
                        log.info("begin insert activity-user uid{}" + k);
                        activityUserService.save(Long.parseLong(k), new BigDecimal(v.toString()));
                        log.info("end insert activity-user uid{}" + k);
                    } else {
                        ActivityUser user = new ActivityUser();
                        user.setReward(new BigDecimal(v.toString()));
                        user.setUid(Long.parseLong(k));
                        activityUserService.updateByUid(user);
                    }
                }
            });
        });

        return ApiResponse.ok(true);
    }

    @ApiOperation("保存中奖纪录")
    @PostMapping("/reward/prize/save")
    public ApiResponse<ActivityStateVo> activityPrizeSave(@ApiParam(value = "中奖纪录", required = true)
                                                          @NotNull(message = "records is null")
                                                          @RequestBody
                                                                  List<ActivityPrize> records) {
        log.info("POST /reward/prize/save ......start... list={}", records);
        ActivityStateVo activityStateVo = new ActivityStateVo();
        activityStateVo.setState("success");
        try {
            activityPrizeService.saveActivityPrize(records);
        } catch (Exception e) {
            activityStateVo.setState("fail");
            activityStateVo.setErrorMsg("save error");
            log.error("/v1/act/reward/prize/save save error {}", e);
        }
        return ApiResponse.ok(activityStateVo);
    }

    @ApiOperation("保存最终 中奖纪录")
    @PostMapping("/reward/prize/savefinal")
    public ApiResponse<ActivityStateVo> activityPrizeSaveFinal(@ApiParam(value = "中奖纪录", required = true)
                                                               @NotNull(message = "records is null")
                                                               @RequestBody
                                                                       List<ActivityPrize> records) {
        log.info("POST /reward/prize/savefinal ......start... list={}", records);
        ActivityStateVo activityStateVo = new ActivityStateVo();
        activityStateVo.setState("success");
        try {
            activityPrizeService.updateActivityPrize(records);
        } catch (Exception e) {
            activityStateVo.setState("fail");
            activityStateVo.setErrorMsg("save error");
            log.error("/v1/act/reward/prize/save save error {}", e);
        }
        return ApiResponse.ok(activityStateVo);
    }

    @ApiOperation("查询中奖列表")
    @GetMapping("/reward/prize/list")
    public ApiResponse<ActivityPrizeVo> queryActivityPrizeList(ActivityPrize prizes) {
        log.info("GET /reward/prize/list\" ......start... list={}", prizes);
        ActivityPrizeVo prizeVo = new ActivityPrizeVo();
        try {
            prizeVo.setPrizes(activityPrizeService.queryActivityPrize(prizes));
            prizeVo.setState("success");
        } catch (Exception e) {
            prizeVo.setState("fail");
            log.error("/v1/act/reward/prize/list queryActivityPrize error {}", e);
        }
        return ApiResponse.ok(prizeVo);
    }

    @ApiOperation("更新活动用户返奖失败")
    @PostMapping("/reward/lock/update")
    public ApiResponse<Boolean> updateRewardLock(
            @ApiParam(value = "data", required = true)
            @NotNull(message = "REQUIRE_DATA")
            @RequestParam("data") String data) {
        log.info("POST /reward/lock/update ......start... list={}", data);
        JSONArray objects = JSON.parseArray(data);
        objects.forEach(n -> {
            JSONObject jsonObject = JSON.parseObject(n.toString());
            // Map<Long, String> map = (Map) n;
            jsonObject.forEach((k, v) -> {
                if (k != null && org.apache.commons.lang3.StringUtils.isNotEmpty(k) && Long.parseLong(k) != 0) {
                    List list = activityRewardService.queryUserlockByUserId(Long.parseLong(k), "ET");
                    if (list != null && list.size() > 0) {
                        activityRewardService.updateRewardLockState(Long.parseLong(k), 3);
                    }
                }
            });
        });

        return ApiResponse.ok(true);
    }

    @ApiOperation("保存锁仓纪录")
    @PostMapping("/reward/lock/savesingle")
    public ApiResponse<ActivityStateVo> activityRewardLockSaveSingle(@ApiParam(value = "锁仓纪录", required = true)
                                                                     @NotNull(message = "records is null")
                                                                     @RequestBody
                                                                             List<ActivityRewardLock> records) {
        log.info("POST /reward/lock/savesingle ......start... list={}", records);
        ActivityStateVo activityStateVo = new ActivityStateVo();
        activityStateVo.setState("success");
        try {
            activityRewardService.saveRewardLock(records);
        } catch (Exception e) {
            activityStateVo.setState("fail");
            activityStateVo.setErrorMsg("save error");
            log.error("/v1/act/reward/lock/savesingle activityRewardLockSaveSingle error {}", e);
        }
        return ApiResponse.ok(activityStateVo);
    }


    @ApiOperation("修改锁仓纪录状态为解锁成功")
    @PostMapping("/reward/lock/updatesuccess")
    public ApiResponse<ActivityStateVo> activityRewardLockSaveSingle(@ApiParam(value = "修改锁仓纪录状态为解锁成功", required = true)
                                                                     @NotNull(message = "uid is null")
                                                                     @RequestBody Long records) {
        log.info("POST /reward/lock/updatesuccess ......start... list={}", records);
        ActivityStateVo activityStateVo = new ActivityStateVo();
        activityStateVo.setState("success");
        try {
            activityRewardService.updateRewardLockStateSuccess(records, 2);
        } catch (Exception e) {
            activityStateVo.setState("fail");
            activityStateVo.setErrorMsg("save error");
            log.error("/v1/act/reward/lock/updatesuccess activityRewardLockSaveSingle error {}", e);
        }
        return ApiResponse.ok(activityStateVo);
    }
}
