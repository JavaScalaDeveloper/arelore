package com.arelore.server.core.biz.word.admin;

import com.arelore.server.core.service.BaseService;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBookRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBookResponse;

public interface AdminWordBookService extends BaseService<AdminWordBookRequest, AdminWordBookResponse> {
    AdminWordBookResponse getByCode(String code);

    void refreshWordCount(String bookCode);
}
