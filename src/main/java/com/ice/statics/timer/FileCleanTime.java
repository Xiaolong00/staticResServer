package com.ice.statics.timer;

import com.ice.statics.Constant;
import ice.tool.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * @Author : 张美景
 * @Date : 2019.01.16 16:54
 * @Function :文件清除定时器
 */
@Component
@Configurable
@EnableScheduling
public class FileCleanTime {

    private Logger log = LoggerFactory.getLogger(FileCleanTime.class);

    /**
     * 每天凌晨扫描文件并将符合条件的压缩文件清除
     *
     * @Author : 张美景
     * @Date : 2019.01.16 16:55
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduleCleanZipFile() {

        try{

            log.info("压缩文件清除开始");

            if(StringUtils.isBlank(Constant.res_location)){
                return;
            }

            File file = new File(Constant.res_location);

            File[] files = file.exists() ? file.listFiles() : null;

            if(files == null){
                return;
            }

            this.cleanZipFiles(Arrays.asList(files));

            log.info("压缩文件清除结束");

        } catch (Exception e){

            log.error("压缩文件清除失败", e);

        }
    }

    //扫描文件并将符合条件的压缩文件清除
    private void cleanZipFiles(List<File> files) {
        
        if(CollectionUtils.isEmpty(files)){
            return;
        }

        files.forEach(file -> {

            if(file.isDirectory()){

                File[] childFiles = file.listFiles();

                if(childFiles == null || childFiles.length == 0){

                    file.delete();

                } else {

                    this.cleanZipFiles(Arrays.asList(childFiles));

                }

            } else {

                this.doCleanZipFile(file);

            }

        });

    }


    private void doCleanZipFile(File file) {

        if(file == null || !file.exists() || !file.getName().matches(".*.zip")){

            return;
        }

        LocalDateTime lastModifiedTime = DateUtil.ofEpochMillis(file.lastModified());

        if(Constant.clean_days != null){
            lastModifiedTime = lastModifiedTime.plusDays(Constant.clean_days);
        }

        if(!LocalDateTime.now().isBefore(lastModifiedTime)){

            file.delete();

        }

    }

}
