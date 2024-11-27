package edu.example.wayfarer.dto;


import lombok.Getter;
import lombok.Setter;

public class KakaoDTO {

    @Getter
    @Setter
    public static class OAuthToken {
        private String access_token;
        private String token_type;
        private String refresh_token;
        private int expires_in;
        private String scope;
        private int refresh_token_expires_in;
    }

    @Getter
    @Setter
    public static class KakaoProfile {
        private Long id;
        private String connected_at;
        private Properties properties;
        private KakaoAccount kakao_account;

        @Getter
        @Setter
        public static class Properties {
            private String nickname;
            private String profile_image;
            private String thumbnail_image;
        }

        @Getter
        @Setter
        public static class KakaoAccount {
            private String email;
            private Boolean is_email_verified;
            private Boolean has_email;
            private Boolean profile_nickname_needs_agreement;
            private Boolean profile_image_needs_agreement;
            private Boolean email_needs_agreement;
            private Boolean is_email_valid;
            private Profile profile;

            @Getter
            @Setter
            public static class Profile {
                private String nickname;
                private Boolean is_default_nickname;
                private String thumbnail_image_url;
                private String profile_image_url;
                private Boolean is_default_image;
            }
        }
    }
}