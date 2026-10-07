package org.eu.liaohongdong.common.api;

import com.github.pagehelper.PageInfo;
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

    // 将PageHelper分页后的list转为分页信息
    public static <T> CommonPage<T> restPage(List<T> list) {
        CommonPage<T> result = new CommonPage<T>();
        PageInfo<T> pageInfo = new PageInfo<T>(list);
        result.setPageNo(pageInfo.getPageNum()); // 默认从0开始
        result.setPageSize(pageInfo.getPageSize());
        result.setTotalPage(pageInfo.getPages());
        result.setTotal(pageInfo.getTotal());
        result.setList(pageInfo.getList());
        return result;
    }

    // 将SpringData分页后的list转为分页信息
    public static <T> CommonPage<T> restPage(Page<T> pageInfo) {
        CommonPage<T> result = new CommonPage<T>();
        result.setPageNo(pageInfo.getNumber() + 1); // 默认从1开始
        result.setPageSize(pageInfo.getSize());
        result.setTotalPage(pageInfo.getTotalPages());
        result.setTotal(pageInfo.getTotalElements());
        result.setList(pageInfo.getContent());
        return result;
    }

}
