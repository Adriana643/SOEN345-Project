package com.soen345.Ticket.Reservation.Application.config;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

@Configuration
public class FirebaseConfig {
    @Bean
    public void initializeFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            ClassLoader classLoader = FirebaseConfig.class.getClassLoader();
            File file = new File(Objects.requireNonNull(
                    classLoader.getResource("serviceAccountKey.json")).getFile());

            FileInputStream serviceAccount = new FileInputStream(file);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
        }
    }
}
