package com.bookworm.config;

import com.bookworm.model.*;
import com.bookworm.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initDatabase(CategoryRepository categoryRepo,
                                         BrandRepository brandRepo,
                                         ProductRepository productRepo,
                                         UserRepository userRepo,
                                         PasswordEncoder encoder,
                                         BasketRepository basketRepo,
                                         GiftPointsRepository giftPointsRepo) {
        return args -> {
            if (categoryRepo.count() > 0) return;

            // 1. Seed Categories from Wireframe
            Category cSelfHelp = categoryRepo.save(new Category("self-help", "Self-help", "Self-help & Personal Development", 12));
            Category cFiction = categoryRepo.save(new Category("fiction", "Fiction", "Fiction & Novels", 24));
            Category cMystery = categoryRepo.save(new Category("mystery", "Mystery", "Mystery & Crime Thrillers", 18));
            Category cChildren = categoryRepo.save(new Category("childrens", "Children's", "Children's books and adventures", 10));
            Category cSciFi = categoryRepo.save(new Category("science-fiction", "Science Fiction", "Sci-Fi and Space Exploration", 15));
            categoryRepo.saveAll(List.of(
                    new Category("romance", "Romance", "Romance Novels", 8),
                    new Category("fantasy", "Fantasy", "Epic & Urban Fantasy", 14),
                    new Category("biography", "Biography", "Biographies & Memoirs", 9),
                    new Category("cooking", "Cooking", "Cookbooks and Recipes", 6)
            ));

            // 2. Seed Brands / Authors from Wireframe
            Brand bArjun = brandRepo.save(new Brand("Arjun Patel", "Author", "Productivity & Focus Expert", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150"));
            Brand bRaj = brandRepo.save(new Brand("Raj Patel", "Author", "Lifelong Learning & Mindset Strategist", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150"));
            Brand bJames = brandRepo.save(new Brand("James Wright", "Author", "Goal Achievement Coach", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150"));
            Brand bAdams = brandRepo.save(new Brand("James Adams", "Author", "Suspense & Psychological Fiction", "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=150"));
            Brand bJessica = brandRepo.save(new Brand("Jessica Martin", "Author", "Contemporary Romance", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150"));
            Brand bLaura = brandRepo.save(new Brand("Laura Mitchell", "Author", "Hard Sci-Fi Author", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150"));
            Brand bDaniel = brandRepo.save(new Brand("Daniel Reed", "Author", "Minimalism & Wellness Guide", "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=150"));
            Brand bClara = brandRepo.save(new Brand("Clara Nelson", "Author", "Horror & Dark Fantasy Novelist", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150"));
            Brand bEmily = brandRepo.save(new Brand("Emily Parker", "Author", "Children's Fiction Writer", "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=150"));

            // 3. Seed Products from Wireframe
            // Recommended for You
            createProduct(productRepo, "The Art of Focus", bArjun, cSelfHelp, "Practical guide to mastering focus & boosting productivity every day.", "Paperback", new BigDecimal("399.00"), "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=300");
            createProduct(productRepo, "The Art of Learning", bRaj, cSelfHelp, "Master the mindset and methods for effective lifelong learning.", "Paperback", new BigDecimal("259.00"), "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=300");
            createProduct(productRepo, "The Path to Success", bJames, cSelfHelp, "A practical guide to achieving goals with clarity and confidence.", "Paperback", new BigDecimal("359.00"), "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=300");

            // Bestsellers this Month
            createProduct(productRepo, "The Midnight Hour", bAdams, cMystery, "Haunting tale of a man's journey & the shadows of a forgotten past.", "Paperback", new BigDecimal("299.00"), "https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?w=300");
            createProduct(productRepo, "Beneath the Stars", bJessica, cFiction, "A heartwarming tale, where two souls discover who they need.", "Hardcover", new BigDecimal("499.00"), "https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=300");
            createProduct(productRepo, "The Final Frontier", bLaura, cSciFi, "A mission to space secrets to change humanity forever.", "Paperback", new BigDecimal("359.00"), "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=300");

            // New Launches
            createProduct(productRepo, "Joy of Minimalism", bDaniel, cSelfHelp, "Declutter your life to uncover peace, clarity, and joy.", "Paperback", new BigDecimal("149.00"), "https://images.unsplash.com/photo-1499750310107-5fef28a66643?w=300");
            createProduct(productRepo, "The Vanishing House", bClara, cMystery, "A chilling mystery unfolds within a house that disappears.", "eBook", new BigDecimal("99.00"), "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=300");
            createProduct(productRepo, "The Lost Kitten", bEmily, cChildren, "A heartwarming tale of courage, friendship, and feline adventure.", "Hardcover", new BigDecimal("339.00"), "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=300");

            // 4. Seed a Demo Test Customer
            User demoUser = new User("Jane Doe", "customer@example.com", encoder.encode("Password123!"), "+919876543210");
            demoUser = userRepo.save(demoUser);
            basketRepo.save(new Basket(demoUser));
            giftPointsRepo.save(new GiftPoints(demoUser, 150));
        };
    }

    private void createProduct(ProductRepository repo, String title, Brand brand, Category cat, String syn, String format, BigDecimal price, String img) {
        Product p = new Product();
        p.setTitle(title);
        p.setBrand(brand);
        p.setCategory(cat);
        p.setSynopsis(syn);
        p.setFullDescription(syn + " Full edition covering exhaustive chapters and practical guides.");
        p.setFormat(format);
        p.setPrice(price);
        p.setCoverImageUrl(img);
        p.setTentativeDeliveryDate("Mon, 21 Jul");
        p.setTentativeDeliveryDays(3);
        p.setIsbn("978-" + (int)(Math.random() * 900000000 + 100000000));
        p.setPublisher("BookWorm Publications");
        p.setPageCount(250 + (int)(Math.random() * 200));
        p.setInStock(true);
        p.setStockQuantity(50);
        repo.save(p);
    }
}
