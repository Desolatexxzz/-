package com.zhouyue.waimai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhouyue.waimai.mapper.UserMapper;
import com.zhouyue.waimai.pojo.User;
import com.zhouyue.waimai.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
