package com.mycompany.controller;

import java.security.ProviderException;
import java.util.LinkedList;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.mycompany.entity.Comment;
import com.mycompany.entity.Post;
import com.mycompany.entity.User;

import com.mycompany.exception.IncorrectUserException;

import com.mycompany.service.PostService;
import com.mycompany.service.UserService;


@Controller
@RequestMapping("/post")
public class PostController {
	
	@Autowired
	private PostService postService;
	
	@Autowired
	private UserService userService;
		
	@GetMapping("/create")
	public String createPost(Model model) {
		Post post = new Post();
		model.addAttribute("post", post);
		return "create-post";
	}
	
	
	@PostMapping("/create")
	public String savePost(@ModelAttribute("post") Post post) {
		postService.addPost(post);
		return "redirect:/home";
	}
	
	@PostMapping("/updatepost")
	public String updatePost(@RequestParam(name = "id") int id, Model model) throws ProviderException {
		
		Post post = postService.getPostById(id);
		model.addAttribute("post", post);
		return "update-post";
	}
	
	@PostMapping("/update") 
	public String updatePost(@ModelAttribute("post") Post post, Model model) {
		
		postService.updatePost(post);
		return "redirect:/home";
	}
	
	
	@RequestMapping(value="/searchresult", method = {RequestMethod.GET, RequestMethod.POST})
	public String searchPostResult(Model model, @RequestParam(name = "searchentry") String searchEntry) {
		User user = null;
		String[] searchBy = searchEntry.split(" ");
		if (searchEntry.startsWith("#")) {
			model.addAttribute("tag", searchBy[0]);
		}
		else {
			user = userService.getUser(searchBy[0].substring(1));
			model.addAttribute("tag", null);
		}
		if( userService.getUser(null) == null) {
			model.addAttribute("isLoggedIn", false);
		}
		else {
			model.addAttribute("isLoggedIn", true);
		}
		model.addAttribute("comment", new Comment());
		model.addAttribute("username", searchBy[0]);
		model.addAttribute("IsUsername", null);
		model.addAttribute("user", user);
		

		Set<Post> searchList = postService.getPostOnSearch(searchBy[0].substring(1));
		model.addAttribute("posts", searchList);
		return "profile";	
	}
	
	@PostMapping("/delete")
	public String deletePosts(@RequestParam(name = "id") int id) throws IncorrectUserException {
		postService.deletePost(id);
		return "redirect:/";
	}
	
	@GetMapping("/{id}")
	public String showPost(@PathVariable int id, Model model) {
		User user = userService.getUser(null);
		String username;
		if(user == null) {
			username = null;
		}
		else {
			username = user.getUsername();
		}
		model.addAttribute("username", username);
		model.addAttribute("post", postService.getPostById(id));
		model.addAttribute("comment", new Comment());
		return "post";
	}
	
	@PostMapping("/report")
	public String reportPost(@RequestParam(name = "id") int id ) {
		postService.reportPost(id);
		return "redirect:/";
	}


}
