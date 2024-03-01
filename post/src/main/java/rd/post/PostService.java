package rd.post;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {
    
    private final PostRepository repository;


    public Page<Post> getPosts(int pageNumber, int pageSize, String authorEmail, Boolean includeNonPublished) {

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

        var post = Post.builder()
                        .authorEmail(newPost.getAuthorEmail())
                        .content(newPost.getContent())
                        .published(newPost.getPublished())
                        .createdAt(LocalDateTime.now(ZoneId.of("UTC+1")))
                        .build();

        return repository.insert(post);
    }

    public Post updatePost(Post post) {

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

}
