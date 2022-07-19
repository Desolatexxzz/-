package com.zhouyue.waimai.util;

/**
 * 基于 threadlocal 封装工具类，用户保存和获取当前登录的用户 id
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
