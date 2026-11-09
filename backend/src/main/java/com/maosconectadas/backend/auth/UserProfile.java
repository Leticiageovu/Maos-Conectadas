package com.maosconectadas.backend.auth;

public record UserProfile(String name, String phone, String email) {
    public static UserProfile from(UserAccount account) {
        return new UserProfile(account.getName(), account.getPhone(), account.getEmail());
    }
}
