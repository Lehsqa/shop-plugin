import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.net.HttpURLConnection;
import java.net.URL;

public class ShopPlugin extends JavaPlugin {

    private Map<Material, Double> prices;
    private Inventory shopInventory;

    @Override
    public void onEnable() {
        prices = new HashMap<>();
        prices.put(Material.DIAMOND, 10.0);
        prices.put(Material.GOLD_INGOT, 1.0);
        prices.put(Material.IRON_INGOT, 0.5);

        shopInventory = getServer().createInventory(null, 9, "Shop");
        for (Map.Entry<Material, Double> set : prices.entrySet()) {
            ItemStack item = new ItemStack(set.getKey());
            item.setAmount(1);
            shopInventory.addItem(item);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("sell") && args.length == 1 && sender instanceof Player) {
            try {
                Material itemId = ((Player) sender).getInventory().getItemInMainHand().getType();
                double price = Double.parseDouble(args[0]);
                prices.put(itemId, price);
                ItemStack item = new ItemStack(Material.EMERALD);
                item.setAmount(1);
                shopInventory.addItem(item);
                sender.sendMessage("Sold item with ID " + Material.EMERALD + " for " + price + " coins");
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid price");
            }
        } else if (label.equalsIgnoreCase("shop") && sender instanceof Player) {
            ((Player) sender).openInventory(shopInventory);
            return true;
        } else if (label.equalsIgnoreCase("buy") && args.length == 1 && sender instanceof Player) {
            try {
                Material itemId = Material.getMaterial(args[0]);
                if (prices.containsKey(Material.IRON_INGOT)) {
                    double price = prices.get(Material.IRON_INGOT);
                    if (price > 0) {
                        Player player = (Player) sender;
                        String baseUrl = "http://127.0.0.1:5000/create_transaction";
                        String senderName = "camille";
                        String receiverName = "bertrand";
                        String amount = "10";
                        String sb = baseUrl + "?sender_name=" + senderName +
                                "&receiver_name=" + receiverName +
                                "&amount=" + amount;
                        int responseCode;
                        URL url = new URL(sb);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        responseCode = connection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            ItemStack item = new ItemStack(Material.IRON_INGOT);
                            item.setAmount(1);
                            player.getInventory().addItem(item);
                            player.sendMessage("You bought an item with ID " + Material.IRON_INGOT + " for " + price + " coins");
                        } else {
                            player.sendMessage("You don't have enough coins");
                        }
                    } else {
                        sender.sendMessage("This item cannot be purchased");
                    }
                } else {
                    sender.sendMessage("Invalid item ID");
                }
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid item ID");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }
}
