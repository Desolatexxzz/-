package com.zhouyue.waimai.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhouyue.waimai.pojo.Employee;
import com.zhouyue.waimai.pojo.R;
import com.zhouyue.waimai.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;
    /**
     * 登录接口
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        // 1. 将页面提交的密码进行 md5 加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        // 2. 根据用户提交的用户名查询数据库
        Employee emp = employeeService.getOne(new QueryWrapper<Employee>().eq("username", employee.getUsername()));
        // 3. 如果没有查询到则返回登录失败结果
        if (emp == null){
            return R.error("账号不存在");
        }
        // 4. 进行密码比对
        if (!emp.getPassword().equals(password)){
            return R.error("密码错误");
        }
        // 5. 查看员工状态，是否被禁用
        if (emp.getStatus() == 0){
            return R.error("账号被禁用");
        }
        // 6. 登录成功, 将员工 id 存入 session
        request.getSession().setAttribute("empId", emp.getId());
        return R.success(emp);
    }

    /**
     * 退出登录
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> loginOut(HttpServletRequest request){
        // 清理 session 保存的用户 id
        request.getSession().removeAttribute("empId");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee){
        //设置初始密码 123456，需要进行 md5 加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //设置创建时间
//        employee.setCreateTime(LocalDateTime.now());
        //设置更新时间
//        employee.setUpdateTime(LocalDateTime.now());
        //设置创建人
//        employee.setCreateUser((Long) request.getSession().getAttribute("empId"));
//        employee.setUpdateUser((Long) request.getSession().getAttribute("empId"));
        boolean save = employeeService.save(employee);
        if (save){
            return R.success("添加成功");
        }
        return R.error("出现错误, 添加失败");
    }

    /**
     * 员工信息的分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);

        //构造分页构造器
        Page pageInfo = new Page(page, pageSize);
        //构造条件构造器
        QueryWrapper queryWrapper = new QueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), "name", name);
        //添加排序条件
        queryWrapper.orderByDesc("update_time");
        //执行查询
        employeeService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 更新员工（前端的编辑和禁用功能）
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee){
//        employee.setUpdateUser((Long) request.getSession().getAttribute("empId"));
//        employee.setUpdateTime(LocalDateTime.now());
        log.info(employee.toString());
        boolean flag = employeeService.updateById(employee);
        if (flag){
            return R.success("更新成功");
        }
        return R.error("更新失败");
    }

    /**
     * 根据 id 获取员工信息
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getEmployeeById(@PathVariable Long id){
        Employee employee = employeeService.getById(id);
        return R.success(employee);
    }

}
