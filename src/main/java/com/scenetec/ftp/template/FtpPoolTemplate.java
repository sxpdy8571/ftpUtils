package com.scenetec.ftp.template;

import com.scenetec.ftp.core.FtpClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author shendunyuan@scenetec.com
 * @date 2018/12/20
 */
@Slf4j
@Component
public class FtpPoolTemplate extends FtpBaseTemplate {

	private GenericObjectPool<FTPClient> ftpClientPool;

	public FtpPoolTemplate(FtpClientFactory ftpClientFactory) {
		this.ftpClientPool = new GenericObjectPool<>(ftpClientFactory);
	}

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
			// 从池中获取对象
			client = getFtpClient();
			// 上传
			return uploadHandle(client, inputStream, remotePath);
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("上传文件异常。");
		} finally {
			// 将对象放回池中
			if (client != null) {
				ftpClientPool.returnObject(client);
			}
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
			// 从池中获取对象
			client = getFtpClient();
			// 下载
			return downloadHandle(client, remotePath, outputStream);
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("下载文件异常。");
		} finally {
			if (client != null) {
				ftpClientPool.returnObject(client);
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
			// 从池中获取对象
			client = getFtpClient();
			// 删除文件
			return deleteHandle(client, remotePath);
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("删除文件异常。");
		} finally {
			if (client != null) {
				ftpClientPool.returnObject(client);
			}
		}
		return false;
	}


	/**
	 * 验证连接是否成功
	 * @return 连接登录成功返回true，否则返回false
	 */
	private FTPClient getFtpClient () {
		FTPClient client = null;
		try {
			while (true) {
				// 获取客户端
				client = ftpClientPool.borrowObject();
				// 验证客户端
				if (client == null) {
					continue;
				} else {
					if (!client.isConnected() || !FTPReply.isPositiveCompletion(client.getReplyCode())) {
						ftpClientPool.invalidateObject(client);
					} else {
						break;
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return client;
	}

}
