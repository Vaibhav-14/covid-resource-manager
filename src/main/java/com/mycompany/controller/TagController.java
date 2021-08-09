package com.mycompany.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mycompany.entity.Tag;
import com.mycompany.service.TagService;

@Controller
@RequestMapping("/tag")
public class TagController {
	
	@Autowired
	private TagService tagService;
	
	@GetMapping("/search")
	@ResponseBody
	public List<String> getTagsByKeyword(@RequestParam String term) {
		return tagService.searchTagsByKeyWord(term);
	}
	
	@GetMapping("/create")
	public String addTags(Model model) {
		model.addAttribute("tag", new Tag());
		return "create-tag";
	}
	
	@PostMapping("/create")
	public String saveTags(@ModelAttribute("tag") Tag tag) {
		tagService.addTags(tag);
		return "redirect:/";
	}
	
}
