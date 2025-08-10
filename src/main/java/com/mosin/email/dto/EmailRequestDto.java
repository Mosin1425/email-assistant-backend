package com.mosin.email.dto;

import lombok.Data;

@Data
public class EmailRequestDto {

	private String emailContent;
	private String tone;
}
