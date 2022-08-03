package com.ruoyi.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.core.domain.dto.InvoiceSearchReq;
import com.ruoyi.common.core.domain.entity.OutputInvoiceMotor;
import com.ruoyi.common.core.domain.vo.InvoiceInfoVO;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.mapper.InvoiceMapper;
import com.ruoyi.system.service.IInvoiceService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 参数配置 服务层实现
 *
 * @author ruoyi
 */
@Service
public class InvoiceServiceImpl extends ServiceImpl<InvoiceMapper, OutputInvoiceMotor> implements IInvoiceService {


    @Override
    public List<InvoiceInfoVO> getInvoiceInfo(InvoiceSearchReq searchReq) {
        if (StringUtils.isEmpty(searchReq.getVin()) && StringUtils.isEmpty(searchReq.getStartDate()) && StringUtils.isEmpty(searchReq.getEndDate())) {
            throw new ServiceException("必须有一个参数");
        }
        List<InvoiceInfoVO> invoiceInfo = baseMapper.getInvoiceInfo(searchReq);
        return invoiceInfo;
    }


}