package com.arelore.server.core.user.strategy.impl;

import com.arelore.server.core.user.strategy.FileProcessingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Iterator;

/**
 * Excel文件处理策略
 */
@Slf4j
@Component
public class ExcelFileProcessingStrategy implements FileProcessingStrategy {
    
    private static final int MAX_CONTENT_LENGTH = 10000; // 最大提取字数
    
    @Override
    public boolean supports(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        return "application/vnd.ms-excel".equals(contentType) || 
               "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType) ||
               (originalFilename != null && (originalFilename.endsWith(".xls") || originalFilename.endsWith(".xlsx")));
    }
    
    @Override
    public String processFile(MultipartFile file) throws Exception {
        log.info("开始处理Excel文件：{}", file.getOriginalFilename());
        
        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook;
            String originalFilename = file.getOriginalFilename();
            
            if (originalFilename != null && originalFilename.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            } else {
                workbook = new HSSFWorkbook(inputStream);
            }
            
            StringBuilder content = new StringBuilder();
            
            // 遍历所有工作表
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                if (sheet == null) continue;
                
                // 遍历所有行
                Iterator<Row> rowIterator = sheet.iterator();
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    
                    // 遍历所有单元格
                    Iterator<Cell> cellIterator = row.cellIterator();
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        String cellValue = getCellValue(cell);
                        
                        if (cellValue != null && !cellValue.trim().isEmpty()) {
                            content.append(cellValue).append(" ");
                        }
                    }
                    content.append("\n");
                }
            }
            
            workbook.close();
            
            String result = content.toString();
            
            // 限制提取字数
            if (result.length() > MAX_CONTENT_LENGTH) {
                result = result.substring(0, MAX_CONTENT_LENGTH);
                log.info("Excel文件内容过长，已截断至{}字符", MAX_CONTENT_LENGTH);
            }
            
            log.info("Excel文件处理完成，提取内容长度：{}", result.length());
            return result;
        }
    }
    
    /**
     * 获取单元格的值
     */
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
    
    @Override
    public String getType() {
        return "excel";
    }
}
