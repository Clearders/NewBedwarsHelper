package org.exmple.newbedwarshelper.client.z_commands.itemmodelenhance;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.concurrent.CompletableFuture;

public final class ItemIdArgumentType implements ArgumentType<String> {
    private ItemIdArgumentType() {
    }

    public static ItemIdArgumentType itemId() {
        return new ItemIdArgumentType();
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        while (reader.canRead() && reader.peek() != ' ') {
            reader.skip();
        }

        String result = reader.getString().substring(start, reader.getCursor());
        if (result.isEmpty()) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().createWithContext(reader, "item_id");
        }

        return result;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        BuiltInRegistries.ITEM.keySet().forEach(id -> builder.suggest(id.toString()));
        return builder.buildFuture();
    }
}
