package org.eu.liaohongdong.common.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonPageTest {

    @Test
    void restPage_fromMybatisPlusPage_mapsAllFields() {
        Page<String> page = new Page<>(2, 10);
        page.setTotal(55);
        page.setRecords(List.of("a", "b"));

        CommonPage<String> result = CommonPage.restPage(page);

        assertEquals(2, result.getPageNo());
        assertEquals(10, result.getPageSize());
        assertEquals(6, result.getTotalPage());
        assertEquals(55L, result.getTotal());
        assertEquals(List.of("a", "b"), result.getList());
    }

    @Test
    void restPage_fromMybatisPlusFirstPage_yieldsPageNoOne() {
        Page<String> page = new Page<>(1, 10);
        page.setTotal(30);
        page.setRecords(List.of("v"));

        CommonPage<String> result = CommonPage.restPage(page);

        assertEquals(1, result.getPageNo());
        assertEquals(10, result.getPageSize());
        assertEquals(3, result.getTotalPage());
        assertEquals(30L, result.getTotal());
        assertEquals(List.of("v"), result.getList());
    }

    @Test
    void restPage_fromMybatisPlusEmptyPage_yieldsZeroCounts() {
        Page<String> page = new Page<>(1, 10);
        page.setTotal(0);
        page.setRecords(List.of());

        CommonPage<String> result = CommonPage.restPage(page);

        assertEquals(1, result.getPageNo());
        assertEquals(10, result.getPageSize());
        assertEquals(0, result.getTotalPage());
        assertEquals(0L, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }

    @Test
    void restPage_fromSpringDataPage_mapsAllFieldsOneBased() {
        PageImpl<String> page = new PageImpl<>(List.of("m", "n"), PageRequest.of(1, 10), 55);

        CommonPage<String> result = CommonPage.restPage(page);

        assertEquals(2, result.getPageNo());
        assertEquals(10, result.getPageSize());
        assertEquals(6, result.getTotalPage());
        assertEquals(55L, result.getTotal());
        assertEquals(List.of("m", "n"), result.getList());
    }

    @Test
    void restPage_fromSpringDataFirstPage_yieldsPageNoOne() {
        PageImpl<String> page = new PageImpl<>(List.of("a"), PageRequest.of(0, 5), 12);

        CommonPage<String> result = CommonPage.restPage(page);

        assertEquals(1, result.getPageNo());
        assertEquals(5, result.getPageSize());
        assertEquals(3, result.getTotalPage());
        assertEquals(12L, result.getTotal());
        assertEquals(List.of("a"), result.getList());
    }

    @Test
    void restPage_fromSpringDataEmptyPage_yieldsOneBasedPageNo() {
        PageImpl<String> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        CommonPage<String> result = CommonPage.restPage(page);

        assertEquals(1, result.getPageNo());
        assertEquals(10, result.getPageSize());
        assertEquals(0, result.getTotalPage());
        assertEquals(0L, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }

    @Test
    void restPage_bothOverloads_yieldConsistentOneBasedPageNo() {
        Page<String> mybatisPlusPage = new Page<>(1, 10);
        mybatisPlusPage.setTotal(30);
        mybatisPlusPage.setRecords(List.of("v"));
        PageImpl<String> springDataPage = new PageImpl<>(List.of("v"), PageRequest.of(0, 10), 30);

        CommonPage<String> fromMybatisPlus = CommonPage.restPage(mybatisPlusPage);
        CommonPage<String> fromSpringData = CommonPage.restPage(springDataPage);

        assertEquals(fromMybatisPlus.getPageNo(), fromSpringData.getPageNo());
        assertEquals(fromMybatisPlus.getPageSize(), fromSpringData.getPageSize());
        assertEquals(fromMybatisPlus.getTotalPage(), fromSpringData.getTotalPage());
        assertEquals(fromMybatisPlus.getTotal(), fromSpringData.getTotal());
        assertEquals(fromMybatisPlus.getList(), fromSpringData.getList());
    }
}
