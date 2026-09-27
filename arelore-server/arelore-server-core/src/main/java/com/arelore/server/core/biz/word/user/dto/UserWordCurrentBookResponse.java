package com.arelore.server.core.biz.word.user.dto;

import com.arelore.server.core.biz.word.user.entity.UserWordCurrentBook;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserWordCurrentBookResponse extends UserWordCurrentBook {
    private String bookName;
    private String bookDescription;
    private Integer wordCount;
}
