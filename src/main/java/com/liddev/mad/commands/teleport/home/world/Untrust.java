package com.liddev.mad.commands.teleport.home.world;

import com.liddev.mad.core.MadCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author Renlar <liddev.com>
 */
public class Untrust extends MadCommand {

  public Untrust(ConfigurationSection config) {
    super(config);
  }

  @Override
  public boolean run(CommandSender sender, String alias, String[] args) {
    return false;
  }

}
