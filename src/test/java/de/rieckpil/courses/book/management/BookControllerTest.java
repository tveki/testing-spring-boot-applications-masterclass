package de.rieckpil.courses.book.management;

import de.rieckpil.courses.config.WebSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
// see https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.7-Release-Notes#migrating-from-websecurityconfigureradapter-to-securityfilterchain
@Import(WebSecurityConfig.class)
class BookControllerTest {

  @MockBean
  private BookManagementService bookManagementService;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldGetEmptyArrayWhenNoBooksExists() throws Exception {
    mockMvc
      .perform(
        get("/api/books")
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(
        status().isOk()
      )
      .andExpect(
        content().contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(
        jsonPath("$.size()", is(0))
      )
      .andDo(
        print()
      )
      .andReturn();
  }

  @Test
  void shouldNotReturnXML() throws Exception {
    mockMvc
      .perform(
        get("/api/books")
          .accept(MediaType.APPLICATION_XML)
      )
      .andExpect(
        status().isNotAcceptable()
      );
  }

  @Test
  void shouldGetBooksWhenServiceReturnsBooks() throws Exception {
    Book firstBook = createBook(
      100L,
      "123456",
      "War and peace",
      "Tolstoy",
      null,
      "",
      500L,
      null,
      null
    );

    when(bookManagementService.getAllBooks()).thenReturn(List.of(firstBook));

    mockMvc
      .perform(
        get("/api/books")
          .accept(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.size()", is(1)))
      .andExpect(jsonPath("$[0].isbn", is("123456")))
      .andExpect(jsonPath("$[0].title", is("War and peace")));
  }

  private Book createBook(Long id, String isbn, String title, String author, String description, String genre, Long pages, String publisher, String thumbnailUrl) {
    Book result = new Book();
    result.setId(id);
    result.setIsbn(isbn);
    result.setTitle(title);
    result.setAuthor(author);
    result.setDescription(description);
    result.setGenre(genre);
    result.setPages(pages);
    result.setPublisher(publisher);
    result.setThumbnailUrl(thumbnailUrl);
    return result;
  }

}
