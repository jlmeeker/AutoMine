package org.meekers.plugins.automine;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

/**
 *
 * @author jaredm
 */
public class AutoMine extends JavaPlugin {

    public void onEnable() {
        this.saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(new AutoMinePluginListener(this), this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = Bukkit.getPlayer(sender.getName());
        Location ploc = player.getLocation().clone();
        World pworld = player.getWorld();

        if (cmd.getName().equalsIgnoreCase("amine")) {
            if (args.length > 3 || args.length < 3) {
                return false;
            }

            // Force length and width to be positive, height can be negative or positive
            int length = Math.abs(Integer.parseInt(args[0]));
            int width = Math.abs(Integer.parseInt(args[1]));
            int height = Integer.parseInt(args[2]);

            int totalBlocks = Math.abs(length) * Math.abs(width) * Math.abs(height);
            int neededExp = totalBlocks / this.getConfig().getInt("blocksper1xp");

            if (neededExp == 0) {
                neededExp = 1;
            }

            int playerExp = player.getLevel();

            if (playerExp < neededExp) {
                player.sendMessage("Insufficient experience, " + neededExp + " necessary.");
                return true;
            } else {
                player.setLevel(player.getLevel()-neededExp);
                player.sendMessage("You spent "+neededExp+ " experience.");
            }
//            player.sendMessage("You need " + neededExp + " experience to mine " + totalBlocks + " blocks.");
            //player.sendMessage("Your location is "+ploc.toString());
            //player.sendMessage("You are facing " + ploc.getDirection().toString() );
            // Get the blocks and change them to air

            // X is East (positive) to West (negative)
            // Z is North (negative) to South (positive)
            Vector direction = ploc.getDirection();
            double v_x = direction.getX();
            double v_z = direction.getZ();

            // Define our facing direction
            boolean facingXpos = false;
            boolean facingZpos = false;
            boolean facingX = false;

            // Choose base block
            Location baseLoc = ploc.clone();
            Location blockLoc = baseLoc.clone();
            blockLoc.setWorld(ploc.getWorld());

            if (Math.abs(v_x) > Math.abs(v_z)) {
                // length is X, width is Z
                facingX = true;
                if (v_x > 0) {
                    baseLoc.setX(baseLoc.getX() + 1);
//                    baseLoc.setZ(baseLoc.getZ() - (int)Math.ceil(width/2.0));
                    facingXpos = true;  //East
                } else {
                    baseLoc.setX(baseLoc.getX() - 1);
//                    baseLoc.setZ(baseLoc.getZ() + (int)Math.ceil(width/2.0));
                }
            } else {
                // length is Z, width is X
                if (v_z > 0) {
                    baseLoc.setZ(baseLoc.getZ() + 1);
//                    baseLoc.setX(baseLoc.getX() + (int)Math.ceil(width/2.0));
                    facingZpos = true; // South
                } else {
                    baseLoc.setZ(baseLoc.getZ() - 1);
//                    baseLoc.setX(baseLoc.getX() - (int)Math.ceil(width/2.0));
                }
            }


            // works for horiz
            for (int l = 0; l < length; l++) {
                if (facingX) {
                    if (facingXpos) {
                        blockLoc.setX(baseLoc.getX() + l);
                    } else {
                        blockLoc.setX(baseLoc.getX() - l);
                    }
                } else {
                    if (facingZpos) {
                        blockLoc.setZ(baseLoc.getZ() + l);
                    } else {
                        blockLoc.setZ(baseLoc.getZ() - l);
                    }
                }
                for (int h = 0; h < Math.abs(height); h++) {
                    if (height > 0) {
                        blockLoc.setY(baseLoc.getY() + h);
                    } else {
                        blockLoc.setY(baseLoc.getY() - h);
                    }
                    for (int w = 0; w < width; w++) {
                        if (facingX) {
                            if (facingXpos) {
                                blockLoc.setZ(baseLoc.getZ() + w);
                            } else {
                                blockLoc.setZ(baseLoc.getZ() - w);
                            }
                        } else {
                            if (facingZpos) {
                                blockLoc.setX(baseLoc.getX() - w);
                            } else {
                                blockLoc.setX(baseLoc.getX() + w);
                            }
                        }
                        Block baseBlock = pworld.getBlockAt(blockLoc);
                        baseBlock.setType(Material.AIR);
                    }
                }
            }

            return true;
        }
        return false;
    }
}
