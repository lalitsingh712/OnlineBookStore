package com.bookstore.seeder;

import com.bookstore.entity.Book;
import com.bookstore.entity.Category;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        if (bookRepository.count() < 400) {
            System.out.println("Initiating Database Seeder... Fetching 400 open-source books from Gutendex.");
            RestTemplate restTemplate = new RestTemplate();
            Random random = new Random();
            
            String nextUrl = "https://gutendex.com/books/";
            int totalFetched = 0;
            
            while (nextUrl != null && totalFetched < 400) {
                try {
                    ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                            nextUrl,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<Map<String, Object>>() {}
                    );

                    Map<String, Object> body = response.getBody();
                    if (body != null) {
                        List<Map<String, Object>> results = (List<Map<String, Object>>) body.get("results");
                        if (results != null) {
                            List<Book> booksToSave = new ArrayList<>();
                            
                            for (Map<String, Object> item : results) {
                                String title = (String) item.get("title");
                                
                                // Parse Authors
                                List<Map<String, Object>> authorsList = (List<Map<String, Object>>) item.get("authors");
                                String author = "Unknown";
                                if (authorsList != null && !authorsList.isEmpty()) {
                                    author = (String) authorsList.get(0).get("name");
                                }
                                
                                // Parse Subjects inside category
                                List<String> subjects = (List<String>) item.get("subjects");
                                String categoryName = "Fiction";
                                if (subjects != null && !subjects.isEmpty()) {
                                    categoryName = subjects.get(0).length() > 255 ? subjects.get(0).substring(0, 250) : subjects.get(0);
                                }
                                
                                final String finalCategoryName = categoryName;
                                Category dbCategory = categoryRepository.findByName(finalCategoryName)
                                        .orElseGet(() -> categoryRepository.save(new Category(finalCategoryName)));
                                
                                // Parse Formats for image
                                Map<String, String> formats = (Map<String, String>) item.get("formats");
                                String imageUrl = "https://images.unsplash.com/photo-1543722530-d2c3201371e7?auto=format&fit=crop&w=300&q=80";
                                if (formats != null && formats.containsKey("image/jpeg")) {
                                    imageUrl = formats.get("image/jpeg");
                                }

                                // Random price between 10.0 and 99.0 for realism
                                double price = 10.0 + (random.nextDouble() * 89.0);
                                price = Math.round(price * 100.0) / 100.0;

                                Book book = new Book(title, author, dbCategory, price, imageUrl);
                                booksToSave.add(book);
                                totalFetched++;
                            }
                            
                            bookRepository.saveAll(booksToSave);
                            System.out.println("Fetched and stored " + totalFetched + " books so far...");
                        }
                        
                        nextUrl = (String) body.get("next");
                    } else {
                        break;
                    }
                } catch (Exception e) {
                    System.err.println("Error fetching from Gutendex: " + e.getMessage());
                    break;
                }
            }
            System.out.println("Database Seeding Completed. Total Books in DB: " + bookRepository.count());
        } else {
            System.out.println("Database looks full. Skipping Seeder initialization.");
        }
    }
}
