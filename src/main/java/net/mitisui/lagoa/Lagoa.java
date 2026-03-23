package net.mitisui.lagoa;

import com.mojang.authlib.properties.PropertyMap;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.mitisui.lagoa.commands.LCMDmanager;
import net.mitisui.lagoa.events.AreaEvents;
import net.mitisui.lagoa.events.EvUtils;
import net.mitisui.lagoa.mecanicas.area.AreaManager;
import net.mitisui.lagoa.mecanicas.cargos.CargoManager;
import net.mitisui.lagoa.mecanicas.clima.ClimaManager;
import net.mitisui.lagoa.mecanicas.clima.LCMDtempo;
import net.mitisui.lagoa.mecanicas.permissoes.PermissaoManager;
import net.mitisui.lagoa.utils.IronsSpellsIntegration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import java.util.Optional;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Lagoa.MODID)
public class Lagoa {
    public static final String MODID = "lagoa";
    private static final Logger LOGGER = LogUtils.getLogger();


    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> LAGOA = CREATIVE_MODE_TABS.register("lagoa_dos_sapos_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.lagoa"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> {
                        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
                        head.set(DataComponents.PROFILE, new ResolvableProfile(
                                Optional.of("Choke7"),
                                Optional.empty(),
                                new PropertyMap()
                        ));
                        return head;
                    })
                    .displayItems((parameters, output) -> {
        output.accept(Items.NETHER_STAR);
    }).build());

    public Lagoa(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        CREATIVE_MODE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        if (ModList.get().isLoaded("irons_spellbooks")) {
            NeoForge.EVENT_BUS.register(IronsSpellsIntegration.class);
        }

        NeoForge.EVENT_BUS.register(AreaEvents.class);
        NeoForge.EVENT_BUS.register(ClimaManager.class);
        NeoForge.EVENT_BUS.register(LCMDtempo.class);
        NeoForge.EVENT_BUS.register(EvUtils.class);
        NeoForge.EVENT_BUS.register(AreaEvents.class);
        NeoForge.EVENT_BUS.register(LCMDmanager.class);


        modEventBus.addListener(this::addCreative);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) event.accept(Items.NETHER_STAR);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        CargoManager.setServer(event.getServer());
        CargoManager.carregar();
        AreaManager.carregar();
        PermissaoManager.carregar();
    }
}
