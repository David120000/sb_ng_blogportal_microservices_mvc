package rd.post;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import rd.post.model.NewPostDTO;
import rd.post.model.Post;

@Service
@RequiredArgsConstructor
public class PostService {
    
    private final PostRepository repository;
    private final UserIdentityFeignClient userClient;


    public Page<Post> getPosts(int pageNumber, int pageSize, String authorEmail, Boolean includeNonPublished) {

        if(pageNumber < 0) throw new IllegalArgumentException("Page number cannot be negative.");
        if(pageSize < 1) throw new IllegalArgumentException("Page size should be at least one.");

        Page<Post> pages = null;

        if(authorEmail != null && !authorEmail.isEmpty()) {
            
            if(includeNonPublished != null && includeNonPublished) {
                
                pages = this.getAllPostsByAuthor(authorEmail, pageNumber, pageSize);
            }
            else {
                pages = this.getPublicPostsByAuthor(authorEmail, pageNumber, pageSize);
            }
        }
        else {
            pages = this.getPublicPosts(pageNumber, pageSize);
        }

        return pages;
    }

    public Post getPostById(String id) {    
        return repository.findById(id)
            .orElseThrow();
    }

    public Post saveNewPost(NewPostDTO newPost) {

        this.validateNewPostDTO(newPost);

        var authorValid = userClient
            .checkIfUserExists(newPost.getAuthorEmail())
            .getBody();

        if(!authorValid) {
            throw new IllegalArgumentException("Invalid post author. " + newPost.getAuthorEmail() + " is not registered.");
        }

        var post = Post.builder()
                        .authorEmail(newPost.getAuthorEmail())
                        .content(newPost.getContent())
                        .published(newPost.getPublished())
                        .createdAt(LocalDateTime.now(ZoneId.of("UTC+1")))
                        .build();

        return repository.insert(post);
    }

    public Post updatePost(Post post) {

        this.validePostToUpdate(post);

        var authorValid = userClient
            .checkIfUserExists(post.getAuthorEmail())
            .getBody();

        if(!authorValid) {
            throw new IllegalArgumentException("Invalid post author. " + post.getAuthorEmail() + " is not registered.");
        }

        Post updatedPost = null;

        if(repository.existsById(post.getId())) {
            
            updatedPost = repository.save(post);
        }
        else {
            throw new NoSuchElementException("Could not find a post with id " + post.getId() + ".");
        }

        return updatedPost;
    }

    public void deletePostById(String id) {
        repository.deleteById(id);
    }

    private Page<Post> getPublicPosts(int pageNumber, int pageSize) {

        return repository.findByPublishedOrderByCreatedAtDesc(true, PageRequest.of(pageNumber, pageSize));
    }

    private Page<Post> getPublicPostsByAuthor(String authorEmail, int pageNumber, int pageSize) {

        return repository.findByAuthorEmailAndPublishedOrderByCreatedAtDesc(authorEmail, true, PageRequest.of(pageNumber, pageSize));
    }

    private Page<Post> getAllPostsByAuthor(String authorEmail, int pageNumber, int pageSize) {

        return repository.findByAuthorEmailOrderByCreatedAtDesc(authorEmail, PageRequest.of(pageNumber, pageSize));
    }

    private void validateNewPostDTO(NewPostDTO newPost) {

        if(newPost.getContent() == null || newPost.getContent().isEmpty()) 
            throw new IllegalArgumentException("Post content cannot be empty or null.");

        if(newPost.getAuthorEmail() == null || newPost.getAuthorEmail().isEmpty()) 
            throw new IllegalArgumentException("Post author cannot be empty or null.");

        if(newPost.getPublished() == null)
            throw new IllegalArgumentException("Post publishment status can be either true or false, but not null.");
    }

    private void validePostToUpdate(Post postToUpdate) {

        if(postToUpdate.getAuthorEmail() == null || postToUpdate.getAuthorEmail().isBlank())
           throw new IllegalArgumentException("Post author cannot be empty or null.");

        if(postToUpdate.getCreatedAt() == null || postToUpdate.getCreatedAt().isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("Post creation time cannot be null or in the future.");

        if(postToUpdate.getContent() == null || postToUpdate.getContent().isEmpty())
            throw new IllegalArgumentException("Post content cannot be empty or null.");
    }

}
