package me.yukino.fireprocess;

import me.yukino.fireprocess.model.CellPredictModelProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FireprocessApplication {

    public static void main(String[] args) {
        CellPredictModelProvider.initModel();
        SpringApplication.run(FireprocessApplication.class, args);
    }

}
