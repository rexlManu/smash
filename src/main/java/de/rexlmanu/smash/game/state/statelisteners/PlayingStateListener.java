package de.rexlmanu.smash.game.state.statelisteners;

import com.google.inject.Inject;
import de.rexlmanu.smash.SmashPlugin;
import de.rexlmanu.smash.configuration.ConfigurationProvider;
import de.rexlmanu.smash.game.arena.Arena;
import de.rexlmanu.smash.game.arena.ArenaProvider;
import de.rexlmanu.smash.game.scoreboard.ScoreboardCreator;
import de.rexlmanu.smash.game.scoreboard.SidebarLayout;
import de.rexlmanu.smash.game.scoreboard.TablistLayout;
import de.rexlmanu.smash.game.state.GameProvider;
import de.rexlmanu.smash.game.state.GameStateListener;
import de.rexlmanu.smash.game.user.GameUser;
import de.rexlmanu.smash.game.user.UserManager;
import de.rexlmanu.smash.language.Language;
import de.rexlmanu.smash.utility.LanguageUtils;
import de.rexlmanu.smash.utility.PlayerUtils;
import de.rexlmanu.smash.utility.replace.ParameterModifier;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class PlayingStateListener extends GameStateListener implements Runnable, ScoreboardCreator {

    private Map<UUID, Long> onSmashCooldown = new HashMap<>();

    private SmashPlugin plugin;
    private GameProvider gameProvider;
    private ArenaProvider arenaProvider;
    private ConfigurationProvider configurationProvider;
    private UserManager userManager;

    private int startCountdown;
    private BukkitTask startCountdownTask, gameTask;
    private Arena arena;

    @Inject
    public PlayingStateListener(SmashPlugin plugin, GameProvider gameProvider, ArenaProvider arenaProvider, ConfigurationProvider configurationProvider) {
        this.plugin = plugin;
        this.gameProvider = gameProvider;
        this.arenaProvider = arenaProvider;
        this.configurationProvider = configurationProvider;
        this.userManager = this.plugin.userManager();
    }

    @Override
    public void onBegin() {
        this.arena = this.arenaProvider.arenas().stream().min((o1, o2) -> (int) (this.getVoteCountByArena(o2) - this.getVoteCountByArena(o1))).orElse(null);
        if (this.arena == null) {
            Bukkit.broadcastMessage("The arena could not be found via voting. Forgot to setup one?");
            return;
        }

        this.startCountdown = 5;

        this.userManager.users().forEach(user -> {
            Player player = user.asPlayer();
            PlayerUtils.resetPlayer(player);
            user.votedArena(null);
            player.setLevel(0);
            player.setExp(0);
            user.sendMessage("message.arena-was-won-voting", this.arena.name());
            user.sound(Sound.ENTITY_PLAYER_LEVELUP, 0.2f);

            if (user.selectedCharacter() == null) {
                user.selectedCharacter(this.plugin.characterHandler().random());
                user.sendMessage("message.force-character-selected", user.message(String.format("character.%s.name", user.selectedCharacter().name())));
            }
            user.live(configurationProvider.get(ConfigurationProvider.LIVES));

            this.arena.getRandomRespawnLocation().ifPresent(arenaLocation -> player.teleport(arenaLocation.toBukkitLocation()));

            player.setWalkSpeed(0);
        });

        this.plugin.scoreboardManager().update(this);

        this.startCountdownTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, this, 0, 20);
        this.gameTask = Bukkit.getScheduler().runTaskTimer(this.plugin, this::onTick, 0, 1);
    }

    @Override
    public void onEnd() {
        this.gameTask.cancel();
        this.onSmashCooldown.clear();
    }

    @Override
    public void run() {
        this.userManager.users().forEach(user -> {
            user.asPlayer().sendTitle(user.message("message.game-start.title", this.startCountdown), user.message("message.game-start.subtitle"), 5, 15, 0);
            user.sound(Sound.ENTITY_CHICKEN_EGG, 0.4f);
        });
        this.startCountdown--;

        if (this.startCountdown == 0) {
            this.startCountdownTask.cancel();
            this.userManager.users().forEach(user -> {
                user.asPlayer().sendTitle(user.message("message.game-started.title"), user.message("message.game-started.subtitle"), 0, 20, 10);
                user.asPlayer().setAllowFlight(true);
                user.asPlayer().setWalkSpeed(0.2f);
                user.asPlayer().setExp(1);
            });
            return;
        }
    }

    private void onTick() {
        this.userManager.users().forEach(user -> {
            Player player = user.asPlayer();
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(user.message("message.playing.actionbar", user.damage() + "%")));
            if (player.getExp() < 1) {
                float newExp = player.getExp() + ((float) 1 / (float) (this.configurationProvider.get(ConfigurationProvider.DOUBLE_JUMP_COOLDOWN) * 20));
                if (newExp > 1) {
                    player.setExp(1);
                    player.setAllowFlight(true);
                } else {
                    player.setExp(newExp);
                }
            }

            if (this.onSmashCooldown.containsKey(player.getUniqueId()) && (this.onSmashCooldown.get(player.getUniqueId()) - System.currentTimeMillis()) < 0) {
                this.onSmashCooldown.remove(player.getUniqueId());
            }
        });
    }

    private long getVoteCountByArena(Arena arena) {
        return this.userManager.users().stream().filter(user -> user.votedArena() != null && user.votedArena().equals(arena)).count();
    }

    @Override
    public SidebarLayout getSidebarLayout(GameUser user) {
        return SidebarLayout
                .builder()
                .lines(LanguageUtils.toList(ParameterModifier
                        .of(user.message("scoreboard.sidebar.playing"))
                        .replace("%lives", "❤❤❤")
                        .replace("%kills", "0")
                        .replace("%aliveplayers", String.valueOf(this.userManager.users().size()))
                        .toString()))
                .title("scoreboard.title.playing")
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

    @EventHandler
    public void handle(PlayerMoveEvent event) {
        if (this.startCountdown > 0) {
            Location from = event.getFrom();
            Location to = event.getTo();
            to.setX(from.getX());
            to.setY(from.getY());
            to.setZ(from.getZ());
            return;
        }

        if (!event.getTo().clone().subtract(0, 1, 0).getBlock().getType().equals(Material.AIR) && !this.userManager.isSpectator(event.getPlayer())) {

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

    @EventHandler
    public void handle(PlayerDeathEvent event) {
        Player player = event.getEntity();
        GameUser user = this.userManager.find(player.getUniqueId());
        user.damage(0);
        user.live(user.live() - 1);

        event.setKeepInventory(true);
        event.setKeepLevel(true);
        player.spigot().respawn();
        player.getInventory().clear();
        player.setLevel(0);

        this.plugin.scoreboardManager().updateSidebar(user, this);

        if (player.getKiller() == null) {
            user.sendMessage("message.suicide");
        } else {
            user.sendMessage("message.killed-by", player.getKiller().getName());
        }
        if (user.live() > 0) {
            this.arena.getRandomRespawnLocation().ifPresent(arenaLocation -> player.teleport(arenaLocation.toBukkitLocation()));
            player.setExp(1f);
            player.setAllowFlight(true);
            return;
        }
        user.sendMessage("message.all-lives-gone");

        player.setGameMode(GameMode.SPECTATOR);
        user.spectator(true);
    }

    @EventHandler
    public void handle(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;
        if(this.startCountdown > 0) return;

        Player player = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        GameUser gameUser = this.userManager.find(player.getUniqueId());
        gameUser.damage(gameUser.damage() + ThreadLocalRandom.current().nextInt(5, 7));

        Vector vector = player.getLocation().toVector().subtract(damager.getLocation().toVector())
                .multiply(this.calculateKnockbackFactor(gameUser));
        vector.setY(this.configurationProvider.get(ConfigurationProvider.KNOCK_BACK_HEIGHT));
        player.setVelocity(vector);

        event.setDamage(0);
    }

    private float calculateKnockbackFactor(GameUser user) {
        return user.damage() / (float) this.configurationProvider.get(ConfigurationProvider.KNOCK_BACK_FACTOR);
    }

    @EventHandler
    public void handle(PlayerToggleFlightEvent event) {
        if (this.startCountdown > 0) return;
        Player player = event.getPlayer();
        if (this.userManager.isSpectator(player) || player.getExp() != 1) return;
        event.setCancelled(true);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setExp(0);

        Vector direction = player.getEyeLocation().getDirection();
        direction.multiply(this.configurationProvider.get(ConfigurationProvider.DOUBLE_JUMP_FACTOR));
        if (direction.getY() > 1) {
            direction.setY(1);
        }
        if (direction.getY() < 0) {
            direction.setY(0);
        }
        // direction.setY(this.configurationProvider.get(ConfigurationProvider.DOUBLE_JUMP_HEIGHT));

        player.setVelocity(direction);
        player.getLocation().getWorld().playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, 0, 20);
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1, 0.5f);
    }

    @EventHandler
    public void handle(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!event.isSneaking() || !PlayerUtils.isBlocksInAir(event.getPlayer(), 2) || this.userManager.isSpectator(player) || this.onSmashCooldown.containsKey(player.getUniqueId())) {
            return;
        }

        player.setVelocity(player.getLocation().toVector().normalize().multiply(0.5).setY(-3));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 2, 1);
        player.getWorld().playEffect(player.getLocation(), Effect.WITHER_BREAK_BLOCK, 1, 3);
        this.onSmashCooldown.put(player.getUniqueId(), System.currentTimeMillis() + (1000 * this.configurationProvider.get(ConfigurationProvider.SMASH_COOLDOWN)));
        player.getNearbyEntities(3, 10, 3)
                .stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> ((Player) entity))
                .forEach(target -> {
                    Vector vector = target.getLocation().toVector().subtract(player.getLocation().toVector())
                            .multiply(this.calculateKnockbackFactor(this.userManager.find(target.getUniqueId())));

                    vector.setY(this.configurationProvider.get(ConfigurationProvider.KNOCK_BACK_HEIGHT));
                    target.setVelocity(vector);
                });
    }

    @EventHandler
    public void handle(PlayerInteractEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void handle(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (!event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
            event.setCancelled(true);
        }

        if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            player.setHealth(0);
        }

    }

    /*
        Events that only get cancelled
     */

    @EventHandler
    public void handle(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void handle(InventoryClickEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void handle(BlockPlaceEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void handle(BlockBreakEvent event) {
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
