package com.zhouyue.waimai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhouyue.waimai.mapper.ShoppingCartMapper;
import com.zhouyue.waimai.pojo.ShoppingCart;
import com.zhouyue.waimai.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
