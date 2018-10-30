package banbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;

@SpringBootApplication
public class BanBotApplication {

    public static void main(String[] args) {
        //Add this line to initialize bots context
        ApiContextInitializer.init();
        SpringApplication.run(BanBotApplication.class, args);
    }

}
