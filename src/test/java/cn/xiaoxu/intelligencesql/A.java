package cn.xiaoxu.intelligencesql;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.xiaoxu.intelligencesql.model.entity.User;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class A {
	@Test
	public void test1(){
		List<User> userList = new ArrayList<>();
		System.out.println(userList);
		userList.forEach(i -> {
			System.out.println(i + "111111");
		});
	}

	@Test
	public void test2(){
		// 通过模板配置 获取文件模板
		cn.hutool.core.io.resource.Resource resource = ResourceUtil.getResourceObj("templates/typescript_type.ftl");
		InputStream is = resource.getStream();
		File file = new File(File.separator + "tmp" + File.separator, "test.txt");
		try {
			FileUtils.copyInputStreamToFile(is,file);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			file.delete();
		}
	}
}
