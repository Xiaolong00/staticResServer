package com.ice.statics;

import com.ice.statics.auth.AuthCheck;
import com.ice.statics.utils.FileUtils;
import ice.tool.GsonUtil;
import ice.tool.PropertiesProvider;
import ice.tool.exception.CommonException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.*;

@RestController
public class ResUploadAction {

    private Logger log = Logger.getLogger(ResUploadAction.class);

    private PropertiesProvider pp;

    @Resource(name = "simpleAuthCheck")
    private AuthCheck authCheck;

    public ResUploadAction() {

        pp = PropertiesProvider.getInstance("/sys_config.properties");

        if (pp == null) {
            throw new CommonException("找不到系统文件：/sys_config.properties");
        }
    }


    @RequestMapping("/download")
    public void download(HttpServletRequest req, HttpServletResponse resp, String relativeFilePath) {
        String fileName = "/Users/raines/Desktop/my/demo" + relativeFilePath;
        File file = new File(fileName);
        try {
            // 如果文件不存在
            if (file == null || !file.exists()) {
                String msg = "file not exists!";
                System.out.println(msg);
                PrintWriter out = null;
                out = resp.getWriter();
                out.write(msg);
                out.flush();
                out.close();
                return;
            }
            String fileType = req.getSession().getServletContext()
                    .getMimeType(fileName);
            if (fileType == null) {
                fileType = "application/octet-stream";
            }
            resp.setContentType(fileType);
            System.out.println("文件类型是：" + fileType);
            String simpleName = fileName
                    .substring(fileName.lastIndexOf("/") + 1);
            String newFileName = new String(simpleName.getBytes(), "ISO8859-1");
            //inline，默认值，表示回复中的消息体会以页面的一部分或者整个页面的形式展示。
            // attachment意味着消息体应该被下载到本地，filename是要传送的文件的初始名称的字符串
            resp.setHeader("Content-disposition", "inline;filename=" + newFileName);
            byte[] bFile = Files.readAllBytes(file.toPath());
            ServletOutputStream outputStream = resp.getOutputStream();
            outputStream.write(bFile);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 文件上传
     *
     * @param req  expand_dir：自定义拓展目录参数
     * @param resp
     */
    @RequestMapping("/upload")
    public void upoload(HttpServletRequest req, HttpServletResponse resp) {

        Map<String, String[]> pm = req.getParameterMap();
        Map<String, String> resMap = new HashMap<String, String>();
        boolean b = authCheck.check(pm);
        if (!b) {
            resMap.put("code", "" + Constant.c_auth_failed);
            resMap.put("msg", Constant.m_auth_failed);
            outMsg(resp, resMap, "json");
            return;
        }

        MultipartHttpServletRequest mr = (MultipartHttpServletRequest) req;

        MultiValueMap<String, MultipartFile> mfm = mr.getMultiFileMap();

        String strRes = checkFileSize(mfm);
        if (!"".equals(strRes)) {
            resMap.put("code", "" + Constant.c_out_size);
            resMap.put("msg", Constant.m_out_size);
            resMap.put("desc", strRes);
            outMsg(resp, resMap, "json");
            return;
        }

        String appCode = req.getParameter("app_code");
        String expandDir = req.getParameter("expand_dir");
        Map<String, String> pathMap = saveFile(mfm, appCode, expandDir);
        resMap.put("code", "" + Constant.c_success);
        resMap.putAll(pathMap);
        outMsg(resp, resMap, "json");
        return;
    }

    private Map<String, String> saveFile(MultiValueMap<String, MultipartFile> mfm, String appCode, String expandDir) {
        Map<String, String> pm = new HashMap<String, String>();
        Set<Map.Entry<String, List<MultipartFile>>> set = mfm.entrySet();
        Iterator<Map.Entry<String, List<MultipartFile>>> ir = set.iterator();
        while (ir.hasNext()) {
            Map.Entry<String, List<MultipartFile>> entry = ir.next();
            String key = entry.getKey();
            String paths = "";
            for (MultipartFile mf : entry.getValue()) {

                String nwname = randFilename(mf.getOriginalFilename());

                String realPath = this.getRealPath(nwname, Constant.res_location, appCode, expandDir);

                try {
                    _saveFile(realPath, mf);

                    paths += this.getReturnPath(nwname, appCode, expandDir);

                } catch (Exception e) {
                    log.error("文件上传中出现异常===" + realPath, e);
                    paths += ";failed";
                    e.printStackTrace();
                }
            }
            paths = paths.replaceFirst(";", "");
            pm.put(key, paths);
        }
        return pm;
    }

    private String getReturnPath(String nwname, String appCode, String expandDir) {

        StringBuilder returnPath = new StringBuilder();

        returnPath.append(";");

        returnPath.append(appCode).append(File.separator);

        returnPath.append(getMyGroup(nwname)).append(File.separator);

        returnPath.append(expandDir).append(File.separator);

        returnPath.append(nwname);

        return returnPath.toString();
    }

    private String getRealPath(String nwname, String resLocation, String appCode, String expandDir) {

        if (StringUtils.isBlank(resLocation)) {
            throw new CommonException("本地路径不能为空：resLocation");
        }

        StringBuilder builder = new StringBuilder();

        builder.append(resLocation).append(File.separator);

        builder.append(appCode).append(File.separator);

        builder.append(getMyGroup(nwname)).append(File.separator);

        if (!StringUtils.isEmpty(expandDir)) {
            builder.append(expandDir).append(File.separator);
        }

        builder.append(nwname);

        return builder.toString();

    }

    private static String randFilename(String fileName) {
        String subfix = "";
        String prefix = "";
        int rnum = new Random().nextInt(100);
        String newName = new Date().getTime() + "-" + rnum;
        int ed = fileName.lastIndexOf(".");
        if (ed > 0) {
            subfix = fileName.substring(ed);
            prefix = fileName.substring(0, ed);
        }
        return prefix + "_" + newName + subfix;
    }

    private void _saveFile(String path, MultipartFile mf) throws Exception {

        File upFile = new File(path);

        List<File> forCreate = new ArrayList<File>();
        File cf = upFile;
        while (cf != null && !cf.exists()) {
            forCreate.add(cf);
            cf = cf.getParentFile();
        }
        for (int i = forCreate.size() - 1; i >= 0; i--) {
            if (i > 0) {
                forCreate.get(i).mkdir();
                continue;
            }
            forCreate.get(i).createNewFile();
        }

        System.out.println(upFile.getAbsolutePath());

        mf.transferTo(upFile);
    }

    private void outMsg(HttpServletResponse resp, Object o, String type) {
        try {
            resp.setContentType(type);
            resp.getWriter().write(GsonUtil.ObjToString(o, null));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String checkFileSize(MultiValueMap<String, MultipartFile> mfm) {
        Set<Map.Entry<String, List<MultipartFile>>> set = mfm.entrySet();
        Iterator<Map.Entry<String, List<MultipartFile>>> ir = set.iterator();
        while (ir.hasNext()) {
            Map.Entry<String, List<MultipartFile>> entry = ir.next();
            for (MultipartFile mf : entry.getValue()) {
                String subfix = getSubfix(mf.getOriginalFilename());
                long rsize = mf.getSize();
                Long setsize = Long.valueOf(pp.getValue("size.other"));
                try {
                    setsize = Long.valueOf(pp.getValue("size." + subfix.toLowerCase()));
                } catch (Exception e) {
                    log.info(subfix + "类型文件未设置大小限制，采用默认值", e);
                }
                if (rsize > setsize * 1024l) {
                    //文件大小超过设置的默认值
                    return "文件域：" + entry.getKey() + "，文件过大，所有文件上传失败！";
                }
            }
        }
        return "";
    }

    private String getSubfix(String fileName) {
        String subfix = "";
        int ed = fileName.lastIndexOf(".");
        if (ed > 0) {
            subfix = fileName.substring(ed + 1);
        }
        return subfix;
    }

    private String getMyGroup(String fileName) {
        if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg") ||
                fileName.toLowerCase().endsWith(".png") || fileName.toLowerCase().endsWith(".gif") ||
                fileName.toLowerCase().endsWith(".bmp")) {
            return "imgs";
        }
        if (fileName.toLowerCase().endsWith(".doc") || fileName.toLowerCase().endsWith(".docx") ||
                fileName.toLowerCase().endsWith(".xls") || fileName.toLowerCase().endsWith(".xlsx") ||
                fileName.toLowerCase().endsWith(".csv") || fileName.toLowerCase().endsWith(".ppt") ||
                fileName.toLowerCase().endsWith(".pptx")) {
            return "docs";
        }
        return "other";
    }
}
