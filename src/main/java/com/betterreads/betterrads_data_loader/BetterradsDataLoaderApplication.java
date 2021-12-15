package com.betterreads.betterrads_data_loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import com.betterreads.betterrads_data_loader.author.Author;
import com.betterreads.betterrads_data_loader.author.AuthorRepository;
import com.betterreads.betterrads_data_loader.book.Book;
import com.betterreads.betterrads_data_loader.book.BookRepository;
import com.betterreads.betterrads_data_loader.connection.DataStaxAstraProperties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(DataStaxAstraProperties.class)
public class BetterradsDataLoaderApplication {

	@Autowired
	AuthorRepository authorRepository;

	@Autowired
	BookRepository bookRepository;

	@Value("${datadump.location.author}")
	private String authorDumpLocation;

	@Value("${datadump.location.work}")
	private String workDumpLocation;

	public static void main(String[] args) {
		SpringApplication.run(BetterradsDataLoaderApplication.class, args);
	}

	@PostConstruct
	public void start() {
		initAuthor();
    		initWork();
		System.out.println("Started");
	}

	private void initWork() {

		Path path = Paths.get(workDumpLocation);
		DateTimeFormatter dFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
		try (Stream<String> lines = Files.lines(path)) {

			lines.forEach(line -> {
				String jsonString = line.substring(line.indexOf("{"));
				JSONObject json;
				try {
					json = new JSONObject(jsonString);
					Book book = new Book();
					book.setId(json.getString("key").replace("/works/", ""));
					JSONObject descriptionObj = json.optJSONObject("description");
					if (descriptionObj != null) {
						book.setDescription(descriptionObj.optString("value"));
					}
					JSONObject publishedObj = json.optJSONObject("created");
					if (publishedObj != null) {
						String dateStr = publishedObj.getString("value");
						book.setPublishedDate(LocalDate.parse(dateStr,dFormatter));
					}
					JSONArray coverJsonArray = json.optJSONArray("covers");
					if (coverJsonArray != null) {
						List<String> coverIds = new ArrayList<>();
						for (int i = 0; i < coverJsonArray.length(); i++) {
							coverIds.add(coverJsonArray.getString(i));
						}
						book.setCoverIds(coverIds);

					}
					JSONArray authorJsonArray = json.optJSONArray("authors");
					if (authorJsonArray != null) {
						List<String> authorIds = new ArrayList<>();
						for (int i = 0; i < authorJsonArray.length(); i++) {
							String authorId = authorJsonArray.getJSONObject(i).getJSONObject("author").getString("key")
									.replace("/authors/", "");
							authorIds.add(authorId);
						}
					book.setAuthorIds(authorIds);
						List<String> authorNames = authorIds.stream().map(id -> authorRepository.findById(id))
								.map(optionalAuthor -> {
									if (!optionalAuthor.isPresent())
										return "Unknown Author";
									return optionalAuthor.get().getName();
								}).collect(Collectors.toList());
						book.setAuthorNames(authorNames);

					}

					book.setName(json.optString("title"));
					bookRepository.save(book);

				} catch (Exception e) {
				
					e.printStackTrace();
				}

			});
		} catch (IOException e) {
			e.printStackTrace();
		
		}

	}

	private void initAuthor() {
		Path path = Paths.get(authorDumpLocation);
		try (Stream<String> lines = Files.lines(path)) {

			lines.forEach(line -> {
				String jsonString = line.substring(line.indexOf("{"));
				JSONObject json;
				try {
					json = new JSONObject(jsonString);
					Author author = new Author();
					author.setId(json.optString("key").replace("/authors/", ""));
					author.setName(json.optString("name"));
					author.setPersonalName(json.optString("personal_name"));
					authorRepository.save(author);
				} catch (JSONException e) {
					
					e.printStackTrace();
				}

			});
		} catch (IOException e) {
			e.printStackTrace();
		
		}
	}

	@Bean
	public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStaxAstraProperties astraProperties) {
		Path bundle = astraProperties.getSecureConnectBundle().toPath();
		return builder -> builder.withCloudSecureConnectBundle(bundle);
	}

}
