package com.arelore.server.user.service.impl;

import cn.hutool.core.codec.Base64;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.arelore.server.core.registration.entity.CommonBinaryFile;
import com.arelore.server.core.registration.mapper.CommonBinaryFileMapper;
import com.arelore.server.core.user.dto.CommonBinaryFileByHashResponse;
import com.arelore.server.user.service.CommonBinaryFileQueryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CommonBinaryFileQueryServiceImpl implements CommonBinaryFileQueryService {
    private final CommonBinaryFileMapper commonBinaryFileMapper;

    public CommonBinaryFileQueryServiceImpl(CommonBinaryFileMapper commonBinaryFileMapper) {
        this.commonBinaryFileMapper = commonBinaryFileMapper;
    }

    @Override
    public CommonBinaryFileByHashResponse getByHash(String hashValueHex) {
        if (!StringUtils.hasText(hashValueHex) || hashValueHex.trim().length() != 64) {
            return null;
        }
        byte[] hashBytes = hexToBytes(hashValueHex.trim());
        LambdaQueryWrapper<CommonBinaryFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommonBinaryFile::getHashValue, hashBytes)
            .eq(CommonBinaryFile::getStatus, 1)
            .last("limit 1");
        CommonBinaryFile file = commonBinaryFileMapper.selectOne(wrapper);
        if (file == null || file.getFileData() == null || file.getFileData().length == 0) {
            return null;
        }
        CommonBinaryFileByHashResponse response = new CommonBinaryFileByHashResponse();
        response.setHashValue(hashValueHex.trim().toLowerCase());
        response.setContentType(detectContentType(file.getFileData()));
        response.setBase64Data(Base64.encode(file.getFileData()));
        return response;
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return bytes;
    }

    private String detectContentType(byte[] data) {
        if (data.length >= 8
            && (data[0] & 0xFF) == 0x89
            && (data[1] & 0xFF) == 0x50
            && (data[2] & 0xFF) == 0x4E
            && (data[3] & 0xFF) == 0x47) {
            return "image/png";
        }
        if (data.length >= 3
            && (data[0] & 0xFF) == 0xFF
            && (data[1] & 0xFF) == 0xD8
            && (data[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (data.length >= 6) {
            String head = new String(data, 0, 6);
            if ("GIF87a".equals(head) || "GIF89a".equals(head)) {
                return "image/gif";
            }
        }
        if (data.length >= 12) {
            String riff = new String(data, 0, 4);
            String webp = new String(data, 8, 4);
            if ("RIFF".equals(riff) && "WEBP".equals(webp)) {
                return "image/webp";
            }
        }
        return "application/octet-stream";
    }
}

