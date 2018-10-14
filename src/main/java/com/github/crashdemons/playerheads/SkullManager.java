
package com.github.crashdemons.playerheads;

import com.github.crashdemons.playerheads.backports.Backports;
import java.util.UUID;
import java.lang.reflect.Field;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.github.crashdemons.playerheads.backports.FutureMaterial;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import com.github.crashdemons.playerheads.backports.FutureItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import org.shininet.bukkit.playerheads.Config;

/**
 * Defines an abstract class of methods for creating, updating, and applying information to heads managed by the plugin.
 * @author crash
 * @author x7aSv
 */
public final class SkullManager {
    
    private SkullManager(){}
    
    /**
     * Applies Lore text (including the PlayerHeads plugin name) to a playerhead's meta.
     * @param headMeta The SkullMeta associated with the playerhead to modify
     * @param extra Extra lore text to display under the "PlayerHeads" line.
     */
    private static void applyLore(SkullMeta headMeta,String extra){
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(" ");
        lore.add(ChatColor.BLUE+""+ChatColor.ITALIC+"PlayerHeads");
        if(!extra.isEmpty()) lore.add(extra);
        headMeta.setLore(lore);
    }
    
    /**
     * Sets an owning player to a playerhead
     * @param headMeta  The SkullMeta associated with the playerhead to modify
     * @param owner The OfflinePlayer owning to own the head.
     */
    private static void applyOwningPlayer(SkullMeta headMeta,OfflinePlayer owner){
        //headMeta.setOwningPlayer( owner );
        Backports.setOwningPlayer_ByName(headMeta, owner);//pretty ugly solution
    }
    /**
     * Sets a display name for the playerhead item's meta
     * @param headMeta The SkullMeta associated with the playerhead to modify
     * @param display The string containing the display name to set
     */
    private static void applyDisplayName(SkullMeta headMeta,String display){
        headMeta.setDisplayName(display);
    }
    
