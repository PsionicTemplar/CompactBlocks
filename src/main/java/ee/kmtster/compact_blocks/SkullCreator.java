package ee.kmtster.compact_blocks;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Modernized SkullCreator for Spigot 1.21.1+
 * No reflection required.
 */
public class SkullCreator {

    private SkullCreator() {}

    /**
     * Creates a player skull item.
     */
    public static ItemStack createSkull() {
        return new ItemStack(Material.PLAYER_HEAD);
    }

    public static ItemStack itemFromUuid(UUID id) {
        ItemStack item = createSkull();
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(id));
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Creates a player skull item from a Mojang texture URL.
     */
    public static ItemStack itemFromUrl(String url) {
        ItemStack item = createSkull();
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            setSkin(meta, url);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Sets the skin of a Block to a custom texture URL.
     */
    public static void blockFromUrl(Block block, String url) {
        block.setType(Material.PLAYER_HEAD);
        if (block.getState() instanceof Skull skull) {
            // In modern Spigot, we apply to the BlockState's Meta or Profile
            PlayerProfile profile = createProfileWithTexture(url);
            skull.setOwnerProfile(profile);
            skull.update(true, false);
        }
    }
    
    /**
     * Modifies a skull to use the skin based on the given base64 string.
    *
    * @param item   The ItemStack to put the base64 onto. Must be a player skull.
    * @param base64 The base64 string containing the texture.
    * @return The head with a custom texture.
    */
    public static ItemStack itemWithBase64(ItemStack item, String base64) {
        if (!(item.getItemMeta() instanceof SkullMeta meta)) {
            return item;
        }

        // 1. Create a profile
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
        PlayerTextures textures = profile.getTextures();

        try {
            // 2. Decode the Base64 to find the actual Mojang URL
            // Minecraft Base64 skins are just JSON strings that contain a URL.
            String decoded = new String(Base64.getDecoder().decode(base64));
            JsonObject json = JsonParser.parseString(decoded).getAsJsonObject();
            String urlString = json.getAsJsonObject("textures")
                                   .getAsJsonObject("SKIN")
                                   .get("url").getAsString();

            // 3. Set the skin via URL (The Spigot-friendly way)
            textures.setSkin(new URL(urlString));
            profile.setTextures(textures);
            
            // 4. Apply to meta
            meta.setOwnerProfile(profile);
            item.setItemMeta(meta);

        } catch (Exception e) {
            Bukkit.getLogger().warning("Could not decode Base64 texture: " + e.getMessage());
        }

        return item;
    }

   public static ItemStack itemFromBase64(String base64) {
       return itemWithBase64(createSkull(), base64);
   }

    /**
     * Helper to apply a texture URL to SkullMeta using the 1.21.1 PlayerProfile API.
     */
    private static void setSkin(SkullMeta meta, String url) {
        meta.setOwnerProfile(createProfileWithTexture(url));
    }

    /**
     * Creates a PlayerProfile with a custom skin texture.
     */
    private static PlayerProfile createProfileWithTexture(String urlString) {
        // Create a profile with a random UUID to ensure it doesn't conflict with real players
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
        PlayerTextures textures = profile.getTextures();
        
        try {
            URL url = new URL(urlString);
            textures.setSkin(url);
            profile.setTextures(textures);
        } catch (MalformedURLException e) {
            Bukkit.getLogger().severe("Invalid URL provided to SkullCreator: " + urlString);
        }
        
        return profile;
    }
}