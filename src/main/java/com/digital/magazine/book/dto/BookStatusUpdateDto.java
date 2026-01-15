package com.digital.magazine.book.dto;

import com.digital.magazine.common.enums.BookStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookStatusUpdateDto {

	private BookStatus status;

}
