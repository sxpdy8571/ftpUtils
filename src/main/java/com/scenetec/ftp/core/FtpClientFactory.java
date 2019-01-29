package com.scenetec.ftp.core;

import com.scenetec.ftp.config.FtpClientProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.springframework.stereotype.Component;

/**
 * @author shendunyuan@scenetec.com
 * @date 2018/12/19
 */
@Slf4j
@Component
public class FtpClientFactory extends BasePooledObjectFactory<FTPClient> {

	private FtpClientProperties config;

	public FtpClientFactory(FtpClientProperties config) {
		this.config = config;
	}

	@Override
	public FTPClient create() {

		FTPClient ftpClient = new FTPClient();
		ftpClient.setControlEncoding(config.getEncoding());
		if (null != config.getConnectTimeout()) {
			ftpClient.setConnectTimeout(config.getConnectTimeout());
		}

		try {
			ftpClient.connect(config.getHost(), config.getPort());
			int replyCode = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(replyCode)) {
				ftpClient.disconnect();
				log.warn("FTPServer refused connection,replyCode:{}", replyCode);
				return null;
			}

			if (!ftpClient.login(config.getUsername(), config.getPassword())) {
				log.warn("FTPClient login failed... username is {}; password: {}", config.getUsername(), config.getPassword());
			}

			ftpClient.setBufferSize(config.getBufferSize());
			ftpClient.setFileType(config.getTransferFileType());
			if (config.isPassiveMode()) {
				ftpClient.enterLocalPassiveMode();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("Failed to create FTP connection");
		}

		return ftpClient;
	}

	void destroyObject(FTPClient client) {
		destroyObject(wrap(client));
	}

	/**
	 * 用PooledObject封装对象放入池中
	 * @param ftpClient ftp客户端
	 * @return 默认池对象
	 */
	@Override
	public PooledObject<FTPClient> wrap(FTPClient ftpClient) {
		return new DefaultPooledObject<>(ftpClient);
	}

	/**
	 * 销毁FtpClient对象
	 * @param ftpPooled ftp池对象
	 */
	@Override
	public void destroyObject(PooledObject<FTPClient> ftpPooled) {
		if (ftpPooled == null) {
			return;
		}

		FTPClient ftpClient = ftpPooled.getObject();

		close(ftpClient);
	}

	/**
	 * 销毁FtpClient对象
	 * @param client ftp对象
	 */
	public void close(FTPClient client) {
		try {
			if (client.isConnected()) {
				client.logout();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("Failure to destroy FTP connection pool.");
		} finally {
			try {
				client.disconnect();
			} catch (Exception ex) {
				ex.printStackTrace();
				log.error("Failed to close FTP connection pool.");
			}
		}
	}

	/**
	 * 验证FtpClient对象
	 * @param ftpPooled ftp池对象
	 * @return 发送一个NOOP命令到FTP服务器，如果成功返回true，否则为false。
	 */
	@Override
	public boolean validateObject(PooledObject<FTPClient> ftpPooled) {
		try {
			FTPClient ftpClient = ftpPooled.getObject();
			if (ftpClient == null) {
				return false;
			}
			if (!ftpClient.isConnected()) {
				return false;
			}
			return ftpClient.sendNoOp();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

}
