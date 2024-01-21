package io.bhex.broker.admin.util;

import lombok.Builder;
import lombok.Getter;

/**
 * @ProjectName: broker-server
 * @Package: io.bhex.broker.server
 * @Author: ming.xu
 * @CreateDate: 20/08/2018 10:28 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
public class PageUtil {

    public static Page pageCount(Integer current, Integer pageSize, Integer total) {
        int fromIndex = (current - 1) * pageSize;

        fromIndex = fromIndex <= 0 || fromIndex > total ? 0 : fromIndex;
        int end = fromIndex + pageSize;
        int endIndex = end <= total ? end : total;
        return Page.builder().start(fromIndex).offset(endIndex).build();
    }



    @Getter
    @Builder
    public static class Page {

        private Integer start;

        private Integer offset;
    }
}
