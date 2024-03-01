package rd.post;

import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {
    
    private final PostService service;


    @GetMapping("/post/get/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable(name="id") String id) {

        var post = service.getPostById(id);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/post/get")
    public Page<Post> getMethodName(
            @RequestParam int pageNumber,
            @RequestParam int pageSize,
            @RequestParam(required = false) String authorEmail,
            @RequestParam(required = false) Boolean includeNonPublished
        ) {

        var pages = service.getPosts(pageNumber, pageSize, authorEmail, includeNonPublished);
        return pages;
    }
    

    @PostMapping("/post/new")
    public ResponseEntity<Post> saveNewPost(@RequestBody NewPostDTO newPost) {
        
        var persistedPost = service.saveNewPost(newPost);
        return ResponseEntity.ok(persistedPost);
    }

    @PutMapping("/post/update")
    public ResponseEntity<Post> updatePost(@RequestBody Post post) {
        
        var updatedPost = service.updatePost(post);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/post/delete/{id}")
    public ResponseEntity<Void> deletePostById(@PathVariable(name="id") String id) {

        service.deletePostById(id);
        return ResponseEntity.accepted().build();
    }


}