    /**
     * Applies a texture-url to a playerhead's meta.
     * @param headMeta The SkullMeta associated with the playerhead to modify
     * @param uuid A UUID to associate with the head and texture
     * @param texture The Base64-encoded Texture-URL tags.
     * @return true: the information was properly set on the playerhead; false: there was an error setting the profile field.
     * @author x7aSv
     */
    private static boolean applyTexture(SkullMeta headMeta, UUID uuid, String texture){//credit to x7aSv
        //System.out.println("Applying texture...");
        GameProfile profile = new GameProfile(uuid, null);
        profile.getProperties().put("textures", new Property("textures", texture));
        try {
            Field profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (IllegalArgumentException | NoSuchFieldException | SecurityException | IllegalAccessException error) {
            error.printStackTrace();
            return false;
        }
       // System.out.println("done applying.");
        return true;
    }
    
    /**
     * Creates a stack of heads for the specified Mob's SkullType.
     * 
     * The quantity of heads will be defined by Config.defaultStackSize (usually 1)
     * 
     * @param type The TexturedSkullType to create heads of.
     * @param useVanillaHeads Whether to permit vanilla head-items to be used in place of custom playerheads for supported mobs.
     * @return The FutureItemStack of heads desired.
     * @see org.shininet.bukkit.playerheads.Config#defaultStackSize
     */
    public static FutureItemStack MobSkull(TexturedSkullType type, boolean useVanillaHeads){
        return MobSkull(type,Config.defaultStackSize, useVanillaHeads);
    }
    
    /**
     * Creates a stack of heads for the specified Mob's SkullType
     * @param type The TexturedSkullType to create heads of.
     * @param quantity the number of heads to create in the stack
     * @param useVanillaHeads Whether to permit vanilla head-items to be used in place of custom playerheads for supported mobs.
     * @return The FutureItemStack of heads desired.
     */
    public static FutureItemStack MobSkull(TexturedSkullType type,int quantity,boolean useVanillaHeads){
        FutureMaterial mat = type.getMaterial();
        
        
        if(type.hasDedicatedItem()){
            if(useVanillaHeads)
                return new FutureItemStack(mat,quantity);
            else mat=FutureMaterial.PLAYER_HEAD;
        }
        
        //System.out.println("Player-head");
        FutureItemStack stack = new FutureItemStack(mat,quantity);
        SkullMeta headMeta = (SkullMeta) stack.getItemMeta();
        //applyOwningPlayer(headMeta,Bukkit.getOfflinePlayer(type.getOwner()));
        applyTexture(headMeta,type.getOwner(),type.getTexture());
        applyDisplayName(headMeta,ChatColor.RESET + "" + ChatColor.YELLOW + type.getDisplayName());
        applyLore(headMeta,ChatColor.GREEN+"Mob Drop");
        stack.setItemMeta(headMeta);
        return stack;
    }
    private static FutureItemStack PlayerSkull(OfflinePlayer owner){
        return PlayerSkull(owner,Config.defaultStackSize);
    }
    private static FutureItemStack PlayerSkull(OfflinePlayer owner, int quantity){
        FutureItemStack stack = new FutureItemStack(FutureMaterial.PLAYER_HEAD,quantity);
        SkullMeta headMeta = (SkullMeta) stack.getItemMeta();
        String name=null;
        if(owner!=null){
            applyOwningPlayer(headMeta,owner);
            name = owner.getName();
        }
        if(name==null) name="Unknown";//only used for display purposes.
        applyDisplayName(headMeta,ChatColor.RESET + "" + ChatColor.YELLOW + TexturedSkullType.getDisplayName(name));
        applyLore(headMeta,ChatColor.RED+"Player Drop");
        stack.setItemMeta(headMeta);
        return stack;
    }
    
    /**
     * Creates a stack of playerheads for the given username.
     * 
     * The username given is translated to an OfflinePlayer by bukkit at the time of creation.
     * 
     * Note: if the owner name is null or can't be translated to a player internally, a generic playerhead will be created with the name "Unknown".
     * 
     * The quantity of heads will be defined by Config.defaultStackSize (usually 1)
     * @param owner The username to create a head for.
     * @return The FutureItemStack of heads desired.
     * @see org.shininet.bukkit.playerheads.Config#defaultStackSize
     */
    public static FutureItemStack PlayerSkull(String owner){
        return PlayerSkull(owner,Config.defaultStackSize);
    }
    /**
     * Creates a stack of playerheads for the given username.
     * 
     * The username given is translated to an OfflinePlayer by bukkit at the time of creation. NOTE: null or empty usernames will be rejected with an IllegalArgumentException
     * 
     * @param owner The username to create a head for.
     * @param quantity The number of heads to create in the stack.
     * @return The FutureItemStack of heads desired.
     * @throws IllegalArgumentException passed a null or empty username.
     */
    public static FutureItemStack PlayerSkull(String owner, int quantity){
        if(owner==null || owner.isEmpty()) throw new IllegalArgumentException("Creating a playerhead with a null or empty username is not possible with this method.");
        OfflinePlayer op = Bukkit.getOfflinePlayer(owner);
        return PlayerSkull(op,quantity);
    }
    
    /**
     * Spawns a skull itemstack using an input spawn string or username.
     * 
     * The quantity of heads will be defined by Config.defaultStackSize (usually 1)
     * @param spawnString the spawn string indicating the type of skull or username indicating the owner of the skull.
     * @param usevanillaheads Whether to permit vanilla head-items to be used in place of custom playerheads for supported mobs. (if the spawn string is recognized)
     * @return The skull itemstack desired. If the spawn string is recognized, this will be the corresponding entity's head, otherwise it will be a playerhead for the name supplied.
     */
    public static FutureItemStack spawnSkull(String spawnString, boolean usevanillaheads){
        return spawnSkull(spawnString,Config.defaultStackSize,usevanillaheads);
    }
    
    /**
     * Spawns a skull itemstack using an input spawn string or username.
     * @param spawnString the spawn string indicating the type of skull or username indicating the owner of the skull.
     * @param quantity the number of items to spawn in this stack.
     * @param usevanillaheads Whether to permit vanilla head-items to be used in place of custom playerheads for supported mobs. (if the spawn string is recognized)
     * @return The skull itemstack desired. If the spawn string is recognized, this will be the corresponding entity's head, otherwise it will be a playerhead for the name supplied.
     */
    public static FutureItemStack spawnSkull(String spawnString, int quantity, boolean usevanillaheads){
        TexturedSkullType type;
        if(spawnString.isEmpty()) type=TexturedSkullType.PLAYER;
        else type = TexturedSkullType.getBySpawnName(spawnString);
        if(type==null){
            return PlayerSkull(spawnString,quantity);
        }else{
            return MobSkull(type,quantity,usevanillaheads);
        }
    }
    
    /**
     * Updates the blockstate of a head.
     * 
     * Originally this method also updated legacy username-based skulls to the correct owner - currently it only updates the blockstate.
     * Since these skulls are upgraded and textured-skulls have embedded textures, this method may not be necessary and may be removed in the future.
     * 
     * @param skullState the blockstate to update.
     */
    public static void updatePlayerSkullState(BlockState skullState){
        //for a skull belonging to a player drop, this shouldn't really be necessary to reset the owner.
        //and for textured mobheads, the texture is embedded, so shouldn't need updating...
        /*
        OfflinePlayer op = skullState.getOwningPlayer();
        String owner=null;
        if(op!=null) owner=op.getName();
        if(owner==null) owner=skullState.getOwner();//this is deprecated, but the above method does NOT get the name tag from the NBT.
        if(owner==null) return;

        skullState.setOwningPlayer(Bukkit.getOfflinePlayer(skullType.getOwner()));
        */
        skullState.update();
    }
    /*
    //TODO: reinvestigate these approaches as they're preferred to the deprecated username method.
    //these do not properly update head skin in server testing.
    public static FutureItemStack PlayerSkull(UUID owner){
        return PlayerSkull(owner,Config.defaultStackSize);
    }
    
    public static FutureItemStack PlayerSkull(UUID owner, int quantity){
        OfflinePlayer op = Bukkit.getOfflinePlayer(owner);
        //this is great but it doesn't update the texture
        return PlayerSkull(op,quantity);
    }
    */
}
