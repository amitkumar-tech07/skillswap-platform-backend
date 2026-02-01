package com.backend.skillswap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

@Slf4j
@SpringBootApplication
public class SkillswapApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(SkillswapApplication.class, args);

        // Get environment to access properties & profiles
        Environment env = context.getEnvironment();

        log.info("\n\n==========================================");
        log.info("   🚀 SkillSwap Backend Started Successfully!");
        log.info("   ➤ Application : {}", env.getProperty("spring.application.name", "SkillSwap"));
        log.info("   ➤ Active Profile(s): {}", env.getActiveProfiles().length == 0 ? "default" : String.join(", ", env.getActiveProfiles()));
        log.info("   ➤ Server Running On: http://localhost:{}", env.getProperty("server.port", "8080"));
        log.info("==========================================\n");
    }
}
