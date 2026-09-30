package com.arelore.server.core.biz.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.arelore.server.core.biz.user.entity.UserAuthSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAuthSessionMapper extends BaseMapper<UserAuthSession> {
}
