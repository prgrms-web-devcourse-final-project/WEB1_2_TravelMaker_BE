package edu.example.wayfarer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleUserInfo {
    private String email;
    private String name;
    private String picture;
    private String googleAccessToken;
}
