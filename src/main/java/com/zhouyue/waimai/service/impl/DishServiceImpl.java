package com.zhouyue.waimai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhouyue.waimai.mapper.DishMapper;
import com.zhouyue.waimai.pojo.Dish;
import com.zhouyue.waimai.service.DishService;
import org.springframework.stereotype.Service;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
}
