package com.ruoyi.web.controller.tool;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.dto.InvoiceSearchReq;
import com.ruoyi.common.core.domain.vo.InvoiceInfoVO;
import com.ruoyi.system.service.IInvoiceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
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

    public static void main(String[] args) {
//        try {
//            readTextFromPdf("C:\\Users\\Dereck Z Qin\\Downloads\\7830_00838468_电子增值税普通发票_发票联.pdf");
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

//        List<String> strs = new ArrayList<String>();
//        String str = "hfh 4576867543 ghhg 4174656473 ghfng9837463647443";
//        String pattern = "(41|45)\\d{8}";
//        Pattern r = Pattern.compile(pattern);
//        Matcher m = r.matcher(str);
//        while (m.find()) {
//            strs.add(m.group());
//        }
//        System.out.println("====>>>" + strs);

        BigDecimal bigDecimal = new BigDecimal("100.9873000");
        BigDecimal bigDecimal2 = new BigDecimal("100");
        BigDecimal bigDecimal3 = new BigDecimal("101.1");

        DecimalFormat decimalFormat = new DecimalFormat("0.00#####");
        String format = decimalFormat.format(bigDecimal);
        String format2 = decimalFormat.format(bigDecimal2);
        String format1 = decimalFormat.format(bigDecimal3);

        System.out.println(format);
        System.out.println(format2);
        System.out.println(format1);
    }

    /**
     * 读取文本
     *
     * @param page
     * @param doc
     * @throws Exception
     */
    private static void readText(int page, PDDocument doc) throws Exception {
        if (doc == null) {
            return;
        }
        AccessPermission ap = doc.getCurrentAccessPermission();
        if (!ap.canExtractContent()) {
            String tmp = "can not extract content";
            tmp = tmp + System.lineSeparator() + "--------" + (page + 1) + "/" + doc.getNumberOfPages() + "--------" + System.lineSeparator();
            System.out.println("====>>>文本:" + tmp);
            return;
        }
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        stripper.setStartPage(page + 1);
        stripper.setEndPage(page + 1);
        String text = stripper.getText(doc);
        text = text + System.lineSeparator() + "--------" + (page + 1) + "/" + doc.getNumberOfPages() + "--------" + System.lineSeparator();
        System.out.println("====>>>文本:" + text);
    }

    /**
     * 读取文本
     *
     * @param
     * @throws Exception
     */
    public static void readTextFromPdf(String filePath) throws Exception {
        // 加载一个pdf对象
        PDDocument doc = Loader.loadPDF(new File(filePath));
        int pages = doc.getNumberOfPages();
        for (int i = 0; i < pages; i++) {
            //读取文字
            readText(i, doc);
        }
        doc.close();
    }


}