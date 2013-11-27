package com.liddev.teleportmadness.Managers;

import com.liddev.teleportmadness.Commands.Home;
import com.liddev.teleportmadness.Commands.MadCommand;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

/**
 *
 * @author Renlar < liddev.com >
 */
public enum MadCommandManager implements CommandEnum, MadCommand{

    HOME(HomeCommandManager.class, true, null, 0, -1, "h", "home"),;
        
    private MadCommand command;
    private int min, max;
    private String permission;
    private boolean console;
    private ArrayList<String> aliases;

    MadCommandManager(Class<?> cClass, boolean console, String permission, int minArgs, int maxArgs, String... aliases) {
        try {
            this.command = (MadCommand) cClass.newInstance();
        } catch (InstantiationException e) {
            Bukkit.getServer().getLogger().log(Level.SEVERE, "InstantiationException: Error instancing command!");
        } catch (IllegalAccessException e) {
            Bukkit.getServer().getLogger().log(Level.SEVERE, "IllegalAccessException: Error instancing command!");
        }

        this.console = console;
        this.permission = permission;
        this.min = minArgs;
        this.max = maxArgs;
        this.aliases = new ArrayList<String>();
        this.aliases.addAll(Arrays.asList(aliases));

    }

    @Override
    public boolean run(CommandSender sender, String[] commands) {
        if (sender != null) {
            if (sender instanceof Player) {
                Bukkit.getServer().getLogger().log(Level.FINEST,
                        "{0} issued command: {1}", new Object[]{sender.getName(),
                            this.get().getClass()});
            } else if (sender instanceof ConsoleCommandSender) {
                Bukkit.getServer().getLogger().finest("Console Issued Command.");
            } else {
                Bukkit.getServer().getLogger().finest("Madness is here the sender "
                        + "could not be found");
            }
        } else {
            sender = Bukkit.getConsoleSender();
        }
        if (permission != null && (sender instanceof Player) && !sender.hasPermission(permission)) {
            Bukkit.getServer().getLogger().log(Level.WARNING,
                    "{0} tried to run command {1}, but does not have permission, {3}",
                    new Object[]{sender.getName(), command.getClass(), permission});
            noPermission(sender);
            return false;
        }
        try {
            return command.run(sender, commands);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isValid(String[] command) {
        String testString = "";
        boolean valid = false;
        if (this.command instanceof CommandEnum) {
            String[] args = new String[command.length - 1];
            for (int i = 1; i < command.length; i++) {
                args[i - 1] = command[i];
            }
            ((CommandEnum) this.command).isValid(command);
        }
        if (aliases.contains(testString)) {
            valid = true;
        }
        for (String s : command) {
            testString += s;
            for (String n : aliases) {
                if (testString.equalsIgnoreCase(n)) {
                    valid = true;
                }
            }
        }
        return valid;
    }

    @Override
    public String getDesc() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getHelp() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public MadCommand get() {
        return command;
    }

    public List<String> getAliases() {
        return (List<String>) aliases.clone();
    }

    //TODO: load messages from config file.
    public void noPermission(CommandSender sender) {
        sender.sendMessage("Monkies with Banannas! You don't have a tail to do that.");
    }

    public void wrongLength(CommandSender sender) {
        sender.sendMessage("You have the wrong number of rocks for that potato.");
    }

    public void notPlayer(CommandSender sender) {
        sender.sendMessage("This is not the command you are looking for.");
    }

    public static String[] reduceArgs(String[] args) {
        String[] newArgs = new String[args.length - 1];
        for (int i = 1; i < args.length; i++) {
            newArgs[i - 1] = args[i];
        }
        return newArgs;
    }

}