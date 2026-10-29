package com.arelore.server.core.biz.word.admin.support;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.arelore.server.core.biz.word.admin.dto.AdminWordPictureContentResponse;
import com.arelore.server.core.common.time.ChinaTime;
import com.arelore.server.core.registration.entity.CommonBinaryFile;
import com.arelore.server.core.registration.mapper.CommonBinaryFileMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 单词配图/语音二进制：按内容哈希去重写入 common_binary_file。
 * ext_json 含 contentType、source、fileSize、refCount，以及可选溯源字段（如 wordBaseInfoId）；
 * 引用减到 0 则物理删除，不改表结构。
 */
@Component
public class WordPictureBinaryStore {
    private static final int MAX_BYTES = 5 * 1024 * 1024;
    private static final HttpClient CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(8))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    private final CommonBinaryFileMapper fileMapper;

    public WordPictureBinaryStore(CommonBinaryFileMapper fileMapper) {
        this.fileMapper = fileMapper;
    }

    /**
     * 下载远程图并计入一次引用。返回写入单词 pictures[] 的对象（含 fileId，保留原 url 供采纳匹配）。
     */
    public JSONObject saveRemoteImage(String url, String source, String photographer) throws Exception {
        if (!StringUtils.hasText(url)) {
            throw new IllegalArgumentException("图片地址为空");
        }
        byte[] data = download(url.trim());
        String contentType = detectContentType(data);
        byte[] hash = sha256(data);
        Long fileId = insertOrIncrease(hash, data, contentType, source, null);
        JSONObject item = new JSONObject();
        item.put("fileId", fileId);
        item.put("fileHash", toHex(hash));
        item.put("fileSize", data.length);
        item.put("url", url.trim());
        item.put("source", StringUtils.hasText(source) ? source.trim().toUpperCase() : "PIXABAY");
        if (StringUtils.hasText(photographer)) {
            item.put("photographer", photographer.trim());
        }
        return item;
    }

    /**
     * 写入任意二进制（如图片/mp3）并计入一次引用。
     * 返回含 fileId、fileHash、fileSize、contentType、source 的对象。
     */
    public JSONObject saveBytes(byte[] data, String contentType, String source) throws Exception {
        return saveBytes(data, contentType, source, null);
    }

    /**
     * @param extraExt 合并进 common_binary_file.ext_json（如 wordBaseInfoId 溯源），可为 null
     */
    public JSONObject saveBytes(byte[] data, String contentType, String source, JSONObject extraExt)
        throws Exception {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("文件内容为空");
        }
        if (data.length > MAX_BYTES) {
            throw new IllegalArgumentException("文件超过 5MB");
        }
        String type = StringUtils.hasText(contentType) ? contentType.trim() : detectContentType(data);
        byte[] hash = sha256(data);
        Long fileId = insertOrIncrease(hash, data, type, source, extraExt);
        JSONObject item = new JSONObject();
        item.put("fileId", fileId);
        item.put("fileHash", toHex(hash));
        item.put("fileSize", data.length);
        item.put("contentType", type);
        item.put("source", StringUtils.hasText(source) ? source.trim().toUpperCase() : "BINARY");
        if (extraExt != null && extraExt.getLong("wordBaseInfoId") != null) {
            item.put("wordBaseInfoId", extraExt.getLong("wordBaseInfoId"));
        }
        return item;
    }

    /**
     * 轻量存在性检查（不读 file_data）。无效 status=0 视为不存在。
     * 供批量生成释义语音等场景：元数据仍有 fileId 但文件已被删时需重新生成。
     */
    public boolean exists(Long fileId) {
        if (fileId == null) {
            return false;
        }
        CommonBinaryFile row = fileMapper.selectOne(new LambdaQueryWrapper<CommonBinaryFile>()
            .select(CommonBinaryFile::getId, CommonBinaryFile::getStatus)
            .eq(CommonBinaryFile::getId, fileId)
            .last("limit 1"));
        if (row == null) {
            return false;
        }
        return row.getStatus() == null || row.getStatus() != 0;
    }

    public AdminWordPictureContentResponse loadContent(Long fileId) {
        if (fileId == null) {
            return null;
        }
        CommonBinaryFile file = fileMapper.selectById(fileId);
        if (file == null || file.getFileData() == null || file.getFileData().length == 0) {
            return null;
        }
        if (file.getStatus() != null && file.getStatus() == 0) {
            return null;
        }
        String contentType = contentTypeFromExt(file.getExtJson());
        if (!StringUtils.hasText(contentType) || "application/octet-stream".equals(contentType)) {
            contentType = detectContentType(file.getFileData());
        }
        AdminWordPictureContentResponse response = new AdminWordPictureContentResponse();
        response.setFileId(file.getId());
        response.setContentType(contentType);
        response.setBase64Data(Base64.getEncoder().encodeToString(file.getFileData()));
        return response;
    }

    private static String contentTypeFromExt(String extJson) {
        if (!StringUtils.hasText(extJson)) {
            return null;
        }
        try {
            JSONObject ext = JSONObject.parseObject(extJson);
            return ext == null ? null : ext.getString("contentType");
        } catch (Exception e) {
            return null;
        }
    }

    /** 这些 fileId 各减少 1 次引用，计数到 0 则物理删除 */
    public void release(Collection<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }
        Set<Long> unique = new LinkedHashSet<>();
        for (Long id : fileIds) {
            if (id != null) {
                unique.add(id);
            }
        }
        for (Long id : unique) {
            fileMapper.update(null, new LambdaUpdateWrapper<CommonBinaryFile>()
                .eq(CommonBinaryFile::getId, id)
                .setSql(adjustRefCountSql(-1)));
            fileMapper.delete(new LambdaQueryWrapper<CommonBinaryFile>()
                .eq(CommonBinaryFile::getId, id)
                .apply("CAST(IFNULL(JSON_EXTRACT(ext_json, '$.refCount'), 0) AS SIGNED) <= 0"));
        }
    }

    public static Set<Long> fileIdsOf(JSONArray pictures) {
        Set<Long> ids = new LinkedHashSet<>();
        if (pictures == null) {
            return ids;
        }
        for (int i = 0; i < pictures.size(); i++) {
            JSONObject item = pictures.getJSONObject(i);
            if (item == null) {
                continue;
            }
            Long fileId = item.getLong("fileId");
            if (fileId != null) {
                ids.add(fileId);
            }
        }
        return ids;
    }

    private Long insertOrIncrease(
        byte[] hash,
        byte[] data,
        String contentType,
        String source,
        JSONObject extraExt
    ) {
        int fileSize = data == null ? 0 : data.length;
        CommonBinaryFile existing = fileMapper.selectOne(new LambdaQueryWrapper<CommonBinaryFile>()
            .select(CommonBinaryFile::getId)
            .eq(CommonBinaryFile::getHashValue, hash)
            .last("limit 1"));
        if (existing != null) {
            // 引用 +1，并补写/刷新 fileSize、contentType、溯源 id（兼容历史缺 size 的行）
            fileMapper.update(null, new LambdaUpdateWrapper<CommonBinaryFile>()
                .eq(CommonBinaryFile::getId, existing.getId())
                .setSql(bumpRefAndMetaSql(fileSize, contentType, source, extraExt))
                .set(CommonBinaryFile::getStatus, 1));
            return existing.getId();
        }
        CommonBinaryFile row = new CommonBinaryFile();
        row.setHashValue(hash);
        row.setFileData(data);
        row.setStatus(1);
        // 显式写东八区时间，避免 MySQL 机器为 UTC 时 CURRENT_TIMESTAMP 差 8 小时
        row.setCreateTime(ChinaTime.now());
        row.setModifyTime(ChinaTime.now());
        JSONObject ext = new JSONObject();
        ext.put("contentType", contentType);
        ext.put("source", StringUtils.hasText(source) ? source.trim().toUpperCase() : "PIXABAY");
        ext.put("fileSize", fileSize);
        ext.put("refCount", 1);
        mergeExtraExt(ext, extraExt);
        row.setExtJson(ext.toJSONString());
        try {
            fileMapper.insert(row);
            return row.getId();
        } catch (DuplicateKeyException e) {
            CommonBinaryFile again = fileMapper.selectOne(new LambdaQueryWrapper<CommonBinaryFile>()
                .select(CommonBinaryFile::getId)
                .eq(CommonBinaryFile::getHashValue, hash)
                .last("limit 1"));
            if (again == null) {
                throw e;
            }
            fileMapper.update(null, new LambdaUpdateWrapper<CommonBinaryFile>()
                .eq(CommonBinaryFile::getId, again.getId())
                .setSql(bumpRefAndMetaSql(fileSize, contentType, source, extraExt))
                .set(CommonBinaryFile::getStatus, 1));
            return again.getId();
        }
    }

    private static void mergeExtraExt(JSONObject target, JSONObject extraExt) {
        if (target == null || extraExt == null || extraExt.isEmpty()) {
            return;
        }
        for (String key : extraExt.keySet()) {
            if (!StringUtils.hasText(key) || extraExt.get(key) == null) {
                continue;
            }
            // 保留内部计数/元数据键不被覆盖
            if ("refCount".equals(key) || "fileSize".equals(key) || "contentType".equals(key) || "source".equals(key)) {
                continue;
            }
            target.put(key, extraExt.get(key));
        }
    }

    /** 在已有 ext_json 上增减 refCount，不新增表字段 */
    private static String adjustRefCountSql(int delta) {
        String op = delta >= 0 ? "+ " + delta : "- " + Math.abs(delta);
        return "ext_json = JSON_SET(IFNULL(ext_json, '{}'), '$.refCount', "
            + "CAST(IFNULL(JSON_EXTRACT(ext_json, '$.refCount'), 1) AS SIGNED) " + op + ")";
    }

    /**
     * 引用 +1，并写入 fileSize / contentType / source / 可选溯源字段。
     */
    private static String bumpRefAndMetaSql(
        int fileSize,
        String contentType,
        String source,
        JSONObject extraExt
    ) {
        String type = StringUtils.hasText(contentType) ? contentType.trim().replace("'", "''") : "application/octet-stream";
        String src = StringUtils.hasText(source) ? source.trim().toUpperCase().replace("'", "''") : "BINARY";
        StringBuilder sql = new StringBuilder();
        sql.append("ext_json = JSON_SET(IFNULL(ext_json, '{}'), ")
            .append("'$.refCount', CAST(IFNULL(JSON_EXTRACT(ext_json, '$.refCount'), 1) AS SIGNED) + 1, ")
            .append("'$.fileSize', ").append(Math.max(0, fileSize)).append(", ")
            .append("'$.contentType', '").append(type).append("', ")
            .append("'$.source', '").append(src).append("'");
        Long wordBaseInfoId = extraExt == null ? null : extraExt.getLong("wordBaseInfoId");
        if (wordBaseInfoId != null) {
            sql.append(", '$.wordBaseInfoId', ").append(wordBaseInfoId);
        }
        String word = extraExt == null ? null : extraExt.getString("word");
        if (StringUtils.hasText(word)) {
            String safe = word.trim().replace("\\", "\\\\").replace("'", "''");
            if (safe.length() > 128) {
                safe = safe.substring(0, 128);
            }
            sql.append(", '$.word', '").append(safe).append("'");
        }
        sql.append(")");
        return sql.toString();
    }

    private static byte[] download(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(20))
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .header("Referer", "https://pixabay.com/")
            .header("Accept", "image/avif,image/webp,image/apng,image/*,*/*;q=0.8")
            .GET()
            .build();
        HttpResponse<byte[]> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("下载图片 HTTP " + response.statusCode());
        }
        byte[] body = response.body();
        if (body == null || body.length == 0) {
            throw new IllegalStateException("图片内容为空");
        }
        if (body.length > MAX_BYTES) {
            throw new IllegalStateException("图片超过 5MB");
        }
        return body;
    }

    private static byte[] sha256(byte[] data) throws Exception {
        return MessageDigest.getInstance("SHA-256").digest(data);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    static String detectContentType(byte[] data) {
        if (data == null || data.length == 0) {
            return "application/octet-stream";
        }
        if (data.length >= 4
            && (data[0] & 0xFF) == 0x89
            && data[1] == 0x50
            && data[2] == 0x4E
            && data[3] == 0x47) {
            return "image/png";
        }
        if (data.length >= 3 && (data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8) {
            return "image/jpeg";
        }
        if (data.length >= 6) {
            String head = new String(data, 0, 6);
            if ("GIF87a".equals(head) || "GIF89a".equals(head)) {
                return "image/gif";
            }
        }
        if (data.length >= 12 && "RIFF".equals(new String(data, 0, 4)) && "WEBP".equals(new String(data, 8, 4))) {
            return "image/webp";
        }
        // MP3：ID3 头或帧同步字
        if (data.length >= 3 && data[0] == 'I' && data[1] == 'D' && data[2] == '3') {
            return "audio/mpeg";
        }
        if (data.length >= 2 && (data[0] & 0xFF) == 0xFF && (data[1] & 0xE0) == 0xE0) {
            return "audio/mpeg";
        }
        return "application/octet-stream";
    }
}
