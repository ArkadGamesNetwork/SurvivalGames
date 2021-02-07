package fr.mrcubee.survivalgames;

import fr.mrcubee.langlib.Lang;
import fr.mrcubee.survivalgames.command.LangCommand;
import fr.mrcubee.survivalgames.listeners.RegisterListeners;
import fr.mrcubee.survivalgames.step.StepManager;
import fr.mrcubee.survivalgames.step.steps.FeastStep;
import fr.mrcubee.survivalgames.step.steps.GameStep;
import fr.mrcubee.survivalgames.step.steps.PvpStep;
import fr.mrcubee.survivalgames.world.BiomeReplacer;
import fr.mrcubee.util.FileUtil;
import fr.mrcubee.world.WorldSpawnSetup;

import java.io.File;

import net.arkadgames.survivalgame.sql.DataBaseManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class SurvivalGames extends JavaPlugin {

	private Game game;
	private Timer timer;

	@Override
	public void onLoad() {
		saveDefaultConfig();

		BiomeReplacer.removeOcean();
		this.game = new Game(this);
		File worldFile = new File("./" + this.game.getGameSetting().getWorldName());
		File netherFile = new File("./" + this.game.getGameSetting().getWorldName() + "_nether");
		File endFile = new File("./" + this.game.getGameSetting().getWorldName() + "_the_end");
		FileUtil.delete(worldFile);
		FileUtil.delete(netherFile);
		FileUtil.delete(endFile);
	}

	@Override
	public void onEnable() {
		LangCommand langCommand = new LangCommand();
		PluginCommand pluginCommand;
		StepManager stepManager;

		Lang.setDefaultLang("EN_us");
		this.game.init();

		// **Commands** //
		pluginCommand = getCommand("lang");
		pluginCommand.setExecutor(langCommand);
		pluginCommand.setTabCompleter(langCommand);

		// **STEPS** //
		stepManager = this.game.getStepManager();
		stepManager.registerStep(PvpStep.create(this.game));
		stepManager.registerStep(GameStep.create(this.game));
		stepManager.registerStep(FeastStep.create(this.game));
		// **END STEPS** //

		this.timer = new Timer(this);

		// **WORLD SETUP** //
		if (!WorldSpawnSetup.setup(this.game.getGameWorld(), this.game.getGameSetting().getLoadSize(), this.getLogger())) {
			getServer().shutdown();
			return;
		}
		this.game.getSpawnTerrainForming().runTaskTimer(this, 0L, 10L);
		// **END WORLD SETUP**//

		RegisterListeners.register(this);
		/*
		try {
			dataBase = new DataBase(this);
		} catch (SQLException e) {
			this.getLogger().severe("Error to connect to DataBase !");
			e.printStackTrace();
			this.getServer().shutdown();
			return;
		}boolean
		*/
		getGame().setGameStats(GameStats.WAITING);
		this.timer.runTaskTimer(this, 0L, 20L);
	}

	@Override
	public void onDisable() {
		File logsFile = new File("./logs");
		FileUtil.delete(logsFile);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.isOp())
			return false;
		if (getGame().getGameStats() == GameStats.WAITING)
			getGame().forceStart();
		if (getGame().getGameStats() == GameStats.DURING)
			getGame().forcePvp();
		return true;
	}

	public DataBaseManager getDataBaseManager() {
		return this.game.getDataBaseManager();
	}

	public Game getGame() {
		return this.game;
	}
}
