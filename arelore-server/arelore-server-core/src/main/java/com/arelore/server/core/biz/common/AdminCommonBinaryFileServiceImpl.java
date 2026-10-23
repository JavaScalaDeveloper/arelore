package com.arelore.server.core.biz.common;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.arelore.server.core.biz.common.dto.AdminCommonBinaryFileRequest;
import com.arelore.server.core.biz.common.dto.AdminCommonBinaryFileResponse;
import com.arelore.server.core.registration.entity.CommonBinaryFile;
import com.arelore.server.core.registration.mapper.CommonBinaryFileMapper;
import com.arelore.server.core.service.BaseServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class AdminCommonBinaryFileServiceImpl
    extends BaseServiceImpl<AdminCommonBinaryFileRequest, AdminCommonBinaryFileResponse, CommonBinaryFile>
    implements AdminCommonBinaryFileService {

    private static final String FILE_SIZE_EXPR =
        "CAST(IFNULL(JSON_EXTRACT(ext_json, '$.fileSize'), 0) AS UNSIGNED)";

    private final CommonBinaryFileMapper mapper;

    public AdminCommonBinaryFileServiceImpl(CommonBinaryFileMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected CommonBinaryFileMapper mapper() {
        return mapper;
    }

    @Override
    protected Class<AdminCommonBinaryFileResponse> responseClass() {
        return AdminCommonBinaryFileResponse.class;
    }

    @Override
    protected LambdaQueryWrapper<CommonBinaryFile> buildWrapper(AdminCommonBinaryFileRequest request) {
        LambdaQueryWrapper<CommonBinaryFile> wrapper = new LambdaQueryWrapper<>();
        // 列表绝不查 file_data，避免把大 BLOB 拉进内存
        wrapper.select(
            CommonBinaryFile::getId,
            CommonBinaryFile::getCreateTime,
            CommonBinaryFile::getModifyTime,
            CommonBinaryFile::getHashValue,
            CommonBinaryFile::getStatus,
            CommonBinaryFile::getExtJson
        );
        if (request == null) {
            wrapper.orderByDesc(CommonBinaryFile::getId);
            return wrapper;
        }
        if (request.getId() != null) {
            wrapper.eq(CommonBinaryFile::getId, request.getId());
        }
        if (request.getStatus() != null) {
            wrapper.eq(CommonBinaryFile::getStatus, request.getStatus());
        }
        String source = StringUtils.hasText(request.getSourceFilter())
            ? request.getSourceFilter()
            : request.getSource();
        if (StringUtils.hasText(source)) {
            String safe = source.trim().toUpperCase(Locale.ROOT).replace("'", "");
            wrapper.apply("JSON_UNQUOTE(JSON_EXTRACT(ext_json, '$.source')) = {0}", safe);
        }
        if (StringUtils.hasText(request.getContentType())) {
            String type = request.getContentType().trim().replace("'", "");
            wrapper.apply("JSON_UNQUOTE(JSON_EXTRACT(ext_json, '$.contentType')) LIKE {0}", "%" + type + "%");
        }
        if (request.getCreateTimeFrom() != null) {
            wrapper.ge(CommonBinaryFile::getCreateTime, request.getCreateTimeFrom());
        }
        if (request.getCreateTimeTo() != null) {
            wrapper.le(CommonBinaryFile::getCreateTime, request.getCreateTimeTo());
        }
        if (request.getModifyTimeFrom() != null) {
            wrapper.ge(CommonBinaryFile::getModifyTime, request.getModifyTimeFrom());
        }
        if (request.getModifyTimeTo() != null) {
            wrapper.le(CommonBinaryFile::getModifyTime, request.getModifyTimeTo());
        }
        if (request.getFileSizeMin() != null && request.getFileSizeMin() >= 0) {
            wrapper.apply(FILE_SIZE_EXPR + " >= {0}", request.getFileSizeMin());
        }
        if (request.getFileSizeMax() != null && request.getFileSizeMax() >= 0) {
            wrapper.apply(FILE_SIZE_EXPR + " <= {0}", request.getFileSizeMax());
        }
        applySort(wrapper, request.getSortFields());
        return wrapper;
    }

    private static void applySort(LambdaQueryWrapper<CommonBinaryFile> wrapper, List<String> sortFields) {
        if (sortFields == null || sortFields.isEmpty()) {
            wrapper.orderByDesc(CommonBinaryFile::getId);
            return;
        }
        // 去重且保序；仅允许白名单，防注入
        Set<String> seen = new LinkedHashSet<>();
        List<String> orderParts = new ArrayList<>();
        for (String raw : sortFields) {
            if (!StringUtils.hasText(raw)) {
                continue;
            }
            String key = raw.trim();
            if (!seen.add(key)) {
                continue;
            }
            switch (key) {
                case "createTime":
                    orderParts.add("create_time DESC");
                    break;
                case "modifyTime":
                    orderParts.add("modify_time DESC");
                    break;
                case "fileSize":
                    orderParts.add(FILE_SIZE_EXPR + " DESC");
                    break;
                default:
                    break;
            }
        }
        if (orderParts.isEmpty()) {
            wrapper.orderByDesc(CommonBinaryFile::getId);
            return;
        }
        orderParts.add("id DESC");
        wrapper.last("ORDER BY " + String.join(", ", orderParts));
    }

    @Override
    protected AdminCommonBinaryFileResponse toResponse(CommonBinaryFile entity) {
        if (entity == null) {
            return null;
        }
        AdminCommonBinaryFileResponse res = new AdminCommonBinaryFileResponse();
        res.setId(entity.getId());
        res.setCreateTime(entity.getCreateTime());
        res.setModifyTime(entity.getModifyTime());
        res.setStatus(entity.getStatus());
        res.setExtJson(entity.getExtJson());
        res.setHashHex(toHex(entity.getHashValue()));
        fillExtFields(res, entity.getExtJson(), entity.getFileData());
        return res;
    }

    @Override
    public AdminCommonBinaryFileResponse detail(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID 不能为空");
        }
        CommonBinaryFile row = mapper.selectById(id);
        if (row == null) {
            throw new IllegalArgumentException("记录不存在");
        }
        AdminCommonBinaryFileResponse res = toResponse(row);
        byte[] data = row.getFileData();
        if (data != null && data.length > 0) {
            if (res.getFileSize() == null) {
                res.setFileSize((long) data.length);
                res.setFileSizeText(formatFileSize(data.length));
            }
            res.setBase64Data(Base64.getEncoder().encodeToString(data));
            if (!StringUtils.hasText(res.getContentType())) {
                res.setContentType(guessContentType(data));
            }
        }
        return res;
    }

    @Override
    public int deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        List<Long> unique = new ArrayList<>();
        Set<Long> seen = new LinkedHashSet<>();
        for (Long id : ids) {
            if (id != null && seen.add(id)) {
                unique.add(id);
            }
        }
        if (unique.isEmpty()) {
            return 0;
        }
        int rows = 0;
        for (Long id : unique) {
            rows += mapper.deleteById(id);
        }
        return rows;
    }

    private static void fillExtFields(AdminCommonBinaryFileResponse res, String extJson, byte[] fileData) {
        if (!StringUtils.hasText(extJson)) {
            if (fileData != null) {
                res.setFileSize((long) fileData.length);
                res.setFileSizeText(formatFileSize(fileData.length));
            }
            return;
        }
        try {
            JSONObject ext = JSONObject.parseObject(extJson);
            if (ext == null) {
                return;
            }
            res.setContentType(ext.getString("contentType"));
            res.setSource(ext.getString("source"));
            Long size = ext.getLong("fileSize");
            if (size == null && fileData != null) {
                size = (long) fileData.length;
            }
            res.setFileSize(size);
            res.setFileSizeText(size == null ? null : formatFileSize(size));
            Integer ref = ext.getInteger("refCount");
            res.setRefCount(ref);
        } catch (Exception ignored) {
            // ignore bad ext_json
        }
    }

    /** 1024 进制：B / KB / MB / GB */
    static String formatFileSize(long bytes) {
        if (bytes < 0) {
            return "-";
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return trimDecimal(kb) + " KB";
        }
        double mb = kb / 1024.0;
        if (mb < 1024) {
            return trimDecimal(mb) + " MB";
        }
        double gb = mb / 1024.0;
        return trimDecimal(gb) + " GB";
    }

    private static String trimDecimal(double v) {
        if (v >= 100) {
            return String.format(Locale.ROOT, "%.0f", v);
        }
        if (v >= 10) {
            return String.format(Locale.ROOT, "%.1f", v);
        }
        return String.format(Locale.ROOT, "%.2f", v);
    }

    private static String toHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String guessContentType(byte[] data) {
        if (data.length >= 3 && data[0] == 'I' && data[1] == 'D' && data[2] == '3') {
            return "audio/mpeg";
        }
        if (data.length >= 2 && (data[0] & 0xFF) == 0xFF && (data[1] & 0xE0) == 0xE0) {
            return "audio/mpeg";
        }
        if (data.length >= 3 && (data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8) {
            return "image/jpeg";
        }
        if (data.length >= 4 && (data[0] & 0xFF) == 0x89 && data[1] == 0x50) {
            return "image/png";
        }
        return "application/octet-stream";
    }
}
