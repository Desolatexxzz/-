package com.zhouyue.waimai.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhouyue.waimai.pojo.OrderDetail;
import com.zhouyue.waimai.pojo.Orders;
import com.zhouyue.waimai.pojo.OrdersDto;
import com.zhouyue.waimai.pojo.R;
import com.zhouyue.waimai.service.OrderDetailService;
import com.zhouyue.waimai.service.OrderService;
import com.zhouyue.waimai.util.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 提交订单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        orderService.submit(orders);
        return R.success("订单提交成功");
    }

    /**
     * 分页获取所有订单信息
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(@RequestParam int page, @RequestParam int pageSize, String number, String beginTime, String endTime){
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        if (number != null){
            queryWrapper.like("number", number);
        }
        if (beginTime != null && endTime != null){
            log.info("beginTime: " + beginTime + " endTime: " + endTime);
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime begin = LocalDateTime.parse(beginTime, df);
            LocalDateTime end = LocalDateTime.parse(endTime, df);
            log.info("begin: " + begin + " end: " + end);
            queryWrapper.ge("order_time", begin);
            queryWrapper.le("order_time", end);
        }
        queryWrapper.orderByDesc("order_time");
        Page<Orders> orderPage = new Page<Orders>(page, pageSize);
        Page<Orders> ordersPage = orderService.page(orderPage, queryWrapper);
        return R.success(ordersPage);
    }

    /**
     * 更改订单状态
     * @param orders
     * @return
     */
    @PutMapping
    public R<String> order(@RequestBody Orders orders){
        Orders orders1 = orderService.getById(orders.getId());
        orders1.setStatus(orders.getStatus());
        orderService.updateById(orders1);
        return R.success("派送中...");
    }

    /**
     * 获取当前用户的订单
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> userOrders(int page, int pageSize){
        Long userId = BaseContext.getCurrentId();
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("order_time");
        Page<Orders> page1 = new Page<>(page, pageSize);
        Page<Orders> ordersPage = orderService.page(page1, queryWrapper);
        Page<OrdersDto> dtoPage = new Page<>();
        BeanUtils.copyProperties(ordersPage, dtoPage);
        List<Orders> orders = ordersPage.getRecords();
        List<OrdersDto> ordersDtoList = new ArrayList<>();
        for (Orders order : orders) {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(order, ordersDto);
            ordersDto.setUserName(order.getUserName());
            ordersDto.setPhone(order.getPhone());
            ordersDto.setAddress(order.getAddress());
            ordersDto.setConsignee(order.getConsignee());
            List<OrderDetail> orderDetails = orderDetailService.list(new QueryWrapper<OrderDetail>().eq("order_id", order.getNumber()));
            ordersDto.setOrderDetails(orderDetails);
            ordersDtoList.add(ordersDto);
        }
        dtoPage.setRecords(ordersDtoList);
        return R.success(dtoPage);
    }

    /**
     * 再来一单
     * @param orders
     * @return
     */
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders){
        Orders againOrder = orderService.getById(orders.getId());
        orderService.submit(againOrder);
        return R.success("下单成功");
    }
}
