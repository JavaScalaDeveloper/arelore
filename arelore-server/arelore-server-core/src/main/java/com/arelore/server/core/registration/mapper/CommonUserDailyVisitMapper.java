package com.arelore.server.core.registration.mapper;

import com.arelore.server.core.registration.entity.CommonUserDailyVisit;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

@Mapper
public interface CommonUserDailyVisitMapper extends BaseMapper<CommonUserDailyVisit> {

    /**
     * 按用户+自然日累加访问次数；冲突时 visit_count + 1。
     */
    @Insert("INSERT INTO arelore_common.common_user_daily_visit (user_id, visit_date, visit_count) "
        + "VALUES (#{userId}, #{visitDate}, 1) "
        + "ON DUPLICATE KEY UPDATE visit_count = visit_count + 1, modify_time = CURRENT_TIMESTAMP")
    int upsertVisit(@Param("userId") BigDecimal userId, @Param("visitDate") LocalDate visitDate);
}
