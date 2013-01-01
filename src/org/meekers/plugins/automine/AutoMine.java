package org.meekers.plugins.automine;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
        Inventory playerinv = player.getInventory();
        Location ploc = player.getLocation().clone();
        World pworld = player.getWorld();
        Material material = null;

        if (cmd.getName().equalsIgnoreCase("amine")) {
            if (args.length > 4 || args.length < 3) {
                return false;
            }

            if (args.length == 4) {
                material = Material.valueOf(args[3].toUpperCase());
                player.sendMessage("building with " + material.name());
            }

            // Force length and width to be positive, height can be negative or positive
            int length = Math.abs(Integer.parseInt(args[0]));
            int width = Math.abs(Integer.parseInt(args[1]));
            int height = Integer.parseInt(args[2]);

            // height of -1 is the level we're on... give one more so it does what we expect
            if (height < 0) {
                height--;
            }

            int totalBlocks = Math.abs(length * width * height);
            int playerExp = player.getLevel();
            int blocksper1xp = this.getConfig().getInt("blocksper1xp");
            int neededExp = 1; // Charge at least 1 xp
            int MaxneededExp = totalBlocks / blocksper1xp;

            if (MaxneededExp == 0) {
                MaxneededExp = 1;
            }

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
            //Location baseLoc = ploc.clone();
            Location baseLoc = player.getTargetBlock(null, 100).getLocation();
            Location blockLoc = baseLoc.clone();
            blockLoc.setWorld(ploc.getWorld());

            if (Math.abs(v_x) > Math.abs(v_z)) {
                // length is X, width is Z
                facingX = true;
                if (v_x > 0) {
                    baseLoc.setX(baseLoc.getX());
                    facingXpos = true;  //East
                } else {
                    baseLoc.setX(baseLoc.getX());
                }
            } else {
                // length is Z, width is X
                if (v_z > 0) {
                    baseLoc.setZ(baseLoc.getZ());
                    facingZpos = true; // South
                } else {
                    baseLoc.setZ(baseLoc.getZ());
                }
            }

            // Only charge for non-AIR blocks
            int blockCount = 0;
            boolean expSpent = false;
            while (expSpent != true) {
                building:
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
                            if (baseBlock.getType() != Material.BEDROCK && (baseBlock.getType() != Material.AIR || args.length == 4)) {
                                if (playerExp >= neededExp) {
                                    blockCount++;
                                    if (blockCount % blocksper1xp == 0) {
                                        neededExp++;
                                    }
                                    if (args.length == 4) {
                                        if (playerinv.contains(material)) {
                                            playerinv.removeItem(new ItemStack[]{new ItemStack(material.getId(), 1)});
                                            baseBlock.setType(material);
                                        } else {
                                            player.sendMessage("You ran out of " + material.name() + " building block #" + blockCount);
                                            blockCount--;
                                            break building;
                                        }
                                    } else {
                                        if (this.getConfig().getBoolean("dropsenabled")) {
                                            baseBlock.breakNaturally();
                                        } else {
                                            baseBlock.setType(Material.AIR);
                                        }
                                    }
                                } else {
                                    expSpent = true;
                                }
                            }
                        }
                    }
                }
                expSpent = true;
            }
            if (blockCount == 0) {
                neededExp = 0;
            }
            player.setLevel(player.getLevel() - neededExp);
            player.sendMessage("You spent " + neededExp + " experience on " + blockCount + " blocks.");
            return true;
        }
        return false;
    }
}
