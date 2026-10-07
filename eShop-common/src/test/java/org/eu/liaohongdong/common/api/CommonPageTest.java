package org.eu.liaohongdong.common.api;

import com.github.pagehelper.Page;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonPageTest {

    @Test
    void restPage_fromPageHelperPage_mapsAllFields() {
        Page<String> page = new Page<>(2, 10);
        page.setTotal(55);
        page.add("a");
        page.add("b");

        CommonPage<String> result = CommonPage.restPage(page);

        assertEquals(2, result.getPageNo());
        assertEquals(10, result.getPageSize());
        assertEquals(6, result.getTotalPage());
        assertEquals(55L, result.getTotal());
        assertEquals(List.of("a", "b"), result.getList());
    }

    @Test
    void restPage_fromPageHelperPlainList_fallsBackToWholeListAsOnePage() {
        List<String> list = new ArrayList<>(List.of("x", "y", "z"));

        CommonPage<String> result = CommonPage.restPage(list);

        assertEquals(1, result.getPageNo());
        assertEquals(3, result.getPageSize());
        assertEquals(1, result.getTotalPage());
        assertEquals(3L, result.getTotal());
        assertEquals(List.of("x", "y", "z"), result.getList());
    }

    @Test
    void restPage_fromPageHelperEmptyList_yieldsZeroCounts() {
        CommonPage<String> result = CommonPage.restPage(new ArrayList<>());

        assertEquals(1, result.getPageNo());
        assertEquals(0, result.getPageSize());
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
        Page<String> pageHelperPage = new Page<>(1, 10);
        pageHelperPage.setTotal(30);
        pageHelperPage.add("v");
        PageImpl<String> springDataPage = new PageImpl<>(List.of("v"), PageRequest.of(0, 10), 30);

        CommonPage<String> fromPageHelper = CommonPage.restPage(pageHelperPage);
        CommonPage<String> fromSpringData = CommonPage.restPage(springDataPage);

        assertEquals(fromPageHelper.getPageNo(), fromSpringData.getPageNo());
        assertEquals(fromPageHelper.getPageSize(), fromSpringData.getPageSize());
        assertEquals(fromPageHelper.getTotalPage(), fromSpringData.getTotalPage());
        assertEquals(fromPageHelper.getTotal(), fromSpringData.getTotal());
        assertEquals(fromPageHelper.getList(), fromSpringData.getList());
    }
}
