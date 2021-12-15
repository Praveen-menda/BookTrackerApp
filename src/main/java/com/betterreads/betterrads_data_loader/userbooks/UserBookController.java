package com.betterreads.betterrads_data_loader.userbooks;

import java.time.LocalDate;
import java.util.Optional;

import com.betterreads.betterrads_data_loader.book.Book;
import com.betterreads.betterrads_data_loader.book.BookRepository;
import com.betterreads.betterrads_data_loader.user.BooksByUser;
import com.betterreads.betterrads_data_loader.user.BooksByUserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UserBookController {


    @Autowired 
    UserBookRepsitory userBooksRepository;

    @Autowired 
    BooksByUserRepository booksByUserRepository;

    @Autowired 
    BookRepository bookRepository;
    @PostMapping("/addUserBook")
    public ModelAndView addBookForUser(@RequestBody MultiValueMap<String,String> formData, @AuthenticationPrincipal OAuth2User principal)
    {

    /*    UserBooks userBooks = new UserBooks();
        UserBookPrimaryKey key = new UserBookPrimaryKey();
        if(principal == null || principal.getAttribute("login") == null)
         return null;
        key.setUserId(principal.getAttribute("login"));
        String bookId = formData.getFirst("bookId");
        key.setBookId(formData.getFirst("bookId"));

        userBooks.setStartedDate(LocalDate.parse(formData.getFirst("startDate")));
        userBooks.setEndDate(LocalDate.parse(formData.getFirst("completedDate")));
        userBooks.setRating(Integer.parseInt(formData.getFirst("rating")));
        userBooks.setReadingStatus(formData.getFirst("status"));

        userBookRepsitory.save(userBooks);



        return new ModelAndView("redirect:/books/" +bookId) ;*/
        if (principal == null || principal.getAttribute("login") == null) {
            return null;
        }

        String bookId = formData.getFirst("bookId");
        Optional<Book> optionalBook = bookRepository.findById(bookId);
        if (!optionalBook.isPresent()) {
            return new ModelAndView("redirect:/");
        }
        Book book = optionalBook.get();

        UserBooks userBooks  = new UserBooks();
        UserBookPrimaryKey key = new UserBookPrimaryKey();
        String userId = principal.getAttribute("login");
        key.setUserId(userId);
        key.setBookId(bookId);

        userBooks.setKey(key);

        int rating = Integer.parseInt(formData.getFirst("rating"));

        userBooks.setStartedDate(LocalDate.parse(formData.getFirst("startDate")));
        userBooks.setEndDate(LocalDate.parse(formData.getFirst("completedDate")));
        userBooks.setRating(rating);
        userBooks.setReadingStatus(formData.getFirst("readingStatus"));

       
        userBooksRepository.save(userBooks);

        BooksByUser booksByUser = new BooksByUser();
        booksByUser.setId(userId);
        booksByUser.setBookId(bookId);
        booksByUser.setBookName(book.getName());
        booksByUser.setCoverIds(book.getCoverIds());
        booksByUser.setAuthorNames(book.getAuthorNames());
        booksByUser.setReadingStatus(formData.getFirst("readingStatus"));
        booksByUser.setRating(rating);
        booksByUserRepository.save(booksByUser);


        return new ModelAndView("redirect:/books/" + bookId);
        
    }
    
}
