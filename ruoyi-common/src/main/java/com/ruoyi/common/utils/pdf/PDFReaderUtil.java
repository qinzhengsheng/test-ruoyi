package com.ruoyi.common.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.dom4j.DocumentException;
import org.springframework.web.multipart.MultipartFile;
import pwc.taxtech.biz.common.dto.OperationResultDto;
import pwc.taxtech.biz.common.service.CKHelper;
import pwc.taxtech.biz.common.service.FileUploadService;
import pwc.taxtech.biz.common.util.CommonUtils;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class PDFReaderUtil {
    protected static final String kpje_key = "(小写)";
    protected static final String kpje_key2 = "（小写）";
    protected static final String slv_key = "税率";
    private static final String FPDM = "发票代码";
    private static final String FPHM = "发票号码";
    private static final String KPRQ = "开票日期";
    private static final String JYM = "校验码";
    private static final String MC = "名称";
    private static final String NSRSBH = "纳税人识别号";
    private static final String DZDH = "地址、电话";
    private static final String KHHJZH = "开户行及账号";
    private static final String JQBH = "机器编号";
    private static final String HJ = "合计";
    private static final String HJ_H = "合";
    private static final String HJ_J = "计";
    private static final String JSHJ = "价税合计";
    private static final String HWLWFWMC = "货物或应税劳务、服务名称";
    private static final String HWLWFWMC_P = "货物或应税劳务、服";
    private static final String GGXH = "规格型号";
    private static final String DW = "单位";
    private static final String DW_D = "单";
    private static final String DW_W = "位";
    private static final String SL = "数量";
    private static final String SL_S = "数";
    private static final String SL_L = "量";
    private static final String DJ = "单价";
    private static final String DJ_D = "单";
    private static final String DJ_J = "价";
    private static final String JE = "金额";
    private static final String JE_J = "金";
    private static final String JE_E = "额";
    private static final String SLV = "税率";
    private static final String SLV_S = "税";
    private static final String SLV_LV = "率";
    private static final String SE = "税额";
    private static final String SE_S = "税";
    private static final String SE_E = "额";
    private static final String MMQ = "密码区";
    private static final String BZ = "备注";
    private static final String MMQ_BZ = "密码区备注";
    private static ReceiptPosition jqbhR = null;
    private static ReceiptPosition hjR = null;
    private static ReceiptPosition jshjR = null;
    private static ReceiptPosition hwmcR = null;
    private static ReceiptPosition ggxhR = null;
    private static ReceiptPosition danweiR = null;
    private static ReceiptPosition shuliangR = null;
    private static ReceiptPosition danjiaR = null;
    private static ReceiptPosition jineR = null;
    private static ReceiptPosition shuilvR = null;
    private static ReceiptPosition shuieR = null;
    private static ReceiptPosition mmqR = null;
    private static ReceiptPosition bzR = null;
    private static ReceiptPosition skrR = null;
    private static ReceiptPosition xh$R = null;
    private static ReceiptPosition hwmc$R = null;
    private static ReceiptPosition ggxh$R = null;
    private static ReceiptPosition dw$R = null;
    private static ReceiptPosition sl$R = null;
    private static ReceiptPosition dj$R = null;
    private static ReceiptPosition je$R = null;
    private static ReceiptPosition slv$R = null;
    private static ReceiptPosition se$R = null;

    public PDFReaderUtil() {
    }

    private static String trim(String str) {
        if (str == null) {
            return "";
        } else {
            str = str.trim();
            str = str.replaceFirst(":", "");
            str = str.replaceFirst("：", "");
            return str.trim();
        }
    }

    public static JSONObject readReceiptPdfPath(String pdfFilePath) throws Exception {
        if (StringUtils.isNotBlank(pdfFilePath)) {
            File file = new File(pdfFilePath);
            return readReceiptPdfFile(file);
        }
        return null;
    }

    public static JSONArray readReceiptPdfFile2(File file) throws Exception {
        List<ReceiptPosition> mainList = null;
        List<List<ReceiptPosition>> addendumList = new ArrayList<>();
        PDDocument document = null;
        JSONArray result = new JSONArray();

        try {
            document = PDDocument.load(file);
            int pageCount = document.getNumberOfPages();
            PDFTextStripperNew stripperMain;
            if (pageCount == 1) {
                stripperMain = new PDFTextStripperNew();
                stripperMain.setStartPage(1);
                stripperMain.setEndPage(1);
                stripperMain.setSortByPosition(true);
                stripperMain.getText(document);
                mainList = stripperMain.getPosList();
            } else if (pageCount > 1) {
                stripperMain = new PDFTextStripperNew();
                stripperMain.setStartPage(1);
                stripperMain.setEndPage(1);
                stripperMain.setSortByPosition(true);
                stripperMain.getText(document);
                mainList = stripperMain.getPosList();
                for (int i = 2; i <= pageCount; i++) {
                    List<ReceiptPosition> addendumList1 = null;
                    PDFTextStripperNew stripperAddendum = new PDFTextStripperNew();
                    stripperAddendum.setStartPage(i);
                    stripperAddendum.setEndPage(i);
                    stripperAddendum.setSortByPosition(true);
                    stripperAddendum.getText(document);
                    addendumList1 = stripperAddendum.getPosList();
                    addendumList.add(addendumList1);
                }
            }
        } finally {
            if (document != null) {
                document.close();
            }
        }

        specificDW(mainList);
        if (addendumList.size() > 0) {
            JSONObject json = reorganizationRegulation(mainList);
            result.add(json);
            try {
                for (int i = 0; i < addendumList.size(); i++) {
                    List<ReceiptPosition> list = addendumList.get(i);
                    json = reorganizationRegulation(list);
                    result.add(json);
                }
                return result;
            } catch (Exception var11) {
                result.add(json);
                return result;
            }
        } else {
            JSONObject json = reorganizationRegulation(mainList);
            result.add(json);
            return result;
        }
    }

    public static String getPdfCont(MultipartFile multipartFile) {
        //multipartFile为multipartFile文件类型，将文件转化为文件流被PDDocument加载
        PDDocument document = null;
        try {
            document = PDDocument.load(multipartFile.getInputStream());

            document.getClass();
//使用PDFTextStripper 工具
            PDFTextStripper tStripper = new PDFTextStripper();
//设置文本排序，有规则输出
            tStripper.setSortByPosition(true);
//获取所有文字信息
            String info = tStripper.getText(document);
            return info;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static JSONObject readReceiptPdfFile(File file) throws Exception {
        List<ReceiptPosition> mainList = null;
        List<List<ReceiptPosition>> addendumList = new ArrayList<>();
        PDDocument document = null;

        try {
            document = PDDocument.load(file);
            int pageCount = document.getNumberOfPages();
            PDFTextStripperNew stripperMain;
            if (pageCount == 1) {
                stripperMain = new PDFTextStripperNew();
                stripperMain.setStartPage(1);
                stripperMain.setEndPage(1);
                stripperMain.setSortByPosition(true);
                stripperMain.getText(document);
                mainList = stripperMain.getPosList();
            } else if (pageCount > 1) {
                stripperMain = new PDFTextStripperNew();
                stripperMain.setStartPage(1);
                stripperMain.setEndPage(1);
                stripperMain.setSortByPosition(true);
                stripperMain.getText(document);
                mainList = stripperMain.getPosList();
                for (int i = 2; i <= pageCount; i++) {
                    List<ReceiptPosition> addendumList1 = null;
                    PDFTextStripperNew stripperAddendum = new PDFTextStripperNew();
                    stripperAddendum.setStartPage(i);
                    stripperAddendum.setEndPage(i);
                    stripperAddendum.setSortByPosition(true);
                    stripperAddendum.getText(document);
                    addendumList1 = stripperAddendum.getPosList();
                    addendumList.add(addendumList1);
                }
//                PDFTextStripperNew stripperAddendum = new PDFTextStripperNew();
//                stripperAddendum.setStartPage(3);
//                stripperAddendum.setEndPage(3);
//                stripperAddendum.setSortByPosition(true);
//                stripperAddendum.getText(document);
//                addendumList = stripperAddendum.getPosList();

            }
        } finally {
            if (document != null) {
                document.close();
            }

        }

        specificDW(mainList);
        if (addendumList.size() > 0) {
            JSONObject json = reorganizationRegulation(mainList);

            try {
                JSONArray qdxxs = new JSONArray();
                for (int i = 0; i < addendumList.size(); i++) {
                    List<ReceiptPosition> list = addendumList.get(i);
                    JSONObject xhqdJson = reorganizationRegulationSA(list);
                    xhqdJson.put("page", i + 1);
                    qdxxs.add(xhqdJson);
                }
                json.put("qdpgs", qdxxs);
                return json;
            } catch (Exception var11) {
                return json;
            }
        } else {
            return reorganizationRegulation(mainList);
        }
    }

    private static JSONObject reorganizationRegulationSA(List<ReceiptPosition> addendumList) {
        JSONObject json = new JSONObject();
        SortUtil.sort(addendumList, new String[]{"posY"});
        List<ReceiptPosition> indexList = new ArrayList<>();
        List<ReceiptPosition> lineStrList = new ArrayList<>();

        for (int i = 0; i < addendumList.size(); i++) {
            ReceiptPosition rp = addendumList.get(i);
            if (i + 1 < addendumList.size()) {
                ReceiptPosition rp2 = addendumList.get(i + 1);
                if (Math.abs(rp.getPosY() - rp2.getPosY()) < 2.0F) {
                    indexList.add(rp);
                } else {
                    indexList.add(rp);
                    SortUtil.sort(indexList, new String[]{"posX"});
                    ReceiptPosition receiptPosition = new ReceiptPosition();
                    for (ReceiptPosition r : indexList) {
                        String text = receiptPosition.getText();
                        if (text == null)
                            text = "";
                        text = String.valueOf(String.valueOf(text)) + r.getText();
                        receiptPosition.setPosEndX(r.getPosEndX());
                        receiptPosition.setPosEndY(r.getPosEndY());
                        receiptPosition.setPosLastX(r.getPosLastX());
                        receiptPosition.setPosLastY(r.getPosLastY());
                        receiptPosition.setPosX(r.getPosX());
                        receiptPosition.setPosY(r.getPosY());
                        receiptPosition.setText(text);
                    }
                    lineStrList.add(receiptPosition);
                    indexList = new ArrayList<>();
                }
            } else if (i + 1 == addendumList.size()) {
                SortUtil.sort(indexList, new String[]{"posX"});
                indexList.add(rp);
                ReceiptPosition receiptPosition = new ReceiptPosition();
                for (ReceiptPosition r : indexList) {
                    String text = receiptPosition.getText();
                    if (text == null)
                        text = "";
                    text = String.valueOf(String.valueOf(text)) + r.getText();
                    receiptPosition.setPosEndX(r.getPosEndX());
                    receiptPosition.setPosEndY(r.getPosEndY());
                    receiptPosition.setPosLastX(r.getPosLastX());
                    receiptPosition.setPosLastY(r.getPosLastY());
                    receiptPosition.setPosX(r.getPosX());
                    receiptPosition.setPosY(r.getPosY());
                    receiptPosition.setText(text);
                }
                lineStrList.add(receiptPosition);
            }
        }

        if ("销售货物或者提供应税劳务清单".equals(((ReceiptPosition) lineStrList.get(lineStrList.size() - 1)).getText())) {
            float xsfY$ = 0.0F;
            float bzY$ = 0.0F;
            float zjY$ = 0.0F;
            float xjY$ = 0.0F;
            float xhY$ = 0.0F;
            float fpdmY$ = 0.0F;
            float xsfmcY$ = 0.0F;
            float gmfmcY$ = 0.0F;

            for (int j = 0; j < lineStrList.size(); ++j) {
                ReceiptPosition rp = lineStrList.get(j);
                String text = rp.getText().trim().replaceAll(" ", "").replaceAll("：", ":");
                if (text.indexOf("销售方") > -1 && text.indexOf("填开日期") > -1) {
                    int lastIndex = text.lastIndexOf(":");
                    text = text.substring(lastIndex + 1, text.length()).replace("年", "-").replace("月", "-").replace("日", "");
                    json.put("xhqd_tkrq", text);
                    if (xsfY$ == 0.0F) {
                        xsfY$ = rp.getPosY();
                    }
                }

                if (text.indexOf("备注") > -1 && bzY$ == 0.0F) {
                    bzY$ = rp.getPosY();
                }

                if (text.indexOf("总计") > -1 && zjY$ == 0.0F) {
                    zjY$ = rp.getPosY();
                }

                if (text.indexOf("小计") > -1 && xjY$ == 0.0F) {
                    xjY$ = rp.getPosY();
                }

                if (text.indexOf("序号") > -1 && xhY$ == 0.0F) {
                    xhY$ = rp.getPosY();
                }

                if (text.indexOf("所属增值税电子普通发票代码") > -1 && fpdmY$ == 0.0F) {
                    json.put("xhqd_fpdm", text.substring(text.indexOf(":") + 1, text.lastIndexOf("号码")));
                    json.put("xhqd_fphm", text.substring(text.lastIndexOf(":") + 1, text.lastIndexOf("共")));
                    fpdmY$ = rp.getPosY();
                }

                if (text.indexOf("销售方名称") > -1 && xsfmcY$ == 0.0F) {
                    json.put("xhqd_xsfmc", text.substring(text.indexOf(":") + 1, text.length()));
                    xsfmcY$ = rp.getPosY();
                }

                if (text.indexOf("购买方名称") > -1 && gmfmcY$ == 0.0F) {
                    json.put("xhqd_gmfmc", text.substring(text.indexOf(":") + 1, text.length()));
                    gmfmcY$ = rp.getPosY();
                }
            }

            ArrayList<ReceiptPosition> beanList = new ArrayList();
            for (ReceiptPosition rp : addendumList) {
                if (Math.abs(rp.getPosY() - xhY$) < 2.0F)
                    beanList.add(rp);
            }

            SortUtil.sort(beanList, new String[]{"posX"});
            String text;
            if (xh$R == null) {
                for (ReceiptPosition rp : beanList) {
                    String text1 = rp.getText().trim();
                    text1 = rp.getText().trim();
                    if ("序号".equals(text1)) {
                        xh$R = rp;
                        xh$R.setText("序号");
                    }

                    if ("货物(劳务)名称".equals(text1)) {
                        hwmc$R = rp;
                        hwmc$R.setText("货物名称");
                    }

                    if ("规格型号".equals(text1)) {
                        ggxh$R = rp;
                        ggxh$R.setText("规格型号");
                    }

                    if ("单位".equals(text1)) {
                        dw$R = rp;
                        dw$R.setText("单位");
                    }

                    if ("数量".equals(text1.replaceAll(" ", ""))) {
                        sl$R = rp;
                        sl$R.setText("数量");
                    }

                    if ("单价".equals(text1.replaceAll(" ", ""))) {
                        dj$R = rp;
                        dj$R.setText("单价");
                    }

                    if ("金额".equals(text1.replaceAll(" ", ""))) {
                        je$R = rp;
                        je$R.setText("金额");
                    }

                    if ("税率".equals(text1.replaceAll(" ", ""))) {
                        slv$R = rp;
                        slv$R.setText("税率");
                    }

                    if ("税额".equals(text1.replaceAll(" ", ""))) {
                        se$R = rp;
                        se$R.setText("税额");
                    }
                }
            }

            String xjje = "";
            String xjse = "";
            String zjje = "";
            String zjse = "";
            StringBuilder bzText = new StringBuilder();
            ArrayList<ReceiptPosition> xh$List = new ArrayList();
            ArrayList<ReceiptPosition> hwmc$List = new ArrayList();
            ArrayList<ReceiptPosition> ggxh$List = new ArrayList();
            ArrayList<ReceiptPosition> dw$List = new ArrayList();
            ArrayList<ReceiptPosition> sl$List = new ArrayList();
            ArrayList<ReceiptPosition> dj$List = new ArrayList();
            ArrayList<ReceiptPosition> je$List = new ArrayList();
            ArrayList<ReceiptPosition> slv$List = new ArrayList();
            ArrayList<ReceiptPosition> se$List = new ArrayList();
            ArrayList<ReceiptPosition> bz$List = new ArrayList();

            for (int i = addendumList.size() - 1; i >= 0; --i) {
                ReceiptPosition rp = (ReceiptPosition) addendumList.get(i);
                String text2 = rp.getText().trim().replaceAll(" ", "");
                if (rp.getPosY() < zjY$ - 2.0F && rp.getPosY() > bzY$ - 5.0F && rp.getPosLastX() > xh$R.getPosLastX() + 2.0F) {
                    bz$List.add(rp);
                }

                if (rp.getPosY() < xjY$ + 2.0F && rp.getPosY() > zjY$ - 2.0F) {
                    if (rp.getPosLastX() < slv$R.getPosX() && rp.getPosX() > je$R.getPosX() - 10.0F) {
                        if (StringUtils.isBlank(xjje)) {
                            xjje = text2.replace("¥", "").replace("￥", "");
                        } else {
                            zjje = text2.replace("¥", "").replace("￥", "");
                        }
                    }

                    if (rp.getPosX() > slv$R.getPosLastX()) {
                        if (StringUtils.isBlank(xjse)) {
                            xjse = text2.replace("¥", "").replace("￥", "");
                        } else {
                            zjse = text2.replace("¥", "").replace("￥", "");
                        }
                    }
                }

                if (rp.getPosY() < xhY$ - 2.0F && rp.getPosY() > xjY$ + 2.0F) {
                    rp.setText(text2);
                    if (rp.getPosLastX() < xh$R.getPosLastX() + 2.0F) {
                        xh$List.add(rp);
                    } else if (rp.getPosLastX() < ggxh$R.getPosX() - 8.0F) {
                        hwmc$List.add(rp);
                    } else if (rp.getPosLastX() < dw$R.getPosX() - 3.0F) {
                        ggxh$List.add(rp);
                    } else if (rp.getPosLastX() < sl$R.getPosX() - 10.0F) {
                        dw$List.add(rp);
                    } else if (rp.getPosLastX() < dj$R.getPosX() - 10.0F) {
                        sl$List.add(rp);
                    } else if (rp.getPosLastX() < je$R.getPosX() - 10.0F) {
                        dj$List.add(rp);
                    } else if (rp.getPosLastX() < slv$R.getPosX() - 2.0F) {
                        je$List.add(rp);
                    } else if (rp.getPosLastX() < se$R.getPosX() - 10.0F) {
                        slv$List.add(rp);
                    } else {
                        se$List.add(rp);
                    }
                }
            }

            SortUtil.sort(bz$List, new String[]{"posX"});
            Iterator var59 = bz$List.iterator();

            while (var59.hasNext()) {
                ReceiptPosition bzRp = (ReceiptPosition) var59.next();
                bzText.append(bzRp.getText());
            }

            json.put("xhqd_bz", bzText.toString());
            json.put("xhqd_xjje", xjje);
            json.put("xhqd_xjse", xjse);
            json.put("xhqd_zjje", zjje);
            json.put("xhqd_zjse", zjse);
            if (xh$List.size() > 0) {
                JSONArray jsonArray = new JSONArray();
                JSONObject jsonObject = new JSONObject();

                for (int i = 0; i < xh$List.size(); ++i) {
                    ReceiptPosition xhRp = (ReceiptPosition) xh$List.get(i);
                    String xh = xhRp.getText();
                    StringBuilder hwmc = new StringBuilder();
                    String ggxh = "";
                    String dw = "";
                    String sl = "";
                    String dj = "";
                    String je = "";
                    String slv = "";
                    String se = "";
                    Iterator var42 = ggxh$List.iterator();

                    ReceiptPosition hwmcRp;
                    while (var42.hasNext()) {
                        hwmcRp = (ReceiptPosition) var42.next();
                        if (Math.abs(xhRp.getPosY() - hwmcRp.getPosY()) < 2.0F) {
                            ggxh = hwmcRp.getText();
                        }
                    }

                    var42 = dw$List.iterator();

                    while (var42.hasNext()) {
                        hwmcRp = (ReceiptPosition) var42.next();
                        if (Math.abs(xhRp.getPosY() - hwmcRp.getPosY()) < 2.0F) {
                            dw = hwmcRp.getText();
                        }
                    }

                    var42 = sl$List.iterator();

                    while (var42.hasNext()) {
                        hwmcRp = (ReceiptPosition) var42.next();
                        if (Math.abs(xhRp.getPosY() - hwmcRp.getPosY()) < 2.0F) {
                            sl = hwmcRp.getText();
                        }
                    }

                    var42 = dj$List.iterator();

                    while (var42.hasNext()) {
                        hwmcRp = (ReceiptPosition) var42.next();
                        if (Math.abs(xhRp.getPosY() - hwmcRp.getPosY()) < 2.0F) {
                            dj = hwmcRp.getText();
                        }
                    }

                    var42 = je$List.iterator();

                    while (var42.hasNext()) {
                        hwmcRp = (ReceiptPosition) var42.next();
                        if (Math.abs(xhRp.getPosY() - hwmcRp.getPosY()) < 2.0F) {
                            je = hwmcRp.getText();
                        }
                    }

                    var42 = slv$List.iterator();

                    while (var42.hasNext()) {
                        hwmcRp = (ReceiptPosition) var42.next();
                        if (Math.abs(xhRp.getPosY() - hwmcRp.getPosY()) < 2.0F) {
                            slv = hwmcRp.getText();
                        }
                    }

                    var42 = se$List.iterator();

                    while (var42.hasNext()) {
                        hwmcRp = (ReceiptPosition) var42.next();
                        if (Math.abs(xhRp.getPosY() - hwmcRp.getPosY()) < 2.0F) {
                            se = hwmcRp.getText();
                        }
                    }

                    if (i + 1 < xh$List.size()) {
                        ReceiptPosition xhRp2 = xh$List.get(i + 1);
                        for (ReceiptPosition hwmcRp1 : hwmc$List) {
                            if (hwmcRp1.getPosY() > xhRp2.getPosY() + 2.0F &&
                                    hwmcRp1.getPosY() < xhRp.getPosY() + 2.0F) {
                                hwmc.append(hwmcRp1.getText());
                            }
                        }
                    } else {
                        var42 = hwmc$List.iterator();

                        while (var42.hasNext()) {
                            hwmcRp = (ReceiptPosition) var42.next();
                            if (hwmcRp.getPosY() < xhRp.getPosY() + 2.0F) {
                                hwmc.append(hwmcRp.getText());
                            }
                        }
                    }

                    jsonObject.put("xhqd_xh", xh);
                    jsonObject.put("xhqd_hwmc", hwmc.toString());
                    jsonObject.put("xhqd_ggxh", ggxh);
                    jsonObject.put("xhqd_dw", dw);
                    jsonObject.put("xhqd_sl", sl);
                    jsonObject.put("xhqd_dj", dj);
                    jsonObject.put("xhqd_je", je);
                    jsonObject.put("xhqd_slv", slv.replace("%", ""));
                    jsonObject.put("xhqd_se", se);
                    jsonArray.add(jsonObject);
                    jsonObject = new JSONObject();
                }

                json.put("xhqd_xq", jsonArray);
            }
        }

        return json;
    }

    private static void specificDW(List<ReceiptPosition> list) {
        jqbhR = new ReceiptPosition();
        hjR = new ReceiptPosition();
        jshjR = new ReceiptPosition();
        hwmcR = new ReceiptPosition();
        ggxhR = new ReceiptPosition();
        danweiR = new ReceiptPosition();
        shuliangR = new ReceiptPosition();
        danjiaR = new ReceiptPosition();
        jineR = new ReceiptPosition();
        shuilvR = new ReceiptPosition();
        shuieR = new ReceiptPosition();
        mmqR = new ReceiptPosition();
        bzR = new ReceiptPosition();
        skrR = new ReceiptPosition();
        List<ReceiptPosition> hjList = new ArrayList();
        List<ReceiptPosition> jshjList = new ArrayList();
        List<ReceiptPosition> hwmcList = new ArrayList();
        List<ReceiptPosition> ggxhList = new ArrayList();
        List<ReceiptPosition> danweiList = new ArrayList();
        List<ReceiptPosition> shuliangList = new ArrayList();
        List<ReceiptPosition> danjiaList = new ArrayList();
        List<ReceiptPosition> jineList = new ArrayList();
        List<ReceiptPosition> shuilvList = new ArrayList();
        List<ReceiptPosition> shuieList = new ArrayList();
        List<ReceiptPosition> jqbhList = new ArrayList();
        SortUtil.sort(list, new String[]{"posY"});
        Iterator var13 = list.iterator();

        ReceiptPosition hj;
        while (var13.hasNext()) {
            hj = (ReceiptPosition) var13.next();
            if (trim(StringUtil.removeSpace(hj.getText())).startsWith("合计")) {
                hjList.add(hj);
            }

            if (trim(StringUtil.removeSpace(hj.getText())).startsWith("价税合计")) {
                jshjList.add(hj);
            }

            if (trim(StringUtil.removeSpace(hj.getText())).startsWith("机器编号")) {
                jqbhList.add(hj);
            }
        }

        SortUtil.sort(jqbhList, new String[]{"posY"});
        if (jqbhList.size() > 0) {
            jqbhR = (ReceiptPosition) jqbhList.get(jqbhList.size() - 1);
        }
        reInspection(hjList, list, "合计", "合", "计");
        var13 = hjList.iterator();

        ReceiptPosition rp;
        while (var13.hasNext()) {
            hj = (ReceiptPosition) var13.next();
            Iterator var15 = jshjList.iterator();

            while (var15.hasNext()) {
                rp = (ReceiptPosition) var15.next();
                if (Math.abs(hj.getPosY() - rp.getPosY()) < 25.0F) {
                    hjR.setPosX(hj.getPosX());
                    hjR.setPosY(hj.getPosY());
                    hjR.setText("合计");
                    jshjR.setPosX(rp.getPosX());
                    jshjR.setPosY(rp.getPosY());
                    jshjR.setText("价税合计");
                }
            }
        }

        SortUtil.sort(list, new String[]{"posX"});
        List<List<ReceiptPosition>> baseList = new ArrayList();
        List<ReceiptPosition> indexList = new ArrayList();
        rp = null;

        for (int i = 1; i < list.size(); ++i) {
            ReceiptPosition item = (ReceiptPosition) list.get(i);
            float posX = item.getPosX();
            ReceiptPosition item1 = (ReceiptPosition) list.get(i - 1);
            float posX1 = item1.getPosX();
            indexList.add(item1);
            if (Math.abs(posX - posX1) > 2.0F) {
                baseList.add(indexList);
                indexList = new ArrayList();
            }

            if (trim(StringUtil.removeSpace(item.getText())).startsWith("货物或应税劳务、服")) {
                rp = new ReceiptPosition();
                rp.setPosX(item.getPosX());
                rp.setPosY(item.getPosY());
                rp.setText("货物或应税劳务、服务名称");
                hwmcList.add(rp);
            }

            if (trim(StringUtil.removeSpace(item.getText())).startsWith("规格型号")) {
                rp = new ReceiptPosition();
                rp.setPosX(item.getPosX());
                rp.setPosY(item.getPosY());
                rp.setText("规格型号");
                ggxhList.add(rp);
            }

            if (trim(StringUtil.removeSpace(item.getText())).startsWith("单位")) {
                rp = new ReceiptPosition();
                rp.setPosX(item.getPosX());
                rp.setPosY(item.getPosY());
                rp.setText("单位");
                danweiList.add(rp);
            }

            if (trim(StringUtil.removeSpace(item.getText())).startsWith("数量")) {
                rp = new ReceiptPosition();
                rp.setPosX(item.getPosX());
                rp.setPosY(item.getPosY());
                rp.setText("数量");
                shuliangList.add(rp);
            }

            if (trim(StringUtil.removeSpace(item.getText())).startsWith("单价")) {
                rp = new ReceiptPosition();
                rp.setPosX(item.getPosX());
                rp.setPosY(item.getPosY());
                rp.setText("单价");
                danjiaList.add(rp);
            }

            if (trim(StringUtil.removeSpace(item.getText())).startsWith("金额")) {
                rp = new ReceiptPosition();
                rp.setPosX(item.getPosX());
                rp.setPosY(item.getPosY());
                rp.setText("金额");
                jineList.add(rp);
            }

            if (trim(StringUtil.removeSpace(item.getText())).startsWith("税率")) {
                rp = new ReceiptPosition();
                rp.setPosX(item.getPosX());
                rp.setPosY(item.getPosY());
                rp.setText("税率");
                shuilvList.add(rp);
            }

            if (trim(StringUtil.removeSpace(item.getText())).startsWith("税额")) {
                rp = new ReceiptPosition();
                rp.setPosX(item.getPosX());
                rp.setPosY(item.getPosY());
                rp.setText("税额");
                shuieList.add(rp);
            }
        }

        if (hwmcList.size() > 0) {
            hwmcR = (ReceiptPosition) hwmcList.get(hwmcList.size() - 1);
        }

        Iterator var24 = ggxhList.iterator();

        ReceiptPosition x;
        while (var24.hasNext()) {
            x = (ReceiptPosition) var24.next();
            if (Math.abs(hwmcR.getPosY() - x.getPosY()) < 2.0F) {
                ggxhR = x;
            }
        }

        reInspection(danweiList, list, "单位", "单", "位");
        var24 = danweiList.iterator();

        while (var24.hasNext()) {
            x = (ReceiptPosition) var24.next();
            if (Math.abs(hwmcR.getPosY() - x.getPosY()) < 2.0F) {
                danweiR = x;
            }
        }

        reInspection(shuliangList, list, "数量", "数", "量");
        var24 = shuliangList.iterator();

        while (var24.hasNext()) {
            x = (ReceiptPosition) var24.next();
            if (Math.abs(hwmcR.getPosY() - x.getPosY()) < 2.0F) {
                shuliangR = x;
            }
        }

        reInspection(danjiaList, list, "单价", "单", "价");
        var24 = danjiaList.iterator();

        while (var24.hasNext()) {
            x = (ReceiptPosition) var24.next();
            if (Math.abs(hwmcR.getPosY() - x.getPosY()) < 2.0F) {
                danjiaR = x;
            }
        }

        reInspection(jineList, list, "金额", "金", "额");
        var24 = jineList.iterator();

        while (var24.hasNext()) {
            x = (ReceiptPosition) var24.next();
            if (Math.abs(hwmcR.getPosY() - x.getPosY()) < 2.0F) {
                jineR = x;
            }
        }

        reInspection(shuilvList, list, "税率", "税", "率");
        var24 = shuilvList.iterator();

        while (var24.hasNext()) {
            x = (ReceiptPosition) var24.next();
            if (Math.abs(hwmcR.getPosY() - x.getPosY()) < 2.0F) {
                shuilvR = x;
            }
        }

        reInspection(shuieList, list, "税额", "税", "额");
        var24 = shuieList.iterator();

        while (var24.hasNext()) {
            x = (ReceiptPosition) var24.next();
            if (Math.abs(hwmcR.getPosY() - x.getPosY()) < 2.0F) {
                shuieR = x;
            }
        }

        var24 = baseList.iterator();

        while (true) {
            String t;
            List base;
            do {
                if (!var24.hasNext()) {
                    return;
                }

                base = (List) var24.next();
                SortUtil.sort(base, new String[]{"posY"});
                t = "";

                for (int i = base.size() - 1; i >= 0; --i) {
                    ReceiptPosition item = (ReceiptPosition) base.get(i);
                    t = t + item.getText();
                }
            } while (!trim(StringUtil.removeSpace(t)).startsWith("密码区") && !trim(StringUtil.removeSpace(t)).equals("密码区备注"));

            mmqR.setText("密码区");
            mmqR.setPosX(((ReceiptPosition) base.get(0)).getPosX());
            mmqR.setPosEndX(((ReceiptPosition) base.get(0)).getPosLastX());
            mmqR.setPosY(((ReceiptPosition) base.get(base.size() - 1)).getPosY());
            bzR.setText("备注");
            bzR.setPosX(((ReceiptPosition) base.get(0)).getPosX());
            bzR.setPosEndX(((ReceiptPosition) base.get(0)).getPosLastX());
            bzR.setPosEndY(((ReceiptPosition) base.get(0)).getPosLastY());
            skrR.setText("收款人");
            skrR.setPosY(bzR.getPosEndY() - 10.0F);
        }
    }

    private static void reInspection(List<ReceiptPosition> cjList, List<ReceiptPosition> list, String cj, String cj_c, String cj_j) {
        if (cjList.size() < 1) {
            SortUtil.sort(list, new String[]{"posY"});

            for (int i = 0; i < list.size(); ++i) {
                if (trim(((ReceiptPosition) list.get(i)).getText()).length() == 1 && ((ReceiptPosition) list.get(i)).getText().startsWith(cj_c) && i - 1 >= 0 && trim(((ReceiptPosition) list.get(i - 1)).getText()).length() == 1 && ((ReceiptPosition) list.get(i - 1)).getText().startsWith(cj_j) || trim(((ReceiptPosition) list.get(i)).getText()).length() == 1 && ((ReceiptPosition) list.get(i)).getText().startsWith(cj_c) && i + 1 < list.size() && trim(((ReceiptPosition) list.get(i + 1)).getText()).length() == 1 && ((ReceiptPosition) list.get(i + 1)).getText().startsWith(cj_j)) {
                    ((ReceiptPosition) list.get(i)).setText(cj);
                    cjList.add((ReceiptPosition) list.get(i));
                }
            }
        }

    }

    private static JSONObject reorganizationRegulation(List<ReceiptPosition> list) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fplx", 10);
        String fpdm = "";
        String fphm = "";
        String kprq = "";
        String jym = "";
        String spfmc = "";
        String spfsbh = "";
        String spfdzdh = "";
        String spfyhzh = "";
        String kpfmc = "";
        String kpfsbh = "";
        String kpfdzdh = "";
        String kpfyhzh = "";
        String hjje = "";
        String hjse = "";
        String kpje = "";
        String slv = "";
        String hwmc = "";
        String jqbh = "";
        String skr = "";
        String fh = "";
        String kpr = "";
        String xsf = "";
        String bz = "";
        List<ReceiptPosition> sfkList = new ArrayList();
        List<List<ReceiptPosition>> baseList = new ArrayList();
        List<ReceiptPosition> lineList = new ArrayList();
        SortUtil.sort(list, new String[]{"posY"});
        List<ReceiptPosition> indexList = new ArrayList();

        ReceiptPosition sfk;
        for (int i = list.size() - 1; i > 0; --i) {
            sfk = (ReceiptPosition) list.get(i);
            float posY = sfk.getPosY();
            ReceiptPosition item1 = (ReceiptPosition) list.get(i - 1);
            float posY1 = item1.getPosY();
            indexList.add(sfk);
            if (Math.abs(posY - posY1) > 2.0F) {
                baseList.add(indexList);
                indexList = new ArrayList();
            }

            if (posY1 < bzR.getPosEndY() - 10.0F) {
                sfkList.add(item1);
            }
        }

        SortUtil.sort(sfkList, new String[]{"posX"});
        String sfkText = "";

        for (Iterator var53 = sfkList.iterator(); var53.hasNext(); sfkText = sfkText + sfk.getText()) {
            sfk = (ReceiptPosition) var53.next();
        }

        String regex = "收款人|复核|开票人|销售方";
        Pattern pattern = Pattern.compile(regex);
        String[] splitStrs = pattern.split(sfkText.trim());
        if (splitStrs.length > 4) {
            skr = splitStrs[1];
            fh = splitStrs[2];
            kpr = splitStrs[3];
            xsf = splitStrs[4];
        }

        Iterator var33 = baseList.iterator();

        label320:
        while (var33.hasNext()) {
            List<ReceiptPosition> itemList = (List) var33.next();
            SortUtil.sort(itemList, new String[]{"posX"});
            ReceiptPosition r = new ReceiptPosition();
            String text = "";
            Iterator var37 = itemList.iterator();

            while (true) {
                ReceiptPosition item;
                do {
                    do {
                        do {
                            if (!var37.hasNext()) {
                                text = StringUtil.removeSpace(text);
                                r.setText(text);
                                r.setPosY(((ReceiptPosition) itemList.get(0)).getPosY());
                                r.setPosEndY(((ReceiptPosition) itemList.get(itemList.size() - 1)).getPosY());
                                lineList.add(r);
                                continue label320;
                            }

                            item = (ReceiptPosition) var37.next();
                        } while (item.getPosY() < jqbhR.getPosY() - 5.0F && item.getPosY() > hwmcR.getPosY() && item.getPosX() > mmqR.getPosX());
                    } while (item.getPosY() < jshjR.getPosY() && item.getPosX() > bzR.getPosX());
                } while (item.getPosY() < hwmcR.getPosY() && item.getPosY() > hjR.getPosY() && item.getPosX() > ggxhR.getPosX());

                text = text + item.getText();
            }
        }

        List<String> hwmcL = new ArrayList();
        List<String> ggxhL = new ArrayList();
        List<String> dwL = new ArrayList();
        List<String> slL = new ArrayList();
        List<String> djL = new ArrayList();
        List<String> dxjeL = new ArrayList();
        List<String> slvL = new ArrayList();
        List<String> dxseL = new ArrayList();
        List<Float> agioYList = new ArrayList();
        Iterator var40 = list.iterator();

        ReceiptPosition rec;
        String text;
        while (var40.hasNext()) {
            rec = (ReceiptPosition) var40.next();
            if (rec.getPosY() > hjR.getPosY() - 5.0F && rec.getPosY() < jineR.getPosY() && rec.getPosX() > jineR.getPosX() - 20.0F && rec.getPosX() < shuilvR.getPosX()) {
                text = StringUtil.removeSpace(rec.getText());
                if (StringUtils.isNotBlank(text) && StringUtils.isBlank(kpje)) {
                    kpje = trim(text.replaceAll("¥", "").replaceAll("￥", ""));
                }
            }

            if (rec.getPosY() > hjR.getPosY() && rec.getPosY() < shuilvR.getPosY() && rec.getPosX() > shuilvR.getPosX() - 5.0F && rec.getPosX() < shuilvR.getPosX() + 20.0F) {
                text = StringUtil.removeSpace(rec.getText());
                if (StringUtils.isNotBlank(text)) {
                    slv = text;
                }

                agioYList.add(rec.getPosY());
                slvL.add(StringUtil.removeSpace(rec.getText()));
            }

            if (rec.getPosY() > hjR.getPosY() - 5.0F && rec.getPosY() < shuieR.getPosY() && rec.getPosX() > shuieR.getPosX() - 20.0F) {
                text = StringUtil.removeSpace(rec.getText());
                if (StringUtils.isNotBlank(text) && StringUtils.isBlank(hjse)) {
                    hjse = trim(text.replaceAll("¥", "").replaceAll("￥", ""));
                }
            }

            if (rec.getPosY() < jshjR.getPosY() && rec.getPosY() > skrR.getPosY() && rec.getPosX() > bzR.getPosEndX()) {
                text = StringUtil.removeSpace(rec.getText());
                if (StringUtils.isNotBlank(text)) {
                    bz = bz + text;
                }
            }
        }

        String rqX;
        for (int i = 0; i < agioYList.size(); ++i) {
            Float indexY = (Float) agioYList.get(i);
            Float indexY0 = indexY;
            if (i > 0) {
                indexY0 = (Float) agioYList.get(i - 1);
            }

            String dxhwmc$ = "";
            String ggxh$ = "";
            String dw$ = "";
            String sl$ = "";
            String dj$ = "";
            String dxje$ = "";
            rqX = "";

            for (int j = list.size() - 1; j > 0; --j) {
                float posY = ((ReceiptPosition) list.get(j)).getPosY();
                float posX = ((ReceiptPosition) list.get(j)).getPosX();
                String text1 = StringUtil.removeSpace(((ReceiptPosition) list.get(j)).getText());
                if (posY < (Float) agioYList.get(agioYList.size() - 1) + 2.0F && posY > hjR.getPosY() + 2.0F) {
                    if (posX < ggxhR.getPosX() - 5.0F) {
                        if (i == 0) {
                            if (posY < indexY + 2.0F && posY > hjR.getPosY() + 2.0F) {
                                dxhwmc$ = dxhwmc$ + text1;
                            }
                        } else if (posY > indexY0 && posY < indexY + 2.0F) {
                            dxhwmc$ = dxhwmc$ + text1;
                        }
                    }

                    if (posX < danweiR.getPosX() - 5.0F && posX > ggxhR.getPosX() - 5.0F) {
                        if (i == 0) {
                            if (posY < indexY + 2.0F && posY > hjR.getPosY() + 2.0F) {
                                ggxh$ = ggxh$ + text1;
                            }
                        } else if (posY > indexY0 && posY < indexY + 2.0F) {
                            ggxh$ = ggxh$ + text1;
                        }
                    }

                    if (Math.abs(posY - indexY) < 2.0F && posX < shuliangR.getPosX() - 20.0F && posX > danweiR.getPosX() - 5.0F) {
                        dw$ = text1;
                    }

                    if (Math.abs(posY - indexY) < 2.0F && posX < danjiaR.getPosX() - 20.0F && posX > shuliangR.getPosX() - 10.0F) {
                        sl$ = text1;
                    }

                    if (Math.abs(posY - indexY) < 2.0F && posX < jineR.getPosX() - 20.0F && posX > danjiaR.getPosX() - 20.0F) {
                        dj$ = text1;
                    }

                    if (Math.abs(posY - indexY) < 2.0F && posX < shuilvR.getPosX() - 5.0F && posX > jineR.getPosX() - 20.0F) {
                        dxje$ = text1;
                    }

                    if (Math.abs(posY - indexY) < 2.0F && posX > shuieR.getPosX() - 20.0F) {
                        rqX = text1;
                    }
                }
            }

            hwmcL.add(dxhwmc$);
            ggxhL.add(ggxh$);
            dwL.add(dw$);
            slL.add(sl$);
            djL.add(dj$);
            dxjeL.add(dxje$);
            dxseL.add(rqX);
        }

        var40 = lineList.iterator();

        while (var40.hasNext()) {
            rec = (ReceiptPosition) var40.next();
            text = rec.getText();
            int kpfmcIndex;
            int kpfsbhIndex;
            int kpfdzdhIndex;
            int kpfyhzhIndex;
            if (rec.getPosY() > hwmcR.getPosY()) {
                kpfmcIndex = rec.getText().indexOf("机器编号");
                if (kpfmcIndex > -1) {
                    jqbh = text.substring(kpfmcIndex + 5, kpfmcIndex + 17);
                }

                kpfsbhIndex = rec.getText().indexOf("发票代码");
                if (kpfsbhIndex > -1) {
                    fpdm = text.substring(kpfsbhIndex + 5, text.length());
                }

                kpfdzdhIndex = rec.getText().indexOf("发票号码");
                if (kpfdzdhIndex > -1) {
                    fphm = text.substring(kpfdzdhIndex + 5, text.length());
                }

                kpfyhzhIndex = rec.getText().indexOf("开票日期");
                if (kpfyhzhIndex > -1) {
                    rqX = text.substring(kpfyhzhIndex + 5, text.length());
                    String rq = rqX.replaceAll("年", "").replaceAll("月", "").replaceAll("日", "");
                    kprq = rq.substring(0, 4) + "-" + rq.substring(4, 6) + "-" + rq.substring(6, 8);
                }

                int jymIndex = rec.getText().indexOf("校验码");
                if (jymIndex > -1) {
                    jym = text.substring(jymIndex + 4, text.length());
                }
            }

            if (rec.getPosY() < hwmcR.getPosY() && rec.getPosY() > hjR.getPosY() && rec.getPosX() < ggxhR.getPosX()) {
                if (agioYList.size() > 1 && rec.getPosY() <= (Float) agioYList.get(agioYList.size() - 2)) {
                    continue;
                }

                hwmc = hwmc + text;
            }

            if (rec.getPosY() > hwmcR.getPosY() && rec.getPosY() < jqbhR.getPosY()) {
                kpfmcIndex = rec.getText().indexOf("名称");
                if (kpfmcIndex > -1) {
                    spfmc = text.substring(kpfmcIndex + 3, text.length());
                }

                kpfsbhIndex = rec.getText().indexOf("纳税人识别号");
                if (kpfsbhIndex > -1) {
                    spfsbh = text.substring(kpfsbhIndex + 7, text.length());
                }

                kpfdzdhIndex = rec.getText().indexOf("地址、电话");
                if (kpfdzdhIndex > -1) {
                    spfdzdh = text.substring(kpfdzdhIndex + 6, text.length());
                }

                kpfyhzhIndex = rec.getText().indexOf("开户行及账号");
                if (kpfyhzhIndex > -1) {
                    spfyhzh = text.substring(kpfyhzhIndex + 7, text.length());
                }
            }

            if (rec.getPosY() < jshjR.getPosY()) {
                kpfmcIndex = rec.getText().indexOf("名称");
                if (kpfmcIndex > -1) {
                    kpfmc = text.substring(kpfmcIndex + 3, text.length());
                }

                kpfsbhIndex = rec.getText().indexOf("纳税人识别号");
                if (kpfsbhIndex > -1) {
                    kpfsbh = text.substring(kpfsbhIndex + 7, text.length());
                }

                kpfdzdhIndex = rec.getText().indexOf("地址、电话");
                if (kpfdzdhIndex > -1) {
                    kpfdzdh = text.substring(kpfdzdhIndex + 6, text.length());
                }

                kpfyhzhIndex = rec.getText().indexOf("开户行及账号");
                if (kpfyhzhIndex > -1) {
                    kpfyhzh = text.substring(kpfyhzhIndex + 7, text.length());
                }
            }

            if (Math.abs(rec.getPosY() - jshjR.getPosY()) < 2.0F) {

                String[] amountArr = text.split("小写");
                if (amountArr.length > 1) {
                    hjje = amountArr[1].replaceAll("¥", "").replaceAll("￥", "");
                    hjje = hjje.substring(1, hjje.length());
                }
            }
        }

        if (!NumberValidationUtils.isRealNumber(kpje)) {
            var40 = list.iterator();

            while (var40.hasNext()) {
                rec = (ReceiptPosition) var40.next();
                if (Math.abs(rec.getPosY() - jshjR.getPosY()) < 2.0F) {
                    kpje = kpje + rec.getText();
                }
            }
        }

        if (!NumberValidationUtils.isRealNumber(kpje)) {
            kpje = extractM(kpje);
        }

        jsonObject.put("fpdm", trim(fpdm));
        jsonObject.put("fphm", trim(fphm));
        jsonObject.put("kprq", trim(kprq));
        jsonObject.put("jym", trim(jym));
        jsonObject.put("spfmc", trim(spfmc));
        jsonObject.put("spfsbh", trim(spfsbh));
        jsonObject.put("spfdzdh", trim(spfdzdh));
        jsonObject.put("spfyhzh", trim(spfyhzh));
        jsonObject.put("kpfmc", trim(kpfmc));
        jsonObject.put("kpfsbh", trim(kpfsbh));
        jsonObject.put("kpfdzdh", trim(kpfdzdh));
        jsonObject.put("kpfyhzh", trim(kpfyhzh));
        jsonObject.put("hjje", trim(hjje));
        jsonObject.put("hjse", trim(hjse));
        jsonObject.put("kpje", trim(kpje));
        jsonObject.put("bz", bz);
//        slv = trim(slv);
//        slv = slv.replaceAll("%", "");
//        slv = String.valueOf(StringUtil.parseInt(slv));
//        jsonObject.put("slv", slv);
//        jsonObject.put("hwmc", trim(hwmc));
        jsonObject.put("jqbh", trim(jqbh));
        jsonObject.put("skr", trim(skr));
        jsonObject.put("fh", trim(fh));
        jsonObject.put("kpr", trim(kpr));
        jsonObject.put("xsf", trim(xsf));
        Collections.reverse(hwmcL);
        Collections.reverse(ggxhL);
        Collections.reverse(dwL);
        Collections.reverse(slL);
        Collections.reverse(djL);
        Collections.reverse(dxjeL);
        Collections.reverse(slvL);
        Collections.reverse(dxseL);
        JSONArray goodsArray = new JSONArray();

        for (int i = 0; i < hwmcL.size(); ++i) {
            int hh = i + 1;
            JSONObject goodsObject = new JSONObject();
            goodsObject.put("hh", hh);
            goodsObject.put("hwmc", hwmcL.get(i));
            goodsObject.put("ggxh", ggxhL.get(i));
            goodsObject.put("dw", i > dwL.size() - 1 ? "" : (String) dwL.get(i));
            goodsObject.put("sl", i > slL.size() - 1 ? "" : (String) slL.get(i));
            goodsObject.put("dj", i > djL.size() - 1 ? "" : (String) djL.get(i));
            goodsObject.put("je", i > dxjeL.size() - 1 ? "" : (String) dxjeL.get(i));
            goodsObject.put("slv", i > slvL.size() - 1 ? "" : ((String) slvL.get(i)).replace("%", ""));
            goodsObject.put("se", i > dxseL.size() - 1 ? "" : (String) dxseL.get(i));
            goodsArray.add(goodsObject);
        }

        jsonObject.put("hwmxs", goodsArray);
        return jsonObject;
    }

    private static String extractM(String parm) {
        Pattern p = Pattern.compile("[0-9\\.]+");
        Matcher m = p.matcher(parm);

        String str;
        for (str = ""; m.find(); str = str + m.group()) {
        }

        return str;
    }

    public static String getQRCodeInfoFromPdf(String filename) {
        try {
            return extractImages(pdfFileToImage(filename));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } catch (DocumentException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String saveImgFile(BufferedImage image) {
        String targetPath = FileUploadService.getNowPath() + "/" + CommonUtils.getUUID() + ".png";
        try {
            InputStream byteInputStream = null;
            image.flush();
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            ImageOutputStream imOut;
            imOut = ImageIO.createImageOutputStream(bs);
            ImageIO.write(image, "png", imOut);
            byteInputStream = new ByteArrayInputStream(bs.toByteArray());
            byteInputStream.close();

            File uploadFile = new File(targetPath);
            FileOutputStream fops;
            fops = new FileOutputStream(uploadFile);
            fops.write(readInputStream(byteInputStream));
            fops.flush();
            fops.close();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return targetPath;
    }

    public static List<String> pdfFileToImages2(String filename) throws Exception {
        //pdf文件
        File pdffile = new File(filename);
        // 转成的 png 文件存储全路径及文件名
        List<String> filePathList = new ArrayList<>();
        try {
            try (PDDocument doc = PDDocument.load(pdffile);) {
                PDFRenderer renderer = new PDFRenderer(doc);
                int pageCount = doc.getNumberOfPages();
                int pageIndex = 0;
                if (pageCount > 0) {
                    while (pageIndex < pageCount) {
                        BufferedImage image = renderer.renderImage(pageIndex, 2.0f);
                        String filePath = saveImgFile(image);
                        if (!CKHelper.isEmpty(filePath)) {
                            filePathList.add(filePath);
                        }
                        pageIndex++;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
//            finally {
//                instream.close();
//            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return filePathList;
    }

    /**
     * pdf 转 png
     */
    public static String pdfFileToImage(String filename) {
        //pdf文件
        File pdffile = new File(filename);
        // 转成的 png 文件存储全路径及文件名
        String targetPath = FileUploadService.getNowPath() + "/" + CommonUtils.getUUID() + ".png";
        try {
//            FileInputStream instream = new FileInputStream(pdffile);
            InputStream byteInputStream = null;
            try (PDDocument doc = PDDocument.load(pdffile);) {
                PDFRenderer renderer = new PDFRenderer(doc);
                int pageCount = doc.getNumberOfPages();
                if (pageCount > 0) {
                    BufferedImage image = renderer.renderImage(0, 2.0f);
                    image.flush();
                    ByteArrayOutputStream bs = new ByteArrayOutputStream();
                    ImageOutputStream imOut;
                    imOut = ImageIO.createImageOutputStream(bs);
                    ImageIO.write(image, "png", imOut);
                    byteInputStream = new ByteArrayInputStream(bs.toByteArray());
                    byteInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
//            finally {
//                instream.close();
//            }
            File uploadFile = new File(targetPath);
            FileOutputStream fops;
            fops = new FileOutputStream(uploadFile);
            fops.write(readInputStream(byteInputStream));
            fops.flush();
            fops.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return targetPath;
    }

    private static byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }

    /**
     * 识别 png图片中的二维码信息
     */
    public static String extractImages(String filename) throws IOException, DocumentException {
        String returnResult = "";
        MultiFormatReader multiFormatReader = new MultiFormatReader();
        File file = new File(filename);
        BufferedImage image = ImageIO.read(file);
        // 定义二维码参数
        Map hints = new HashMap();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");

        // 获取读取二维码结果
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
        Result result = null;
        try {
            result = multiFormatReader.decode(binaryBitmap, hints);
            returnResult = result.getText();
            file.delete();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return returnResult;
    }

    /**
     * pdf电子签章验签
     *
     * @param pdfByte
     * @return
     * @throws IOException
     * @throws CMSException
     * @throws OperatorCreationException
     * @throws GeneralSecurityException
     */
    public static OperationResultDto validateSignatures(byte[] pdfByte) throws IOException, CMSException {
        OperationResultDto resultDto = new OperationResultDto();
        resultDto.setResult(true);

        try (PDDocument pdfDoc = PDDocument.load(pdfByte)) {
            List<PDSignature> signatures = pdfDoc.getSignatureDictionaries();
            for (PDSignature signature : signatures) {
                String subFilter = signature.getSubFilter();
                byte[] signatureAsBytes = signature.getContents(pdfByte);
                byte[] signedContentAsBytes = signature.getSignedContent(pdfByte);

                CMSSignedData cms = null;
                if ("adbe.pkcs7.detached".equals(subFilter) || "ETSI.CAdES.detached".equals(subFilter)) {
                    try {
                        cms = new CMSSignedData(new CMSProcessableByteArray(signedContentAsBytes), signatureAsBytes);
                    } catch (CMSException e) {
                        resultDto.setResult(false);
                        resultDto.setResultMsg("pdf签章校验异常：" + e.getMessage());
                    }
                } else if ("adbe.pkcs7.sha1".equals(subFilter)) {
                    MessageDigest md = null;
                    try {
                        md = MessageDigest.getInstance("SHA1");
                    } catch (NoSuchAlgorithmException e) {
                        resultDto.setResult(false);
                        resultDto.setResultMsg("pdf签章校验异常：" + e.getMessage());
                        return resultDto;
                    }
                    byte[] calculatedDigest = md.digest(signedContentAsBytes);
                    byte[] signedDigest = (byte[]) cms.getSignedContent().getContent();
                    boolean digestsMatch = Arrays.equals(calculatedDigest, signedDigest);
                    if (digestsMatch)
                        System.out.println("    Document SHA1 digest matches.");
                    else {
                        resultDto.setResult(false);
                        resultDto.setResultMsg("!!! Document SHA1 digest does not match!");
                        return resultDto;
                    }

                    cms = new CMSSignedData(new ByteArrayInputStream(signatureAsBytes));
                } else if ("adbe.x509.rsa.sha1".equals(subFilter) || "ETSI.RFC3161".equals(subFilter)) {
                    resultDto.setResult(false);
                    resultDto.setResultMsg("!!! SubFilter %s not yet supported.\n" + subFilter);

                    return resultDto;
                } else if (subFilter != null) {
                    resultDto.setResult(false);
                    resultDto.setResultMsg("!!! Unknown SubFilter %s.\n" + subFilter);
                    return resultDto;
                } else {
                    resultDto.setResult(false);
                    resultDto.setResultMsg("!!! Missing SubFilter.");
                    return resultDto;

                }

                SignerInformation signerInfo = (SignerInformation) cms.getSignerInfos().getSigners().iterator().next();
                X509CertificateHolder cert = (X509CertificateHolder) cms.getCertificates().getMatches(signerInfo.getSID())
                        .iterator().next();
                SignerInformationVerifier verifier = null;
                try {
                    verifier = new JcaSimpleSignerInfoVerifierBuilder().setProvider(new BouncyCastleProvider()).build(cert);
                } catch (OperatorCreationException e) {
                    resultDto.setResult(false);
                    resultDto.setResultMsg("pdf签章校验异常：" + e.getMessage());
                    return resultDto;
                } catch (CertificateException e) {
                    resultDto.setResult(false);
                    resultDto.setResultMsg("pdf签章校验异常：" + e.getMessage());
                    return resultDto;
                }

                boolean verifyResult = signerInfo.verify(verifier);
                if (verifyResult) {
                    resultDto.setResult(true);
                    resultDto.setResultMsg("");
                    return resultDto;
                } else {
                    resultDto.setResult(false);
                    resultDto.setResultMsg("!!! Signature verification failed!");
                    return resultDto;
                }
            }
        }
        return resultDto;
    }

    public static void mergePdf(String newFilePath, String newFileName, List<String> mergeFilePaths) {
        try {
            File newPath = new File(newFilePath);
            File newFile = new File(newFilePath, newFileName);
            if (!newPath.exists()) {
                newPath.mkdirs();
            }
            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            PDFMergerUtility PDFMerger = new PDFMergerUtility();
            PDFMerger.setDestinationFileName(newFilePath + newFileName);
            List<PDDocument> documentList = new ArrayList<>();
            for (int i = 0; i < mergeFilePaths.size(); i++) {
                File file = new File(mergeFilePaths.get(i));
                PDDocument doc = PDDocument.load(file);
                documentList.add(doc);
                PDFMerger.addSource(file);
            }
            PDFMerger.mergeDocuments();
            //合并完成之后关闭doc
            documentList.forEach(doc -> {
                try {
                    doc.close();
                } catch (IOException e) {
                }
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }
}
