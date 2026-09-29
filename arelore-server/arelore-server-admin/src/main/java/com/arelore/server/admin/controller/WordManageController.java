package com.arelore.server.admin.controller;

import com.arelore.server.core.common.dto.IdRequest;
import com.arelore.server.core.common.dto.PageResult;
import com.arelore.server.core.common.result.Result;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBookRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBookResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordCategoryRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordCategoryResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordEntryRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordEntryResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordLanguageRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordLanguageResponse;
import com.arelore.server.core.biz.word.admin.AdminWordBookService;
import com.arelore.server.core.biz.word.admin.AdminWordCategoryService;
import com.arelore.server.core.biz.word.admin.AdminWordEntryService;
import com.arelore.server.core.biz.word.admin.AdminWordLanguageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/word")
public class WordManageController {
    private final AdminWordLanguageService languageService;
    private final AdminWordCategoryService categoryService;
    private final AdminWordBookService bookService;
    private final AdminWordEntryService entryService;

    public WordManageController(
        AdminWordLanguageService languageService,
        AdminWordCategoryService categoryService,
        AdminWordBookService bookService,
        AdminWordEntryService entryService
    ) {
        this.languageService = languageService;
        this.categoryService = categoryService;
        this.bookService = bookService;
        this.entryService = entryService;
    }

    @PostMapping("/language/list")
    public Result<PageResult<AdminWordLanguageResponse>> languageList(
        @RequestBody(required = false) AdminWordLanguageRequest request
    ) {
        return Result.success(languageService.pageQuery(request == null ? new AdminWordLanguageRequest() : request));
    }

    @PostMapping("/language/create")
    public Result<String> languageCreate(@RequestBody AdminWordLanguageRequest request) {
        languageService.create(request);
        return Result.success("创建成功");
    }

    @PostMapping("/language/update")
    public Result<String> languageUpdate(@RequestBody AdminWordLanguageRequest request) {
        if (request.getId() == null) {
            return Result.error("ID 不能为空");
        }
        languageService.update(request);
        return Result.success("更新成功");
    }

    @PostMapping("/language/delete")
    public Result<String> languageDelete(@RequestBody IdRequest request) {
        Long id = request == null ? null : request.getId();
        if (id == null) {
            return Result.error("ID 不能为空");
        }
        languageService.deleteById(id);
        return Result.success("删除成功");
    }

    @PostMapping("/category/list")
    public Result<PageResult<AdminWordCategoryResponse>> categoryList(
        @RequestBody(required = false) AdminWordCategoryRequest request
    ) {
        return Result.success(categoryService.pageQuery(request == null ? new AdminWordCategoryRequest() : request));
    }

    @PostMapping("/category/create")
    public Result<String> categoryCreate(@RequestBody AdminWordCategoryRequest request) {
        categoryService.create(request);
        return Result.success("创建成功");
    }

    @PostMapping("/category/update")
    public Result<String> categoryUpdate(@RequestBody AdminWordCategoryRequest request) {
        if (request.getId() == null) {
            return Result.error("ID 不能为空");
        }
        categoryService.update(request);
        return Result.success("更新成功");
    }

    @PostMapping("/category/delete")
    public Result<String> categoryDelete(@RequestBody IdRequest request) {
        Long id = request == null ? null : request.getId();
        if (id == null) {
            return Result.error("ID 不能为空");
        }
        categoryService.deleteById(id);
        return Result.success("删除成功");
    }

    @PostMapping("/book/list")
    public Result<PageResult<AdminWordBookResponse>> bookList(@RequestBody(required = false) AdminWordBookRequest request) {
        return Result.success(bookService.pageQuery(request == null ? new AdminWordBookRequest() : request));
    }

    @PostMapping("/book/create")
    public Result<String> bookCreate(@RequestBody AdminWordBookRequest request) {
        if (request.getWordCount() == null) {
            request.setWordCount(0);
        }
        bookService.create(request);
        return Result.success("创建成功");
    }

    @PostMapping("/book/update")
    public Result<String> bookUpdate(@RequestBody AdminWordBookRequest request) {
        if (request.getId() == null) {
            return Result.error("ID 不能为空");
        }
        bookService.update(request);
        return Result.success("更新成功");
    }

    @PostMapping("/book/delete")
    public Result<String> bookDelete(@RequestBody IdRequest request) {
        Long id = request == null ? null : request.getId();
        if (id == null) {
            return Result.error("ID 不能为空");
        }
        bookService.deleteById(id);
        return Result.success("删除成功");
    }

    @PostMapping("/entry/list")
    public Result<PageResult<AdminWordEntryResponse>> entryList(
        @RequestBody(required = false) AdminWordEntryRequest request
    ) {
        return Result.success(entryService.pageQuery(request == null ? new AdminWordEntryRequest() : request));
    }

    /** 词条详情（含 ext_info）；列表接口不返回该大字段 */
    @PostMapping("/entry/detail")
    public Result<AdminWordEntryResponse> entryDetail(@RequestBody IdRequest request) {
        Long id = request == null ? null : request.getId();
        if (id == null) {
            return Result.error("ID 不能为空");
        }
        return Result.success(entryService.getById(id));
    }

    @PostMapping("/entry/create")
    public Result<String> entryCreate(@RequestBody AdminWordEntryRequest request) {
        entryService.create(request);
        return Result.success("创建成功");
    }

    @PostMapping("/entry/update")
    public Result<String> entryUpdate(@RequestBody AdminWordEntryRequest request) {
        if (request.getId() == null) {
            return Result.error("ID 不能为空");
        }
        entryService.update(request);
        return Result.success("更新成功");
    }

    @PostMapping("/entry/delete")
    public Result<String> entryDelete(@RequestBody IdRequest request) {
        Long id = request == null ? null : request.getId();
        if (id == null) {
            return Result.error("ID 不能为空");
        }
        entryService.deleteById(id);
        return Result.success("删除成功");
    }
}
