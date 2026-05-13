package org.exmple.newbedwarshelper.client.z_commands.itemmodelenhance;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.exmple.newbedwarshelper.client.itemmodelenhance.ItemScaleRegistry;

public final class ImeCommand {
    private static final String ITEM_NOT_FOUND_KEY = "commands.newbedwarshelper.itemmodelenhance.item_not_found";
    private static final String SCALE_SET_KEY = "commands.newbedwarshelper.itemmodelenhance.scale_set";
    private static final String SCALE_GET_KEY = "commands.newbedwarshelper.itemmodelenhance.scale_get";
    private static final String SCALE_RESET_KEY = "commands.newbedwarshelper.itemmodelenhance.scale_reset";
    private static final String SCALE_RESET_ALL_KEY = "commands.newbedwarshelper.itemmodelenhance.scale_reset_all";

    private ImeCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommands.literal("ime")
                            .then(ClientCommands.argument("item", ItemIdArgumentType.itemId())
                                    .then(ClientCommands.literal("set")
                                            .then(ClientCommands.argument("scale", FloatArgumentType.floatArg(0.01f, 10.0f))
                                                    .executes(ImeCommand::executeSet)))
                                    .then(ClientCommands.literal("get")
                                            .executes(ImeCommand::executeGet))
                                    .then(ClientCommands.literal("reset")
                                            .executes(ImeCommand::executeReset)))
            );

            dispatcher.register(
                    ClientCommands.literal("imereset")
                            .executes(ctx -> {
                                ItemScaleRegistry.clearAll();
                                ctx.getSource().sendFeedback(Component.translatable(SCALE_RESET_ALL_KEY).withStyle(ChatFormatting.GREEN));
                                return 1;
                            })
            );
        });
    }

    private static int executeSet(CommandContext<FabricClientCommandSource> ctx) {
        FabricClientCommandSource source = ctx.getSource();
        String itemIdStr = ctx.getArgument("item", String.class);
        float scale = ctx.getArgument("scale", Float.class);

        Item item = parseItemById(itemIdStr);
        if (item == null || item == Items.AIR) {
            sendItemNotFound(source, itemIdStr);
            return 0;
        }

        ItemScaleRegistry.setScale(item, scale);

        MutableComponent message = Component.translatable(
                SCALE_SET_KEY,
                Component.literal(itemIdStr).withStyle(ChatFormatting.YELLOW),
                Component.literal(String.format("%.2f", scale)).withStyle(ChatFormatting.GREEN)
        ).withStyle(ChatFormatting.WHITE);
        source.sendFeedback(message);
        return 1;
    }

    private static int executeGet(CommandContext<FabricClientCommandSource> ctx) {
        FabricClientCommandSource source = ctx.getSource();
        String itemIdStr = ctx.getArgument("item", String.class);

        Item item = parseItemById(itemIdStr);
        if (item == null || item == Items.AIR) {
            sendItemNotFound(source, itemIdStr);
            return 0;
        }

        float scale = ItemScaleRegistry.getScale(item);
        MutableComponent message = Component.translatable(
                SCALE_GET_KEY,
                Component.literal(itemIdStr).withStyle(ChatFormatting.YELLOW),
                Component.literal(String.format("%.2f", scale)).withStyle(ChatFormatting.GREEN)
        ).withStyle(ChatFormatting.WHITE);
        source.sendFeedback(message);
        return 1;
    }

    private static int executeReset(CommandContext<FabricClientCommandSource> ctx) {
        FabricClientCommandSource source = ctx.getSource();
        String itemIdStr = ctx.getArgument("item", String.class);

        Item item = parseItemById(itemIdStr);
        if (item == null || item == Items.AIR) {
            sendItemNotFound(source, itemIdStr);
            return 0;
        }

        ItemScaleRegistry.clearScale(item);
        MutableComponent message = Component.translatable(
                SCALE_RESET_KEY,
                Component.literal(itemIdStr).withStyle(ChatFormatting.YELLOW)
        ).withStyle(ChatFormatting.WHITE);
        source.sendFeedback(message);
        return 1;
    }

    private static Item parseItemById(String itemIdStr) {
        return ItemScaleRegistry.findItemById(itemIdStr);
    }

    private static void sendItemNotFound(FabricClientCommandSource source, String itemIdStr) {
        source.sendFeedback(Component.translatable(
                ITEM_NOT_FOUND_KEY,
                Component.literal(itemIdStr).withStyle(ChatFormatting.RED)
        ).withStyle(ChatFormatting.RED));
    }
}
