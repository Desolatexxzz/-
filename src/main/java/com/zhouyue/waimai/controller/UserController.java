package com.zhouyue.waimai.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhouyue.waimai.pojo.R;
import com.zhouyue.waimai.pojo.User;
import com.zhouyue.waimai.service.UserService;
import com.zhouyue.waimai.util.SMSUtils;
import com.zhouyue.waimai.util.UserName;
import com.zhouyue.waimai.util.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    /**
     * 发送手机验证码
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpServletRequest request){
        log.info(user.getPhone());
        //调用工具类生成验证码
        Integer code = ValidateCodeUtils.generateValidateCode(6);
        //发送发证吗
        SMSUtils.send(user.getPhone(), code);
        //将生成的验证码保存到 session
        HttpSession session = request.getSession();
        session.setAttribute("code", code);
        return R.success("发送成功, 请耐心等待");
    }

    /**
     * 移动端登录
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpServletRequest request){
        log.info(map.toString());
        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
        //从 session 获取保存的验证码
        Integer saveCode = (Integer) request.getSession().getAttribute("code");
        //进行验证码比对
//        if (code.equals(saveCode.toString())){ //先注释，避免每次登录都要发送验证码，发消息也是要钱的。。。
        if (true){
            //成功，则登录成功
            //登录成功，判断是否存在表中，不存在则保存到表中
            User user = userService.getOne(new QueryWrapper<User>().eq("phone", phone));
            if (user == null){
                user = new User();
                user.setPhone(phone);
                String name = UserName.getStringRandom(11);
                user.setName(name);
                userService.save(user);
            }
            request.getSession().setAttribute("user", user.getId());
            return R.success(user);
        }
        return R.error("登录失败");
    }
}
