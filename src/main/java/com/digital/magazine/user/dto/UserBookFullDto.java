package com.digital.magazine.user.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserBookFullDto {

    private Long id;
    private String title;
    private String author;
    private String category;
    private String coverImage;
    private boolean paid;
    private Double price;

    private List<BookContentDto> contents;
}

