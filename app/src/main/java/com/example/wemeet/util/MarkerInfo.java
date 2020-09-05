package com.example.wemeet.util;

import com.example.wemeet.pojo.BugProperty;

import lombok.Data;
import lombok.experimental.Accessors;

// 用作地图上每个marker存储的对象
@Data
@Accessors(chain = true)
public class MarkerInfo {
    private BugProperty bugProperty;
    private boolean caught;
    private String userAnswer;
}
