package study.datajpa.repository;

import lombok.Getter;

@Getter
public class UsernameOnlyDto {
    private final String username;

    // !주의: 파라미터명이 엔티티 필드명이랑 일지해야 함
    public UsernameOnlyDto(String username) {
        this.username = username;
    }
}
