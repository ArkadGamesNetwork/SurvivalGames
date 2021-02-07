package fr.mrcubee.survivalgames.listeners.player;

import java.util.Set;

import fr.mrcubee.langlib.Lang;
import fr.mrcubee.scoreboard.Score;
import fr.mrcubee.survivalgames.Game;
import fr.mrcubee.survivalgames.kit.KitManager;
import net.arkadgames.survivalgame.sql.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import fr.mrcubee.survivalgames.GameStats;
import fr.mrcubee.survivalgames.SurvivalGames;
import fr.mrcubee.survivalgames.kit.Kit;

public class PlayerDeath implements Listener {

    private final SurvivalGames survivalGames;

    public PlayerDeath(SurvivalGames survivalGames) {
        this.survivalGames = survivalGames;
    }

    private String getKitsName(KitManager kitManager, Player player) {
        Kit[] kits;
        StringBuilder stringBuilder;

        if (kitManager == null || player == null)
            return null;
        kits = kitManager.getKitByPlayer(player);
        if (kits == null || kits.length <= 0)
            return "no kit";
        stringBuilder = new StringBuilder();
        stringBuilder.append(kits[0].getDisplayName(player));
        for (int i = 1; i < kits.length; i++) {
            stringBuilder.append(ChatColor.GRAY.toString());
            stringBuilder.append(", ");
            stringBuilder.append(kits[i].toString());
        }
        return stringBuilder.toString();
    }

    private void victory(Game game) {
        Set<Player> players;

        if (game == null)
            return;
        players = game.getPlayerInGame();
        if (players == null || players.isEmpty())
            return;
        for (Player player : Bukkit.getOnlinePlayers())
            player.playSound(player.getLocation(), Sound.ENDERDRAGON_DEATH, 100, 1);
        players.forEach(player -> {
            PlayerData playerData = game.getDataBaseManager().getPlayerData(player.getUniqueId());

            if (playerData != null) {
                playerData.setLastWin(true);
                playerData.setWin(playerData.getWin() + 1);
                playerData.setPlayTime(playerData.getPlayTime() + ((System.currentTimeMillis() - game.getGameStartTime()) / 1000));
            }
            game.broadcastMessage("broadcast.player.win", "&c%s &6WIN THE GAME !!!", true, player.getName());
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerDeathEvent(PlayerDeathEvent event) {
        Game game = this.survivalGames.getGame();
        KitManager kitManager;
        PlayerData playerData;
        Score score;

        if (game.getGameStats() != GameStats.DURING)
            return;
        if (event.getEntity().getKiller() != null) {
            score = game.getPluginScoreBoardManager().getKillObjective().getScore(event.getEntity().getKiller().getName());
            game.getPluginScoreBoardManager().getKillObjective().setScore(event.getEntity().getKiller().getName(),
            (score == null) ? 1 : score.getScore() + 1);
        }
        kitManager = game.getKitManager();
        playerData = game.getDataBaseManager().getPlayerData(event.getEntity().getUniqueId());
        if (playerData != null) {
            playerData.setLastWin(false);
            playerData.setPlayTime(playerData.getPlayTime() + ((System.currentTimeMillis() - game.getGameStartTime()) / 1000));
        }
        event.getDrops().removeIf(itemStack -> !kitManager.canLostItem(event.getEntity(), itemStack));
        event.setDeathMessage(null);
        event.getEntity().setMaxHealth(20);
        event.getEntity().setHealth(event.getEntity().getMaxHealth());
        game.addSpectator(event.getEntity());
        if (game.getNumberPlayer() > 1) {
            for (Player player : Bukkit.getOnlinePlayers())
                player.playSound(player.getLocation(), Sound.AMBIENCE_THUNDER, 100, 1);
            event.setDeathMessage(null);
            for (Player player : Bukkit.getOnlinePlayers())
                player.sendMessage(Lang.getMessage(player, "broadcast.player.death", "&c%s(&7%s&c) &6is Dead ! There are &c%d players left.", true,
                        event.getEntity().getName(), getKitsName(kitManager, player), game.getNumberPlayer()));
        } else {
            for (Player player : Bukkit.getOnlinePlayers())
                player.sendMessage(Lang.getMessage(player, "broadcast.player.lastDeath", "&c%s(&7%s&c) &6is Dead !", true,
                        event.getEntity().getName(), getKitsName(kitManager, player)));
            victory(game);
        }
        game.getKitManager().removeKit(event.getEntity());
    }
}
