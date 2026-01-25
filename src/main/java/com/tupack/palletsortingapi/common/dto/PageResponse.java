package com.tupack.palletsortingapi.common.dto;

import lombok.Data;

@Data
public class PageResponse {
  private Integer pageNumber;
  private Integer pageSize;
  private Long totalElements;
  private Integer totalPages;

}
