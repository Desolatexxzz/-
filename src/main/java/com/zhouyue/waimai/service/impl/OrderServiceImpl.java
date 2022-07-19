package com.zhouyue.waimai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhouyue.waimai.mapper.OrderMapper;
import com.zhouyue.waimai.pojo.*;
import com.zhouyue.waimai.service.*;
import com.zhouyue.waimai.util.BaseContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private UserService userService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 提交订单
     * @param orders
     */
    @Override
    public void submit(Orders orders) {
        Long currentId = BaseContext.getCurrentId();
        //获取购物车信息
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(new QueryWrapper<ShoppingCart>().eq("user_id", currentId));
        //获取用户信息
        User user = userService.getById(currentId);
        Long addressBookId = orders.getAddressBookId();
        //获取地址信息
        AddressBook addressBook = addressBookService.getById(addressBookId);
        //生成订单号
        long id = IdWorker.getId();
        //计算订单总金额
        AtomicInteger amount = new AtomicInteger(0); //是原则操作，避免多线程出现问题
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart shoppingCart : shoppingCarts) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setName(shoppingCart.getName());
            orderDetail.setOrderId(id);
            orderDetail.setDishId(shoppingCart.getDishId());
            orderDetail.setDishFlavor(shoppingCart.getDishFlavor());
            orderDetail.setSetmealId(shoppingCart.getSetmealId());
            orderDetail.setNumber(shoppingCart.getNumber());
            orderDetail.setAmount(shoppingCart.getAmount());
            orderDetail.setImage(shoppingCart.getImage());
            amount.addAndGet(shoppingCart.getAmount().multiply(new BigDecimal(shoppingCart.getNumber())).intValue());
            orderDetails.add(orderDetail);
        }
        //添加订单相应信息
        orders.setNumber(String.valueOf(id));
        orders.setStatus(2);
        orders.setUserId(currentId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setAmount(new BigDecimal(amount.get()));
        orders.setUserName(user.getName());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        orders.setConsignee(addressBook.getConsignee());
        this.save(orders);
        //向订单明细表中插入数据
        orderDetailService.saveBatch(orderDetails);
        //清空购物车
        shoppingCartService.remove(new QueryWrapper<ShoppingCart>().eq("user_id", currentId));

    }
}
