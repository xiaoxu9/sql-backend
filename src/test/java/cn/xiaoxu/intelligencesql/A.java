package cn.xiaoxu.intelligencesql;

import cn.xiaoxu.intelligencesql.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

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
}
