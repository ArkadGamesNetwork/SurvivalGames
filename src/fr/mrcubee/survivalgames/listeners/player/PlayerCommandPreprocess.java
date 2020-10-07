package fr.mrcubee.survivalgames.listeners.player;

import fr.mrcubee.survivalgames.Game;
import fr.mrcubee.survivalgames.GameStats;
import fr.mrcubee.survivalgames.SurvivalGames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerCommandPreprocess implements Listener {

	private final SurvivalGames survivalGames;

	public PlayerCommandPreprocess(SurvivalGames survivalGames) {
		this.survivalGames = survivalGames;
	}
	
	@EventHandler
	public void playerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Game game = this.survivalGames.getGame();
		Player player = event.getPlayer();
		Player target;
		String[] args = event.getMessage().split(" ");

		if ((!player.isOp())
		|| (!player.getGameMode().equals(GameMode.SPECTATOR) || game.getGameStats() == GameStats.DURING))
			return;
		if (args.length < 1 || (!args[0].equalsIgnoreCase("/tp")))
			return;
		event.setCancelled(true);
		if (args.length < 2)
			return;
		if ((target = Bukkit.getPlayer(args[1])) == null) {
			player.sendMessage(ChatColor.RED + "The player is not connected or does not exist.");
			return;
		}
		player.teleport(target);
	}
	
}
