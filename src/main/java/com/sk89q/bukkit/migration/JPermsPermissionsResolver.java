package com.sk89q.bukkit.migration;

import com.johnymuffin.jperms.beta.JohnyPerms;
import com.johnymuffin.jperms.beta.JohnyPermsAPI;
import com.johnymuffin.jperms.core.models.PermissionsGroup;
import com.projectposeidon.api.PoseidonUUID;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.UUID;

public class JPermsPermissionsResolver implements PermissionsResolver {
    private Server server;
    private JohnyPermsAPI api;

    public JPermsPermissionsResolver(final Server server) throws MissingPluginException, PluginAccessException {
        this.server = server;
        if (Bukkit.getServer().getPluginManager().getPlugin("JPerms") == null) {
            throw new MissingPluginException();
        }
        try {
            this.api = JohnyPerms.getJPermsAPI();
        } catch (Exception e) {
            throw new PluginAccessException();
        }

    }

    public void load() {

    }

    private UUID getPlayerUUID(final String name) {
        for(Player player : Bukkit.getOnlinePlayers()) {
            if(player.getName().equalsIgnoreCase(name)) {
                return player.getUniqueId();
            }
        }
        return PoseidonUUID.getPlayerGracefulUUID(name);
    }

    public boolean hasPermission(final String name, final String permission) {
        UUID playerUUID = getPlayerUUID(name);

        return api.getUser(playerUUID).hasPermissionSomehow(permission, true) || api.getUser(playerUUID).getGroup().hasPermission(permission, true);
    }

    @Override
    public boolean hasPermission(String worldName, String name, String permission) {
        //JPerms 1.0.0 does not support world permissions
        return hasPermission(name, permission);
    }

    public boolean inGroup(final String name, final String group) {
        String[] groups = getGroups(name);
        for(String g : groups) {
            if(g.equalsIgnoreCase(group)) {
                return true;
            }
        }
        return false;
    }

    public String[] getGroups(final String name) {
        UUID playerUUID = getPlayerUUID(name);
        PermissionsGroup group = api.getUser(playerUUID).getGroup();
        String[] groups = new String[group.getInheritanceGroups().length + 1];
        groups[0] = group.getName();
        for(int i = 0; i < group.getInheritanceGroups().length; i++) {
            groups[i + 1] = group.getInheritanceGroups()[i].getName();
        }
        return groups;
    }

    public static class PluginAccessException extends Exception {
        private static final long serialVersionUID = 7044832912491608706L;
    }

    public static class MissingPluginException extends Exception {
        private static final long serialVersionUID = 7044832912491608706L;
    }
}
