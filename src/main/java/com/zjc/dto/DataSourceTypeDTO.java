package com.zjc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceTypeDTO {
    private String name;
    private String description;
    private String classFullName;
}