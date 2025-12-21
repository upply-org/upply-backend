package com.upply.common;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {

    private List<T> content;

    private int number;     // page number
    private int size;       // size of each page

    private Long totalElements;
    private int totalPages;

    private boolean first;
    private boolean last;
}
