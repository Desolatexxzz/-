package com.zhouyue.waimai.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhouyue.waimai.pojo.*;
import com.zhouyue.waimai.service.CategoryService;
import com.zhouyue.waimai.service.DishFlavorService;
import com.zhouyue.waimai.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获得所有菜品信息
     * @return
     */
    @GetMapping("/page")
    public R<Page> getAllDishes(int page, int pageSize, String name){
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> rePage = new Page<>();
        QueryWrapper<Dish> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), "name", name);
        queryWrapper.orderByDesc("update_time");
        Page<Dish> dishPage = dishService.page(pageInfo, queryWrapper);
        BeanUtils.copyProperties(dishPage, rePage, "records");
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = new ArrayList<>();
        for (Dish dish : records) {
            Long categoryId = dish.getCategoryId();
            Category category = categoryService.getById(categoryId);
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish,dishDto);
            dishDto.setCategoryName(category.getName());
            list.add(dishDto);
        }
        rePage.setRecords(list);
        return R.success(rePage);
    }

    /**
     * 新增一个菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        //每次新增，清理对应分类缓存
        Long categoryId = dishDto.getCategoryId();
        String key = "dish_" + categoryId;
        redisTemplate.delete(key);
        dishService.save(dishDto);
        log.info(dishDto.getId().toString());
        for (DishFlavor flavor : dishDto.getFlavors()) {
            flavor.setDishId(dishDto.getId());
            dishFlavorService.save(flavor);
        }
        return R.success("添加成功");

    }

    /**
     * 根据 id 获取菜品信息（包括口味）
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getDishById(@PathVariable Long id){
        Dish dish = dishService.getById(id);
        List<DishFlavor> dishFlavors = dishFlavorService.list(new QueryWrapper<DishFlavor>().eq("dish_id", id));
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);
        dishDto.setFlavors(dishFlavors);
        return R.success(dishDto);
    }

    /**
     * 更新菜品信息
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        //每次修改，清理对应分类缓存
        Long categoryId = dishDto.getCategoryId();
        String key = "dish_" + categoryId;
        redisTemplate.delete(key);
        dishService.updateById(dishDto);
        // 对于口味的配置我们选择先删除之前的口味再添加当前口味
        Long id = dishDto.getId();
        dishFlavorService.remove(new QueryWrapper<DishFlavor>().eq("dish_id", id));
        for (DishFlavor flavor : dishDto.getFlavors()) {
            flavor.setDishId(id);
            dishFlavorService.save(flavor);
        }
        return R.success("更新成功");
    }

    /**
     * 删除/批量删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam(name = "ids") Long[] ids){
        int count = dishService.count(new QueryWrapper<Dish>().eq("status", 1).in("id", ids));
        if (count != 0){
            return R.error("存在启售的菜品, 删除失败");
        }
        for (Long id : ids) {
            //删除菜品口味记录
            dishFlavorService.remove(new QueryWrapper<DishFlavor>().eq("dish_id", id));
            //删除菜品
            dishService.removeById(id);
        }
        return R.success("删除成功");
    }

    /**
     * 修改/批量修改菜品信息
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable int status, @RequestParam(name = "ids") Long[] ids){
        for (Long id : ids) {
            Dish dish = new Dish();
            dish.setId(id);
            dish.setStatus(status);
            dishService.updateById(dish);
        }
        return R.success("更新成功");
    }

    /**
     * 通过菜品分类id查询所有菜品
     * @return
     */
//    @GetMapping("/list")
//    public R<List<Dish>> getDishesByCategoryId(Long categoryId, String name){
//        QueryWrapper<Dish> queryWrapper = new QueryWrapper<>();
//        if (StringUtils.isNotEmpty(name)){
//            queryWrapper.like("name", name);
//        }else{
//            queryWrapper.eq("category_id", categoryId);
//        }
//        queryWrapper.eq("status", 1);
//        queryWrapper.orderByDesc("update_time");
//        List<Dish> dishList = dishService.list(queryWrapper);
//        return R.success(dishList);
//    }
    @GetMapping("/list")
    public R<List<DishDto>> getDishesByCategoryId(Long categoryId, String name){
        String key = "dish_" + categoryId;
        //从redis中获取数据
        List<DishDto> list = (List<DishDto>) redisTemplate.opsForValue().get(key);
        //存在则直接返回
        if (list != null){
            return R.success(list);
        }
        //不存在则查询数据库，并将菜品缓存到redis
        QueryWrapper<Dish> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(name)){
            queryWrapper.like("name", name);
        }else{
            queryWrapper.eq("category_id", categoryId);
        }
        queryWrapper.eq("status", 1);
        queryWrapper.orderByDesc("update_time");
        List<Dish> dishList = dishService.list(queryWrapper);
        List<DishDto> dishDtoList = new ArrayList<>();
        for (Dish dish : dishList) {
            List<DishFlavor> dishFlavor = dishFlavorService.list(new QueryWrapper<DishFlavor>().eq("dish_id", dish.getId()));
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish, dishDto);
            dishDto.setFlavors(dishFlavor);
            dishDtoList.add(dishDto);
        }
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);
        return R.success(dishDtoList);
    }
}
