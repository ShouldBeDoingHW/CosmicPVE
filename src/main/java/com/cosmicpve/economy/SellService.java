package com.cosmicpve.economy;

import com.cosmicpve.registry.ModDataComponents;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Server-side plan-then-commit commodity selling over carried inventory only. */
public final class SellService {
    private final MoneyService money = new MoneyService();

    public Result sellHand(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty() || !eligible(held)) return Result.rejected(Status.INVALID_HAND);
        return execute(player, held.getItem());
    }

    public Result sellAll(ServerPlayer player) { return execute(player, null); }

    private Result execute(ServerPlayer player, Item only) {
        List<ItemStack> inventory = player.getInventory().getNonEquipmentItems();
        Plan plan;
        try { plan = plan(inventory, only); }
        catch (ArithmeticException overflow) { return Result.rejected(Status.OVERFLOW); }
        if (plan.lines().isEmpty()) return Result.rejected(only == null ? Status.NOTHING_TO_SELL : Status.NONE_MATCHING);
        long balance = money.balance(player);
        if (!MoneyService.canAdd(balance, plan.totalCents())) return Result.rejected(Status.OVERFLOW);

        List<ItemStack> snapshot = inventory.stream().map(ItemStack::copy).toList();
        for (ItemStack stack : inventory)
            if (eligible(stack) && (only == null || stack.is(only))) stack.setCount(0);
        if (!money.add(player, plan.totalCents())) {
            for (int i = 0; i < inventory.size(); i++) inventory.set(i, snapshot.get(i).copy());
            return Result.rejected(Status.OVERFLOW);
        }
        player.getInventory().setChanged();
        return new Result(Status.SUCCESS, plan.lines(), plan.totalCents());
    }

    static Plan plan(List<ItemStack> inventory, Item only) {
        var quantities = new LinkedHashMap<Item, Integer>();
        for (Item item : SellPriceRegistry.prices().keySet()) quantities.put(item, 0);
        for (ItemStack stack : inventory) {
            if (!eligible(stack) || (only != null && !stack.is(only))) continue;
            quantities.computeIfPresent(stack.getItem(), (item, count) -> Math.addExact(count, stack.getCount()));
        }
        var lines = new ArrayList<SaleLine>(); long total = 0;
        for (Map.Entry<Item, Integer> entry : quantities.entrySet()) {
            if (entry.getValue() == 0) continue;
            long subtotal = Math.multiplyExact(SellPriceRegistry.price(entry.getKey()).orElseThrow(), entry.getValue());
            total = Math.addExact(total, subtotal);
            lines.add(new SaleLine(entry.getKey(), entry.getValue(), subtotal));
        }
        return new Plan(List.copyOf(lines), total);
    }

    public static boolean eligible(ItemStack stack) {
        return !stack.isEmpty() && SellPriceRegistry.price(stack.getItem()).isPresent() && !hasCosmicIdentity(stack);
    }

    static boolean hasCosmicIdentity(ItemStack stack) {
        return stack.has(ModDataComponents.CUSTOM_ENCHANT_META.get())
                || stack.has(ModDataComponents.ARMOR_SET_ID.get()) || stack.has(ModDataComponents.ARMOR_SET_CRYSTAL.get())
                || stack.has(ModDataComponents.COSMIC_ENCHANT_BOOK.get()) || stack.has(ModDataComponents.COSMIC_DUST.get())
                || stack.has(ModDataComponents.UNEXAMINED_BOOK.get()) || stack.has(ModDataComponents.ENCHANTMENT_ORB.get())
                || stack.has(ModDataComponents.BLACK_SCROLL.get()) || stack.has(ModDataComponents.WEAPON_SKIN.get())
                || stack.has(ModDataComponents.WEAPON_SKIN_ITEM.get()) || stack.has(ModDataComponents.HEROIC.get())
                || stack.has(ModDataComponents.BANKNOTE.get()) || stack.has(ModDataComponents.MOB_SPAWNER.get())
                || stack.has(ModDataComponents.SPACE_CHEST.get()) || stack.has(ModDataComponents.TRIAL_TRINKET.get())
                || stack.has(ModDataComponents.TRIAL_PORTAL_MODIFIERS.get()) || stack.has(ModDataComponents.MASK_ITEM.get())
                || stack.has(ModDataComponents.MASK_LOADOUT.get()) || stack.has(ModDataComponents.HIDDEN_GRAVEYARD_KEY.get())
                || stack.has(ModDataComponents.OMNI_ARMOR.get()) || stack.has(ModDataComponents.HOLY.get())
                || stack.has(ModDataComponents.MYSTERY_SPAWNER.get()) || stack.has(ModDataComponents.VKIT_CRYSTAL.get())
                || stack.has(ModDataComponents.VKIT_EQUIPMENT.get()) || stack.has(ModDataComponents.STORED_XP_BOTTLE.get())
                || stack.has(ModDataComponents.ENCHANTED_BLACK_SCROLL.get()) || stack.has(ModDataComponents.SIGNATURE_WEAPON.get())
                || stack.has(ModDataComponents.ADMIN_ABUSE_REWARD.get()) || stack.has(ModDataComponents.COSMIC_BOOK_RATE_OVERRIDE.get());
    }

    public record SaleLine(Item item, int quantity, long subtotalCents) {}
    public record Plan(List<SaleLine> lines, long totalCents) {}
    public record Result(Status status, List<SaleLine> lines, long totalCents) {
        static Result rejected(Status status) { return new Result(status, List.of(), 0); }
        public boolean succeeded() { return status == Status.SUCCESS; }
    }
    public enum Status { SUCCESS, INVALID_HAND, NONE_MATCHING, NOTHING_TO_SELL, OVERFLOW }
}
