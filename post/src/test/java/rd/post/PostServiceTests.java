package rd.post;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class PostServiceTests {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository repository;

    @Mock
    private UserIdentityFeignClient userClient;

    private Post post1;
    private Post post2;
    private Post post3;
    private Post post4;


    @BeforeEach
    void BeforeEach() {
        MockitoAnnotations.openMocks(this);

        post1 = Post.builder()
            .id("4a2e7e42-833a-46b5-9976-b369f6041df0")
            .authorEmail("batman@waynecorp.com")
            .createdAt(LocalDateTime.now())
            .content("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin pulvinar lectus et odio euismod, quis pellentesque justo mattis. "
                + "\nNullam pulvinar eros nec odio cursus, ut molestie velit tempus. Phasellus lacus erat, porta vitae eros quis, pharetra viverra dui. "
                + "\nEtiam efficitur pellentesque libero non ullamcorper. Proin eu aliquet arcu. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. "
                + "\nClass aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. ")
            .published(true)
            .build();
        
        post2 = Post.builder()
            .id("0b69eaf8-1b40-457f-93d3-b0c65759d4ff")
            .authorEmail("batman@waynecorp.com")
            .createdAt(LocalDateTime.now().minusDays(2).minusHours(4))
            .content("Praesent venenatis, lectus sit amet gravida faucibus, nibh ex sodales nunc, ut hendrerit nisi tortor quis erat. "
                + "\nMauris quis posuere libero, quis lobortis odio.")
            .published(true)
            .build();

        post3 = Post.builder()
            .id("46a93600-71ed-4160-a88c-37c398b820aa")
            .authorEmail("clark_k@dailyplanet.net")
            .createdAt(LocalDateTime.now().minusDays(1).plusMinutes(23))
            .content("Nullam efficitur tellus vel consequat lacinia. Fusce ornare odio sapien, eget egestas enim dictum ac. Praesent dictum purus a tristique pretium.")
            .published(true)
            .build();

        post4 = Post.builder()
            .id("4e7708ed-231a-4d9b-b16b-0fe26e044ce8")
            .authorEmail("clark_k@dailyplanet.net")
            .createdAt(LocalDateTime.now().minusDays(4).minusHours(3).plusMinutes(37))
            .content("Curabitur scelerisque sapien tortor, non volutpat nisl feugiat non. Quisque et aliquet dolor. Nam id fermentum tortor, eu rutrum sem. "
                + "\nSuspendisse elementum, nulla in ultrices tempor, elit sem porta sapien, nec ultricies mauris diam at ligula.")
            .published(false)
            .build();
    }


    @Test
    void testDeletePostById() {

        doNothing()
            .when(repository).deleteById(anyString());

        doThrow(new IllegalArgumentException())
            .when(repository).deleteById(null);


        assertDoesNotThrow(
            () -> postService.deletePostById(post1.getId())
        );
        assertThrows(
            IllegalArgumentException.class, 
            () -> postService.deletePostById(null)
        );
    }

    @Test
    void testGetPostById_validPostIdShouldReturnData() {

        when(repository.findById(post2.getId()))
            .thenReturn(Optional.of(post2));

        Post post = postService.getPostById("0b69eaf8-1b40-457f-93d3-b0c65759d4ff");

        assertNotNull(post);
        assertEquals(post2.getId(), post.getId());
        assertEquals(post2.getAuthorEmail(), post.getAuthorEmail());
        assertEquals(post2.getContent(), post.getContent());
        assertEquals(post2.getCreatedAt(), post.getCreatedAt());
        assertEquals(post2.isPublished(), post.isPublished());


        verify(repository, times(1)).findById(anyString());
    }

    @Test
    void testGetPostById_invalidPostIdShouldThrow() {

        when(repository.findById(
                argThat((postId) -> {
                    boolean invalidUUIDFormat = false;
                    
                    try{
                        var parsedId = UUID.fromString(postId);
                        invalidUUIDFormat = !(parsedId instanceof UUID);
                    }
                    catch(IllegalArgumentException e) {
                        invalidUUIDFormat = true;
                    }

                    return invalidUUIDFormat;
                })
            ))
            .thenThrow(new NoSuchElementException());

        assertThrows(NoSuchElementException.class, () -> postService.getPostById("notValidUUID"));

        verify(repository, times(1)).findById(anyString());
    }

    @Test
    void testGetPosts_validPageArgumentsShouldReturnPublicPosts() {
        
        int pageNumber = 0;
        int pageSize = 5;

        when(repository.findByPublishedOrderByCreatedAtDesc(true, PageRequest.of(pageNumber, pageSize)))
            .thenReturn(new PageImpl<Post>(List.of(post1, post3, post2)));

        Page<Post> posts = postService.getPosts(pageNumber, pageSize, null, null);
        
        assertNotNull(posts.getContent());
        assertTrue(posts.getContent().stream().allMatch(post -> post.isPublished() == true));
        assertTrue(posts.getContent().stream().noneMatch(post -> post.isPublished() == false));
        assertTrue(posts.getNumberOfElements() <= pageSize);
        assertTrue(posts.getNumberOfElements() <= posts.getTotalElements());
        assertEquals(pageNumber, posts.getNumber());

        verify(repository, times(1)).findByPublishedOrderByCreatedAtDesc(anyBoolean(), any());
    }

    @Test
    void testGetPosts_negativePageNumberShouldThrow() {

        Throwable throwable =
            assertThrows(
                IllegalArgumentException.class, 
                () -> postService.getPosts(-1, 5, null, null)
            );
        
        assertEquals("Page number cannot be negative.", throwable.getMessage());
    }

    @Test
    void testGetPosts_zeroPageSizeShouldThrow() {

        Throwable throwable =
            assertThrows(
                IllegalArgumentException.class, 
                () -> postService.getPosts(0, 0, null, null)
            );
        
        assertEquals("Page size should be at least one.", throwable.getMessage());
    }

    @Test
    void testGetPosts_getByOnlyAuthorShouldReturnPublicPostsByThatAuthor() {

        when(repository.findByAuthorEmailAndPublishedOrderByCreatedAtDesc(
                "clark_k@dailyplanet.net", 
                true, 
                PageRequest.of(0, 5)
            ))
            .thenReturn(new PageImpl<Post>(List.of(post3)));

        Page<Post> posts = postService.getPosts(0, 5, post3.getAuthorEmail(), null);

        assertNotNull(posts.getContent());
        assertTrue(posts.getContent().stream().allMatch(post -> post.getAuthorEmail().equals(post3.getAuthorEmail())));
        assertTrue(posts.getContent().stream().allMatch(post -> post.isPublished() == true));

        verify(repository, times(1)).findByAuthorEmailAndPublishedOrderByCreatedAtDesc(anyString(), anyBoolean(), any());
    }

    @Test
    void testGetPosts_getByAuthorAndIncludeNonPublishedShouldReturnAllPostsByThatAuthor() {

        when(repository.findByAuthorEmailOrderByCreatedAtDesc(
                "clark_k@dailyplanet.net", 
                PageRequest.of(0, 5) // I couldn't use intThat matcher for this because it has a default return value of 0 which is not allowed as pageSize.
            ))
            .thenReturn(new PageImpl<Post>(List.of(post3, post4)));

        Page<Post> posts = postService.getPosts(0, 5, post3.getAuthorEmail(), true);

        assertNotNull(posts.getContent());
        assertTrue(posts.getContent().stream().allMatch(post -> post.getAuthorEmail().equals(post3.getAuthorEmail())));
        assertFalse(posts.getContent().stream().allMatch(post -> post.isPublished() == true));
        assertFalse(posts.getContent().stream().allMatch(post -> post.isPublished() == false));
    }

    @Test
    void testSaveNewPost_validRequestShouldReturnThePersistedObject() {

        when(userClient.checkIfUserExists("batman@waynecorp.com"))
            .thenReturn(ResponseEntity.ok(true));
        
        when(repository.insert(
                any(Post.class) // I couldn't use argThat matcher for this because of ambiguity-related errors.
            ))
            .thenReturn(post2);

        NewPostDTO newPost = NewPostDTO.builder()
            .authorEmail("batman@waynecorp.com")
            .content("Praesent venenatis, lectus sit amet gravida faucibus, nibh ex sodales nunc, ut hendrerit nisi tortor quis erat. "
                + "\nMauris quis posuere libero, quis lobortis odio.")
            .published(true)
            .build();

        Post persistedPost = postService.saveNewPost(newPost);

        assertNotNull(persistedPost);
        assertEquals(newPost.getAuthorEmail(), persistedPost.getAuthorEmail());
        assertEquals(newPost.getContent(), persistedPost.getContent());
        assertEquals(newPost.getPublished(), persistedPost.isPublished());
        assertNotNull(persistedPost.getId());
        assertNotNull(persistedPost.getCreatedAt());

        verify(userClient, times(1)).checkIfUserExists(anyString());
        verify(repository, times(1)).insert(any(Post.class));
    }

    @Test
    void testSaveNewPost_notRegisteredAuthorShouldThrow() {

        when(userClient.checkIfUserExists(
                argThat((email) -> !(email.equals("batman@waynecorp.com") || email.equals("clark_k@dailyplanet.net")))
            ))
            .thenReturn(ResponseEntity.ok(false));

        NewPostDTO newPost = NewPostDTO.builder()
            .authorEmail("bruce@gmail.com")
            .content("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
            .published(true)
            .build();
        
        Throwable throwable = 
            assertThrows(IllegalArgumentException.class, () -> postService.saveNewPost(newPost));

        assertEquals("Invalid post author. bruce@gmail.com is not registered.", throwable.getMessage());
    }

    @Test
    void testSaveNewPost_nullContentShouldThrow() {

        NewPostDTO newPostNullContent = NewPostDTO.builder()
            .authorEmail("batman@waynecorp.com")
            .published(true)
            .build();

        Throwable throwableNullContent = 
            assertThrows(IllegalArgumentException.class, () -> postService.saveNewPost(newPostNullContent));
        
        assertEquals("Post content cannot be empty or null.", throwableNullContent.getMessage());
    }

    @Test
    void testSaveNewPost_nullAuthorShouldThrow() {

        NewPostDTO newPostNullAuthor = NewPostDTO.builder()
            .content("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
            .published(true)
            .build();

        Throwable throwableNullAuthor = 
            assertThrows(IllegalArgumentException.class, () -> postService.saveNewPost(newPostNullAuthor));
        
        assertEquals("Post author cannot be empty or null.", throwableNullAuthor.getMessage());
    }

    @Test
    void testSaveNewPost_nullPublishmentStatusShouldThrow() {

        NewPostDTO newPostNullPublished = NewPostDTO.builder()
            .authorEmail("batman@waynecorp.com")
            .content("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
            .build();

        Throwable throwableNullPublished = 
            assertThrows(IllegalArgumentException.class, () -> postService.saveNewPost(newPostNullPublished));
        
        assertEquals("Post publishment status can be either true or false, but not null.", throwableNullPublished.getMessage());
    }

    @Test
    void testUpdatePost_validRequestShouldUpdateAndReturn() {

        when(userClient.checkIfUserExists("batman@waynecorp.com"))
            .thenReturn(ResponseEntity.ok(true));

        when(repository.existsById("4a2e7e42-833a-46b5-9976-b369f6041df0"))
            .thenReturn(true);

        when(repository.save(
                argThat((post) -> post.getId().equals("4a2e7e42-833a-46b5-9976-b369f6041df0"))
            ))
            .thenReturn(
                Post.builder()
                    .id(post1.getId())
                    .authorEmail(post1.getAuthorEmail())
                    .createdAt(post1.getCreatedAt())
                    .content(post1.getContent())
                    .published(post1.isPublished())
                    .build()
            );

        Post updatedPost = postService.updatePost(post1);
        
        assertNotNull(updatedPost);
        assertNotEquals(post1, updatedPost);
        assertEquals(post1.getId(), updatedPost.getId());
        assertEquals(post1.getAuthorEmail(), updatedPost.getAuthorEmail());
        assertEquals(post1.getCreatedAt(), updatedPost.getCreatedAt());
        assertEquals(post1.getContent(), updatedPost.getContent());
        assertEquals(post1.isPublished(), updatedPost.isPublished());

        verify(userClient, times(1)).checkIfUserExists(anyString());
        verify(repository, times(1)).existsById(anyString());
        verify(repository, times(1)).save(any(Post.class));
    }

    @Test
    void testUpdatePost_notRegisteredAuthorShouldThrow() {

        when(userClient.checkIfUserExists(
                argThat((email) -> !(email.equals("batman@waynecorp.com") || email.equals("clark_k@dailyplanet.net")))
            ))
            .thenReturn(ResponseEntity.ok(false));

        Post postToUpdate = Post.builder()
            .id(post2.getId())
            .authorEmail("bruce@gmail.com")
            .createdAt(post2.getCreatedAt())
            .content(post2.getContent())
            .published(!post2.isPublished())
            .build();
        
        Throwable throwable = 
            assertThrows(IllegalArgumentException.class, () -> postService.updatePost(postToUpdate));

        assertEquals("Invalid post author. bruce@gmail.com is not registered.", throwable.getMessage());
    }

    @Test
    void testUpdatePost_nonExistentPostIdShouldThrow() {

        when(userClient.checkIfUserExists("clark_k@dailyplanet.net"))
            .thenReturn(ResponseEntity.ok(true));

        when(repository.existsById(
                argThat((id) -> 
                    !(id.equals(post1.getId()) || id.equals(post2.getId()) || id.equals(post3.getId()))
                )
            ))
            .thenThrow(new NoSuchElementException("Could not find a post with id " + post4.getId() + "."));

        Throwable throwableNonExistentId =
            assertThrows(NoSuchElementException.class, () -> postService.updatePost(post4));

        assertEquals("Could not find a post with id " + post4.getId() + ".", throwableNonExistentId.getMessage());
    }

    @Test
    void testUpdatedPost_nullPostIdShouldThrow() {

        when(userClient.checkIfUserExists("clark_k@dailyplanet.net"))
            .thenReturn(ResponseEntity.ok(true));

        when(repository.existsById(null))
            .thenThrow(IllegalArgumentException.class);

        Post postToUpdate = Post.builder()
            .authorEmail("clark_k@dailyplanet.net")
            .createdAt(post3.getCreatedAt())
            .content(post3.getContent())
            .published(post3.isPublished())
            .build();

        assertThrows(IllegalArgumentException.class, () -> postService.updatePost(postToUpdate));
    }

    @Test
    void testUpdatedPost_nullAuthorShouldThrow() {

        Post postToUpdate = Post.builder()
            .id(post3.getId())
            .createdAt(post3.getCreatedAt())
            .content(post3.getContent())
            .published(post3.isPublished())
            .build();
        
        Throwable throwableNullAuthor = 
            assertThrows(IllegalArgumentException.class, () -> postService.updatePost(postToUpdate));

        assertEquals("Post author cannot be empty or null.", throwableNullAuthor.getMessage());
    }

    @Test
    void testUpdatedPost_futureCreationDateShouldThrow() {

        Post postToUpdate = Post.builder()
            .id(post3.getId())
            .authorEmail(post3.getAuthorEmail())
            .createdAt(LocalDateTime.now().plusDays(2))
            .content(post3.getContent())
            .published(post3.isPublished())
            .build();
        
        Throwable throwableInvalidDate = 
            assertThrows(IllegalArgumentException.class, () -> postService.updatePost(postToUpdate));

        assertEquals("Post creation time cannot be null or in the future.", throwableInvalidDate.getMessage());
    }

    @Test
    void testUpdatedPost_emptyContentShouldThrow() {

        Post postToUpdate = Post.builder()
            .id(post3.getId())
            .authorEmail(post3.getAuthorEmail())
            .createdAt(post3.getCreatedAt())
            .content("")
            .published(post3.isPublished())
            .build();
        
        Throwable throwableEmptyContent = 
            assertThrows(IllegalArgumentException.class, () -> postService.updatePost(postToUpdate));

        assertEquals("Post content cannot be empty or null.", throwableEmptyContent.getMessage());
    }
    
}
