package com.finance.backend.config;

import com.finance.backend.model.Role;
import com.finance.backend.model.Transaction;
import com.finance.backend.model.TransactionType;
import com.finance.backend.model.User;
import com.finance.backend.repository.TransactionRepository;
import com.finance.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;


@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

   
    @Override
    public void run(String... args) {
        log.info("Running DataSeeder...");

       
        if (userRepository.count() == 0) {
            seedUsers();
        } else {
            log.info("Users already exist — skipping user seeding.");
        }

       
        if (transactionRepository.count() == 0) {
            seedTransactions();
        } else {
            log.info("Transactions already exist — skipping transaction seeding.");
        }

        log.info("DataSeeder complete.");
    }

    
    private void seedUsers() {
        log.info("Seeding default users...");

        // Admin user — full system access
        User admin = User.builder()
                .username("admin")
                .email("admin@finance.com")
                .passwordHash(passwordEncoder.encode("admin123"))  // BCrypt hash
                .role(Role.ADMIN)
                .status("ACTIVE")
                .build();
        userRepository.save(admin);
        log.info("Created admin user: admin@finance.com / admin123");

        // Analyst user — can view + create/edit transactions
        User analyst = User.builder()
                .username("alice_analyst")
                .email("alice@finance.com")
                .passwordHash(passwordEncoder.encode("alice123"))
                .role(Role.ANALYST)
                .status("ACTIVE")
                .build();
        userRepository.save(analyst);
        log.info("Created analyst user: alice@finance.com / alice123");

        // Viewer user — read-only access
        User viewer = User.builder()
                .username("bob_viewer")
                .email("bob@finance.com")
                .passwordHash(passwordEncoder.encode("bob12345"))
                .role(Role.VIEWER)
                .status("ACTIVE")
                .build();
        userRepository.save(viewer);
        log.info("Created viewer user: bob@finance.com / bob12345");
    }

    /**
     * seedTransactions - Creates sample financial records to demonstrate dashboard
     */
    private void seedTransactions() {
        log.info("Seeding sample transactions...");

        // Get admin user to set as creator of sample transactions
        User admin = userRepository.findByEmailIgnoreCase("admin@finance.com")
                .orElseThrow(() -> new RuntimeException("Admin user not found during seeding"));

        LocalDate today = LocalDate.now();
        LocalDate lastMonth = today.minusMonths(1);
        LocalDate twoMonthsAgo = today.minusMonths(2);

        // ── Income Transactions ──────────────────────────────────────────────
        saveTransaction(admin, new BigDecimal("50000.00"), TransactionType.INCOME,
                "Salary", today.minusDays(5), "Monthly salary - April 2026");

        saveTransaction(admin, new BigDecimal("50000.00"), TransactionType.INCOME,
                "Salary", lastMonth.minusDays(5), "Monthly salary - March 2026");

        saveTransaction(admin, new BigDecimal("50000.00"), TransactionType.INCOME,
                "Salary", twoMonthsAgo.minusDays(5), "Monthly salary - February 2026");

        saveTransaction(admin, new BigDecimal("5000.00"), TransactionType.INCOME,
                "Freelance", today.minusDays(10), "Web design project payment");

        saveTransaction(admin, new BigDecimal("2000.00"), TransactionType.INCOME,
                "Investment", lastMonth, "Dividend income");

        // ── Expense Transactions ─────────────────────────────────────────────
        saveTransaction(admin, new BigDecimal("15000.00"), TransactionType.EXPENSE,
                "Rent", today.minusDays(1), "Monthly apartment rent");

        saveTransaction(admin, new BigDecimal("15000.00"), TransactionType.EXPENSE,
                "Rent", lastMonth.minusDays(1), "Monthly apartment rent");

        saveTransaction(admin, new BigDecimal("8000.00"), TransactionType.EXPENSE,
                "Food", today.minusDays(3), "Groceries and restaurants");

        saveTransaction(admin, new BigDecimal("2500.00"), TransactionType.EXPENSE,
                "Transport", today.minusDays(8), "Fuel and cab fares");

        saveTransaction(admin, new BigDecimal("3000.00"), TransactionType.EXPENSE,
                "Utilities", lastMonth.minusDays(15), "Electricity, water, internet");

        saveTransaction(admin, new BigDecimal("1500.00"), TransactionType.EXPENSE,
                "Entertainment", today.minusDays(12), "Movies, streaming services");

        saveTransaction(admin, new BigDecimal("4000.00"), TransactionType.EXPENSE,
                "Shopping", lastMonth.minusDays(20), "Clothing and electronics");

        log.info("Seeded {} sample transactions.", transactionRepository.count());
    }

    /** Helper method to create and save a transaction */
    private void saveTransaction(User createdBy, BigDecimal amount, TransactionType type,
                                 String category, LocalDate date, String notes) {
        Transaction t = Transaction.builder()
                .amount(amount)
                .type(type)
                .category(category)
                .date(date)
                .notes(notes)
                .createdBy(createdBy)
                .deleted(false)
                .build();
        transactionRepository.save(t);
    }
}
