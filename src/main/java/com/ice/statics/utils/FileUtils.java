package com.ice.statics.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ice.tool.PropertiesProvider;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

public class FileUtils {

	private FileUtils() {

	}

	// 下载
	public static void download(HttpServletRequest request,
			HttpServletResponse response, String relativeFilePath) {
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			request.setCharacterEncoding("UTF-8");
			response.setCharacterEncoding("UTF-8");

			String fileName = request.getSession().getServletContext()
					.getRealPath("/")
					+ relativeFilePath;
			System.out.println("文件路径"+fileName);
			fileName = fileName.replace("\\", "/");// 统一分隔符格式
			File file = new File(fileName);
			// 如果文件不存在
			if (file == null || !file.exists()) {
				String msg = "file not exists!";
				System.out.println(msg);
				PrintWriter out = response.getWriter();
				out.write(msg);
				out.flush();
				out.close();
				return;
			}

			String fileType = request.getSession().getServletContext()
					.getMimeType(fileName);
			if (fileType == null) {
				fileType = "application/octet-stream";
			}
			response.setContentType(fileType);
			System.out.println("文件类型是：" + fileType);
			String simpleName = fileName
					.substring(fileName.lastIndexOf("/") + 1);
			String newFileName = new String(simpleName.getBytes(), "ISO8859-1");
			//inline，默认值，表示回复中的消息体会以页面的一部分或者整个页面的形式展示。
			// attachment意味着消息体应该被下载到本地，filename是要传送的文件的初始名称的字符串
			response.setHeader("Content-disposition", "attachment;filename=" + newFileName);
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis);
			bos = new BufferedOutputStream(response.getOutputStream());

			byte[] buffer = new byte[1024];
			int length = 0;

			while ((length = bis.read(buffer)) != -1) {
				bos.write(buffer, 0, length);
			}

			if (bis != null)
				bis.close();
			if (bos != null)
				bos.close();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				safeClose(fis);
			}
		}
	}


	public static void safeClose(FileInputStream fis) {
		if (fis != null) {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 文件上传
	 *
	 * @param request
	 *            HttpServletRequest
	 * @param relativeUploadPath
	 *            上传文件保存的相对路径，例如"upload/"，注意，末尾的"/"不要丢了
	 * @param maxSize
	 *            上传的最大文件尺寸，单位字节
	 * @param thresholdSize
	 *            最大缓存，单位字节
	 * @param fileTypes
	 *            文件类型，会根据上传文件的后缀名判断。<br>
	 *            比如支持上传jpg,jpeg,gif,png图片,那么此处写成".jpg .jpeg .gif .png",<br>
	 *            也可以写成".jpg/.jpeg/.gif/.png"，类型之间的分隔符是什么都可以，甚至可以不要，<br>
	 *            直接写成".jpg.jpeg.gif.png"，但是类型前边的"."不能丢
	 * @return
	 */
	public static List<String> upload(HttpServletRequest request,
			String relativeUploadPath, int maxSize, int thresholdSize,
			String fileTypes) {
		// 设置字符编码
		try {
			request.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		String serverPath = request.getSession().getServletContext()
				.getRealPath("/").replace("\\", "/");
		fileTypes = fileTypes.toLowerCase(); // 将后缀全转换为小写

		// 如果上传文件目录和临时目录不存在则自动创建
		if (!new File(serverPath + relativeUploadPath).exists()) {
			new File(serverPath + relativeUploadPath).mkdirs();
		}

		MultipartHttpServletRequest mr = (MultipartHttpServletRequest)request;
		String uoloadpath = serverPath + relativeUploadPath;
		Iterator<String> fir = mr.getFileNames();
		List<String> filePaths = new ArrayList<String>();
		while(fir.hasNext()) {
			String fname = fir.next();
			MultipartFile mFile = mr.getFile(fname);
			if(mFile.getSize() > maxSize) {
				continue;
			}
			try {
				//随机产生文件名称
				String rname = randFilename(mFile);
				File upFile = new File(uoloadpath + "/" + rname);
				//filePaths.add(upFile.getAbsolutePath().replace("\\", "/"));
				if(!upFile.exists()) {
					upFile.createNewFile();
				}
				mFile.transferTo(upFile);
				filePaths.add(relativeUploadPath + upFile.getName());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return filePaths;
	}

	private static String randFilename(MultipartFile mFile) {
		String OrigName = mFile.getOriginalFilename();
		String subfix = "";
		String prefix = "";
		int rnum = new Random().nextInt(100);
		String newName = new Date().getTime() + "-" + rnum;
		int ed = OrigName.lastIndexOf(".");
		if(ed > 0) {
			subfix = OrigName.substring(ed, OrigName.length());
			prefix = OrigName.substring(0, ed - 1);
		}
		return prefix + "_" + newName + subfix;
	}

	/**
	 * 文件上传
	 *
	 * @param request
	 *            HttpServletRequest
	 * @param relativeUploadPath
	 *            上传文件保存的相对路径，例如"upload/"，注意，末尾的"/"不要丢了
	 * @param maxSize
	 *            上传的最大文件尺寸，单位字节
	 * @param fileTypes
	 *            文件类型，会根据上传文件的后缀名判断。<br>
	 *            比如支持上传jpg,jpeg,gif,png图片,那么此处写成".jpg .jpeg .gif .png",<br>
	 *            也可以写成".jpg/.jpeg/.gif/.png"，类型之间的分隔符是什么都可以，甚至可以不要，<br>
	 *            直接写成".jpg.jpeg.gif.png"，但是类型前边的"."不能丢
	 * @return
	 */
	public static List<String> upload(HttpServletRequest request,
			String relativeUploadPath, int maxSize, String fileTypes) {
		return upload(request, relativeUploadPath, maxSize, 5 * 1024, fileTypes);
	}

	/**
	 * 文件上传，不限大小
	 *
	 * @param request
	 *            HttpServletRequest
	 * @param relativeUploadPath
	 *            上传文件保存的相对路径，例如"upload/"，注意，末尾的"/"不要丢了
	 * @param fileTypes
	 *            文件类型，会根据上传文件的后缀名判断。<br>
	 *            比如支持上传jpg,jpeg,gif,png图片,那么此处写成".jpg .jpeg .gif .png",<br>
	 *            也可以写成".jpg/.jpeg/.gif/.png"，类型之间的分隔符是什么都可以，甚至可以不要，<br>
	 *            直接写成".jpg.jpeg.gif.png"，但是类型前边的"."不能丢
	 * @return
	 */
	public static List<String> upload(HttpServletRequest request,
			String relativeUploadPath, String fileTypes) {
		return upload(request, relativeUploadPath, 3*1024*1024, 5 * 1024, fileTypes);
	}

	/**
	 * 读取.properties文件的数据
	 *
	 * @param configPath 文件路径
	 * @param key 键
	 * @return 值
	 */
	public static String read(String configPath, String key) {

		return PropertiesProvider.getInstance(configPath).getValue(key);

	}
}
