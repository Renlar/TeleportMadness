package com.liddev.teleportmadness;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.liddev.teleportmadness.Managers.Data;
import com.liddev.teleportmadness.Managers.HomeCommandManager;
import com.liddev.teleportmadness.Managers.MadCommandManager;
import javax.persistence.PersistenceException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
import org.h2.jdbcx.JdbcDataSource;

/**
 *
 * @author Renlar < liddev.com >
 */
public class TeleportMadness extends JavaPlugin {

    private PlayerListener playerListener;
    private ClaimListener claimListener;
    private HomeCommandManager m;
    private Data dataManager;
    private EbeanServer ebean;

    public PluginDescriptionFile dsc;

    @Override
    public void onEnable() {
        dsc = getDescription();
        getLogger().log(Level.INFO, "Loading {0}.", new Object[]{dsc.getFullName()});
        if (!checkDepend()) {
            return;
        }

        setupDatabase();
        dataManager = new Data(this);
        dataManager.loadData();

        claimListener = new ClaimListener(this);
        playerListener = new PlayerListener(this);

        getServer().getPluginManager().registerEvents(playerListener, this);
        getServer().getPluginManager().registerEvents(claimListener, this);

        getLogger().log(Level.INFO, "{0}: Load Complete.", new Object[]{dsc.getFullName()});
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "{0}: Shuting Down, Saving data!", new Object[]{dsc.getFullName()});
        dataManager.saveAll();
        getLogger().log(Level.FINE, "{0}: Data Save Complete, Clearing Memory.", new Object[]{dsc.getFullName()});
        playerListener = null;
        dataManager.clearMemory();
        dsc = null;
        getLogger().log(Level.INFO, "{0}: Shutdown Complete.", new Object[]{dsc.getFullName()});
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] args) {
        if (cmnd.getName().equalsIgnoreCase("home")) {
            for (HomeCommandManager cm : HomeCommandManager.values()) {
                if (cm.isValid(args)) {
                    return cm.run(cs, args);
                }
            }
            return false;
        } else if (cmnd.getName().equalsIgnoreCase("mad")) {
            for (MadCommandManager cm : MadCommandManager.values()) {
                if (cm.isValid(args)) {
                    return cm.run(cs, args);
                }
            }
            return false;
        }
        return false;
    }

    public boolean checkDepend() {
        List<String> depends = dsc.getDepend();
        ArrayList<String> disabled = new ArrayList<String>();
        for (String s : depends) {
            if (getServer().getPluginManager().getPlugin(s) == null) {
                disabled.add(s);
            }
        }
        if (!disabled.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            builder.append("These dependencies are required and missing: ");
            for (String s : disabled) {
                builder.append(s).append(", ");
            }
            builder.append("\n Exiting plugin as can not continue.");
            getLogger().log(Level.SEVERE, builder.toString());
            return false;
        }
        return true;
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(PermissionGroup.class);
        list.add(Home.class);
        list.add(PlayerData.class);
        list.add(ClaimData.class);
        return list;
    }

    private boolean setupDatabase() {
        //constructDatabase();
        try {
            getDatabase().find(Home.class).findRowCount();
        } catch (PersistenceException ex) {
            System.out.println("Installing Database.");
            installDDL();
            return true;
        }
        return false;
    }

    @Override
    protected void installDDL() {
        SpiEbeanServer eServer = (SpiEbeanServer) getDatabase();
        DdlGenerator DDLGen = eServer.getDdlGenerator();

        DDLGen.runScript(true, DDLGen.generateCreateDdl());

        URL updateScript = getClass().getResource("/update.sql");
        if (updateScript != null) {
            BufferedReader in;
            StringBuilder s = new StringBuilder();
            String line;
            try {
                in = new BufferedReader(new InputStreamReader(updateScript.openStream()));
                while ((line = in.readLine()) != null) {
                    s.append(line);
                }
                in.close();
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Could not load update.sql from jar", e);
            }

            DDLGen.runScript(true, s.toString());
        }
    }

    /*@Override
    public EbeanServer getDatabase() {
        return ebean;
    }

    public void constructDatabase() {
        if (dsc.isDatabaseEnabled()) {
            ServerConfig db = new ServerConfig();

            db.setDefaultServer(false);
            db.setRegister(false);
            db.setClasses(getDatabaseClasses());
            db.setName(dsc.getName());

            JdbcDataSource dataSource = new JdbcDataSource();
            dataSource.setURL("jdbc:h2:" + getDataFolder() + "\\" + "Mad" + ";DB_CLOSE_ON_EXIT=FALSE");
            dataSource.setUser("TeleportMadness");
            dataSource.setPassword("");
            db.setDataSource(dataSource);

            ebean = EbeanServerFactory.create(db);
        }
    }*/

    public boolean isInGroup(Player p) {
        getDatabase().find(PlayerData.class).where().ieq("playerName", p.getName());
        return true;
    }

    public Data getDataManager() {
        return dataManager;
    }
}