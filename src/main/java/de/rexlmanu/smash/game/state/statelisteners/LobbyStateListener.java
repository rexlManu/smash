package de.rexlmanu.smash.game.state.statelisteners;

import com.google.inject.Inject;
import de.rexlmanu.smash.SmashPlugin;
import de.rexlmanu.smash.configuration.ConfigurationProvider;
import de.rexlmanu.smash.configuration.element.ConfigurationItem;
import de.rexlmanu.smash.game.arena.ArenaProvider;
import de.rexlmanu.smash.game.events.UserChangeLocaleEvent;
import de.rexlmanu.smash.game.events.UserSelectedCharacterEvent;
import de.rexlmanu.smash.game.inventory.ArenaSelectorMenu;
import de.rexlmanu.smash.game.inventory.CharacterSelectorMenu;
import de.rexlmanu.smash.game.item.ConfigurationItemHandler;
import de.rexlmanu.smash.game.scoreboard.ScoreboardCreator;
import de.rexlmanu.smash.game.scoreboard.SidebarLayout;
import de.rexlmanu.smash.game.scoreboard.TablistLayout;
import de.rexlmanu.smash.game.state.GameProvider;
import de.rexlmanu.smash.game.state.GameState;
import de.rexlmanu.smash.game.state.GameStateListener;
import de.rexlmanu.smash.game.user.GameUser;
import de.rexlmanu.smash.game.user.UserManager;
import de.rexlmanu.smash.language.Language;
import de.rexlmanu.smash.utility.BiValue;
import de.rexlmanu.smash.utility.LanguageUtils;
import de.rexlmanu.smash.utility.PlayerUtils;
import de.rexlmanu.smash.utility.menu.Menu;
import de.rexlmanu.smash.utility.menu.SingleMenu;
import de.rexlmanu.smash.utility.replace.ParameterModifier;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class LobbyStateListener extends GameStateListener implements ScoreboardCreator, Runnable {

    private Map<ItemStack, BiValue<UUID, Consumer<PlayerInteractEvent>>> itemStackInteraction = new HashMap<>();

    private ConfigurationProvider configurationProvider;
    private SmashPlugin plugin;
    private GameProvider gameProvider;
    private ArenaProvider arenaProvider;

    private UserManager userManager;

    private int countdown;
    private BukkitTask countdownTask = null;
    private Menu arenaSelectorMenu = null;
    private SingleMenu characterSelectorMenu = null;

    @Inject
    public LobbyStateListener(ConfigurationProvider configurationProvider, SmashPlugin plugin, GameProvider gameProvider, ArenaProvider arenaProvider) {
        this.configurationProvider = configurationProvider;
        this.plugin = plugin;
        this.gameProvider = gameProvider;
        this.arenaProvider = arenaProvider;
        this.userManager = this.plugin.userManager();
    }

    @Override
    public void onBegin() {
        Bukkit.getWorlds().forEach(world -> {
            world.setDifficulty(Difficulty.PEACEFUL);
            world.setFullTime(0);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            world.setGameRule(GameRule.DO_TILE_DROPS, false);
            world.setGameRule(GameRule.DO_FIRE_TICK, false);
            world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
            world.setGameRule(GameRule.MOB_GRIEFING, false);
            world.setThundering(false);
        });

        this.arenaSelectorMenu = new ArenaSelectorMenu(this.plugin, this.arenaProvider);
        this.characterSelectorMenu = new CharacterSelectorMenu(this.plugin);

        this.countdown = this.configurationProvider.get(ConfigurationProvider.LOBBY_COUNTDOWN);
        this.countdownTask = Bukkit.getScheduler().runTaskTimer(this.plugin, this, 0, 20);

        this.userManager.users().forEach(GameUser::reset);
    }

    @Override
    public void onEnd() {
        this.itemStackInteraction.clear();
        this.countdownTask.cancel();
        this.arenaSelectorMenu.unregister();
        this.characterSelectorMenu.unregister();
    }

    @Override
    public void run() {
        int userSize = this.userManager.users().size();

        Integer countdownResetValue = this.configurationProvider.get(ConfigurationProvider.LOBBY_COUNTDOWN);
        if (userSize < this.configurationProvider.get(ConfigurationProvider.MIN_PLAYER) || this.userManager.users().stream().anyMatch(GameUser::editor)) {
            this.countdown = countdownResetValue;
        } else {
            this.countdown--;
        }
        // Start game
        if (this.countdown == 0) {
            this.gameProvider.setGame(GameState.PLAYING);
            return;
        }

        this.userManager.users().forEach(user -> {
            this.plugin.scoreboardManager().updateSidebar(user, this);
            Player player = user.asPlayer();
            player.setLevel(this.countdown);
            player.setExp((float) this.countdown / (float) countdownResetValue);

            if (this.countdown < 6 || this.countdown == 10) {
                user.sendMessage("message.lobby-countdown-start." + (this.countdown == 1 ? "singular" : "plural"), this.countdown);
            }
        });
    }

    @Override
    public SidebarLayout getSidebarLayout(GameUser user) {
        return SidebarLayout
                .builder()
                .lines(LanguageUtils.toList(ParameterModifier
                        .of(user.message("scoreboard.sidebar.lobby"))
                        .replace("%character", user.selectedCharacter() == null ? ChatColor.GRAY + "???" : user.message(String.format("character.%s.name", user.selectedCharacter().name())))
                        .replace("%countdown", String.valueOf(this.countdown))
                        .replace("%players", String.valueOf(this.userManager.users().size()))
                        .toString()))
                .title("scoreboard.title.lobby")
                .build();
    }

    @Override
    public TablistLayout getTablistLayout(GameUser user, Language language) {
        return TablistLayout
                .builder()
                .prefix("")
                .chatColor(org.bukkit.ChatColor.GRAY)
                .options(Map.of(
                        Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER
                ))
                .suffix(user.selectedCharacter() == null ? "" : (ChatColor.DARK_GRAY + " × " + language.translate(String.format("character.%s.name", user.selectedCharacter().name()))))
                .build();
    }


    private void giveLobbyItems(GameUser user) {
        Player player = user.asPlayer();
        this.giveItem(user, this.configurationProvider.get(ConfigurationProvider.ARENA_SELECTOR_ITEM), (e) -> {
            this.arenaSelectorMenu.open(player);
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, 1);
        });
        this.giveItem(user, this.configurationProvider.get(ConfigurationProvider.CHARACTER_SELECTOR_ITEM), (e) -> {
            this.characterSelectorMenu.open(player);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        });
        this.giveItem(user, this.configurationProvider.get(ConfigurationProvider.BACK_TO_LOBBY_ITEM), e -> {
            player.playSound(player.getLocation(), Sound.ENTITY_TURTLE_LAY_EGG, 1, 1);
            user.leaveGame();
        });
    }

    private void giveItem(GameUser user, ConfigurationItem item, Consumer<PlayerInteractEvent> eventConsumer) {
        ItemStack itemStack = ConfigurationItemHandler.createItem(user, item);
        user.asPlayer().getInventory().setItem(item.slot(), itemStack);
        this.itemStackInteraction.put(itemStack, new BiValue<>(user.uuid(), eventConsumer));
    }

    @EventHandler
    public void handle(PlayerLoginEvent event) {
        Integer maxPlayers = this.configurationProvider.get(ConfigurationProvider.PLAYER_LIMIT);
        if (this.userManager.users().size() == maxPlayers && !event.getPlayer().hasPermission("smash.join-full-server")) {
            event.disallow(PlayerLoginEvent.Result.KICK_FULL, "Server is full.");
            return;
        }

        if (this.userManager
                .users()
                .stream()
                .map(GameUser::asPlayer)
                .allMatch(player -> player.hasPermission("smash.join-full-server")
                        || player.hasPermission("smash.join-full-server.bypass"))) {
            return;
        }

        Optional<GameUser> target = this.userManager
                .users()
                .stream()
                .filter(user -> !user.asPlayer().hasPermission("smash.join-full-server")
                && !user.asPlayer().hasPermission("smash.join-full-server.bypass"))
                .sorted((o1, o2) -> ThreadLocalRandom.current().nextBoolean() ? 1 : -1)
                .findAny();

        target.ifPresentOrElse(user -> {
            user.asPlayer().kickPlayer(user.message("message.kicked-by-vip"));
            event.allow();
        }, () -> event.disallow(PlayerLoginEvent.Result.KICK_FULL, "Server is full."));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handle(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        GameUser user = userManager.find(player.getUniqueId());
        PlayerUtils.resetPlayer(player);

        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(configurationProvider.get(ConfigurationProvider.SPAWN_LOCATION));

        this.plugin.scoreboardManager().update(this);

        ConfigurationItemHandler.createItem(user, this.configurationProvider.get(ConfigurationProvider.ARENA_SELECTOR_ITEM));

        this.giveLobbyItems(user);

        this.userManager.users().forEach(target -> target.sendMessage("message.player-joined", player.getName()));
    }

    @EventHandler
    public void handle(UserSelectedCharacterEvent event) {
        this.plugin.scoreboardManager().updateSidebar(event.user(), this);
        this.userManager.users().forEach(user -> {
            this.plugin.scoreboardManager().updateTablist(user, this);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handle(UserChangeLocaleEvent event) {
        this.giveLobbyItems(event.user());
        this.plugin.scoreboardManager().updateSingle(event.user(), this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handle(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.plugin.scoreboardManager().update(this);
        for (Map.Entry<ItemStack, BiValue<UUID, Consumer<PlayerInteractEvent>>> itemStackBiValueEntry : this.itemStackInteraction
                .entrySet()
                .stream()
                .filter(itemStackBiValueEntry -> player.getUniqueId().equals(itemStackBiValueEntry.getValue().first())).collect(Collectors.toList())) {
            this.itemStackInteraction.remove(itemStackBiValueEntry.getKey());
        }

        this.userManager.users().forEach(target -> target.sendMessage("message.player-left", player.getName()));
    }

    @EventHandler
    public void handle(PlayerInteractEvent event) {
        if (this.userManager.find(event.getPlayer().getUniqueId()).editor()) {
            return;
        }
        event.setCancelled(true);

        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        if (!event.hasItem()) {
            return;
        }
        if (this.itemStackInteraction.containsKey(event.getItem())) {
            this.itemStackInteraction.get(event.getItem()).second().accept(event);
            return;
        }
    }

    @EventHandler
    public void handle(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        event.setFormat(ChatColor.translateAlternateColorCodes(
                '&',
                this.configurationProvider.get(ConfigurationProvider.LOBBY_CHAT_FORMAT)
                        .replace("%player", player.getName())
                        .replace("%message", event.getMessage())
        ));
    }

    /*
        Events that only get cancelled
     */

    @EventHandler
    public void handle(PlayerSwapHandItemsEvent event) {
        if (this.userManager.find(event.getPlayer().getUniqueId()).editor()) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void handle(InventoryClickEvent event) {
        if (this.userManager.find(event.getWhoClicked().getUniqueId()).editor()) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void handle(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void handle(BlockPlaceEvent event) {
        if (this.userManager.find(event.getPlayer().getUniqueId()).editor()) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void handle(BlockBreakEvent event) {
        if (this.userManager.find(event.getPlayer().getUniqueId()).editor()) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void handle(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void handle(EntityPickupItemEvent event) {
        event.setCancelled(true);
    }
}
