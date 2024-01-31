package de.rieckpil.courses.book.management;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookSynchronizationListenerTest {

  private final static String VALID_ISBN = "1234567891234";

  @Mock
  private BookRepository bookRepository;

  @Mock
  private OpenLibraryApiClient openLibraryApiClient;

  @InjectMocks
  private BookSynchronizationListener cut;

  @Captor
  private ArgumentCaptor<Book> bookArgumentCaptor;

  @Test
  void shouldRejectBookWhenIsbnIsMalformed() {
    BookSynchronization synchronization = new BookSynchronization("12345");
    cut.consumeBookUpdates(synchronization);
    verifyNoInteractions(bookRepository);
    verifyNoInteractions(openLibraryApiClient);
  }

  @Test
  void shouldNotOverrideWhenBookAlreadyExists() {
//    when(bookRepository.findByIsbn("1234567890000")).thenReturn(null);
  }

  @Test
  void shouldThrowExceptionWhenProcessingFails() {
  }

  @Test
  void shouldStoreBookWhenNewAndCorrectIsbn() {
  }

}
