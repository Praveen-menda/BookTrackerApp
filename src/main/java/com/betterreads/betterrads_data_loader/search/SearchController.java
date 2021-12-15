package com.betterreads.betterrads_data_loader.search;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;


import reactor.core.publisher.Mono;

@Controller
public class SearchController {

    private final String COVER_IMAGE = "http://covers.openlirary.org/b/id/";
    private final WebClient webClient;
    public SearchController(WebClient.Builder webClientBuilder) {
      this.webClient = webClientBuilder.exchangeStrategies(ExchangeStrategies.builder()
      .codecs(configurer -> configurer
                .defaultCodecs()
                .maxInMemorySize(16 * 1024 * 1024))
              .build()).baseUrl("http://openlibrary.org/search.json").build();
  }


    @GetMapping(value = "/search")
    public String getSearchResult(@RequestParam String query,Model model)
    {
      Mono<SearchResult> foo = this.webClient.get()
             .uri("?q={query}",query)
             .retrieve().bodyToMono(SearchResult.class);
            SearchResult result = foo.block();

           List<SearchResultBook> books = result.getDocs().stream().limit(20)
           .map(bookResult -> {
              bookResult.setKey( bookResult.getKey().replace("/works/", ""));
              String coverId = bookResult.getCover_i();
              String coverImgUrl = "/images/noImage.png";
              if(StringUtils.hasText(coverId))
              {
                coverImgUrl = COVER_IMAGE + coverId +"-M.jpg";
              }
              bookResult.setCover_i(coverImgUrl);
              return bookResult;
           })
           .collect(Collectors.toList());

            model.addAttribute("searchResults",books);

        return "search";
    }
    
}
