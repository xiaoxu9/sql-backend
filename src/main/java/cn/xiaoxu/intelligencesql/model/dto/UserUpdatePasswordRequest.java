package cn.xiaoxu.intelligencesql.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdatePasswordRequest implements Serializable {

	private Long id;

	private String userAccount;

	private String oldPassword;

	private String userPassword;

	private String checkPassword;

	private static final long serialVersionUID = 1L;
}
