package com.arelore.server.core.biz.word.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.arelore.server.core.service.BaseServiceImpl;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBookResponse;
import com.arelore.server.core.biz.word.admin.AdminWordBookService;
import com.arelore.server.core.biz.word.user.dto.UserWordCurrentBookRequest;
import com.arelore.server.core.biz.word.user.dto.UserWordCurrentBookResponse;
import com.arelore.server.core.biz.word.user.entity.UserWordCurrentBook;
import com.arelore.server.core.biz.word.user.mapper.UserWordCurrentBookMapper;
import com.arelore.server.core.biz.word.user.UserWordCurrentBookService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UserWordCurrentBookServiceImpl
    extends BaseServiceImpl<UserWordCurrentBookRequest, UserWordCurrentBookResponse, UserWordCurrentBook>
    implements UserWordCurrentBookService {
    private final UserWordCurrentBookMapper mapper;
    private final AdminWordBookService bookService;

    public UserWordCurrentBookServiceImpl(UserWordCurrentBookMapper mapper, AdminWordBookService bookService) {
        this.mapper = mapper;
        this.bookService = bookService;
    }

    @Override
    protected UserWordCurrentBookMapper mapper() {
        return mapper;
    }

    @Override
    protected Class<UserWordCurrentBookResponse> responseClass() {
        return UserWordCurrentBookResponse.class;
    }

    @Override
    protected LambdaQueryWrapper<UserWordCurrentBook> buildWrapper(UserWordCurrentBookRequest request) {
        LambdaQueryWrapper<UserWordCurrentBook> wrapper = new LambdaQueryWrapper<>();
        if (request == null) {
            return wrapper;
        }
        if (request.getUserId() != null) {
            wrapper.eq(UserWordCurrentBook::getUserId, request.getUserId());
        }
        if (StringUtils.hasText(request.getBookCode())) {
            wrapper.eq(UserWordCurrentBook::getBookCode, request.getBookCode());
        }
        wrapper.orderByDesc(UserWordCurrentBook::getId);
        return wrapper;
    }

    @Override
    public List<UserWordCurrentBookResponse> list(UserWordCurrentBookRequest request) {
        List<UserWordCurrentBookResponse> list = super.list(request);
        for (UserWordCurrentBookResponse item : list) {
            fillBook(item);
        }
        return list;
    }

    @Override
    public int create(UserWordCurrentBookRequest request) {
        prepareSwitch(request, null);
        return super.create(request);
    }

    @Override
    public int update(UserWordCurrentBookRequest request) {
        UserWordCurrentBookResponse exists = request.getId() == null ? null : getById(request.getId());
        prepareSwitch(request, exists);
        return super.update(request);
    }

    private void prepareSwitch(UserWordCurrentBookRequest request, UserWordCurrentBookResponse exists) {
        if (request == null || !StringUtils.hasText(request.getBookCode())) {
            throw new IllegalArgumentException("单词本code不能为空");
        }
        AdminWordBookResponse book = bookService.getByCode(request.getBookCode());
        if (book == null || book.getStatus() != null && book.getStatus() == 0) {
            throw new IllegalArgumentException("单词本不存在或已停用");
        }
        int wordCount = book.getWordCount() == null ? 0 : book.getWordCount();
        boolean reset = exists == null || !request.getBookCode().equals(exists.getBookCode());
        if (reset) {
            request.setLearnDone(0);
            request.setLearnTodo(wordCount);
            request.setReviewDone(0);
            request.setReviewTodo(0);
            return;
        }
        request.setLearnDone(exists.getLearnDone() == null ? 0 : exists.getLearnDone());
        request.setLearnTodo(exists.getLearnTodo() == null ? wordCount : exists.getLearnTodo());
        request.setReviewDone(exists.getReviewDone() == null ? 0 : exists.getReviewDone());
        request.setReviewTodo(exists.getReviewTodo() == null ? 0 : exists.getReviewTodo());
    }

    private void fillBook(UserWordCurrentBookResponse current) {
        if (current == null || !StringUtils.hasText(current.getBookCode())) {
            return;
        }
        AdminWordBookResponse book = bookService.getByCode(current.getBookCode());
        if (book == null) {
            return;
        }
        current.setBookName(book.getName());
        current.setBookDescription(book.getDescription());
        current.setWordCount(book.getWordCount());
    }
}
