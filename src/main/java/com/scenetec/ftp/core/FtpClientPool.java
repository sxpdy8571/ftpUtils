package com.scenetec.ftp.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.BaseObjectPool;
import org.springframework.util.ObjectUtils;

import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author shendunyuan@scenetec.com
 * @date 2018/12/19
 */
@Slf4j
public class FtpClientPool extends BaseObjectPool<FTPClient> {

	private static final int DEFAULT_POOL_SIZE = 5;
	private final BlockingQueue<FTPClient> pool;
	private final FtpClientFactory factory;


	/**
	 * 初始化连接池，需要注入一个工厂来提供FTPClient实例
	 * @param factory
	 */
	public FtpClientPool(FtpClientFactory factory) {
		this(DEFAULT_POOL_SIZE, factory);
	}

	public FtpClientPool(int poolSize, FtpClientFactory factory) {
		this.factory = factory;
		this.pool = new ArrayBlockingQueue<>(poolSize * 2);
		initPool(poolSize);
	}

	/**
	 * 初始化连接池，需要注入一个工厂来提供FTPClient实例
	 * @param maxPoolSize
	 */
	private void initPool(int maxPoolSize) {
		try {
			for (int i = 0; i < maxPoolSize; i++) {
				addObject();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to initialize FTP thread pool.");
		}
	}

	/**
	 * 客户端从池中借出一个对象
	 * @return
	 * @throws Exception
	 * @throws NoSuchElementException
	 * @throws IllegalStateException
	 */
	@Override
	public FTPClient borrowObject() throws Exception, NoSuchElementException, IllegalStateException {
		FTPClient client = pool.take();
		if (ObjectUtils.isEmpty(client)) {
			// 创建新的连接
			client = factory.create();
		}
		// 验证对象是否有效
		else if (!factory.validateObject(factory.wrap(client))) {
			// 对无效的对象进行处理
			invalidateObject(client);
			// 创建新的对象
			client = factory.create();
		}
		// 返回ftp对象
		return client;
	}

	/**
	 * 返还对象到连接池中
	 * @param client
	 * @throws Exception
	 */
	@Override
	public void returnObject(FTPClient client) {
		try {
			long timeout = 3L;
			if (client != null) {
				if (client.isConnected()) {
					if (pool.size() < DEFAULT_POOL_SIZE) {
						// 添加回队列
						if (!pool.offer(client, timeout, TimeUnit.SECONDS)) {
							factory.destroyObject(client);
						}
					} else {
						factory.destroyObject(client);
					}
				} else {
					factory.destroyObject(client);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("Failed to return FTP connection object.");
		}
	}

	/**
	 * 移除无效的对象
	 * @param client
	 * @throws Exception
	 */
	@Override
	public void invalidateObject(FTPClient client) {
		try {
			// 移除无效对象
			pool.remove(client);
			// 注销对象
			factory.destroyObject(client);
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("Failed to remove invalid FTP object.");
		}
	}

	/**
	 * 增加一个新的链接，超时失效
	 * @throws Exception
	 * @throws IllegalStateException
	 * @throws UnsupportedOperationException
	 */
	@Override
	public void addObject() throws Exception, IllegalStateException, UnsupportedOperationException {
		pool.offer(factory.create(), 3, TimeUnit.SECONDS);
	}

	@Override
	public void close() {
		try {
			while (pool.iterator().hasNext()) {
				FTPClient client = pool.take();
				factory.destroyObject(client);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error("Failed to close FTP object.");
		}
	}
}
