package com.zhouyue.waimai.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhouyue.waimai.pojo.R;
import com.zhouyue.waimai.pojo.ShoppingCart;
import com.zhouyue.waimai.service.ShoppingCartService;
import com.zhouyue.waimai.util.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车数据: {}", shoppingCart);
        //设置用户 id
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        Long dishId = shoppingCart.getDishId();
        QueryWrapper<ShoppingCart> queryWrapper = new QueryWrapper<>();
        //判断是菜品还是套餐
        if (dishId != null){
            //判断是否已经加入过，如果加入过则直接将 number 加一
            queryWrapper.eq("user_id", currentId).eq("dish_id", dishId);
        }else{
            //判断是否已经加入过，如果加入过则直接将 number 加一
            queryWrapper.eq("user_id", currentId).eq("setmeal_id", shoppingCart.getSetmealId());
        }
        ShoppingCart one = shoppingCartService.getOne(queryWrapper);
        if (one != null){
            one.setNumber(one.getNumber() + 1);
            shoppingCartService.updateById(one);
        }else {
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            one = shoppingCart;
        }
        return R.success(one);
    }

    /**
     * 减少加入购物车中的菜品或套餐的数量
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        Long currentId = BaseContext.getCurrentId();
        QueryWrapper<ShoppingCart> queryWrapper = new QueryWrapper<>();
        if (shoppingCart.getDishId() != null){
            queryWrapper.eq("user_id", currentId).eq("dish_id", shoppingCart.getDishId());
        }else {
            queryWrapper.eq("user_id", currentId).eq("setmeal_id", shoppingCart.getSetmealId());
        }
        ShoppingCart one = shoppingCartService.getOne(queryWrapper);
        one.setNumber(one.getNumber() - 1);
        if (one.getNumber() <= 0){
            shoppingCartService.removeById(one.getId());
        }else{
            shoppingCartService.updateById(one);
        }
        return R.success(one);
    }

    /**
     * 获取登录用户添加到购物车的所有订单
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> getOrders(){
        Long loginUserId = BaseContext.getCurrentId();
        List<ShoppingCart> list = shoppingCartService.list(new QueryWrapper<ShoppingCart>().eq("user_id", loginUserId).orderByDesc("create_time"));
        return R.success(list);
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        Long userId = BaseContext.getCurrentId();
        shoppingCartService.remove(new QueryWrapper<ShoppingCart>().eq("user_id", userId));
        return R.success("清空完成");
    }
}
