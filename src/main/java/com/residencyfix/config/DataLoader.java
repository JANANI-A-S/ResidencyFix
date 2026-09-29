package com.residencyfix.config;

import com.residencyfix.model.*;
import com.residencyfix.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public DataLoader(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        categoryRepository.findByNameIgnoreCase("Plumbing")
                .orElseGet(() -> categoryRepository.save(new Category("Plumbing", "Taps, pipes, leakage, showers, drainage")));
        categoryRepository.findByNameIgnoreCase("Electrical")
                .orElseGet(() -> categoryRepository.save(new Category("Electrical", "Fans, lighting, sockets, wiring, switchboards")));
        categoryRepository.findByNameIgnoreCase("Cleaning")
                .orElseGet(() -> categoryRepository.save(new Category("Cleaning", "Washroom hygiene, room sanitization, trash clearance")));
    }
}
