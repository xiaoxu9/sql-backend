package cn.xiaoxu.intelligencesql.core.utils;

import cn.xiaoxu.intelligencesql.core.model.enums.MockParamsRandomTypeEnum;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import net.datafaker.Faker;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

/**
 * 随机数生成工具
 *
 * @Author: xiaoxu   https://github.com/xiaoxu9
 */
public class FakerUtils {

	private static final Faker ZH_FAKER = new Faker(new Locale("zh-CN"));

	private static final Faker EN_FAKER = new Faker();

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public static String getRandomValue(MockParamsRandomTypeEnum randomTypeEnum) {
		// 创建一个随机字符串作为默认值
		String defaultValue = RandomStringUtils.randomAlphanumeric(2, 6);
		if (randomTypeEnum == null) {
			return defaultValue;
		}
		switch (randomTypeEnum) {
			case NAME: // 返回人名
				return ZH_FAKER.name().name();
			case CITY:
				return ZH_FAKER.address().city();
			case EMAIL:
				return EN_FAKER.internet().emailAddress();
			case URL:
				return EN_FAKER.internet().url();
			case IP:
				return ZH_FAKER.internet().ipV4Address();
			case INTEGER:
				return String.valueOf(ZH_FAKER.number().randomNumber());
			case DECIMAL:
				return String.valueOf(RandomUtils.nextFloat(0, 100000));
			case UNIVERSITY:
				return ZH_FAKER.university().name();
			case DATE:
				return EN_FAKER.date()
						.between(Timestamp.valueOf("2022-01-01 00:00:00"), Timestamp.valueOf("2023-01-01 00:00:00"))
						.toLocalDateTime().format(DATE_TIME_FORMATTER);  // 格式化为本地时间格式
			case TIMESTAMP:
				return String.valueOf(EN_FAKER.date()
						.between(Timestamp.valueOf("2022-01-01 00:00:00"), Timestamp.valueOf("2023-01-01 00:00:00"))
						.getTime());
			case PHONE:
				return ZH_FAKER.phoneNumber().cellPhone();
			default:
				return defaultValue;
		}
	}


	public static void main(String[] args) {
		getRandomValue(null);
	}

}
