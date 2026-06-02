package me.gemini.gravespro.util;

import org.bukkit.inventory.ItemStack;
import java.io.*;
import java.util.*;

public class ItemSerialization {

    public static String serializeItemMap(Map<Integer, ItemStack> items) {
        if (items == null || items.isEmpty()) return "";
        
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(bos)) {
            
            // Filter non-null and non-air items first
            Map<Integer, ItemStack> filtered = new HashMap<>();
            for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().getType().isAir()) {
                    filtered.put(entry.getKey(), entry.getValue());
                }
            }
            
            dos.writeInt(filtered.size());
            for (Map.Entry<Integer, ItemStack> entry : filtered.entrySet()) {
                dos.writeInt(entry.getKey()); // Slot index
                byte[] bytes = entry.getValue().serializeAsBytes();
                dos.writeInt(bytes.length);
                dos.write(bytes);
            }
            
            dos.flush();
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize items", e);
        }
    }

    public static Map<Integer, ItemStack> deserializeItemMap(String data) {
        if (data == null || data.isEmpty()) return new HashMap<>();
        
        try (ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode(data));
             DataInputStream dis = new DataInputStream(bis)) {
            
            int size = dis.readInt();
            Map<Integer, ItemStack> items = new HashMap<>(size);
            
            for (int i = 0; i < size; i++) {
                int slot = dis.readInt();
                int length = dis.readInt();
                byte[] bytes = new byte[length];
                dis.readFully(bytes);
                
                try {
                    ItemStack item = ItemStack.deserializeBytes(bytes);
                    items.put(slot, item);
                } catch (Exception e) {
                    org.bukkit.Bukkit.getLogger().severe("Failed to deserialize item in slot " + slot + "! It might be from a newer version or corrupt.");
                }
            }
            
            return items;
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize item map. The binary stream is likely corrupted.", e);
        }
    }

    public static String serializeItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return "null";
        return Base64.getEncoder().encodeToString(item.serializeAsBytes());
    }

    public static ItemStack deserializeItem(String data) {
        if (data == null || data.equals("null") || data.isEmpty()) return null;
        try {
            return ItemStack.deserializeBytes(Base64.getDecoder().decode(data));
        } catch (Exception e) {
            return null;
        }
    }

    public static String serializeItems(List<ItemStack> items) {
        Map<Integer, ItemStack> map = new HashMap<>();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) != null) {
                map.put(i, items.get(i));
            }
        }
        return serializeItemMap(map);
    }

    public static List<ItemStack> deserializeItems(String data) {
        Map<Integer, ItemStack> map = deserializeItemMap(data);
        List<ItemStack> list = new ArrayList<>();
        if (map.isEmpty()) return list;
        
        int max = 0;
        for (int slot : map.keySet()) max = Math.max(max, slot);
        
        for (int i = 0; i <= max; i++) {
            list.add(map.get(i));
        }
        return list;
    }
}
