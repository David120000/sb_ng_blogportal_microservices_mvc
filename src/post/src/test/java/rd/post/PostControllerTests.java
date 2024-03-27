package rd.post;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import rd.post.model.NewPostDTO;
import rd.post.model.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.booleanThat;
import static org.mockito.ArgumentMatchers.intThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasItem;

@WebMvcTest(PostController.class)
public class PostControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    private Post post1;
    private Post post2;
    private Post post3;


    @BeforeEach
    void BeforeEach() {

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
            .published(false)
            .build();

        post3 = Post.builder()
            .id("46a93600-71ed-4160-a88c-37c398b820aa")
            .authorEmail("clark_k@dailyplanet.net")
            .createdAt(LocalDateTime.now().minusDays(1).plusMinutes(23))
            .content("Nullam efficitur tellus vel consequat lacinia. Fusce ornare odio sapien, eget egestas enim dictum ac. Praesent dictum purus a tristique pretium.")
            .published(true)
            .build();
    }


    @Test
    void testDeletePostById_validIdShouldBeAccepted() throws Exception {

        doNothing().when(postService).deletePostById(anyString());

        mockMvc.perform(
                delete("/api/post/delete/" + post1.getId())
            )
            .andDo(print())
            .andExpect(status().isAccepted());

        verify(postService, times(1)).deletePostById(anyString());
    }

    @Test
    void testGetPosts_validPageNumberAndPageSizeShouldReturnPublicPostsPage() throws Exception {

        when(postService.getPosts(
                intThat(pageNumber -> pageNumber >= 0), 
                intThat(pageSize -> pageSize > 0), 
                isNull(), 
                isNull()
            ))
            .thenReturn(new PageImpl<Post>(List.of(post1, post3)));

        mockMvc.perform(
                get("/api/post/get")
                    .param("pageNumber", "0")
                    .param("pageSize", "5")
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").exists())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].id").value(post1.getId()))
            .andExpect(jsonPath("$.content[1].id").value(post3.getId()))
            .andExpect(jsonPath("$.numberOfElements", lessThanOrEqualTo(5)))
            .andExpect(jsonPath("$.number", equalTo(0)))
            .andExpect(jsonPath("$.totalElements", equalTo(2)));

        verify(postService, times(1)).getPosts(anyInt(), anyInt(), isNull(), isNull());
    }

    @Test
    void testGetPosts_validAuthorAndPageAttributesShouldReturnPublicPostsPageByAuthor() throws Exception {

        when(postService.getPosts(
                intThat(pageNumber -> pageNumber >= 0), 
                intThat(pageSize -> pageSize > 0), 
                argThat(author -> author.equals("batman@waynecorp.com")), 
                isNull()
            ))
            .thenReturn(new PageImpl<Post>(List.of(post1)));

        mockMvc.perform(
                get("/api/post/get")
                    .param("pageNumber", "0")
                    .param("pageSize", "5")
                    .param("authorEmail", "batman@waynecorp.com")
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").exists())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[*].authorEmail", everyItem(is("batman@waynecorp.com"))))
            .andExpect(jsonPath("$.numberOfElements", lessThanOrEqualTo(5)))
            .andExpect(jsonPath("$.number", equalTo(0)));

        verify(postService, times(1)).getPosts(anyInt(), anyInt(), anyString(), isNull());
    }

    @Test
    void testGetPosts_validAuthorAndPublishedAndPageAttributesShouldReturnAllPostsPageByAuthor() throws Exception {

        when(postService.getPosts(
                intThat(pageNumber -> pageNumber >= 0), 
                intThat(pageSize -> pageSize > 0), 
                argThat(author -> author.equals("batman@waynecorp.com")), 
                booleanThat(published -> published == true)
            ))
            .thenReturn(new PageImpl<Post>(List.of(post1, post2)));

        mockMvc.perform(
                get("/api/post/get")
                    .param("pageNumber", "0")
                    .param("pageSize", "5")
                    .param("authorEmail", "batman@waynecorp.com")
                    .param("includeNonPublished", "true")
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").exists())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[*].authorEmail", everyItem(is("batman@waynecorp.com"))))
            .andExpect(jsonPath("$.content[*].published", hasItem(false)))  // non-published posts are included
            .andExpect(jsonPath("$.numberOfElements", lessThanOrEqualTo(5)))
            .andExpect(jsonPath("$.number", equalTo(0)));

        verify(postService, times(1)).getPosts(anyInt(), anyInt(), anyString(), anyBoolean());
    }

    @Test
    void testGetPostById_validIdShouldReturnPost() throws Exception {

        when(postService.getPostById(
                argThat(id -> id.equals(post1.getId()))
            ))
            .thenReturn(post1);

        mockMvc.perform(
                get("/api/post/get/" + "4a2e7e42-833a-46b5-9976-b369f6041df0")
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(post1.getId()))
            .andExpect(jsonPath("$.authorEmail").value(post1.getAuthorEmail()))
            .andExpect(jsonPath("$.content").value(post1.getContent()))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.published").value(post1.isPublished()));

        verify(postService, times(1)).getPostById(anyString());
    }

    @Test
    void testGetPostById_invalidIdShouldReturnBadRequestError() throws Exception {

        when(postService.getPostById(
                argThat(id -> {
                    boolean invalidUUIDFormat = false;
                    
                    try{
                        var parsedId = UUID.fromString(id);
                        invalidUUIDFormat = !(parsedId instanceof UUID);
                    }
                    catch(IllegalArgumentException e) {
                        invalidUUIDFormat = true;
                    }

                    return invalidUUIDFormat;
                })
            ))
            .thenThrow(new NoSuchElementException());

        mockMvc.perform(
                get("/api/post/get/" + "notValidUUID")
            )
            .andDo(print())
            .andExpect(status().is4xxClientError());
            // the ControllerAdvice catches the Exception and returns 400 BAD REQUEST instead of a 5xx server error

        verify(postService, times(1)).getPostById(anyString());
    }

    @Test
    void testSaveNewPost_validRequestShouldSaveAndReturnPersistedPost() throws Exception {

        when(postService.saveNewPost(
                argThat(newPostDTO -> 
                    (newPostDTO.getAuthorEmail().equals("batman@waynecorp.com")
                    && newPostDTO.getContent().equals("Quisque mi velit, commodo sit amet lectus vitae, tincidunt convallis lorem.")
                    && newPostDTO.getPublished() == true)
                )
            ))
            .thenReturn(Post.builder()
                .id(UUID.randomUUID().toString())
                .authorEmail("batman@waynecorp.com")
                .createdAt(LocalDateTime.now())
                .content("Quisque mi velit, commodo sit amet lectus vitae, tincidunt convallis lorem.")
                .published(true)
                .build()
            );

        mockMvc.perform(
                post("/api/post/new")
                    .accept(MediaType.APPLICATION_JSON)
                    .content("{\"authorEmail\":\"batman@waynecorp.com\",\"content\":\"Quisque mi velit, commodo sit amet lectus vitae, tincidunt convallis lorem.\",\"published\":true}")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.authorEmail").value("batman@waynecorp.com"))
            .andExpect(jsonPath("$.content").value("Quisque mi velit, commodo sit amet lectus vitae, tincidunt convallis lorem."))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.published").value(true));

        verify(postService, times(1)).saveNewPost(any(NewPostDTO.class));
    }

    @Test
    void testSaveNewPost_notRegisteredAuthorShouldReturnBadRequestError() throws Exception {

        when(postService.saveNewPost(
                argThat(newPostDTO -> 
                    !(newPostDTO.getAuthorEmail().equals("batman@waynecorp.com") ||  newPostDTO.getAuthorEmail().equals("clark_k@dailyplanet.net"))
                )
            ))
            .thenThrow(new IllegalArgumentException("Invalid post author. bruce@gmail.com is not registered."));

        mockMvc.perform(
                post("/api/post/new")
                    .accept(MediaType.APPLICATION_JSON)
                    .content("{\"authorEmail\":\"bruce@gmail.com\",\"content\":\"Quisque mi velit, commodo sit amet lectus vitae, tincidunt convallis lorem.\",\"published\":true}")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().is4xxClientError())
            .andExpect(content().string("Invalid post author. bruce@gmail.com is not registered."));
            // the ControllerAdvice catches the Exception and returns 400 BAD REQUEST instead of a 5xx server error

        verify(postService, times(1)).saveNewPost(any(NewPostDTO.class));
    }

    @Test
    void testSaveNewPost_nullContentShouldReturnBadRequestError() throws Exception {

        when(postService.saveNewPost(
                argThat(newPostDTO -> 
                    (newPostDTO.getAuthorEmail() == null ||  newPostDTO.getContent() == null || newPostDTO.getPublished() == null)
                )
            ))
            .thenThrow(IllegalArgumentException.class);

        mockMvc.perform(
                post("/api/post/new")
                    .accept(MediaType.APPLICATION_JSON)
                    .content("{\"authorEmail\":\"batman@waynecorp.com\",\"published\":true}")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().is4xxClientError());
            // the ControllerAdvice catches the Exception and returns 400 BAD REQUEST instead of a 5xx server error

        verify(postService, times(1)).saveNewPost(any(NewPostDTO.class));
    }


    @Test
    void testUpdatePost_validRequestShouldBeSavedAndTheUpdatedPostShouldBeReturned() throws Exception {

        when(postService.updatePost(
                argThat(postToUpdate -> 
                    (postToUpdate.getAuthorEmail().equals(post1.getAuthorEmail())
                    && postToUpdate.getId().equals(post1.getId()))
                )
            ))
            .thenReturn(Post.builder()
                .id(post1.getId())
                .authorEmail(post1.getAuthorEmail())
                .createdAt(post1.getCreatedAt())
                .content("Modified content.")
                .published(post1.isPublished())
                .build()
            );

        mockMvc.perform(
                put("/api/post/update")
                    .accept(MediaType.APPLICATION_JSON)
                    .content("{\"content\":\"Modified content.\",\"id\":\"4a2e7e42-833a-46b5-9976-b369f6041df0\",\"authorEmail\":\"batman@waynecorp.com\",\"published\":true,\"createdAt\":\"" + post1.getCreatedAt().toString() + "\"}")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(post1.getId()))
            .andExpect(jsonPath("$.authorEmail").value(post1.getAuthorEmail()))
            .andExpect(jsonPath("$.content").value("Modified content."))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.published").value(post1.isPublished()));

        verify(postService, times(1)).updatePost(any(Post.class));
    }

    @Test
    void testUpdatePost_notRegisteredUserShouldReturnBadRequestError() throws Exception {

        when(postService.updatePost(
                argThat(postToUpdate -> 
                    !(postToUpdate.getAuthorEmail().equals("batman@waynecorp.com") && postToUpdate.getAuthorEmail().equals("clark_k@dailyplanet.net"))
                )
            ))
            .thenThrow(new IllegalArgumentException("Invalid post author. bruce@gmail.com is not registered."));
          
        mockMvc.perform(
            put("/api/post/update")
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"authorEmail\":\"bruce@gmail.com\",\"content\":\"Modified content.\",\"id\":\"4a2e7e42-833a-46b5-9976-b369f6041df0\",\"published\":true,\"createdAt\":\"" + post1.getCreatedAt().toString() + "\"}")
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().is4xxClientError())
            .andExpect(content().string("Invalid post author. bruce@gmail.com is not registered."));
            // the ControllerAdvice catches the Exception and returns 400 BAD REQUEST instead of a 5xx server error

        verify(postService, times(1)).updatePost(any(Post.class));
    }

    @Test
    void testUpdatePost_invalidPostIdShouldReturnBadRequestError() throws Exception {

        when(postService.updatePost(
                argThat(postToUpdate -> {
                    boolean invalidUUIDFormat = false;
                    
                    try{
                        var parsedId = UUID.fromString(postToUpdate.getId());
                        invalidUUIDFormat = !(parsedId instanceof UUID);
                    }
                    catch(IllegalArgumentException e) {
                        invalidUUIDFormat = true;
                    }

                    return invalidUUIDFormat;
                })
            ))
            .thenThrow(new NoSuchElementException("Could not find a post with id notValidUUID."));
          
        mockMvc.perform(
            put("/api/post/update")
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"notValidUUID\",\"authorEmail\":\"batman@waynecorp.com\",\"content\":\"Modified content.\",\"published\":true,\"createdAt\":\"" + post1.getCreatedAt().toString() + "\"}")
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().is4xxClientError())
            .andExpect(content().string("Could not find a post with id notValidUUID."));
            // the ControllerAdvice catches the Exception and returns 400 BAD REQUEST instead of a 5xx server error

        verify(postService, times(1)).updatePost(any(Post.class));
    }

    @Test
    void testUpdatePost_nullRequestArgumentsShouldReturnBadRequestError() throws Exception {

        when(postService.updatePost(
                argThat(postToUpdate -> 
                    (postToUpdate.getCreatedAt() == null || postToUpdate.getAuthorEmail() == null || postToUpdate.getContent() == null)
                )
            ))
            .thenThrow(IllegalArgumentException.class);
          
        mockMvc.perform(
            put("/api/post/update")
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"4a2e7e42-833a-46b5-9976-b369f6041df0\",\"authorEmail\":\"batman@waynecorp.com\",\"published\":true}")
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().is4xxClientError());
            // the ControllerAdvice catches the Exception and returns 400 BAD REQUEST instead of a 5xx server error

        verify(postService, times(1)).updatePost(any(Post.class));
    }
    
}
