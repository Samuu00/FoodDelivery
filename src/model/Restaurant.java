package model;

import java.util.List;

/** @param categories categorie del menu così come sono nel DB (Pizza, Sushi, Panini, Dolci) */
public record Restaurant(int id, String name, String description, String image, String address, List<String> categories) {}
