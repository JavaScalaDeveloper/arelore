package com.arelore.server.core.biz.word.admin;

import com.arelore.server.core.service.BaseService;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoSyncResponse;

public interface AdminWordBaseInfoService extends BaseService<AdminWordBaseInfoRequest, AdminWordBaseInfoResponse> {
    /**
     * 分页扫描 admin_word_entry 全部词形，拉取外源后新增/更新 admin_word_base_info。
     */
    AdminWordBaseInfoSyncResponse syncFromEntries();
}
