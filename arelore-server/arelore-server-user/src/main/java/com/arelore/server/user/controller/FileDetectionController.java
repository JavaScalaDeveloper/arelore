package com.arelore.server.user.controller;

import cn.hutool.json.JSONUtil;
import com.arelore.server.common.result.Result;
import com.arelore.server.user.dto.FileDetectionResponse;
import com.arelore.server.user.service.FileDetectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件检测控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user/file-detection")
public class FileDetectionController {
    
    @Autowired
    private FileDetectionService fileDetectionService;
    
    /**
     * 文件敏感内容检测
     * 
     * @param file 文件
     * @param sensitiveTypes 敏感信息类型列表（JSON格式）
     * @return 检测结果
     */
    @PostMapping("/detect")
    public Result<FileDetectionResponse> detectFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sensitiveTypes") String sensitiveTypes) {
        try {
            log.info("接收文件检测请求，文件名：{}", file.getOriginalFilename());
            
            // 参数校验
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }
            
            // 解析敏感类型列表
            List<String> types = JSONUtil.toList(sensitiveTypes, String.class);
            if (types == null || types.isEmpty()) {
                return Result.error("请至少选择一种敏感类型");
            }
            
            // 调用服务进行文件检测
            FileDetectionResponse response = fileDetectionService.detectFile(file, types);
            
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("文件检测失败", e);
            return Result.error(e.getMessage());
        }
    }
}
