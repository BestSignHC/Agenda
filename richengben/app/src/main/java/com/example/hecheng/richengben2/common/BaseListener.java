package com.example.hecheng.richengben2.common;

/**
 * Created by HeCheng on 2017/3/28.
 */

public interface BaseListener<T> {
    void getSuccess(T t);
    void getFailure(Exception e);
}
