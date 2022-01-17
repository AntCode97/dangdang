package com.ssafy.dangdang.domain.types;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Email implements Serializable {

    private String account;

    private String domain;

    public static Email of(String email) {
        if (email == null || email.isBlank() || email.isEmpty())
            return null;
        String[] arr = email.split("@");
        return Email.builder()
                .account(arr[0])
                .domain(arr[1])
                .build();
    }

    @Override
    public String toString() {
        return account + "@" + domain;
    }

}
