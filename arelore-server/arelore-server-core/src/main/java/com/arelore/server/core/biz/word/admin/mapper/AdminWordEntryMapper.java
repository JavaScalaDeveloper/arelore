package com.arelore.server.core.biz.word.admin.mapper;

import com.arelore.server.core.biz.word.admin.entity.AdminWordEntry;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface AdminWordEntryMapper extends BaseMapper<AdminWordEntry> {

    @Select("SELECT e.id, e.create_time, e.modify_time, e.book_code, e.word_code, e.word, e.sort_no, e.ext_info "
        + "FROM admin_word_entry e "
        + "LEFT JOIN user_word_learn_record r "
        + "  ON r.book_code = e.book_code AND r.word_code = e.word_code AND r.user_id = #{userId} "
        + "WHERE e.book_code = #{bookCode} AND r.id IS NULL "
        + "ORDER BY e.sort_no ASC, e.id ASC "
        + "LIMIT #{limit}")
    List<AdminWordEntry> selectUnlearned(
        @Param("userId") BigDecimal userId,
        @Param("bookCode") String bookCode,
        @Param("limit") int limit
    );

    /**
     * 复习词：除今天外，之前「最后一天」新学过的单词（以 learn_record.create_time 日期为准）。
     * 从未学过时子查询为 NULL，结果为空。
     */
    @Select("SELECT e.id, e.create_time, e.modify_time, e.book_code, e.word_code, e.word, e.sort_no, e.ext_info "
        + "FROM admin_word_entry e "
        + "INNER JOIN user_word_learn_record r "
        + "  ON r.book_code = e.book_code AND r.word_code = e.word_code AND r.user_id = #{userId} "
        + "WHERE e.book_code = #{bookCode} "
        + "AND DATE(r.create_time) = ("
        + "  SELECT MAX(DATE(r2.create_time)) FROM user_word_learn_record r2 "
        + "  WHERE r2.user_id = #{userId} AND r2.book_code = #{bookCode} "
        + "  AND DATE(r2.create_time) < CURDATE()"
        + ") "
        + "ORDER BY r.create_time ASC, e.sort_no ASC "
        + "LIMIT #{limit}")
    List<AdminWordEntry> selectForReview(
        @Param("userId") BigDecimal userId,
        @Param("bookCode") String bookCode,
        @Param("limit") int limit
    );

    @Select("SELECT COUNT(1) FROM user_word_learn_record "
        + "WHERE user_id = #{userId} AND book_code = #{bookCode}")
    int countLearned(
        @Param("userId") BigDecimal userId,
        @Param("bookCode") String bookCode
    );

    @Select("SELECT COUNT(1) FROM user_word_learn_record r "
        + "WHERE r.user_id = #{userId} AND r.book_code = #{bookCode} "
        + "AND DATE(r.create_time) = ("
        + "  SELECT MAX(DATE(r2.create_time)) FROM user_word_learn_record r2 "
        + "  WHERE r2.user_id = #{userId} AND r2.book_code = #{bookCode} "
        + "  AND DATE(r2.create_time) < CURDATE()"
        + ")")
    int countPreviousLearnDay(
        @Param("userId") BigDecimal userId,
        @Param("bookCode") String bookCode
    );

    @Select("<script>"
        + "SELECT e.id, e.create_time, e.modify_time, e.book_code, e.word_code, e.word, e.sort_no, e.ext_info "
        + "FROM admin_word_entry e "
        + "WHERE e.book_code = #{bookCode} "
        + "AND e.word_code IN "
        + "<foreach collection='wordCodes' item='code' open='(' separator=',' close=')'>#{code}</foreach>"
        + "</script>")
    List<AdminWordEntry> selectByWordCodes(
        @Param("bookCode") String bookCode,
        @Param("wordCodes") List<String> wordCodes
    );
}
