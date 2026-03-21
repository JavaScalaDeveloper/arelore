package com.arelore.server.admin.controller;

import com.arelore.server.common.result.Result;
import com.arelore.server.common.dto.PageResult;
import com.arelore.server.admin.dto.AdminLoginRequest;
import com.arelore.server.admin.dto.AdminUserQueryRequest;
import com.arelore.server.admin.dto.SystemSettingsRequest;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 * 注意：本模块禁止使用 RESTful 风格，所有参数必须通过 Body 传递
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    /**
     * 管理员登录
     * POST /api/admin/login
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody AdminLoginRequest request) {
        // TODO: 实现实际的登录逻辑
        return Result.success(Map.of(
            "token", "mock-jwt-token",
            "username", request.getUsername()
        ));
    }

    /**
     * 获取仪表盘数据
     * POST /api/admin/dashboard
     */
    @PostMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        return Result.success(Map.of(
            "totalUsers", 12580,
            "todayNewUsers", 256,
            "totalMessages", 89654,
            "systemStatus", "正常"
        ));
    }

    /**
     * 获取用户列表（管理员专用）
     * POST /api/admin/users
     */
    @PostMapping("/users")
    public Result<PageResult<Map<String, Object>>> getUserList(@RequestBody AdminUserQueryRequest request) {
        Integer pageNum = request.getPageNum() != null ? request.getPageNum() : 1;
        Integer pageSize = request.getPageSize() != null ? request.getPageSize() : 10;
        
        List<Map<String, Object>> userList = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            userList.add(Map.of(
                "id", ((pageNum - 1) * pageSize + i + 1),
                "username", "用户" + ((pageNum - 1) * pageSize + i + 1),
                "email", "user" + ((pageNum - 1) * pageSize + i + 1) + "@example.com",
                "status", true
            ));
        }
        
        PageResult<Map<String, Object>> pageResult = PageResult.of(userList, pageNum, pageSize, 100L);
        return Result.success(pageResult);
    }

    /**
     * 更新系统设置
     * POST /api/admin/settings/update
     */
    @PostMapping("/settings/update")
    public Result<String> updateSettings(@RequestBody SystemSettingsRequest request) {
        // TODO: 参数校验和业务逻辑
        return Result.success("保存成功");
    }

    /**
     * 获取系统设置
     * POST /api/admin/settings/get
     */
    @PostMapping("/settings/get")
    public Result<Map<String, Object>> getSettings() {
        return Result.success(Map.of(
            "siteName", "Arelore",
            "allowRegister", true,
            "maintenanceMode", false
        ));
    }
}
