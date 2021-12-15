package com.betterreads.betterrads_data_loader.book;

import java.util.Optional;

import com.betterreads.betterrads_data_loader.userbooks.UserBookPrimaryKey;
import com.betterreads.betterrads_data_loader.userbooks.UserBookRepsitory;
import com.betterreads.betterrads_data_loader.userbooks.UserBooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class BookController {

    private final String COVER_IMAGE = "http://covers.openlirary.org/b/id/";
    @Autowired
    BookRepository bookRepository;
    @Autowired
    UserBookRepsitory userBookRepsitory;
    @GetMapping(value = "/books/{bookId}")
    public String getBook(@PathVariable String bookId,Model model,@AuthenticationPrincipal OAuth2User principal)
    {
          Optional<Book> book = bookRepository.findById(bookId);
          if(book.isPresent())
          {
              Book nBook = book.get();
              String coverImgUrl ="src/main/resources/images/noImage.png" ;
                 if(nBook.getCoverIds()!= null && nBook.getCoverIds().size() >0)
              {
                coverImgUrl = COVER_IMAGE + nBook.getCoverIds().get(0) +"-L.jpg";
                  
              }
              model.addAttribute("coverImage", coverImgUrl);

              model.addAttribute("book", nBook);
              
              if(principal != null && principal.getAttribute("login")!= null)
              {

                String userId = principal.getAttribute("login");
                model.addAttribute("loginId", principal.getAttribute("login"));
                UserBookPrimaryKey key = new UserBookPrimaryKey();
                key.setBookId(bookId);
                key.setUserId(userId);
                Optional<UserBooks> userBooks = userBookRepsitory.findById(key);
                if(userBooks.isPresent())
                {
                  model.addAttribute("userBooks", userBooks.get());
                }
                else{
                  model.addAttribute("userBooks", new UserBooks());
                }

              }
              return "book";
          }
          return "book-not-found";
        
    }

    
}
