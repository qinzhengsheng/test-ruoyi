package com.ruoyi.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.common.core.domain.dto.InvoiceSearchReq;
import com.ruoyi.common.core.domain.entity.OutputInvoiceMotor;
import com.ruoyi.common.core.domain.vo.InvoiceInfoVO;

import java.util.List;

/**
 * 参数配置 数据层
 *
 * @author ruoyi
 */
public interface InvoiceMapper extends BaseMapper<OutputInvoiceMotor> {

    /**
     * 发票信息获取
     *
     * @param searchReq 查询条件
     * @return InvoiceInfoVO
     */
    List<InvoiceInfoVO> getInvoiceInfo(InvoiceSearchReq searchReq);


}
