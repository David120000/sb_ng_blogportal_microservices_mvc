package rd.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import rd.post.model.Post;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {

    Page<Post> findByPublishedOrderByCreatedAtDesc(boolean published, Pageable pageable);
    Page<Post> findByAuthorEmailOrderByCreatedAtDesc(String authorEmail, Pageable pageable);
    Page<Post> findByAuthorEmailAndPublishedOrderByCreatedAtDesc(String authorEmail, boolean published, Pageable pageable);

 }
