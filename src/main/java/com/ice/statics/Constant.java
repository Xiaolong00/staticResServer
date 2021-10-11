package com.ice.statics;

import com.ice.statics.utils.StaticLocation;
import ice.tool.PropertiesProvider;
import ice.tool.exception.CommonException;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

public class Constant {

	private static PropertiesProvider pp;

	public static final int c_auth_failed = -101;
	
	public static final int c_out_size = -102;

	public static final int c_success = 0;
	
	public static final String m_auth_failed = "auth failed!";
	
	public static final String m_out_size = "file too larger!";

	public static final String default_cron_days = "15";

	public static final String zip_cron;

	public static final String res_location;

	public static final String zip_dest_dir;

	public static final Integer zip_days;

	public static final Integer clean_days;

	public static final List<String> excluded_dirs;

	static {
		//初始化系统文件
		initPropertiesProvider();

		//初始化压缩文件定时器cron表达式
		String cronMonths = pp.getValue("cron.months");
		if(StringUtils.isBlank(cronMonths)){

			String cronDays = pp.getValue("cron.days");

			cronDays = StringUtils.isEmpty(cronDays) ? default_cron_days : cronDays;

			zip_cron = "0 0 0 */" + cronDays + " * ?";

			//zip_cron = "*/10 * * * * ?";

		} else {
			zip_cron = "0 0 0 0 */" + cronMonths + " ?";
		}

		//初始化本地路径
		String resLocation = StaticLocation.getInstance().getStaticLocations();
//		String resLocation = FileUtils.read("/application.properties", "spring.resources.static-locations");
//		String active = FileUtils.read("/application.properties", "spring.profiles.active");
//		String resLocation = FileUtils.read("/application"+active+".properties", "spring.resources.static-locations");
		if(StringUtils.isNotBlank(resLocation)){

			resLocation = resLocation.replace("file:", "");

			File file = new File(resLocation);

			res_location = file.getAbsolutePath();

		} else {
			res_location = null;
		}

		//初始化压缩文件存储路径
		String zipDestDir = pp.getValue("zip.dest.dir");

		if(StringUtils.isNotBlank(zipDestDir)){

			if(!zipDestDir.startsWith("/")){
				zipDestDir = "/" + zipDestDir;
			}

			zipDestDir.replaceAll("/", Matcher.quoteReplacement(File.separator));

			zip_dest_dir = res_location + zipDestDir;

		} else {
			zip_dest_dir = null;
		}

		//初始化文件打包条件（将多长时间之前的文件打包，单位：天）
		String days = pp.getValue("zip.days");
		if(StringUtils.isNotBlank(days)){

			zip_days = Integer.valueOf(days);

		} else {
			zip_days = null;
		}

		//初始化排除打包的文件目录
		String excludedDir = pp.getValue("excluded.dir");
		if(StringUtils.isNotBlank(excludedDir)){

			excludedDir = excludedDir.replaceAll("/", Matcher.quoteReplacement(File.separator));

			if(excludedDir.endsWith(";")){
				excludedDir = excludedDir.substring(0, excludedDir.length() - 1);
			}

			String[] dirs = excludedDir.split(";");

			excluded_dirs = Arrays.asList(dirs);

		} else {
			excluded_dirs = Collections.EMPTY_LIST;
		}


		//初始化清除压缩文件的条件（将多长时间之前的压缩文件清除，单位：天）
		String cleanDay = pp.getValue("clean.days");
		if(StringUtils.isNotBlank(cleanDay)){

			String[] cleanDays = cleanDay.split("\\*");

			Integer totalCleanDays = 1;

			for (int i = 0; i < cleanDays.length; i++) {
				totalCleanDays *= Integer.valueOf(cleanDays[i]);
			}

			clean_days = totalCleanDays;

		} else {
			clean_days = null;
		}

	}

	//初始化系统文件
	private static void initPropertiesProvider() {

		pp = PropertiesProvider.getInstance("/sys_config.properties");

		if(pp == null){
			throw new CommonException("找不到系统文件：/sys_config.properties");
		}

	}

}
