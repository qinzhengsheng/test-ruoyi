package com.ruoyi.common.core.domain.dto;

import lombok.Data;

/**
 * @author Dereck Z Qin
 * @date 2022/8/2
 */
@Data
public class InvoiceSearchReq {

    /**
     * 车辆VIN
     */
    private String vin;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 截止日期
     */
    private String endDate;


}