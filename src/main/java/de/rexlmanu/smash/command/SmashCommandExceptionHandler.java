package de.rexlmanu.smash.command;

import de.rexlmanu.smash.game.user.GameUser;
import de.rexlmanu.smash.game.user.UserManager;
import io.github.revxrsal.cub.CommandContext;
import io.github.revxrsal.cub.CommandHandler;
import io.github.revxrsal.cub.CommandSubject;
import io.github.revxrsal.cub.HandledCommand;
import io.github.revxrsal.cub.bukkit.SenderNotPlayerException;
import io.github.revxrsal.cub.exception.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SmashCommandExceptionHandler implements CommandExceptionHandler {
    private static final String VOWELS = "aeiou";

    private UserManager userManager;

    public SmashCommandExceptionHandler(UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    public String toString() {
        return "DefaultExceptionHandler";
    }

    @Override
    public void handleException(@NotNull CommandSubject sender,
                                @NotNull CommandHandler commandHandler,
                                @Nullable HandledCommand command,
                                @NotNull List<String> arguments,
                                @NotNull CommandContext context,
                                @NotNull Throwable e,
                                boolean async) {

        GameUser gameUser = this.userManager.find(context.getSubject().getUUID());

        if (e instanceof InvalidValueException) {
            gameUser.sendMessage("command.invalid-value", ((InvalidValueException) e).getValueType().getId(), e.getMessage());
        } else if (e instanceof SenderNotPlayerException) {
            gameUser.sendMessage("command.only-player");
        } else if (e instanceof InvalidCommandException) {
            gameUser.sendMessage("command.invalid-command", ((InvalidCommandException) e).getInput());
        } else if (e instanceof MissingParameterException) {
            MissingParameterException mpe = (MissingParameterException) e;
            gameUser.sendMessage("command.missing-parameter", mpe.getParameter().getName());
        } else if (e instanceof MissingPermissionException) {
            gameUser.sendMessage("command.permission-missing");
        } else if (e instanceof ResolverFailedException) {
            ResolverFailedException rfe = (ResolverFailedException) e;
            gameUser.sendMessage("command.resolve-failed", rfe.getParameter().getName(), rfe.getInput());
        } else if (e instanceof SimpleCommandException) {
            gameUser.sendMessage("command.simple-command", e.getMessage());
        } else if (e instanceof CooldownException) {
            gameUser.sendMessage("command.cooldown", ((CooldownException) e).getTimeFancy());
        } else {
            gameUser.sendMessage("command.error");
            e.printStackTrace();
        }
    }
}
