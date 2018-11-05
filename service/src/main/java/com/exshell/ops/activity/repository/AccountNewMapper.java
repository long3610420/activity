package com.exshell.ops.activity.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.exshell.ops.activity.model.AccountNew;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface AccountNewMapper {

  int insertSelective(AccountNew record);

  /**
   * 更新账户状态
   */
  int updateAccountState(AccountNew account);

  /**
   * 根据状态查询账户
   */
  List<AccountNew> selectByState(@Param("states") List<Integer> states);

  AccountNew selectByPrimaryKey(Long id);

  AccountNew selectByAccountIdAndUserId(Map<String, Object> params);

  AccountNew selectByUserIdAndType(
          @Param("userId") Long userId,
          @Param("type") Integer type,
          @Param("subtype") String subtype
  );

  AccountNew selectByUserIdAndTypes(
          @Param("userId") Long userId,
          @Param("types") Collection<Integer> types
  );

  List<AccountNew> selectByUserId(Long userId);

  List<AccountNew> selectAccountsByUserIdTypes(
          @Param("userId") Long userId,
          @Param("types") Collection<Integer> types
  );

  List<AccountNew> findByUserIdAndType(
          @Param("userId") Long userId,
          @Param("type") Integer type,
          @Param("subType") String subType
  );

  List<AccountNew> findByUserIdAndTypes(
          @NotNull @Param("userId") Long userId,
          @NotNull @Param("type") Integer type,
          @NotNull @Param("subTypes") Collection<String> subTypes
  );

  List<AccountNew> findByPrimaryKeys(@NotNull @Param("ids") Iterable<Long> ids);

  List<AccountNew> findFlMgtAccounts(Map<String, Object> params);

  List<AccountNew> findAccountByStateAndUserId(@Param("states") Collection<Integer> states, @Param("userId") Long userId);

}
