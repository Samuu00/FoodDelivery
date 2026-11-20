package model;

public record User(String username, String email) {
    public String getUsername() { return username; }
    public String getEmail() { return email; }
}
