package com.liddev.mad.commands.teleport.home.server;

import com.liddev.mad.core.MadCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author Renlar <liddev.com>
 */
public class Remove extends MadCommand {

  public Remove(ConfigurationSection config) {
    super(config);
  }

  @Override
  public boolean run(CommandSender sender, String alias, String[] args) {
    return false;
  }

}
