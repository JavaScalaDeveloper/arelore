package com.arelore.server.user.factory;

import com.arelore.server.user.strategy.FileProcessingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件处理策略工厂
 */
@Component
public class FileProcessingStrategyFactory {
    
    @Autowired
    private List<FileProcessingStrategy> strategies;
    
    /**
     * 根据文件获取合适的处理策略
     * 
     * @param file 文件
     * @return 文件处理策略
     */
    public FileProcessingStrategy getStrategy(MultipartFile file) {
        for (FileProcessingStrategy strategy : strategies) {
            if (strategy.supports(file)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("不支持的文件类型：" + file.getContentType());
    }
}
