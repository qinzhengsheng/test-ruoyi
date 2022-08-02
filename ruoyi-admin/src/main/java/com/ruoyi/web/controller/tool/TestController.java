package com.ruoyi.web.controller.tool;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.dto.InvoiceSearchReq;
import com.ruoyi.common.core.domain.vo.InvoiceInfoVO;
import com.ruoyi.system.service.IInvoiceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * swagger 测试方法
 *
 * @author ruoyi
 */
@Api("测试管理")
@RestController
@RequestMapping("/test")
public class TestController extends BaseController {


    @Autowired
    private IInvoiceService invoiceService;

    @ApiOperation("发票信息获取")
    @GetMapping("/getInvoiceInfo")
    public R<List<InvoiceInfoVO>> getInvoiceInfo(@RequestBody InvoiceSearchReq searchReq) {
        List<InvoiceInfoVO> invoiceInfoVOList = invoiceService.getInvoiceInfo(searchReq);
        return R.ok(invoiceInfoVOList);
    }

}