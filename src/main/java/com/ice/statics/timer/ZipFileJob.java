package com.ice.statics.timer;

import com.ice.statics.Constant;
import ice.tool.DateUtil;
import ice.tool.zip4j.CompressUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileFilter;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by 张美景
 *
 * @Title: 文件打包定时器
 * @date 2019/01/15  10:42
 */
public class ZipFileJob extends ApplicationObjectSupport implements Job {

    private Logger log = LoggerFactory.getLogger(ZipFileJob.class);

    public void execute(JobExecutionContext jobExecutionContext) {

        try {

            log.info("开始打包文件~~~");

            this.scheduleZipFile();

            log.info("文件打包完成~~~");

        } catch (Exception e){

            log.error("文件打包失败，请联系管理员~~~", e);
        }

    }

    /**
     * 每天凌晨扫描文件并将符合条件的文件进行打包
     *
     * @Author : 张美景
     * @Date : 2019.01.15 9:28
     */
    private void scheduleZipFile() {
        System.out.println(Constant.res_location);
        if(StringUtils.isBlank(Constant.res_location)){
            return;
        }

        File file = new File(Constant.res_location);

        File[] files = file.listFiles(this.getZipFilter());

        if(files == null){
            return;
        }

        for (int i = 0; i < files.length; i++) {
            System.out.println(files[i].getAbsolutePath());
            List<File> tempFiles = CompressUtils.zip(files[i].getAbsolutePath(), Constant.zip_dest_dir, true, this.getZipFilter());

            //删除已打包的文件
            this.deleteTempFiles(tempFiles);

        }

    }

    //删除已打包的文件
    private void deleteTempFiles(List<File> tempFiles) {

        if(CollectionUtils.isEmpty(tempFiles)){
            return;
        }

        tempFiles.forEach(file -> file.delete());

    }

    private class ZipFilter implements FileFilter{

        @Override
        public boolean accept(File file) {

            if(!file.exists()){
                return false;
            }

            if(StringUtils.isBlank(Constant.res_location)){
                return false;
            }

            String absolutePath = file.getAbsolutePath();

            if(absolutePath == null || absolutePath.length() <= Constant.res_location.length()){
                return false;
            }

            absolutePath = absolutePath.substring(Constant.res_location.length());

            //判断是否是压缩文件
            if(absolutePath.matches(".*.zip")){
                return false;
            }

            if(!Constant.excluded_dirs.contains(absolutePath)){

                if(file.isDirectory()){

                    String[] list = file.list();

                    if(list == null || list.length == 0){

                        file.delete();
                        return false;
                    }

                } else {

                    LocalDateTime lastModifiedTime = DateUtil.ofEpochMillis(file.lastModified());

                    if(Constant.zip_days != null){
                        lastModifiedTime = lastModifiedTime.plusDays(Constant.zip_days);
                    }

                    if(LocalDateTime.now().isBefore(lastModifiedTime)){
                        return false;
                    }

                }

                return true;

            }

            return false;

        }

    }

    public ZipFilter getZipFilter(){
        return new ZipFileJob.ZipFilter();
    }

}
