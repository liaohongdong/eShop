package org.eu.liaohongdong.common.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

// 通用分页数据封装类
@Data
public class CommonPage<T> {
    // 当前页码
    private Integer pageNo;
    // 每页数量
    private Integer pageSize;
    // 总页数
    private Integer totalPage;
    // 总条数
    private Long total;

    private List<?> list;

    // MyBatis-Plus IPage 转换方法
    public static <T> CommonPage<T> restPage(IPage<T> pageInfo) {
        CommonPage<T> result = new CommonPage<T>();
        result.setPageNo((int) pageInfo.getCurrent()); // MP current 从1开始
        result.setPageSize((int) pageInfo.getSize());
        result.setTotalPage((int) pageInfo.getPages());
        result.setTotal(pageInfo.getTotal());
        result.setList(pageInfo.getRecords());
        return result;
    }

    // 将SpringData分页后的list转为分页信息
    public static <T> CommonPage<T> restPage(Page<T> pageInfo) {
        CommonPage<T> result = new CommonPage<T>();
        result.setPageNo(pageInfo.getNumber() + 1); // SpringData number从0，转成1开始
        result.setPageSize(pageInfo.getSize());
        result.setTotalPage(pageInfo.getTotalPages());
        result.setTotal(pageInfo.getTotalElements());
        result.setList(pageInfo.getContent());
        return result;
    }

}
