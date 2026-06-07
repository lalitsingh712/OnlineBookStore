package com.bookstore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {

    @Autowired
    private com.bookstore.service.BookService bookService;

    @Autowired
    private com.bookstore.repository.CategoryRepository categoryRepository;

    @Autowired
    private com.bookstore.repository.CartRepository cartRepository;

    @Autowired
    private com.bookstore.repository.UserRepository userRepository;

    @Autowired
    private com.bookstore.repository.OrderRepository orderRepository;

    @Autowired
    private com.bookstore.repository.ReviewRepository reviewRepository;

    private com.bookstore.entity.User getCurrentUser() {
        try {
            org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
                return null;
            }
            com.bookstore.security.services.UserDetailsImpl userDetails = (com.bookstore.security.services.UserDetailsImpl) authentication.getPrincipal();
            return userRepository.findById(userDetails.getId()).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping("/")
    public String index(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "sort", required = false) String sort,
            org.springframework.ui.Model model) {
        
        model.addAttribute("currentSort", sort);
        
        java.util.List<com.bookstore.entity.Book> allBooks = new java.util.ArrayList<>(bookService.getAllBooks());
        if ("price_asc".equals(sort)) {
            allBooks.sort(java.util.Comparator.comparing(com.bookstore.entity.Book::getPrice));
        } else if ("price_desc".equals(sort)) {
            allBooks.sort(java.util.Comparator.comparing(com.bookstore.entity.Book::getPrice).reversed());
        } else if ("title_asc".equals(sort)) {
            allBooks.sort(java.util.Comparator.comparing(com.bookstore.entity.Book::getTitle));
        }

        if (query != null && !query.trim().isEmpty()) {
            String q = query.trim().toLowerCase();
            model.addAttribute("searchQuery", query);
            model.addAttribute("searchResults", allBooks.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(q) || b.getAuthor().toLowerCase().contains(q))
                .toList());
        }
        
        model.addAttribute("techBooks", allBooks.stream()
            .filter(b -> {
                if (b.getCategory() == null) return false;
                String cat = b.getCategory().getName().toLowerCase();
                return cat.contains("calculus") || cat.contains("economics") || cat.contains("science")
                    || cat.contains("geology") || cat.contains("military") || cat.contains("botany")
                    || cat.contains("constitutional") || cat.contains("insects");
            })
            .limit(4).toList());
        model.addAttribute("fictionBooks", allBooks.stream()
            .filter(b -> {
                if (b.getCategory() == null) return false;
                String cat = b.getCategory().getName().toLowerCase();
                return cat.contains("fiction") || cat.contains("drama") || cat.contains("horror")
                    || cat.contains("fantasy") || cat.contains("mystery") || cat.contains("dystopia");
            })
            .limit(4).toList());
        model.addAttribute("eduBooks", allBooks.stream()
            .filter(b -> {
                if (b.getCategory() == null) return false;
                String cat = b.getCategory().getName().toLowerCase();
                return cat.contains("education") || cat.contains("philosophy") || cat.contains("history")
                    || cat.contains("ethics") || cat.contains("biography") || cat.contains("language")
                    || cat.contains("bible") || cat.contains("liberty");
            })
            .limit(4).toList());
        
        com.bookstore.entity.User user = getCurrentUser();
        model.addAttribute("isAdmin", user != null && "ROLE_ADMIN".equals(user.getRole()));
        model.addAttribute("isUser", user != null);
        model.addAttribute("username", user != null ? user.getUsername() : null);
        
        if (user != null) {
            model.addAttribute("cartCount", cartRepository.findByUserId(user.getId()).map(c -> c.getCartItems().size()).orElse(0));
        } else {
            model.addAttribute("cartCount", 0);
        }
        return "index";
    }

    @GetMapping("/cart")
    public String cart(org.springframework.ui.Model model) {
        com.bookstore.entity.User user = getCurrentUser();
        model.addAttribute("isAdmin", user != null && "ROLE_ADMIN".equals(user.getRole()));
        model.addAttribute("isUser", user != null);
        model.addAttribute("username", user != null ? user.getUsername() : null);
        
        if (user == null) return "redirect:/login";
        
        com.bookstore.entity.Cart cart = cartRepository.findByUserId(user.getId()).orElse(new com.bookstore.entity.Cart(user));
        model.addAttribute("cart", cart);
        model.addAttribute("totalPrice", cart.getCartItems().stream().mapToDouble(i -> i.getBook().getPrice() * i.getQuantity()).sum());
        return "cart";
    }

    @GetMapping("/checkout")
    public String checkout(org.springframework.ui.Model model) {
        com.bookstore.entity.User user = getCurrentUser();
        if (user == null) return "redirect:/login";
        com.bookstore.entity.Cart cart = cartRepository.findByUserId(user.getId()).orElse(new com.bookstore.entity.Cart(user));
        model.addAttribute("cart", cart);
        model.addAttribute("totalPrice", cart.getCartItems().stream().mapToDouble(i -> i.getBook().getPrice() * i.getQuantity()).sum());
        model.addAttribute("isUser", true);
        model.addAttribute("username", user.getUsername());
        return "checkout";
    }

    @GetMapping("/orders")
    public String orders(org.springframework.ui.Model model) {
        com.bookstore.entity.User user = getCurrentUser();
        if (user == null) return "redirect:/login";
        model.addAttribute("orders", orderRepository.findByUserId(user.getId()));
        model.addAttribute("isUser", true);
        model.addAttribute("username", user.getUsername());
        
        // Populate cartCount for header
        int cartCount = cartRepository.findByUserId(user.getId())
            .map(c -> c.getCartItems().size())
            .orElse(0);
        model.addAttribute("cartCount", cartCount);
        return "orders";
    }

    @GetMapping("/admin")
    public String admin(org.springframework.ui.Model model) {
        model.addAttribute("bookCount", bookService.getAllBooks().size());
        model.addAttribute("orderCount", orderRepository.count());
        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("recentBooks", bookService.getAllBooks().stream().limit(10).toList());
        return "admin";
    }

    @GetMapping("/book/{id}")
    public String bookDetails(@PathVariable("id") Long id, org.springframework.ui.Model model) {
        com.bookstore.entity.Book book = bookService.getBookById(id).orElse(null);
        if (book == null) {
            return "redirect:/";
        }
        model.addAttribute("book", book);
        
        com.bookstore.entity.User user = getCurrentUser();
        model.addAttribute("isAdmin", user != null && "ROLE_ADMIN".equals(user.getRole()));
        model.addAttribute("isUser", user != null);
        model.addAttribute("username", user != null ? user.getUsername() : null);
        
        if (user != null) {
            model.addAttribute("cartCount", cartRepository.findByUserId(user.getId()).map(c -> c.getCartItems().size()).orElse(0));
        } else {
            model.addAttribute("cartCount", 0);
        }
        java.util.List<com.bookstore.entity.Review> reviews = reviewRepository.findByBookId(id);
        model.addAttribute("reviews", reviews);
        double avgRating = reviews.isEmpty() ? 0.0 : reviews.stream().mapToInt(com.bookstore.entity.Review::getRating).average().orElse(0.0);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("reviewsCount", reviews.size());

        return "details";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/info/{type}")
    public String infoPage(@PathVariable("type") String type, org.springframework.ui.Model model) {
        String title = "Information";
        String content = "Details not found.";
        
        switch (type) {
            case "tracker":
                title = "Order Tracking Status";
                content = "Track your standard and premium package orders here. Currently all logistics channels are fully operational. Please enter your Order ID in our AI customer support chat widget for real-time tracking updates.";
                break;
            case "shipping":
                title = "Shipping Policy";
                content = "We deliver standard orders globally within 3 to 5 business days. Premium express shipping is free for all orders above ₹50. All packages are securely bound in waterproof packaging to preserve cover quality.";
                break;
            case "returns":
                title = "Cancellation & Return Policy";
                content = "You can cancel any order before it enters the shipping queue for a 100% immediate refund. Returns of physical books are accepted within 30 days of delivery, provided they remain in original condition. Return postage is fully covered.";
                break;
            case "faq":
                title = "Frequently Asked Questions (FAQ)";
                content = "Q: Do you ship physical publications?\nA: Yes! All items in our catalog are high-quality physical prints shipped from our regional warehouses.\n\nQ: How does the AI support bot help me?\nA: Our customer assistant is powered by smart catalog mapping to recommend authors, fetch summary details, and check cart additions.";
                break;
            case "privacy":
                title = "Privacy Policy & GDPR";
                content = "We value your privacy. Your personal user profile, checkout email, order history, and payment session credentials are encrypted end-to-end. We do not sell or lease user telemetry to advertising networks.";
                break;
            case "terms":
                title = "Terms of Service";
                content = "By utilizing the BookStore application, you agree to our standard terms of use, including secure authentication practices, catalog querying limits, and fair use of the customer AI widget.";
                break;
        }
        
        model.addAttribute("title", title);
        model.addAttribute("content", content);
        
        com.bookstore.entity.User user = getCurrentUser();
        model.addAttribute("isAdmin", user != null && "ROLE_ADMIN".equals(user.getRole()));
        model.addAttribute("isUser", user != null);
        model.addAttribute("username", user != null ? user.getUsername() : null);
        
        if (user != null) {
            model.addAttribute("cartCount", cartRepository.findByUserId(user.getId()).map(c -> c.getCartItems().size()).orElse(0));
        } else {
            model.addAttribute("cartCount", 0);
        }
        return "info";
    }
}
