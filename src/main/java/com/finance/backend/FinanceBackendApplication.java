package com.finance.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class FinanceBackendApplication {

    public static void main(String[] args) {
        // SpringApplication.run() bootstraps the entire Spring application context
        SpringApplication.run(FinanceBackendApplication.class, args);

        // Print a welcome message after startup
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║   Finance Data Processing API - RUNNING  ║");
        System.out.println("╠══════════════════════════════════════════╣");
        System.out.println("║  URL     : http://localhost:3000          ║");
        System.out.println("║  Health  : http://localhost:3000/health   ║");
        System.out.println("║  H2 DB   : http://localhost:3000/h2-console║");
        System.out.println("║                                           ║");
        System.out.println("║  Admin   : admin@finance.com              ║");
        System.out.println("║  Password: admin123                       ║");
        System.out.println("╚══════════════════════════════════════════╝\n");
    }
}
