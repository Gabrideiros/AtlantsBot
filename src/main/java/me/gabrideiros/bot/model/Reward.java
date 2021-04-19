package me.gabrideiros.bot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Reward {

    private String key;
    private int amount;
    private List<String> commands;

}
