package com.scenetec.ftp.template;

import com.scenetec.ftp.core.FtpClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author shendunyuan@scenetec.com
 * @date 2019/1/22
 */
@Slf4j
@Component
public class FtpOnceTemplate extends FtpBaseTemplate {

	@Resource
	private FtpClientFactory factory;

	/**
	 * 上传文件
	 *
	 * @param inputStream 文件流
	 * @param remotePath 远程文件，必须包含文件名
	 * @return 上传成功返回true， 否则返回false
	 */
	@Override
	public boolean uploadFile(InputStream inputStream, String remotePath) {
		// 上传
		FTPClient client = null;
		try {
			// 创建对象
			client = factory.create();
			// 上传
			return uploadHandle(client, inputStream, remotePath);
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("上传文件异常。");
		} finally {
			factory.close(client);
		}
		return false;
	}

	/**
	 * 下载文件
	 * @param remotePath 远程文件，必须包含文件名
	 * @param outputStream 本地文件流
	 * @return 下载成功返回true，否则返回false
	 */
	@Override
	public boolean downloadFile(String remotePath, OutputStream outputStream) {
		// 下载
		FTPClient client = null;
		try {
			// 获取对象
			client = factory.create();
			// 文件下载
			return downloadHandle(client, remotePath, outputStream);
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("下载文件异常。");
		} finally {
			if (client != null) {
				factory.close(client);
			}
		}
		return false;
	}

	/**
	 * 删除文件
	 * @param remotePath 远程文件，必须包含文件名
	 * @return 下载成功返回true，否则返回false
	 */
	@Override
	public boolean deleFile(String remotePath) {
		FTPClient client = null;
		try {
			// 获取对象
			client = factory.create();
			// 删除文件
			return deleteHandle(client, remotePath);
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("删除文件异常。");
		} finally {
			if (client != null) {
				factory.close(client);
			}
		}
		return false;
	}

}
