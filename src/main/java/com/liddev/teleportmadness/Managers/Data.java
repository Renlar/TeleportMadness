package com.liddev.teleportmadness.Managers;

import com.liddev.teleportmadness.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.neodatis.odb.ODB;
import org.neodatis.odb.ODBFactory;
import org.neodatis.odb.Objects;
import org.neodatis.odb.core.query.IQuery;
import org.neodatis.odb.core.query.criteria.Where;
import org.neodatis.odb.impl.core.query.criteria.CriteriaQuery;

/**
 *
 * @author Renlar <liddev.com>
 */
//TODO: Consider adding sqlibrary to support other databases.
public class Data {

    private TeleportMadness mad;
    private HashMap<UUID, PlayerData> playerDataMap;
    private HashMap<UUID, WorldData> worldDataMap;
    private List<ClaimData> claimDataList;
    private JumpPoint serverHome;
    private final String DB_LOC;
    private ODB db = null;
    private static Data d;

    public Data(TeleportMadness mad) {
        this.mad = mad;
        d = this;
        DB_LOC = mad.getDataFolder().toString() + File.separator + "TeleportMadness.odb";
        playerDataMap = new HashMap<UUID, PlayerData>();
        worldDataMap = new HashMap<UUID, WorldData>();
        claimDataList = new ArrayList<ClaimData>();
    }

    public void openDatabase() {
        try {
            // Open the database
            db = ODBFactory.open(DB_LOC);
        } catch (Exception e) {
            mad.getLogger().log(Level.SEVERE, "{0} Error opening Database: {1}", new Object[]{mad.dsc.getFullName(), e});
        }
    }

    public void closeDatabase() {
        if (db != null) {
            // Close the database
            db.close();
        }
    }

