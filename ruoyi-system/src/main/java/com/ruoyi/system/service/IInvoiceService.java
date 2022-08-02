package com.ruoyi.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.core.domain.dto.InvoiceSearchReq;
import com.ruoyi.common.core.domain.entity.OutputInvoiceMotor;
import com.ruoyi.common.core.domain.vo.InvoiceInfoVO;

import java.util.List;

/**
 * 参数配置 服务层
 *
 * @author ruoyi
 */
public interface IInvoiceService extends IService<OutputInvoiceMotor> {

    /**
     * 发票信息获取
     *
     * @param searchReq 查询条件
     * @return InvoiceInfoVO
     */
    List<InvoiceInfoVO> getInvoiceInfo(InvoiceSearchReq searchReq);

}