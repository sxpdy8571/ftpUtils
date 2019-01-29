package com.secenetec.ftp.test;

import com.scenetec.ftp.FtpClientApplication;
import com.scenetec.ftp.template.FtpOnceTemplate;
import com.scenetec.ftp.template.FtpPoolTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author shendunyuan@scenetec.com
 * @date 2018/12/20
 */
@SpringBootTest(classes = FtpClientApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class FtpClientPoolTest {

	@Autowired
	private FtpPoolTemplate ftpPoolTemplate;
	@Resource
	private FtpOnceTemplate ftpOnceTemplate;

	private String localPath = "/Users/sdy/test/isv/platform.cer";
	private String localDownPath = "/Users/sdy/test/isv/2019/111test.cer";
	private String remotePath = "/2019/111.cer";

	@Test
	public void upload() {
		boolean bool = ftpPoolTemplate.uploadFile(localPath, remotePath);
		if (bool) {
			System.out.println("success");
		} else {
			System.out.println("failed");
		}
	}

	@Test
	public void up() {
		boolean bool = ftpOnceTemplate.uploadFile(localPath, remotePath);
		if (bool) {
			System.out.println("success");
		} else {
			System.out.println("failed");
		}
	}

	@Test
	public void uploadFile() {
		File file = new File(localPath);
		boolean bool = ftpPoolTemplate.uploadFile(file, remotePath);
		if (bool) {
			System.out.println("success");
		} else {
			System.out.println("failed");
		}
	}

	@Test
	public void downloadFile() {
		boolean bool = ftpPoolTemplate.downloadFile(remotePath, localDownPath);
		if (bool) {
			System.out.println("success");
		} else {
			System.out.println("failed");
		}
	}

	@Test
	public void deleteFile() {
		boolean bool = ftpPoolTemplate.deleteFile(remotePath);
		if (bool) {
			System.out.println("success");
		} else {
			System.out.println("failed");
		}
	}
}
