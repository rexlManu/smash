package de.rexlmanu.smash.game.scoreboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
@AllArgsConstructor
@Builder
public class SidebarLayout {

    private String title;
    private List<String> lines;

}
