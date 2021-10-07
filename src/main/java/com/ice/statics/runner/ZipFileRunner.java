//package com.ice.statics.runner;
//
//import com.ice.statics.Constant;
//import com.ice.statics.timer.ZipFileJob;
//import ice.tool.timer.QuartzUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.util.UUID;
//
///**
// * 文件定时压缩后删除
// */
//@Component
//public class ZipFileRunner implements CommandLineRunner {
//
//    private Logger log = LoggerFactory.getLogger(ZipFileRunner.class);
//
//    @Override
//    public void run(String... strings) {
//
//        String taskName = UUID.randomUUID().toString();
//
//        taskName = taskName.replaceAll("-", "");
//
//        //加入定时任务
//        log.info("文件打包定时任务启动~~~");
////        QuartzUtils.addJob(ZipFileJob.class, "1 * * * * ? ", null, taskName);
//
//        QuartzUtils.addJob(ZipFileJob.class, Constant.zip_cron, null, taskName);
//
//    }
//
//
//}
