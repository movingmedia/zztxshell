package com.wymd.cordova.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.wymd.cordova.applications.AppContents;
import com.wymd.cordova.applications.MovingMediaApplication;

import android.content.Context;
import android.util.Log;

public class ZipUtil {

	private static final int BUFF_SIZE = 1024 * 1024; // 1M Byte

	/**
	 * @param from
	 *            文件所在目录
	 * @param to
	 *            解压缩后目录
	 */
	public static void execute(String from, String to) {
		try {
			int index = from.lastIndexOf("/");
			String absFileName = from.substring(index);
			String baseDir = from.substring(0, index);
			File zipFile = getRealFileName(baseDir, absFileName);

			upZipFile(zipFile, to);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 解压缩功能. 将zipFile文件解压到folderPath目录下.
	 * 
	 * @throws Exception
	 */
	private static int upZipFile(File zipFile, String folderPath)
			throws IOException {
		InputStream is = null;
		OutputStream os = null;
		ZipFile zfile = null;
		try {
			zfile = new ZipFile(zipFile);

			Enumeration<? extends ZipEntry> zList = zfile.entries();
			ZipEntry ze = null;
			byte[] buf = new byte[1024];
			while (zList.hasMoreElements()) {
				ze = (ZipEntry) zList.nextElement();
				if (ze.isDirectory()) {
					Log.d("upZipFile", "ze.getName() = " + ze.getName());
					String dirstr = folderPath + ze.getName();
					// dirstr.trim();
					dirstr = new String(dirstr.getBytes("8859_1"), "GB2312");
					Log.d("upZipFile", "str = " + dirstr);
					File f = new File(dirstr);
					f.mkdir();
					continue;
				}

				Log.d("upZipFile", "ze.getName() = " + ze.getName());
				os = new BufferedOutputStream(new FileOutputStream(
						getRealFileName(folderPath, ze.getName())));
				is = new BufferedInputStream(zfile.getInputStream(ze));
				int readLen = 0;
				while ((readLen = is.read(buf, 0, 1024)) != -1) {
					os.write(buf, 0, readLen);
				}
				is.close();
				os.close();
			}
			zfile.close();
			Log.d("upZipFile", "finishssssssssssssssssssss");

		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Log.d("upZipFile", "走到这里整个解压过程才结束");
		return 0;
	}

	/**
	 * 给定根目录，返回一个相对路径所对应的实际文件名.
	 * 
	 * @param baseDir
	 *            指定根目录
	 * @param absFileName
	 *            相对路径名，来自于ZipEntry中的name
	 * @return java.io.File 实际的文件
	 */
	private static File getRealFileName(String baseDir, String absFileName) {
		String[] dirs = absFileName.split("/");
		File ret = new File(baseDir);
		String substr = null;
		if (dirs.length > 1) {
			for (int i = 0; i < dirs.length - 1; i++) {
				substr = dirs[i];
				try {
					// substr.trim();
					substr = new String(substr.getBytes("8859_1"), "GB2312");

				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				ret = new File(ret, substr);

			}
			Log.d("upZipFile", "1ret = " + ret);
			if (!ret.exists())
				ret.mkdirs();
			substr = dirs[dirs.length - 1];
			try {
				// substr.trim();
				substr = new String(substr.getBytes("8859_1"), "GB2312");
				Log.d("upZipFile", "substr = " + substr);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			ret = new File(ret, substr);
			Log.d("upZipFile", "2ret = " + ret);
			return ret;
		}
		return ret;
	}

	/**
	 * 批量压缩文件（夹）
	 * 
	 * @param resFileList
	 *            要压缩的文件（夹）列表
	 * @param zipFile
	 *            生成的压缩文件
	 * @throws IOException
	 *             当压缩过程出错时抛出
	 */
	public static void zipFiles(Collection<File> resFileList, File zipFile)
			throws IOException {
		ZipOutputStream zipout = new ZipOutputStream(new BufferedOutputStream(
				new FileOutputStream(zipFile), BUFF_SIZE));
		for (File resFile : resFileList) {
			zipFile(resFile, zipout, "");
		}
		zipout.close();
	}

	/**
	 * 批量压缩文件（夹）
	 * 
	 * @param resFileList
	 *            要压缩的文件（夹）列表
	 * @param zipFile
	 *            生成的压缩文件
	 * @param comment
	 *            压缩文件的注释
	 * @throws IOException
	 *             当压缩过程出错时抛出
	 */
	public static void zipFiles(Collection<File> resFileList, File zipFile,
			String comment) throws IOException {
		ZipOutputStream zipout = new ZipOutputStream(new BufferedOutputStream(
				new FileOutputStream(zipFile), BUFF_SIZE));
		for (File resFile : resFileList) {
			zipFile(resFile, zipout, "");
		}
		zipout.setComment(comment);
		zipout.close();
	}

	/**
	 * 解压文件名包含传入文字的文件
	 * 
	 * @param zipFile
	 *            压缩文件
	 * @param folderPath
	 *            目标文件夹
	 * @param nameContains
	 *            传入的文件匹配名
	 * @throws ZipException
	 *             压缩格式有误时抛出
	 * @throws IOException
	 *             IO错误时抛出
	 */
	public static ArrayList<File> upZipSelectedFile(File zipFile,
			String folderPath, String nameContains) throws ZipException,
			IOException {
		ArrayList<File> fileList = new ArrayList<File>();

		File desDir = new File(folderPath);
		if (!desDir.exists()) {
			desDir.mkdir();
		}

		ZipFile zf = new ZipFile(zipFile);
		for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements();) {
			ZipEntry entry = ((ZipEntry) entries.nextElement());
			if (entry.getName().contains(nameContains)) {
				InputStream in = zf.getInputStream(entry);
				String str = folderPath + File.separator + entry.getName();
				str = new String(str.getBytes("8859_1"), "GB2312");
				// str.getBytes("GB2312"),"8859_1" 输出
				// str.getBytes("8859_1"),"GB2312" 输入
				File desFile = new File(str);
				if (!desFile.exists()) {
					File fileParentDir = desFile.getParentFile();
					if (!fileParentDir.exists()) {
						fileParentDir.mkdirs();
					}
					desFile.createNewFile();
				}
				OutputStream out = new FileOutputStream(desFile);
				byte buffer[] = new byte[BUFF_SIZE];
				int realLength;
				while ((realLength = in.read(buffer)) > 0) {
					out.write(buffer, 0, realLength);
				}
				in.close();
				out.close();
				fileList.add(desFile);
			}
		}
		return fileList;
	}

	/**
	 * 获得压缩文件内文件列表
	 * 
	 * @param zipFile
	 *            压缩文件
	 * @return 压缩文件内文件名称
	 * @throws ZipException
	 *             压缩文件格式有误时抛出
	 * @throws IOException
	 *             当解压缩过程出错时抛出
	 */
	public static ArrayList<String> getEntriesNames(File zipFile)
			throws ZipException, IOException {
		ArrayList<String> entryNames = new ArrayList<String>();
		Enumeration<?> entries = getEntriesEnumeration(zipFile);
		while (entries.hasMoreElements()) {
			ZipEntry entry = ((ZipEntry) entries.nextElement());

			// 非目录
			if (!entry.isDirectory()) {
				entryNames.add(new String(getEntryName(entry)
						.getBytes("GB2312"), "8859_1"));
			}

		}
		return entryNames;
	}

	/**
	 * 获得压缩文件内压缩文件对象以取得其属性
	 * 
	 * @param zipFile
	 *            压缩文件
	 * @return 返回一个压缩文件列表
	 * @throws ZipException
	 *             压缩文件格式有误时抛出
	 * @throws IOException
	 *             IO操作有误时抛出
	 */
	public static Enumeration<?> getEntriesEnumeration(File zipFile)
			throws ZipException, IOException {
		ZipFile zf = new ZipFile(zipFile);
		return zf.entries();

	}

	/**
	 * 取得压缩文件对象的注释
	 * 
	 * @param entry
	 *            压缩文件对象
	 * @return 压缩文件对象的注释
	 * @throws UnsupportedEncodingException
	 */
	public static String getEntryComment(ZipEntry entry)
			throws UnsupportedEncodingException {
		return new String(entry.getComment().getBytes("GB2312"), "8859_1");
	}

	/**
	 * 取得压缩文件对象的名称
	 * 
	 * @param entry
	 *            压缩文件对象
	 * @return 压缩文件对象的名称
	 * @throws UnsupportedEncodingException
	 */
	public static String getEntryName(ZipEntry entry)
			throws UnsupportedEncodingException {
		return new String(entry.getName().getBytes("GB2312"), "8859_1");
	}

	/**
	 * 压缩文件
	 * 
	 * @param resFile
	 *            需要压缩的文件（夹）
	 * @param zipout
	 *            压缩的目的文件
	 * @param rootpath
	 *            压缩的文件路径
	 * @throws FileNotFoundException
	 *             找不到文件时抛出
	 * @throws IOException
	 *             当压缩过程出错时抛出
	 */
	private static void zipFile(File resFile, ZipOutputStream zipout,
			String rootpath) throws FileNotFoundException, IOException {
		rootpath = rootpath
				+ (rootpath.trim().length() == 0 ? "" : File.separator)
				+ resFile.getName();
		rootpath = new String(rootpath.getBytes("8859_1"), "GB2312");
		if (resFile.isDirectory()) {
			File[] fileList = resFile.listFiles();
			for (File file : fileList) {
				zipFile(file, zipout, rootpath);
			}
		} else {
			byte buffer[] = new byte[BUFF_SIZE];
			BufferedInputStream in = new BufferedInputStream(
					new FileInputStream(resFile), BUFF_SIZE);
			zipout.putNextEntry(new ZipEntry(rootpath));
			int realLength;
			while ((realLength = in.read(buffer)) != -1) {
				zipout.write(buffer, 0, realLength);
			}
			in.close();
			zipout.flush();
			zipout.closeEntry();
		}
	}

	/**
	 * 文件夹遍历
	 * 
	 * @param strPath
	 * @return
	 */
	// no recursion
	public static LinkedList<File> listLinkedFiles(String strPath) {
		LinkedList<File> list = new LinkedList<File>();
		File dir = new File(strPath);
		File file[] = dir.listFiles();
		for (int i = 0; i < file.length; i++) {
			if (file[i].isDirectory())
				list.add(file[i]);
			else
				System.out.println(file[i].getAbsolutePath());
		}
		// File tmp;
		// while (!list.isEmpty()) {
		// tmp = (File) list.removeFirst();
		// if (tmp.isDirectory()) {
		// file = tmp.listFiles();
		// if (file == null)
		// continue;
		// for (int i = 0; i < file.length; i++) {
		// if (file[i].isDirectory())
		// list.add(file[i]);
		// else
		// System.out.println(file[i].getAbsolutePath());
		// }
		// } else {
		// System.out.println(tmp.getAbsolutePath());
		// }
		// }
		return list;
	}

	// recursion
	private static ArrayList<File> listFiles(String strPath) {
		return refreshFileList(strPath);
	}

	private static ArrayList<File> refreshFileList(String strPath) {
		ArrayList<File> filelist = new ArrayList<File>();
		File dir = new File(strPath);
		File[] files = dir.listFiles();

		if (files == null)
			return null;
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				refreshFileList(files[i].getAbsolutePath());
			} else {
				if (files[i].getName().toLowerCase().endsWith("zip"))
					filelist.add(files[i]);
			}
		}
		return filelist;
	}

	/**
	 * 从流中解压文件
	 * 
	 * @param zipFileName
	 *            zip文件流
	 * @param outputDirectory
	 *            输出目录路径
	 */
	public static void unzip(InputStream zipFileName, String outputDirectory) {
		try {
			ZipInputStream in = new ZipInputStream(zipFileName);
			// 获取ZipInputStream中的ZipEntry条目，一个zip文件中可能包含多个ZipEntry，
			// 当getNextEntry方法的返回值为null，则代表ZipInputStream中没有下一个ZipEntry，
			// 输入流读取完成；
			ZipEntry entry = in.getNextEntry();
			while (entry != null) {

				// 创建以zip包文件名为目录名的根目录
				File file = new File(outputDirectory);
				file.mkdir();
				if (entry.isDirectory()) {
					String name = entry.getName();
					name = name.substring(0, name.length() - 1);

					file = new File(outputDirectory + File.separator + name);
					file.mkdir();

				} else {
					file = new File(outputDirectory + File.separator
							+ entry.getName());
					file.createNewFile();
					FileOutputStream out = new FileOutputStream(file);
					int b;
					while ((b = in.read()) != -1) {
						out.write(b);
					}
					out.close();
				}
				// 读取下一个ZipEntry
				entry = in.getNextEntry();
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 复制、移动文件.
	 * 
	 * @param filePaht
	 * @param outputDirectory
	 * @throws IOException
	 */

	public static void copyDataBase(Context context, String fileName,
			String outputDirectory) {

		// Open your local db as the input stream
		InputStream myInput = null;
		OutputStream myOutput = null;
		try {
			myInput = context.getAssets().open(fileName);

			// Open the empty db as the output stream128
			myOutput = new FileOutputStream(outputDirectory + fileName);
			// transfer bytes from the inputfile to the outputfile130
			byte[] buffer = new byte[1024];
			int length;
			while ((length = myInput.read(buffer)) > 0) {
				myOutput.write(buffer, 0, length);
			}
			// Close the streams136
			myOutput.flush();
			myOutput.close();
			myInput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
