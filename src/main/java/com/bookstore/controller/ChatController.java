package com.bookstore.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Value("${app.gemini.api-key}")
    private String geminiApiKey;

    // Known book descriptions for offline mode
    private static final Map<String, String> BOOK_DESCRIPTIONS = new HashMap<>();

    static {
        BOOK_DESCRIPTIONS.put("the great gatsby", "A dazzling portrait of the Jazz Age, F. Scott Fitzgerald's masterpiece follows the mysterious millionaire Jay Gatsby and his obsessive pursuit of the elusive Daisy Buchanan. Beneath the glittering parties and lavish excess lies a profound meditation on the American Dream and its inevitable disillusionment.");
        BOOK_DESCRIPTIONS.put("pride and prejudice", "Jane Austen's beloved masterpiece follows the spirited Elizabeth Bennet as she navigates love, class, and misunderstanding in Regency-era England. The electric chemistry between Elizabeth and the brooding Mr. Darcy has captivated readers for over two centuries.");
        BOOK_DESCRIPTIONS.put("1984", "George Orwell's chilling dystopian novel paints a terrifying picture of a totalitarian future where Big Brother watches every move and independent thought is a crime. This prophetic masterwork remains startlingly relevant as a warning about the dangers of unchecked governmental power.");
        BOOK_DESCRIPTIONS.put("to kill a mockingbird", "Harper Lee's Pulitzer Prize-winning novel explores racial injustice in the Deep South through the innocent eyes of young Scout Finch. Her father, the noble lawyer Atticus Finch, becomes an enduring symbol of moral courage as he defends a Black man falsely accused of a terrible crime.");
        BOOK_DESCRIPTIONS.put("harry potter", "J.K. Rowling's magical saga follows an orphaned boy who discovers he's a wizard and enters a world of enchantment, friendship, and dark forces at Hogwarts School of Witchcraft and Wizardry. An epic tale of good versus evil that has enchanted an entire generation of readers worldwide.");
        BOOK_DESCRIPTIONS.put("the hobbit", "J.R.R. Tolkien's enchanting adventure follows the homebody hobbit Bilbo Baggins as he's swept into an epic quest with a company of dwarves to reclaim their stolen treasure from the fearsome dragon Smaug. A timeless tale of courage, friendship, and the unexpected hero within us all.");
        BOOK_DESCRIPTIONS.put("frankenstein", "Mary Shelley's groundbreaking Gothic novel tells the haunting story of Victor Frankenstein and the sentient creature he brings to life through forbidden science. A profound exploration of ambition, isolation, and what it truly means to be human — often considered the first science fiction novel ever written.");
        BOOK_DESCRIPTIONS.put("wuthering heights", "Emily Brontë's only novel is a wild, passionate tale of the all-consuming love between Heathcliff and Catherine Earnshaw on the windswept Yorkshire moors. Dark, tempestuous, and utterly unforgettable, it remains one of the most intense love stories in English literature.");
        BOOK_DESCRIPTIONS.put("java", "A comprehensive guide to one of the world's most popular programming languages, covering everything from object-oriented fundamentals to advanced enterprise features. Whether you're a beginner or a seasoned developer, this book will deepen your understanding of Java's powerful ecosystem.");
        BOOK_DESCRIPTIONS.put("spring boot", "Dive into the world of modern Java web development with Spring Boot, the framework that makes building production-ready applications faster than ever. Learn to create RESTful APIs, connect to databases, and deploy microservices with confidence and elegance.");
        BOOK_DESCRIPTIONS.put("python", "An accessible and engaging introduction to Python, the versatile language powering everything from web apps to artificial intelligence. With clear examples and hands-on projects, this book transforms beginners into confident programmers ready to tackle real-world challenges.");
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> chatWithAI(@RequestBody Map<String, String> requestBody) {
        String bookName = requestBody.get("bookName");
        Map<String, String> response = new HashMap<>();

        if (bookName == null || bookName.trim().isEmpty()) {
            response.put("description", "Please provide a valid book name or topic, and I'll help you find information about it!");
            return ResponseEntity.badRequest().body(response);
        }

        // If API key is configured, use Gemini
        if (geminiApiKey != null && !geminiApiKey.equals("YOUR_GEMINI_API_KEY_HERE")) {
            return callGeminiApi(bookName, response);
        }

        // Otherwise use intelligent mock responses
        response.put("description", getMockResponse(bookName));
        return ResponseEntity.ok(response);
    }

    private String getMockResponse(String query) {
        String queryLower = query.toLowerCase().trim();

        // Check for exact or partial matches in known books
        for (Map.Entry<String, String> entry : BOOK_DESCRIPTIONS.entrySet()) {
            if (queryLower.contains(entry.getKey()) || entry.getKey().contains(queryLower)) {
                return entry.getValue();
            }
        }

        // Detect greeting patterns
        if (queryLower.matches(".*(hello|hi|hey|greetings|howdy|what's up).*")) {
            return "Hello! 👋 Welcome to our BookStore! I'm your virtual assistant. You can ask me about any book — try typing a title like \"The Great Gatsby\", \"Harry Potter\", or \"1984\" and I'll tell you all about it!";
        }

        // Detect help/recommendation requests
        if (queryLower.matches(".*(recommend|suggest|what should i read|best book|popular).*")) {
            return "Great question! 📚 Here are some timeless recommendations:\n• **The Great Gatsby** — A Jazz Age masterpiece\n• **1984** — A chilling dystopian classic\n• **To Kill a Mockingbird** — A story of justice and empathy\n• **Harry Potter** — A magical adventure for all ages\n\nType any title to learn more!";
        }

        // Detect thanks
        if (queryLower.matches(".*(thanks|thank you|thx|appreciate).*")) {
            return "You're welcome! 😊 Happy to help. Feel free to ask about any other book or topic anytime!";
        }

        // Generic smart response for unknown books
        return "\"" + query + "\" sounds like a fascinating read! 📖 While I don't have specific details about this title in my offline library, I'd recommend checking our catalog above for similar titles. You can also try asking about popular books like \"Pride and Prejudice\", \"Frankenstein\", or \"The Hobbit\"!";
    }

    private ResponseEntity<Map<String, String>> callGeminiApi(String bookName, Map<String, String> response) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey;

            Map<String, Object> payload = new HashMap<>();
            Map<String, Object> partsMap = new HashMap<>();
            partsMap.put("text", "Give me a brief engaging 2-sentence description of the book: " + bookName);

            Map<String, Object> contentsMap = new HashMap<>();
            contentsMap.put("parts", List.of(partsMap));
            payload.put("contents", List.of(contentsMap));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<Map> geminiResponse = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            Map<String, Object> responseBody = geminiResponse.getBody();

            String generatedDescription = "";
            try {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                generatedDescription = (String) parts.get(0).get("text");
            } catch (Exception e) {
                generatedDescription = "Failed to parse AI response. " + e.getMessage();
            }

            response.put("description", generatedDescription);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Fall back to mock response if Gemini call fails
            response.put("description", getMockResponse(bookName));
            return ResponseEntity.ok(response);
        }
    }
}
