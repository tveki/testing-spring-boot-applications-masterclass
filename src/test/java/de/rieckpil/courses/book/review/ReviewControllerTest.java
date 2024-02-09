package de.rieckpil.courses.book.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.rieckpil.courses.config.WebSecurityConfig;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
// see https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.7-Release-Notes#migrating-from-websecurityconfigureradapter-to-securityfilterchain
@Import(WebSecurityConfig.class)
class ReviewControllerTest {

  @MockBean
  private ReviewService reviewService;

  @Autowired
  private MockMvc mockMvc;

  private ObjectMapper objectMapper;

  @BeforeEach
  public void beforeEach() {
    objectMapper = new ObjectMapper();
  }

  @Test
  void shouldReturnTwentyReviewsWithoutAnyOrderWhenNoParametersAreSpecified() throws Exception {

    ArrayNode arrayNode = objectMapper.createArrayNode();

    ObjectNode stat = objectMapper.createObjectNode();

    stat.put("bookId", 1);
    stat.put("isbn", "42");
    stat.put("avg", 41.5);
    stat.put("ratings", 3);

    arrayNode.add(stat);

    when(reviewService.getAllReviews(20, "none")).thenReturn(arrayNode);

    mockMvc
      .perform(get("/api/books/reviews"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.size()", Matchers.is(1)));
  }

  @Test
  void shouldNotReturnReviewStatisticsWhenUserIsUnauthenticated() throws Exception {
    mockMvc
      .perform(get("/api/books/reviews/statistics"))
      .andExpect(status().isUnauthorized());

    verifyNoInteractions(reviewService);
  }

  @Test
  @WithMockUser(username = "duke")
  void shouldReturnReviewStatisticsWhenUserIsAuthenticated() throws Exception {
    mockMvc
      .perform(get("/api/books/reviews/statistics"))
      .andExpect(status().isOk());

    verify(reviewService).getReviewStatistics();
  }

  @Test
  void shouldReturnReviewStatisticsWhenUserIsAuthenticatedJwt() throws Exception {
    mockMvc
      .perform(get("/api/books/reviews/statistics")
        .with(jwt()))
      .andExpect(status().isOk());

    verify(reviewService).getReviewStatistics();
  }

  @Test
  void shouldCreateNewBookReviewForAuthenticatedUserWithValidPayload() throws Exception {
    String payload = """
      {
        "reviewTitle": "Blabla",
        "reviewContent": "Something",
        "rating": 4
      }
      """;

    when(
      reviewService.createBookReview(
        eq("42"),
        any(BookReviewRequest.class),
        eq("duke"),
        endsWith("spring.io")
      )
    ).thenReturn(84L);

    mockMvc
      .perform(post("/api/books/{isbn}/reviews", 42)
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload)
        .with(jwt().jwt(builder -> builder
          .claim("email", "duke@spring.io")
          .claim("preferred_username", "duke")
        )))
      .andExpect(status().isCreated())
      .andExpect(header().exists("Location"))
      .andExpect(header().string("Location", Matchers.containsString("/books/42/reviews/84")));
  }

  @Test
  void shouldRejectNewBookReviewForAuthenticatedUsersWithInvalidPayload() throws Exception {
    String payload = """
      {
        "reviewContent": "Something",
        "rating": -2
      }
      """;

    mockMvc
      .perform(post("/api/books/{isbn}/reviews", 42)
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload)
        .with(jwt().jwt(builder -> builder
          .claim("email", "duke@spring.io")
          .claim("preferred_username", "duke")
        )))
      .andExpect(status().isBadRequest())
      .andDo(print());
  }

  @Test
  void shouldNotAllowDeletingReviewsWhenUserIsAuthenticatedWithoutModeratorRole() throws Exception {
    mockMvc
      .perform(delete("/api/books/{isbn}/reviews/{reviewId}", 42, 3)
        .with(jwt()))
      .andExpect(status().isForbidden());

    verifyNoInteractions(reviewService);
  }

  @Test
  @WithMockUser(roles = "moderator")
  void shouldAllowDeletingReviewsWhenUserIsAuthenticatedAndHasModeratorRole() throws Exception {
    mockMvc
      .perform(delete("/api/books/{isbn}/reviews/{reviewId}", 42, 3)
        .with(jwt()))
      .andExpect(status().isOk());

    verify(reviewService).deleteReview("42", 3L);
  }
}
