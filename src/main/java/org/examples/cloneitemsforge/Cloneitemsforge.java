package org.examples.cloneitemsforge;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@Mod(Cloneitemsforge.MODID)
public class Cloneitemsforge {
    public static final String MODID = "cloneitems";

    public Cloneitemsforge() {}

    @Mod.EventBusSubscriber(modid = MODID)
    public static class CommandHandler {
        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            event.getDispatcher().register(
                    Commands.literal("duplicar")
                            .requires(cs -> cs.hasPermission(0))

                            // /duplicar
                            .executes(ctx -> duplicar(ctx.getSource(), 1, false))

                            // /duplicar -s, Privacy mode
                            .then(literal("-s")
                                    .executes(ctx -> duplicar(ctx.getSource(), 1, true))
                                    .then(argument("cantidad", IntegerArgumentType.integer(1))
                                            .executes(ctx -> {
                                                int cantidad = IntegerArgumentType.getInteger(ctx, "cantidad");
                                                return duplicar(ctx.getSource(), cantidad, true);
                                            })
                                    )
                            )

                            // /duplicar <cantidad>
                            .then(argument("cantidad", IntegerArgumentType.integer(1))
                                    .executes(ctx -> {
                                        int cantidad = IntegerArgumentType.getInteger(ctx, "cantidad");
                                        return duplicar(ctx.getSource(), cantidad, false);
                                    })
                                    .then(literal("-s")
                                            .executes(ctx -> {
                                                int cantidad = IntegerArgumentType.getInteger(ctx, "cantidad");
                                                return duplicar(ctx.getSource(), cantidad, true);
                                            })
                                    )
                            )
            );
        }

        private static int duplicar(CommandSourceStack source, int cantidad, boolean silent) {
            var player = source.getPlayer();

            if (cantidad > 50) {
                if (!silent)
                    source.sendFailure(Component.translatable("message.cloneitems.max_duplication"));
                return 0;
            } else if (cantidad < 1) {
                if (!silent)
                    source.sendFailure(Component.translatable("message.cloneitems.min_duplication"));
                return 0;
            }

            if (player != null) {
                var itemStack = player.getMainHandItem();
                if (!itemStack.isEmpty()) {
                    int agregados = 0;

                    for (int i = 0; i < cantidad; i++) {
                        var duplicado = itemStack.copy();
                        if (player.getInventory().add(duplicado)) {
                            agregados++;
                        } else {
                            break;
                        }
                    }

                    if (agregados > 0) {
                        if (!silent)
                            source.sendSuccess(() -> Component.translatable("message.cloneitems.success"), false);
                    } else {
                        if (!silent)
                            source.sendFailure(Component.translatable("message.cloneitems.inventory_full"));
                    }
                } else {
                    if (!silent)
                        source.sendFailure(Component.translatable("message.cloneitems.no_item"));
                }
            }

            return 1;
        }
    }
}
