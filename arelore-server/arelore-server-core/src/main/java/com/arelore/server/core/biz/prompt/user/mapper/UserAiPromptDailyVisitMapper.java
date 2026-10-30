package com.arelore.server.core.biz.prompt.user.mapper;

import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptVisitStatsResponse;
import com.arelore.server.core.biz.prompt.user.entity.UserAiPromptDailyVisit;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface UserAiPromptDailyVisitMapper extends BaseMapper<UserAiPromptDailyVisit> {

    @Insert("INSERT INTO arelore_creator.user_ai_prompt_daily_visit "
        + "(user_id, visit_date, visit_count, client) "
        + "VALUES (#{userId}, #{visitDate}, 1, #{client}) "
        + "ON DUPLICATE KEY UPDATE visit_count = visit_count + 1, modify_time = CURRENT_TIMESTAMP")
    int upsertVisit(
        @Param("userId") BigDecimal userId,
        @Param("visitDate") LocalDate visitDate,
        @Param("client") String client
    );

    @Select("<script>"
        + "SELECT visit_date AS visitDate, "
        + "NULL AS client, "
        + "SUM(visit_count) AS pv, "
        + "COUNT(DISTINCT user_id) AS uv "
        + "FROM arelore_creator.user_ai_prompt_daily_visit "
        + "WHERE 1=1 "
        + "<if test='visitDateFrom != null'> AND visit_date &gt;= #{visitDateFrom} </if>"
        + "<if test='visitDateTo != null'> AND visit_date &lt;= #{visitDateTo} </if>"
        + "<if test='client != null and client != \"\"'> AND client = #{client} </if>"
        + "GROUP BY visit_date "
        + "ORDER BY visit_date DESC"
        + "</script>")
    List<UserAiPromptVisitStatsResponse> statsByDay(
        @Param("visitDateFrom") LocalDate visitDateFrom,
        @Param("visitDateTo") LocalDate visitDateTo,
        @Param("client") String client
    );
}
