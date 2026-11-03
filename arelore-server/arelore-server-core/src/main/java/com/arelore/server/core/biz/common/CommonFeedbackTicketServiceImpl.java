package com.arelore.server.core.biz.common;

import com.alibaba.fastjson2.JSONObject;
import com.arelore.server.core.biz.common.dto.CommonFeedbackTicketRequest;
import com.arelore.server.core.biz.common.dto.CommonFeedbackTicketResponse;
import com.arelore.server.core.biz.common.dto.FeedbackAttachmentItem;
import com.arelore.server.core.biz.common.dto.FeedbackFileContentRequest;
import com.arelore.server.core.biz.common.dto.FeedbackFileContentResponse;
import com.arelore.server.core.biz.common.dto.FeedbackFileUploadRequest;
import com.arelore.server.core.biz.common.dto.FeedbackFileUploadResponse;
import com.arelore.server.core.biz.common.support.FeedbackExtSupport;
import com.arelore.server.core.biz.word.user.support.WordUserIdStrSupport;
import com.arelore.server.core.common.exception.BusinessException;
import com.arelore.server.core.common.result.ResultCode;
import com.arelore.server.core.common.time.ChinaTime;
import com.arelore.server.core.registration.entity.CommonBinaryFile;
import com.arelore.server.core.registration.entity.CommonFeedbackMessage;
import com.arelore.server.core.registration.entity.CommonFeedbackTicket;
import com.arelore.server.core.registration.mapper.CommonBinaryFileMapper;
import com.arelore.server.core.registration.mapper.CommonFeedbackMessageMapper;
import com.arelore.server.core.registration.mapper.CommonFeedbackTicketMapper;
import com.arelore.server.core.service.BaseServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Service
public class CommonFeedbackTicketServiceImpl
    extends BaseServiceImpl<CommonFeedbackTicketRequest, CommonFeedbackTicketResponse, CommonFeedbackTicket>
    implements CommonFeedbackTicketService {

    public static final String SENDER_USER = "USER";
    public static final String SENDER_ADMIN = "ADMIN";

    public static final int STATUS_PENDING_ADMIN = 1;
    public static final int STATUS_IN_PROGRESS = 2;
    public static final int STATUS_PENDING_USER = 3;
    public static final int STATUS_CLOSED = 4;

    private static final int DAILY_TICKET_LIMIT = 6;
    private static final int MAX_TITLE_LEN = 128;
    private static final int MAX_FILES_PER_MESSAGE = 5;
    private static final int MAX_UPLOAD_BYTES = 2 * 1024 * 1024;

    private final CommonFeedbackTicketMapper mapper;
    private final CommonFeedbackMessageMapper messageMapper;
    private final CommonBinaryFileMapper binaryFileMapper;
    private final CommonFeedbackMessageService messageService;

    public CommonFeedbackTicketServiceImpl(
        CommonFeedbackTicketMapper mapper,
        CommonFeedbackMessageMapper messageMapper,
        CommonBinaryFileMapper binaryFileMapper,
        CommonFeedbackMessageService messageService
    ) {
        this.mapper = mapper;
        this.messageMapper = messageMapper;
        this.binaryFileMapper = binaryFileMapper;
        this.messageService = messageService;
    }

    @Override
    protected CommonFeedbackTicketMapper mapper() {
        return mapper;
    }

    @Override
    protected Class<CommonFeedbackTicketResponse> responseClass() {
        return CommonFeedbackTicketResponse.class;
    }

    @Override
    protected LambdaQueryWrapper<CommonFeedbackTicket> buildWrapper(CommonFeedbackTicketRequest request) {
        WordUserIdStrSupport.applyIdStrToUserId(request);
        if (request != null && request.getUserId() == null && StringUtils.hasText(request.getUserIdStr())) {
            try {
                request.setUserId(new BigDecimal(request.getUserIdStr().trim()));
            } catch (NumberFormatException ignored) {
                // keep null
            }
        }
        LambdaQueryWrapper<CommonFeedbackTicket> wrapper = new LambdaQueryWrapper<>();
        if (request == null) {
            wrapper.orderByDesc(CommonFeedbackTicket::getModifyTime)
                .orderByDesc(CommonFeedbackTicket::getId);
            return wrapper;
        }
        if (request.getId() != null) {
            wrapper.eq(CommonFeedbackTicket::getId, request.getId());
        }
        if (request.getUserId() != null) {
            wrapper.eq(CommonFeedbackTicket::getUserId, request.getUserId());
        }
        if (StringUtils.hasText(request.getProductCode())) {
            wrapper.eq(CommonFeedbackTicket::getProductCode, request.getProductCode().trim());
        }
        if (request.getStatus() != null) {
            wrapper.eq(CommonFeedbackTicket::getStatus, request.getStatus());
        }
        if (request.getAdminUserId() != null) {
            wrapper.eq(CommonFeedbackTicket::getAdminUserId, request.getAdminUserId());
        }
        wrapper.orderByDesc(CommonFeedbackTicket::getModifyTime)
            .orderByDesc(CommonFeedbackTicket::getId);
        return wrapper;
    }

    @Override
    protected CommonFeedbackTicketResponse toResponse(CommonFeedbackTicket entity) {
        CommonFeedbackTicketResponse res = super.toResponse(entity);
        if (res == null) {
            return null;
        }
        WordUserIdStrSupport.fillIdStr(res);
        if (res.getAdminUserId() != null) {
            res.setAdminUserIdStr(res.getAdminUserId().toPlainString());
        }
        return res;
    }

    @Override
    @Transactional(transactionManager = "userTransactionManager", rollbackFor = Exception.class)
    public CommonFeedbackTicketResponse createTicket(BigDecimal userId, CommonFeedbackTicketRequest request) {
        if (userId == null) {
            throw new BusinessException(ResultCode.USER_NOT_LOGIN);
        }
        if (request == null) {
            throw new BusinessException(ResultCode.PARAMS_VALIDATION_ERROR, "请求不能为空");
        }
        String productCode = request.getProductCode() == null ? "" : request.getProductCode().trim();
        if (!StringUtils.hasText(productCode)) {
            throw new BusinessException(ResultCode.PARAMS_VALIDATION_ERROR, "productCode 不能为空");
        }
        String content = normalizeContent(request.getContentText());
        List<FeedbackAttachmentItem> files = normalizeFiles(request.getFiles());
        if (!StringUtils.hasText(content) && files.isEmpty()) {
            throw new BusinessException(ResultCode.PARAMS_VALIDATION_ERROR, "请填写反馈内容或上传截图");
        }
        enforceDailyLimit(userId);

        LocalDateTime now = ChinaTime.now();
        CommonFeedbackTicket ticket = new CommonFeedbackTicket();
        ticket.setUserId(userId);
        ticket.setProductCode(productCode);
        ticket.setTitle(buildTitle(content, files));
        ticket.setStatus(STATUS_PENDING_ADMIN);
        ticket.setAdminUserId(null);
        ticket.setExtInfo(request.getExtInfo());
        ticket.setCreateTime(now);
        ticket.setModifyTime(now);
        mapper.insert(ticket);

        insertMessage(ticket.getId(), SENDER_USER, userId, content, request.getClientIp(), files, now);
        return detail(ticket.getId(), userId);
    }

    @Override
    @Transactional(transactionManager = "userTransactionManager", rollbackFor = Exception.class)
    public CommonFeedbackTicketResponse reply(
        BigDecimal senderUserId,
        String senderType,
        CommonFeedbackTicketRequest request
    ) {
        if (senderUserId == null) {
            throw new BusinessException(ResultCode.USER_NOT_LOGIN);
        }
        if (request == null || request.getId() == null) {
            throw new BusinessException(ResultCode.PARAMS_VALIDATION_ERROR, "工单ID不能为空");
        }
        String type = senderType == null ? "" : senderType.trim().toUpperCase(Locale.ROOT);
        if (!SENDER_USER.equals(type) && !SENDER_ADMIN.equals(type)) {
            throw new BusinessException(ResultCode.PARAMS_VALIDATION_ERROR, "senderType 非法");
        }
        CommonFeedbackTicket ticket = mapper.selectById(request.getId());
        if (ticket == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "工单不存在");
        }
        if (ticket.getStatus() != null && ticket.getStatus() == STATUS_CLOSED) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "工单已关闭，无法继续回复");
        }
        if (SENDER_USER.equals(type) && ticket.getUserId().compareTo(senderUserId) != 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作该工单");
        }

        String content = normalizeContent(request.getContentText());
        List<FeedbackAttachmentItem> files = normalizeFiles(request.getFiles());
        if (!StringUtils.hasText(content) && files.isEmpty()) {
            throw new BusinessException(ResultCode.PARAMS_VALIDATION_ERROR, "请填写回复内容或上传截图");
        }

        LocalDateTime now = ChinaTime.now();
        insertMessage(ticket.getId(), type, senderUserId, content, request.getClientIp(), files, now);

        Integer nextStatus = SENDER_ADMIN.equals(type) ? STATUS_PENDING_USER : STATUS_PENDING_ADMIN;
        LambdaUpdateWrapper<CommonFeedbackTicket> update = new LambdaUpdateWrapper<CommonFeedbackTicket>()
            .eq(CommonFeedbackTicket::getId, ticket.getId())
            .set(CommonFeedbackTicket::getStatus, nextStatus)
            .set(CommonFeedbackTicket::getModifyTime, now);
        if (SENDER_ADMIN.equals(type)) {
            update.set(CommonFeedbackTicket::getAdminUserId, senderUserId);
        }
        mapper.update(null, update);

        return detail(ticket.getId(), SENDER_USER.equals(type) ? senderUserId : null);
    }

    @Override
    public CommonFeedbackTicketResponse detail(Long ticketId, BigDecimal userIdOrNull) {
        if (ticketId == null) {
            throw new BusinessException(ResultCode.PARAMS_VALIDATION_ERROR, "工单ID不能为空");
        }
        CommonFeedbackTicket ticket = mapper.selectById(ticketId);
        if (ticket == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "工单不存在");
        }
        if (userIdOrNull != null && ticket.getUserId().compareTo(userIdOrNull) != 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权查看该工单");
        }
        CommonFeedbackTicketResponse res = toResponse(ticket);
        res.setMessages(messageService.listByTicketId(ticketId));
        return res;
    }

    @Override
    @Transactional(transactionManager = "userTransactionManager", rollbackFor = Exception.class)
    public CommonFeedbackTicketResponse close(
        Long ticketId,
        BigDecimal userIdOrNull,
        BigDecimal adminUserIdOrNull
    ) {
        CommonFeedbackTicket ticket = requireTicket(ticketId);
        if (userIdOrNull != null && ticket.getUserId().compareTo(userIdOrNull) != 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权关闭该工单");
        }
        LocalDateTime now = ChinaTime.now();
        LambdaUpdateWrapper<CommonFeedbackTicket> update = new LambdaUpdateWrapper<CommonFeedbackTicket>()
            .eq(CommonFeedbackTicket::getId, ticketId)
            .set(CommonFeedbackTicket::getStatus, STATUS_CLOSED)
            .set(CommonFeedbackTicket::getModifyTime, now);
        if (adminUserIdOrNull != null) {
            update.set(CommonFeedbackTicket::getAdminUserId, adminUserIdOrNull);
        }
        mapper.update(null, update);
        return detail(ticketId, userIdOrNull);
    }

    @Override
    @Transactional(transactionManager = "userTransactionManager", rollbackFor = Exception.class)
    public CommonFeedbackTicketResponse updateStatus(Long ticketId, Integer status, BigDecimal adminUserId) {
        requireTicket(ticketId);
        if (status == null
            || status < STATUS_PENDING_ADMIN
            || status > STATUS_CLOSED) {
            throw new BusinessException(ResultCode.PARAMS_VALIDATION_ERROR, "status 非法");
        }
        LocalDateTime now = ChinaTime.now();
        LambdaUpdateWrapper<CommonFeedbackTicket> update = new LambdaUpdateWrapper<CommonFeedbackTicket>()
            .eq(CommonFeedbackTicket::getId, ticketId)
            .set(CommonFeedbackTicket::getStatus, status)
            .set(CommonFeedbackTicket::getModifyTime, now);
        if (adminUserId != null) {
            update.set(CommonFeedbackTicket::getAdminUserId, adminUserId);
        }
        mapper.update(null, update);
        return detail(ticketId, null);
    }

    @Override
    public FeedbackFileUploadResponse uploadFile(FeedbackFileUploadRequest request) {
        if (request == null || !StringUtils.hasText(request.getContentBase64())) {
            throw new BusinessException(ResultCode.PARAMS_VALIDATION_ERROR, "文件内容不能为空");
        }
        String contentType = request.getContentType() == null ? "" : request.getContentType().trim().toLowerCase(Locale.ROOT);
        if (!contentType.startsWith("image/")) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_ALLOWED, "仅支持图片附件");
        }
        byte[] data = decodeBase64(request.getContentBase64());
        if (data.length == 0) {
            throw new BusinessException(ResultCode.PARAMS_VALIDATION_ERROR, "文件内容无效");
        }
        if (data.length > MAX_UPLOAD_BYTES) {
            throw new BusinessException(ResultCode.PARAMS_VALIDATION_ERROR, "单张图片不能超过 2MB");
        }
        String fileName = StringUtils.hasText(request.getFileName()) ? request.getFileName().trim() : "screenshot.png";
        byte[] hash = sha256(data);
        Long fileId = insertOrReuseBinary(hash, data, contentType, fileName);

        FeedbackFileUploadResponse res = new FeedbackFileUploadResponse();
        res.setFileId(fileId);
        res.setFileName(fileName);
        res.setContentType(contentType);
        return res;
    }

    @Override
    public FeedbackFileContentResponse fileContent(FeedbackFileContentRequest request) {
        if (request == null || request.getFileId() == null) {
            throw new BusinessException(ResultCode.PARAMS_VALIDATION_ERROR, "fileId 不能为空");
        }
        CommonBinaryFile file = binaryFileMapper.selectById(request.getFileId());
        if (file == null || file.getStatus() == null || file.getStatus() != 1) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
        FeedbackFileContentResponse res = new FeedbackFileContentResponse();
        res.setFileId(file.getId());
        String contentType = "application/octet-stream";
        if (StringUtils.hasText(file.getExtJson())) {
            try {
                JSONObject ext = JSONObject.parseObject(file.getExtJson());
                if (ext != null && StringUtils.hasText(ext.getString("contentType"))) {
                    contentType = ext.getString("contentType");
                }
            } catch (Exception ignored) {
                // keep default
            }
        }
        res.setContentType(contentType);
        res.setContentBase64(Base64.getEncoder().encodeToString(file.getFileData() == null ? new byte[0] : file.getFileData()));
        return res;
    }

    private void enforceDailyLimit(BigDecimal userId) {
        LocalDateTime dayStart = ChinaTime.nowDate().atStartOfDay();
        Long count = mapper.selectCount(new LambdaQueryWrapper<CommonFeedbackTicket>()
            .eq(CommonFeedbackTicket::getUserId, userId)
            .ge(CommonFeedbackTicket::getCreateTime, dayStart));
        if (count != null && count >= DAILY_TICKET_LIMIT) {
            throw new BusinessException(ResultCode.USER_REGISTER_RATE_LIMITED,
                "每天最多提交 " + DAILY_TICKET_LIMIT + " 条反馈，请明天再试");
        }
    }

    private void insertMessage(
        Long ticketId,
        String senderType,
        BigDecimal senderUserId,
        String content,
        String clientIp,
        List<FeedbackAttachmentItem> files,
        LocalDateTime now
    ) {
        CommonFeedbackMessage message = new CommonFeedbackMessage();
        message.setTicketId(ticketId);
        message.setSenderType(senderType);
        message.setSenderUserId(senderUserId);
        message.setContentText(content == null ? "" : content);
        message.setExtInfo(FeedbackExtSupport.buildMessageExt(clientIp, files));
        message.setModifyTime(now);
        messageMapper.insert(message);
    }

    private CommonFeedbackTicket requireTicket(Long ticketId) {
        if (ticketId == null) {
            throw new BusinessException(ResultCode.PARAMS_VALIDATION_ERROR, "工单ID不能为空");
        }
        CommonFeedbackTicket ticket = mapper.selectById(ticketId);
        if (ticket == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "工单不存在");
        }
        return ticket;
    }

    private static String normalizeContent(String contentText) {
        return contentText == null ? "" : contentText.trim();
    }

    private static List<FeedbackAttachmentItem> normalizeFiles(List<FeedbackAttachmentItem> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }
        if (files.size() > MAX_FILES_PER_MESSAGE) {
            throw new BusinessException(ResultCode.PARAMS_VALIDATION_ERROR,
                "单条消息最多 " + MAX_FILES_PER_MESSAGE + " 个附件");
        }
        return files;
    }

    private static String buildTitle(String content, List<FeedbackAttachmentItem> files) {
        if (StringUtils.hasText(content)) {
            String t = content.replace('\n', ' ').trim();
            return t.length() <= MAX_TITLE_LEN ? t : t.substring(0, MAX_TITLE_LEN);
        }
        return files.isEmpty() ? "意见反馈" : "截图反馈";
    }

    private Long insertOrReuseBinary(byte[] hash, byte[] data, String contentType, String fileName) {
        CommonBinaryFile existing = binaryFileMapper.selectOne(new LambdaQueryWrapper<CommonBinaryFile>()
            .select(CommonBinaryFile::getId)
            .eq(CommonBinaryFile::getHashValue, hash)
            .last("limit 1"));
        if (existing != null) {
            binaryFileMapper.update(null, new LambdaUpdateWrapper<CommonBinaryFile>()
                .eq(CommonBinaryFile::getId, existing.getId())
                .set(CommonBinaryFile::getStatus, 1)
                .set(CommonBinaryFile::getModifyTime, ChinaTime.now()));
            return existing.getId();
        }
        CommonBinaryFile row = new CommonBinaryFile();
        row.setHashValue(hash);
        row.setFileData(data);
        row.setStatus(1);
        row.setCreateTime(ChinaTime.now());
        row.setModifyTime(ChinaTime.now());
        JSONObject ext = new JSONObject();
        ext.put("contentType", contentType);
        ext.put("source", "FEEDBACK");
        ext.put("fileSize", data.length);
        ext.put("fileName", fileName);
        ext.put("refCount", 1);
        row.setExtJson(ext.toJSONString());
        try {
            binaryFileMapper.insert(row);
            return row.getId();
        } catch (DuplicateKeyException e) {
            CommonBinaryFile again = binaryFileMapper.selectOne(new LambdaQueryWrapper<CommonBinaryFile>()
                .select(CommonBinaryFile::getId)
                .eq(CommonBinaryFile::getHashValue, hash)
                .last("limit 1"));
            if (again == null) {
                throw e;
            }
            return again.getId();
        }
    }

    private static byte[] decodeBase64(String raw) {
        String s = raw.trim();
        int comma = s.indexOf(',');
        if (s.startsWith("data:") && comma > 0) {
            s = s.substring(comma + 1);
        }
        try {
            return Base64.getDecoder().decode(s);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ResultCode.PARAMS_VALIDATION_ERROR, "文件 base64 解码失败");
        }
    }

    private static byte[] sha256(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED, "计算文件摘要失败");
        }
    }
}
