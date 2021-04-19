package me.gabrideiros.bot.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class Account {

    private long user;
    private String nickname;
    private List<String> invites;
}
