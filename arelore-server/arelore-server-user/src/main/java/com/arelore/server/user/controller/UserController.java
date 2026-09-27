package com.arelore.server.user.controller;

import com.arelore.server.core.common.result.Result;
import com.arelore.server.core.common.dto.PageResult;
import com.arelore.server.core.biz.user.dto.UserQueryRequest;
import com.arelore.server.core.biz.user.dto.UserSaveRequest;
import com.arelore.server.core.biz.user.dto.UserDeleteRequest;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户控制器
 * 注意：本模块禁止使用 RESTful 风格，所有参数必须通过 Body 传递
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    /**
     * 获取用户列表
     * POST /api/user/list
     */
    @PostMapping("/list")
    public Result<PageResult<String>> getUserList(@RequestBody UserQueryRequest request) {
        Integer pageNum = request.getPageNum() != null ? request.getPageNum() : 1;
        Integer pageSize = request.getPageSize() != null ? request.getPageSize() : 10;
        
        List<String> userList = new ArrayList<>();
        for (int i = 0; i < pageSize; i++) {
            userList.add("用户" + ((pageNum - 1) * pageSize + i + 1));
        }
        
        PageResult<String> pageResult = PageResult.of(userList, pageNum, pageSize, 100L);
        return Result.success(pageResult);
    }

    /**
     * 获取用户详情
     * POST /api/user/detail
     */
    @PostMapping("/detail")
    public Result<String> getUserDetail(@RequestBody UserDeleteRequest request) {
        if (request.getId() == null || request.getId().isEmpty()) {
            return Result.error("用户 ID 不能为空");
        }
        return Result.success("用户详情：" + request.getId());
    }

    /**
     * 创建用户
     * POST /api/user/create
     */
    @PostMapping("/create")
    public Result<String> createUser(@RequestBody UserSaveRequest request) {
        // TODO: 参数校验和业务逻辑
        return Result.success("创建成功");
    }

    /**
     * 更新用户
     * POST /api/user/update
     */
    @PostMapping("/update")
    public Result<String> updateUser(@RequestBody UserSaveRequest request) {
        if (request.getId() == null || request.getId().isEmpty()) {
            return Result.error("用户 ID 不能为空");
        }
        // TODO: 参数校验和业务逻辑
        return Result.success("更新成功");
    }

    /**
     * 删除用户
     * POST /api/user/delete
     */
    @PostMapping("/delete")
    public Result<String> deleteUser(@RequestBody UserDeleteRequest request) {
        if (request.getId() == null || request.getId().isEmpty()) {
            return Result.error("用户 ID 不能为空");
        }
        // TODO: 业务逻辑
        return Result.success("删除成功");
    }
}
