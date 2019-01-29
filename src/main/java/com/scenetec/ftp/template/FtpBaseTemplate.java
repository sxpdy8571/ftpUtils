package com.scenetec.ftp.template;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;

/**
 * @author shendunyuan@scenetec.com
 * @date 2019/1/22
 */
@Slf4j
public abstract class FtpBaseTemplate {

	/**
	 * 上传文件
	 *
	 * @param localPath 本地路径
	 * @param remotePath 远程路径，必须包含文件名（"/"为当前ftp用户根路径）
	 * @return 上传成功返回true， 否则返回false
	 */
	public boolean uploadFile(String localPath, String remotePath) {
		if (StringUtils.isBlank(localPath)) {
			log.error("本地文件路径为空");
			return false;
		}
		return uploadFile(new File(localPath), remotePath);
	}

	/**
	 * 上传文件
	 *
	 * @param localFile 本地文件
	 * @param remotePath 远程文件，必须包含文件名
	 * @return 上传成功返回true， 否则返回false
	 */
	public boolean uploadFile(File localFile, String remotePath) {
		if (!localFile.exists()) {
			log.error("本地文件不存在");
			return false;
		}
		if (!localFile.isFile()) {
			log.error("上传类型不是文件");
		}
		if (StringUtils.isBlank(remotePath)) {
			remotePath = "/";
		}
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		try {
			fis = new FileInputStream(localFile);
			bis = new BufferedInputStream(fis);
			// 上传
			return uploadFile(bis, remotePath);
		} catch (FileNotFoundException fex) {
			fex.printStackTrace();
			log.error("系统找不到指定的文件：{}", localFile);
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("上传文件异常。");
		} finally {
			try {
				if (bis != null) {
					bis.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 上传文件
	 *
	 * @param fileContent 文件内容
	 * @param remotePath 远程文件，必须包含文件名
	 * @return 上传成功返回true， 否则返回false
	 */
	public boolean uploadFile(byte[] fileContent, String remotePath) {
		if (fileContent == null || fileContent.length <= 0) {
			log.error("上传文件内容为空。");
			return false;
		}
		InputStream is = null;
		try {
			is = new ByteArrayInputStream(fileContent);
			// 上传
			return uploadFile(is, remotePath);
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("上传文件异常。原因：【{}】", ex.getMessage());
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 下载文件
	 * @param remotePath 远程文件，必须包含文件名
	 * @param localPath 本地路径，必须包含文件名（全路径）
	 * @return 下载成功返回true，否则返回false
	 */
	public boolean downloadFile(String remotePath, String localPath) {
		if (StringUtils.isBlank(remotePath)) {
			remotePath = "/";
		}
		if (StringUtils.isBlank(localPath)) {
			log.error("本地文件路径为空");
			return false;
		}
		return downloadFile(new File(localPath), remotePath);
	}

	/**
	 * 下载文件
	 * @param remotePath 远程文件，必须包含文件名
	 * @param localFile 本地文件
	 * @return 下载成功返回true，否则返回false
	 */
	public boolean downloadFile(File localFile, String remotePath) {
		// 创建本地文件路径
		if (!localFile.exists()) {
			File parentFile = localFile.getParentFile();
			if (!parentFile.exists()) {
				boolean bool = parentFile.mkdirs();
				if (!bool) {
					log.error("创建本地路径失败");
					return false;
				}
			}
		}
		OutputStream os = null;
		try {
			os = new FileOutputStream(localFile);
			// 下载
			return downloadFile(remotePath, os);
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("下载文件异常。原因：【{}】", ex.getMessage());
		} finally {
			try {
				if (os != null) {
					os.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 删除文件
	 * @param remotePath 远程文件，必须包含文件名
	 * @return 删除成功返回true，否则返回false
	 */
	public boolean deleteFile(String remotePath) {
		if (StringUtils.isBlank(remotePath)) {
			log.info("远程路径为空");
			return false;
		}
		return deleFile(remotePath);
	}

	/**
	 * 上传文件
	 * @param inputStream 文件流
	 * @param remotePath 远程路径，必须包含文件名
	 * @return 上传成功返回true，否则返回false
	 */
	protected abstract boolean uploadFile(InputStream inputStream, String remotePath);
	/**
	 * 下载文件
	 * @param remotePath 远程文件，必须包含文件名
	 * @param outputStream 本地文件流
	 * @return 下载成功返回true，否则返回false
	 */
	protected abstract boolean downloadFile(String remotePath, OutputStream outputStream);
	/**
	 * 删除文件
	 * @param remotePath 程文件，必须包含文件名
	 * @return 删除成功返回true，否则返回false
	 */
	protected abstract boolean deleFile(String remotePath);

	/**
	 * 上传文件
	 *
	 * @param client ftp客户端
	 * @param inputStream 文件流
	 * @param remotePath 远程文件，必须包含文件名
	 * @return 上传成功返回true， 否则返回false
	 */
	protected boolean uploadHandle(FTPClient client, InputStream inputStream, String remotePath) {
		// 上传
		try {
			// 获取远程文件路径
			String remoteFilePath = getRemoteFilePath(remotePath);
			// 获取远程文件名
			String remoteFileName = getRemoteFileName(remotePath);
			// 切换工作路径
			boolean bool = changeDirectory(client, remoteFilePath);
			if (!bool) {
				log.error("切换工作路径失败，{}", client.getReplyString());
				return false;
			}
			// 设置重试次数
			final int retryTime = 3;
			boolean retryResult = false;

			for (int i = 0; i <= retryTime; i++) {
				boolean success = client.storeFile(remoteFileName, inputStream);
				if (success) {
					log.info("文件【{}】上传成功。", remotePath);
					retryResult = true;
					break;
				} else {
					log.error("文件上传失败。{}", client.getReplyString());
				}
				log.warn("文件【{}】上传失败,重试上传...尝试{}次", remotePath, i);
			}

			return retryResult;
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("上传文件异常。");
		}

		return false;
	}

	/**
	 * 下载文件
	 * @param client ftp客户端
	 * @param remotePath 远程文件，必须包含文件名
	 * @param outputStream 本地文件流
	 * @return 下载成功返回true，否则返回false
	 */
	protected boolean downloadHandle(FTPClient client, String remotePath, OutputStream outputStream) {
		// 下载
		try {
			// 获取远程文件路径
			String remoteFilePath = getRemoteFilePath(remotePath);
			// 获取远程文件名
			String remoteFileName = getRemoteFileName(remotePath);
			// 切换工作路径
			boolean bool = client.changeWorkingDirectory(remoteFilePath);
			if (!bool) {
				log.error("切换工作路径失败，{}", client.getReplyString());
				return false;
			}
			// 设置重试次数
			final int retryTime = 3;
			boolean retryResult = false;

			for (int i = 0; i <= retryTime; i++) {
				boolean success = client.retrieveFile(remoteFileName, outputStream);
				if (success) {
					log.info("文件【{}】下载成功。", remotePath);
					retryResult = true;
					break;
				} else {
					log.error("文件下载失败。 {}", client.getReplyString());
				}
				log.warn("文件【{}】下载失败，重试下载...尝试{}次", remotePath, i);
			}
			// 返回结果
			return retryResult;
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("下载文件异常。");
		} finally {

		}
		return false;
	}

	/**
	 * 删除文件
	 * @param remotePath 远程文件，必须包含文件名
	 * @return 删除成功返回true，否则返回false
	 */
	protected boolean deleteHandle(FTPClient client, String remotePath) {
		try {
			// 删除文件
			boolean bool = client.deleteFile(remotePath);
			if (!bool) {
				log.error("删除文件失败，{}", client.getReplyString());
			}
			return bool;
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("删除文件异常。");
		}
		return false;
	}

	/**
	 * 获取远程工作目录
	 * @param remotePath 远程路径
	 * @return 返回工作路径
	 */
	private String getRemoteFilePath(String remotePath) {
		if (StringUtils.isNotBlank(remotePath)) {
			return remotePath.substring(0, remotePath.lastIndexOf("/") + 1);
		}
		return "/";
	}

	/**
	 * 获取远程文件名
	 * @param remotePath 远程路径
	 * @return 返回文件名
	 */
	private String getRemoteFileName(String remotePath) {
		if (StringUtils.isNotBlank(remotePath)) {
			return remotePath.substring(remotePath.lastIndexOf("/") + 1);
		}
		return "";
	}

	/**
	 * 切换工作目录
	 * @param client ftp客户端
	 * @param dir 工作目录
	 * @return 切换成功返回true，否则返回false
	 */
	private boolean changeDirectory(FTPClient client, String dir) {
		try {
			if (client == null || StringUtils.isBlank(dir)) {
				return false;
			}

			String fileBackslashSeparator = "\\";
			String fileSeparator = "/";

			if (StringUtils.contains(dir, fileBackslashSeparator)) {
				dir = StringUtils.replaceAll(dir, fileBackslashSeparator, fileSeparator);
			}

			String[] dirArray = StringUtils.split(dir, fileSeparator);

			String tmp = "";
			for (String aDirArray : dirArray) {
				tmp += fileSeparator + aDirArray;
				if (!client.changeWorkingDirectory(tmp)) {
					// 创建工作目录
					client.makeDirectory(tmp);
					// 切换工作目录
					client.changeWorkingDirectory(tmp);
				}
			}
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("切换工作目录失败。");
		}
		return false;
	}

}
