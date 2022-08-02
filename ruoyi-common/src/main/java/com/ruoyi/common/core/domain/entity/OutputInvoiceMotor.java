package com.ruoyi.common.core.domain.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Dereck Z Qin
 * @date 2022/8/2
 */
@Data
@TableName("output_invoice_motor")
public class OutputInvoiceMotor extends Model<OutputInvoiceMotor> {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 车辆VIN
     */
    private String vin;

    /**
     * 发票代码
     */
    private String invoiceCode;

    /**
     * 发票号码
     */
    private String invoiceNo;

    /**
     * 发票金额（含税）
     */
    private BigDecimal totalAmount;

    /**
     * 不含税价
     */
    private BigDecimal amount;

    /**
     * 增值税税额
     */
    private BigDecimal tax;

    /**
     * 开票日期
     */
    private String invoiceDate;

    /**
     * 销售服务商名称
     */
    private String saleUnitName;

    /**
     * 销售服务商所在省代码
     */
    private String saleUnitProvinceCode;

    /**
     * 销售服务商所在省
     */
    private String saleUnitProvince;

    /**
     * 销售服务商所在市代码
     */
    private String saleUnitCityCode;

    /**
     * 销售服务商所在市
     */
    private String saleUnitCity;

    /**
     * 销售服务商所在区代码
     */
    private String saleUnitAreaCode;

    /**
     * 销售服务商所在区
     */
    private String saleUnitArea;

    /**
     * 销售服务商统一社会信用代码
     */
    private String saleUnitCode;

    /**
     * 销售地址
     */
    private String saleAddress;

    /**
     * 发票状态:已开具，已打印的，已作废，已红冲
     */
    private String status;

}