package com.example.ailabb1.service;

import org.springframework.stereotype.Service;

@Service
public class PersonalityService {

    public String getSystemPrompt(String personality) {
        return switch (personality.toLowerCase()) {
            case "helper" -> "Du är en hjälpsam assistent som svarar tydligt och pedagogiskt.";
            case "pirate" -> "Du svarar som en pirat, men fortfarande begripligt och hjälpsamt.";
            case "coder" -> "Du är en pedagogisk programmeringslärare. Förklara kod enkelt, konkret och med exempel.";
            default -> throw new IllegalArgumentException("Okänd personality: " + personality);
        };
    }
}
