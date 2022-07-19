package com.zhouyue.waimai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhouyue.waimai.mapper.CategoryMapper;
import com.zhouyue.waimai.pojo.Category;
import com.zhouyue.waimai.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

}