    public void loadData() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayer(player);
        }
        loadServerJumps();
        for(World world : Bukkit.getWorlds()){
            loadWorld(world);
        }
    }

    public void savePlayer(PlayerData data) {
        //TODO: add loging and playerdata checks.
        db.store(data);
    }

    public void savePlayers(HashMap<UUID, PlayerData> players) {
        for (Player p : mad.getServer().getOnlinePlayers()) {
            savePlayer(players.get(p.getUniqueId()));
        }
    }

    public void loadPlayer(Player player) {
        if (playerDataMap.get(player.getUniqueId()) != null) {
            mad.getLogger().log(Level.FINEST, "Data for player {0} is alread loaded, reloading.", player);
            playerDataMap.remove(player.getUniqueId());
        } else {
            mad.getLogger().log(Level.FINEST, "Loading data for player {0}", player);
        }
        IQuery query = new CriteriaQuery(PlayerData.class, Where.equal("name", player.getName()));
        Objects<PlayerData> players = db.getObjects(query);
        PlayerData data = (PlayerData) players.getFirst();
        if (data == null) {
            data = new PlayerData();
            data.setPlayer(player);
        }
        db.store(data);
        playerDataMap.put(player.getUniqueId(), data);
    }

    public void unloadPlayer(Player player) {
        playerDataMap.remove(player.getUniqueId());
    }

    public void saveWorld(WorldData data) {
        db.store(data);
    }

    public void saveWorlds(HashMap<UUID, WorldData> worlds) {
        for (World w : mad.getServer().getWorlds()) {
            saveWorld(worlds.get(w.getUID()));
        }
    }

    public void loadWorld(World world) {
        IQuery query = new CriteriaQuery(WorldData.class, Where.equal("name", world.getName()));
        Objects<WorldData> worlds = db.getObjects(query);
        WorldData data = worlds.getFirst();
        if (data == null) {
            data = new WorldData();
            data.setName(world.getName());
        }
        worldDataMap.put(world.getUID(), data);
    }

    public void unloadWorld(World world) {
        worldDataMap.remove(world.getUID());
    }

    public void loadServerJumps() {
        IQuery query = new CriteriaQuery(JumpPoint.class, Where.equal("type", JumpType.GLOBAL));
        Objects<JumpPoint> points = db.getObjects(query);
        serverHome = points.getFirst();
    }
    
    public JumpPoint getServerHome(){
        return serverHome;
    }
    
    public void setServerHome(JumpPoint home){
        serverHome = home;
    }

    public void saveClaim(ClaimData data) {
        db.store(data);
    }

    public void saveClaims(List<ClaimData> claims) {
        for (ClaimData claim : claims) {
            saveClaim(claim);
        }
    }

    public ClaimData loadClaim(Location location) {
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, true, null);
        if (claim != null) {
            return loadClaimData(claim);
        }
        return null;
    }

    public ClaimData loadClaimData(Claim claim) {
        for(ClaimData cd : claimDataList){
            if(cd.getId() == claim.getID()){
                return cd;
            }
        }
        IQuery query = new CriteriaQuery(ClaimData.class, Where.equal("id", claim.getID()));
        Objects<ClaimData> claims = db.getObjects(query);
        ClaimData data = claims.getFirst();
        if (data == null) {
            PermissionGroup group = new PermissionGroup();
            data = new ClaimData();
            data.setId(claim.getID());
            data.setPermissionLevel(PermissionLevel.defaultLevel);
            data.setWorld(claim.getGreaterBoundaryCorner().getWorld());
            data.setPermissionGroup(group);
            db.store(data);
        }
        claimDataList.add(data);
        return data;
    }

    public HashMap<UUID, PlayerData> getPlayerDataMap() {
        return playerDataMap;
    }

    public PlayerData getPlayerData(String player) {
        return playerDataMap.get(Bukkit.getServer().getPlayer(player).getUniqueId());
    }

    public PlayerData getPlayerData(UUID player) {
        return playerDataMap.get(player);
    }

    public PlayerData getPlayerData(Player player) {
        return playerDataMap.get(player.getUniqueId());
    }
    
    public ClaimData getClaimData(long id){
        Claim claim = getClaim(id);
        for(ClaimData data : claimDataList){
            if(data.getId() == claim.getID()){
                return data;
            }
        }
        return null;
    }
    
    public ClaimData getClaimData(Location l){
        Claim claim = getClaim(l);
        for(ClaimData data : claimDataList){
            if(data.getId() == claim.getID()){
                return data;
            }
        }
        return null;
    }
    
    public Claim getClaim(Location l){
        return GriefPrevention.instance.dataStore.getClaimAt(l, true, null);
    }
    
    public Claim getClaim(long id){
        return GriefPrevention.instance.dataStore.getClaim(id);
    }

    public HashMap<UUID, WorldData> getWorldDataMap() {
        return worldDataMap;
    }

    public WorldData getWorldData(UUID world) {
        return worldDataMap.get(world);
    }

    public WorldData getWorldData(World world) {
        return worldDataMap.get(world.getUID());
    }

    public void saveAll() {
        savePlayers(playerDataMap);
        saveWorlds(worldDataMap);
        saveClaims(claimDataList);
    }

    public void clearMemory() {
        mad = null;
        playerDataMap = null;
        worldDataMap = null;
        claimDataList = null;
        serverHome = null;
    }

    public void createClaim(Claim claim) {
        for (Player p : mad.getServer().getOnlinePlayers()) {
            for (JumpPoint j : playerDataMap.get(p.getUniqueId()).getHomes()) {
                if (claim.contains(j.getLocation(), true, true)) {
                    loadClaimData(claim);
                    return;
                }
            }
        }
    }

    public void deleteClaim(Claim claim) {
        for (ClaimData data : claimDataList) {
            if (data.getId() == claim.getID()) {
                db.delete(data);
                claimDataList.remove(data);
                break;
            }
        }
    }

    public void updateClaim(Claim claim) {
        //TODO: deal with events on claim update
    }

    public void modifyClaim(Claim claim) {
        //TODO: deal with events on claim modification
    }
    
    public static Data get(){
        return d;
    }
}
