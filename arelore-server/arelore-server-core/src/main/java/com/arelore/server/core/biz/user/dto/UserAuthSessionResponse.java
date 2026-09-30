package com.arelore.server.core.biz.user.dto;

import com.arelore.server.core.biz.user.entity.UserAuthSession;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserAuthSessionResponse extends UserAuthSession {
}
