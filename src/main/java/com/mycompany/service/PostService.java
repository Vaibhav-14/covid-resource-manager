package com.mycompany.service;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mycompany.dao.IPostFunctionDAO;
import com.mycompany.dao.IRoleFunctionDAO;
import com.mycompany.entity.Post;
import com.mycompany.entity.Role;
import com.mycompany.entity.Tag;
import com.mycompany.entity.User;
import com.mycompany.exception.IncorrectUserException;

@Transactional
@Service("postService")
public class PostService {
	
	@Autowired
	private IPostFunctionDAO postDao;
	
	@Autowired
	private IRoleFunctionDAO roleDao;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private TagService tagService;
	
	
	@Autowired
	private NotificationService notificationService;

	public IPostFunctionDAO getDao() {
		return postDao;
	}

	public void setDao(IPostFunctionDAO dao) {
		this.postDao = dao;
	}
	
	public void addPost(Post post) {
		Set<User> mentionedUsers = userService.getUsersFromString(post.getMessage());
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		User loggedInUser = userService.getUserFromUsername(auth.getName());
		post.setUser(loggedInUser);
		post.setDateTime(new Timestamp(System.currentTimeMillis()));
		addTagsToPost(post);
		postDao.save(post);
		
		String activityType = "@" + loggedInUser.getUsername() + " mentioned you in a post";

		notificationService.saveNotification(loggedInUser, activityType, "post", "post/" + post.getId(), mentionedUsers);
	}

	// retrieve tags from tagStr
	private void addTagsToPost(Post post) {
		Set<Tag> tags = new HashSet<Tag>();
		Iterable<Tag> db_tags = tagService.getAllTags();
		for (String tagString : post.getTagStr().replaceAll("#", "").split(" ")) {
			int flag = 0;
			for (Tag tag : db_tags) {
				if(tag.getName().equals(tagString)) {
					flag = 1;
					tags.add(tag);
					break;
				}
			}
			if(flag == 0) {
				Tag tag = new Tag();
				tag.setName(tagString);
				Set<Post> tag_post = new HashSet<Post>();
				tag_post.add(post);
				tags.add(tag);
			}
		}
		post.setTags(tags);
	}

	public Post getPostById(int id) {
		Post post = postDao.findById(id).get();

		StringBuffer str = new StringBuffer();
		for (Tag tag : post.getTags()) {
			str.append("#" + tag.getName() + " ");
		}
		post.setTagStr(str.toString()); 
		return post;
	}

	public void updatePost(Post post) throws IncorrectUserException {
		Post oldPost = getPostById(post.getId());
		post.setDateTime(oldPost.getDateTime());
		addTagsToPost(post);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		User loggedInUser = userService.getUserFromUsername(auth.getName());
		if(post.getUser().getId() == loggedInUser.getId())
			postDao.save(post);
		else
			throw new IncorrectUserException("We are sorry but you do not have that permission.");

	}
	
	public void deletePost(int id) throws IncorrectUserException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = userService.getUserFromUsername(auth.getName());
		Role role = roleDao.findByName("ADMIN");
		boolean isAdmin = false;
		Post post = postDao.findPostById(id);
		if(post.getUser().getId() == user.getId() || (isAdmin = user.getRoles().contains(role))) {
			postDao.deleteById(id);
			if (isAdmin) {
				String activityType = "Your post violets the Covid Resource Manager Policies. "
						+ "So It has been removed.";

				notificationService.saveNotification(null, activityType, "post", 
									"post/" + id, post.getUser());
			}
		}
		else
			throw new IncorrectUserException("We are sorry but you do not have that permission.");
	}
	
	public List<Post> getAllPost(){
		return postDao.findAllByOrderByDateTimeDesc();
	}
	
	public List<Post> findPostByUsername(String username) {
		return postDao.findPostByUser(userService.getUserFromUsername(username));
	}
	
	public Set<Post> getPostOnSearch(String searchEntry) {
		Set<Post> searchList = new HashSet<>();
		
		//finding posts by username
		searchList.addAll(findPostByUsername(searchEntry));
				
		List<Tag> associatedTags = tagService.getAllTagsByName(searchEntry);
		
		for(Tag currentTag: associatedTags) {
			Set<Post> postsWithcurrentTag = currentTag.getPosts();
			searchList.addAll(postsWithcurrentTag);		
		}

	    return searchList;
	}
	
	
}
