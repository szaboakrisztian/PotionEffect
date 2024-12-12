package PotionEffectsPlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class PotionEffects extends JavaPlugin implements CommandExecutor {

    private File dataFile;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        getLogger().info("PotionSaver enabled");

        // Register command
        if (this.getCommand("pt") != null) {
            this.getCommand("pt").setExecutor(this);
            getLogger().info("Command 'pt' registered successfully.");
        } else {
            getLogger().severe("Command 'pt' not found in plugin.yml.");
        }

        // Initialize the file and load configuration
        initializeConfig();
    }

    private void initializeConfig() {
        dataFile = new File(getDataFolder(), "effectsSaver/potionEffects.yml");
        if (!dataFile.exists()) {
            getLogger().info("potionEffects.yml not found, creating a new one.");
            dataFile.getParentFile().mkdirs();
            saveResource("effectsSaver/potionEffects.yml", false);
        } else {
            getLogger().info("potionEffects.yml found, loading configuration.");
        }
        config = YamlConfiguration.loadConfiguration(dataFile);
        getLogger().info("Configuration loaded: " + config.saveToString());
    }

    @Override
    public void onDisable() {
        getLogger().info("PotionSaver disabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        getLogger().info("Command received: " + cmd.getName());
        if (cmd.getName().equalsIgnoreCase("pt")) {
            if (args.length > 0) {
                getLogger().info("Subcommand: " + args[0]);
                if (args[0].equalsIgnoreCase("save")) {
                    if (args.length < 2) {
                        sender.sendMessage("Error: You must specify a save slot.");
                        return true;
                    }
                    int slot = Integer.parseInt(args[1]);
                    if(slot > 5 || slot<1){
                        sender.sendMessage("Error: Slot cannot be larger than 5 and cannot be negative.");
                        return true;
                    }
                    savePotionEffects(sender, args[1]);
                    return true;
                } else if (args[0].equalsIgnoreCase("load")) {
                    if (args.length < 2) {
                        sender.sendMessage("Error: You must specify a load slot.");
                        return true;
                    }
                    int slot = Integer.parseInt(args[1]);
                    if(slot > 5 || slot<1){
                        sender.sendMessage("Error: Slot cannot be larger than 5 and cannot be negative.");
                        return true;
                    }
                    loadPotionEffects(sender, args[1]);
                    return true;
                } else if (args[0].equalsIgnoreCase("remove")){
                    if (args.length < 2){
                        sender.sendMessage("Error: you must specify a load slot.");
                        return true;
                    }
                    int slot = Integer.parseInt(args[1]);
                    if(slot > 5 || slot<1){
                        sender.sendMessage("Error: Slot cannot be larger than 5 and cannot be negative.");
                        return true;
                    }
                    removePotionEffects(sender, args[1]);
                    return true;
                }
            }
            sender.sendMessage("Usage: /pt <save|load|remove> <slot>");
            return true;
        }
        return false;
    }
    private void removePotionEffects(CommandSender sender, String slot){
        if (!(sender instanceof Player)) {
            sender.sendMessage("Error: Only players can use this command.");
            return;
        }
        Player player = (Player) sender;

        String slotPath = "slots." + slot + ".effects";
        if (config.contains(slotPath)) {
            config.set(slotPath, null);
            sender.sendMessage("Potion effects removed from slot " + slot + ".");
        }
    }

    private void savePotionEffects(CommandSender sender, String slot) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Error: Only players can use this command.");
            return;
        }

        Player player = (Player) sender;
        Collection<PotionEffect> effects = player.getActivePotionEffects();

        List<String> serializedEffects = effects.stream()
                .map(effect -> effect.getType().getName() + "," + effect.getAmplifier())
                .collect(Collectors.toList());

        config.set("slots." + slot + ".effects", serializedEffects);
        try {
            config.save(dataFile);
            getLogger().info("Potion effects saved to slot " + slot + ".");
            sender.sendMessage("Potion effects saved to slot " + slot + "!");
        } catch (IOException e) {
            getLogger().severe("Could not save potion effects to slot " + slot + ": " + e.getMessage());
            sender.sendMessage("Error: Could not save potion effects to slot " + slot + ".");
        }
    }

    private void loadPotionEffects(CommandSender sender, String slot) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Error: Only players can use this command.");
            return;
        }

        Player player = (Player) sender;
        List<String> serializedEffects = config.getStringList("slots." + slot + ".effects");

        if (serializedEffects.isEmpty()) {
            getLogger().info("No potion effects found in slot " + slot + ".");
            sender.sendMessage("No potion effects found in slot " + slot + ".");
            return;
        }

        for (String serializedEffect : serializedEffects) {
            String[] parts = serializedEffect.split(",");
            if (parts.length != 2) continue;

            PotionEffectType type = PotionEffectType.getByName(parts[0]);
            int amplifier = Integer.parseInt(parts[1]);

            if (type != null) {
                PotionEffect effect = new PotionEffect(type, Integer.MAX_VALUE, amplifier);
                player.addPotionEffect(effect);
            }
        }

        getLogger().info("Potion effects loaded from slot " + slot + ".");
        sender.sendMessage("Potion effects loaded from slot " + slot + "!");
    }
}