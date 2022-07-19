package com.zhouyue.waimai.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhouyue.waimai.pojo.*;
import com.zhouyue.waimai.service.CategoryService;
import com.zhouyue.waimai.service.DishService;
import com.zhouyue.waimai.service.SetmealDishService;
import com.zhouyue.waimai.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private DishService dishService;

    /**
     * 分页获取所有/查询套餐
     * @return
     */
    @GetMapping("/page")
    public R<Page> getSetmeals(int page, int pageSize, String name){
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPage = new Page<>();
        QueryWrapper<Setmeal> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(name)){
            queryWrapper.like("name", name);
        }
        queryWrapper.orderByDesc("update_time");
        Page<Setmeal> setmealPage = setmealService.page(pageInfo, queryWrapper);
        BeanUtils.copyProperties(setmealPage, dtoPage, "records");
        List<Setmeal> setmeals = setmealPage.getRecords();
        List<SetmealDto> setmealDtos = new ArrayList<>();
        for (Setmeal setmeal : setmeals) {
            Category category = categoryService.getById(setmeal.getCategoryId());
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(setmeal, setmealDto);
            setmealDto.setCategoryName(category.getName());
            setmealDtos.add(setmealDto);
        }
        dtoPage.setRecords(setmealDtos);
        return R.success(dtoPage);
    }

    /**
     * 添加套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> addSetmeal(@RequestBody SetmealDto setmealDto){
        setmealService.save(setmealDto);
        Long setmealId = setmealDto.getId();
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealId);
        }
        setmealDishService.saveBatch(setmealDishes);
        return R.success("添加成功");
    }

    /**
     * 通过套餐 id 查询套餐相关信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getSetmealById(@PathVariable Long id){
        Setmeal setmeal = setmealService.getById(id);
        Category category = categoryService.getOne(new QueryWrapper<Category>().eq("id", setmeal.getCategoryId()));
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);
        setmealDto.setCategoryName(category.getName());
        List<SetmealDish> list = setmealDishService.list(new QueryWrapper<SetmealDish>().eq("setmeal_id", id));
        setmealDto.setSetmealDishes(list);
        return R.success(setmealDto);

    }

    /**
     * 修改套餐
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateById(setmealDto);
        List<SetmealDish> list = setmealDto.getSetmealDishes();
        Long id = setmealDto.getId();
        setmealDishService.remove(new QueryWrapper<SetmealDish>().eq("setmeal_id", id));
        for (SetmealDish setmealDish : list) {
            setmealDish.setSetmealId(id);
        }
        setmealDishService.saveBatch(list);
        return R.success("修改成功");
    }

    /**
     * 删除/批量删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long[] ids){
        int count = setmealService.count(new QueryWrapper<Setmeal>().eq("status", 1).in("id", ids));
        if (count != 0){
            return R.error("存在启售的套餐, 无法删除");
        }
        for (Long id : ids) {
            setmealDishService.remove(new QueryWrapper<SetmealDish>().eq("setmeal_id", id));
            setmealService.removeById(id);
        }
        return R.success("删除成功");
    }

    /**
     * 修改/批量修改套餐状态
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable int status, Long[] ids){
        for (Long id : ids) {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(id);
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);
        }
        return R.success("修改成功");
    }

    /**
     * 根据分类id查询该分类下的所有套餐
     * @param categoryId
     * @param status
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> getSetmealsByCategpryId(Long categoryId, int status){
        QueryWrapper<Setmeal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_id", categoryId);
        queryWrapper.eq("status", status);
        queryWrapper.orderByDesc("update_time");
        List<Setmeal> setmeals = setmealService.list(queryWrapper);
        return R.success(setmeals);
    }

    /**
     * 获取套餐下的所有菜品
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    public R<List<DishDto>> getSetmealDishes(@PathVariable String id){
        List<SetmealDish> list = setmealDishService.list(new QueryWrapper<SetmealDish>().eq("setmeal_id", id));
        List<DishDto> dishDtoList = new ArrayList<>();
        for (SetmealDish setmealDish : list) {
            Dish dish = dishService.getById(setmealDish.getDishId());
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish, dishDto);
            dishDto.setCopies(setmealDish.getCopies());
            dishDtoList.add(dishDto);
        }
            return R.success(dishDtoList);
    }
}
