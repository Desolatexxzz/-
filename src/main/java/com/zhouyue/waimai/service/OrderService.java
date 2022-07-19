package com.zhouyue.waimai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhouyue.waimai.pojo.Orders;

public interface OrderService extends IService<Orders> {
    /**
     * 提交订单
     * @param orders
     */
    void submit(Orders orders);
}
