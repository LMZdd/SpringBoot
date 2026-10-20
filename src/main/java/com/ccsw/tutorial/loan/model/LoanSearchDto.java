package com.ccsw.tutorial.loan.model;

import com.ccsw.tutorial.common.pagination.PageableRequest;

public class LoanSearchDto {
    private PageableRequest pageable;

    /**
     * @return pageable
     */
    public PageableRequest getPageable() {
        return pageable;
    }

    /**
     * @param pageable new value of {@link #getPageable()}.
     */
    public void setPageable(PageableRequest pageable) {
        this.pageable = pageable;
    }
}
