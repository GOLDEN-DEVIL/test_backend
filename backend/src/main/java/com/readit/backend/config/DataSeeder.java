package com.readit.backend.config;

import com.readit.backend.entity.*;
import com.readit.backend.repository.BookRepository;
import com.readit.backend.repository.CategoryRepository;
import com.readit.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds the database with sample data on first run.
 * Only inserts data if the tables are empty.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            log.info("Database already seeded. Skipping.");
            return;
        }

        log.info("Seeding database with sample data...");

        // ---- Categories ----
        Category fantasy = categoryRepository.save(
                Category.builder().name("Fantasy").description("Fantasy and adventure books").build());
        Category manga = categoryRepository.save(
                Category.builder().name("Manga").description("Japanese manga and graphic novels").build());
        Category romance = categoryRepository.save(
                Category.builder().name("Romance").description("Romance and love stories").build());
        Category horror = categoryRepository.save(
                Category.builder().name("Horror").description("Horror and thriller books").build());
        Category scifi = categoryRepository.save(
                Category.builder().name("Sci-Fi").description("Science fiction novels").build());

        // ---- Books (matching the frontend hardcoded data) ----
        List<Book> books = List.of(
                Book.builder()
                        .title("Harry Potter And The Cursed Child")
                        .author("J.K. Rowling")
                        .description("Harry Potter and the Cursed Child is a play written by Jack Thorne from an original story by Thorne, J. K. Rowling, and John Tiffany. The plot occurs nineteen years after the events of Rowling's novel Harry Potter and the Deathly Hallows.")
                        .genre("Fantasy")
                        .language("English")
                        .price(new BigDecimal("499.00"))
                        .imageUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ0SupMsyxh84UHuq4Cuo32MClNv0AHylmWzMH91UluruTIMFG1gllqUjrXdYo1-yRRos3X0ckLvro2RGNPCksyMIYDWAJ8JqzBuBCIEQ&s=10")
                        .inventory(50)
                        .category(fantasy)
                        .build(),
                Book.builder()
                        .title("The Beginning After The End")
                        .author("TurtleMe")
                        .description("King Grey has unrivaled strength, wealth, and prestige in a world governed by martial ability. However, solitude lingers closely behind those with great power.")
                        .genre("Fantasy and Action")
                        .language("English")
                        .price(new BigDecimal("399.00"))
                        .imageUrl("https://i.pinimg.com/736x/eb/65/17/eb6517718b619d7fb1766c7ccd54376f.jpg")
                        .inventory(30)
                        .category(fantasy)
                        .build(),
                Book.builder()
                        .title("One Piece - Egg Head Arc")
                        .author("Eiichiro Oda")
                        .description("The Egghead Arc is the thirty-second story arc of the manga and anime One Piece, and the first arc of the Final Saga.")
                        .genre("Shonen")
                        .language("English")
                        .price(new BigDecimal("449.00"))
                        .imageUrl("https://m.media-amazon.com/images/I/91+2OXQMXSL._UF1000,1000_QL80_.jpg")
                        .inventory(40)
                        .category(manga)
                        .build(),
                Book.builder()
                        .title("JoJo's Bizarre Adventure - Steel Ball Run")
                        .author("Hirohiko Araki")
                        .description("Steel Ball Run is the seventh part of the JoJo's Bizarre Adventure series. Set in 1890 in the United States, it follows the cross-country horse race Steel Ball Run.")
                        .genre("Bizarre")
                        .language("English")
                        .price(new BigDecimal("499.00"))
                        .imageUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR5SDk9PZtwIXGQk_Dbf835ZIXK9wGgn8fyjQ&s")
                        .inventory(25)
                        .category(manga)
                        .build(),
                Book.builder()
                        .title("The Name Of The Wind")
                        .author("Patrick Rothfuss")
                        .description("The Name of the Wind is a heroic fantasy novel by Patrick Rothfuss. It is the first book in the series The Kingkiller Chronicle.")
                        .genre("Fantasy and Horror")
                        .language("English")
                        .price(new BigDecimal("599.00"))
                        .imageUrl("https://m.media-amazon.com/images/S/compressed.photo.goodreads.com/books/1704917687i/186074.jpg")
                        .inventory(35)
                        .category(fantasy)
                        .build(),
                Book.builder()
                        .title("The Serpent And The Wings Of Night")
                        .author("Carissa Broadbent")
                        .description("A captivating fantasy romance about a human girl raised by a vampire king who must compete in a deadly tournament.")
                        .genre("Fantasy-Romance")
                        .language("English")
                        .price(new BigDecimal("649.00"))
                        .imageUrl("https://i.gr-assets.com/images/S/compressed.photo.goodreads.com/books/1711665394i/60714999._UX160_.jpg")
                        .inventory(20)
                        .category(romance)
                        .build()
        );

        bookRepository.saveAll(books);

        // ---- Demo users ----
        userRepository.save(User.builder()
                .username("admin")
                .email("admin@readit.com")
                .password(passwordEncoder.encode("admin123"))
                .fullName("Admin User")
                .role(Role.ADMIN)
                .build());

        userRepository.save(User.builder()
                .username("demo")
                .email("demo@readit.com")
                .password(passwordEncoder.encode("demo123"))
                .fullName("Demo Reader")
                .phone("+91 12345 67891")
                .address("Gomti Nagar, Lucknow, UP 226010")
                .role(Role.USER)
                .build());

        log.info("Database seeded with {} categories, {} books, and 2 users.",
                5, books.size());
    }
}
