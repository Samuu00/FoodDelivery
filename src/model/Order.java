package model;

import java.time.LocalDateTime;

/** @param items riepilogo leggibile, es. "1× Diavola, 2× Tiramisù" */
public record Order(int id, int number, String status, double total, LocalDateTime date,
                    int restaurantId, String restaurantName, String restaurantImage, String items) {}
