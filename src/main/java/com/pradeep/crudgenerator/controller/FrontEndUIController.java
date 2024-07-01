package com.pradeep.crudgenerator.controller;


import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@Tag(name = "1.Front End Controller")
public class FrontEndUIController {
	
	@RequestMapping("/")
	public String root() {
		return "layouts/layout";
	}

	@RequestMapping("/page/public/{pageId}")
	public String publicPages(@PathVariable("pageId") @NotBlank(message = "pageId is mandatory") String pageId) {
		return "page/public/" + pageId;
	}

}
