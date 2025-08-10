package com.mosin.email.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mosin.email.dto.EmailRequestDto;
import com.mosin.email.service.EmailGeneratorService;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "*")
public class EmailGeneratorController {

	@Autowired
	EmailGeneratorService emailGeneratorService;
	
	@GetMapping("/test")
	public String test() {
		return "Sb sahi h";
	}
	
	@PostMapping("/generate")
	public ResponseEntity<Object> generateEmail(@RequestBody EmailRequestDto emailRequestDto) {
		return emailGeneratorService.generateEmailContent(emailRequestDto);
	}
}
