package net.mitisui.lagoa;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.mitisui.lagoa.commands.*;
import net.mitisui.lagoa.events.*;
import net.mitisui.lagoa.logger.LogWriter;
import net.mitisui.lagoa.logger.ServerLoggerUtils;
import net.mitisui.lagoa.mechanics.TeleportSystem;
import net.mitisui.lagoa.menu.InvestigacaoMenu;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Mod(Lagoa.MODID)
public class Lagoa {

    public static final String MODID = "lagoa";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Lagoa() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        Config.register();

        MinecraftForge.EVENT_BUS.register(this);

        MinecraftForge.EVENT_BUS.register(PistolaEvents.class);
        MinecraftForge.EVENT_BUS.register(AlgemaEvents.class);
        MinecraftForge.EVENT_BUS.register(GlobalEvents.class);
        MinecraftForge.EVENT_BUS.register(SwordEvents.class);
        MinecraftForge.EVENT_BUS.register(InvestigacaoEvents.class);

        MinecraftForge.EVENT_BUS.register(ServerLoggerUtils.class);


    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        if (Config.ALLOW_LOGGER.get()) {
            String serverName = event.getServer().getWorldData().getLevelName();
            LogWriter.initialize(serverName);
            LOGGER.info("Logger habilitado para o servidor: {}", serverName);
        } else {
            LOGGER.info("Logger desabilitado via config");
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            TeleportSystem.tick();
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        LiteralArgumentBuilder<CommandSourceStack> lagoaRoot = Commands.literal("lagoa");

        AlgemaCommands.registrar(lagoaRoot);
        WebLoggerCommands.registrar(lagoaRoot);
        PistolaCommands.registrar(lagoaRoot);
        SwordsCommands.registrar(lagoaRoot);
        TeleportCommands.registrar(lagoaRoot);
        InvestigacaoCommands.registrar(lagoaRoot);

        dispatcher.register(lagoaRoot);

        Lagoa.LOGGER.info("Comandos Lagoa registrados com o prefixo /lagoa");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }
}