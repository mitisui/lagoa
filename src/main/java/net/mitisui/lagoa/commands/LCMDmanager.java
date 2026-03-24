package net.mitisui.lagoa.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.mitisui.lagoa.mecanicas.algema.LCMDalgema;
import net.mitisui.lagoa.mecanicas.anuncio.LCMDads;
import net.mitisui.lagoa.mecanicas.area.LCMDarea;
import net.mitisui.lagoa.mecanicas.cargos.LCMDcargos;
import net.mitisui.lagoa.mecanicas.clima.LCMDclima;
import net.mitisui.lagoa.mecanicas.clima.LCMDtempo;
import net.mitisui.lagoa.mecanicas.espadas.LCMDespadas;
import net.mitisui.lagoa.mecanicas.permissoes.LCMDperm;
import net.mitisui.lagoa.mecanicas.pistola.LCMDpistola;
import net.mitisui.lagoa.mecanicas.pix.LCMDpix;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.minecraft.commands.Commands;

public class LCMDmanager {

    private static final int PERM_DONO = 4;
    private static final int PERM_EXTRA = 3;
    private static final int PERM_MOD = 2;
    private static final int PERM_VIP = 1;
    private static final int PERM_TODOS = 0;

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // Raiz /lagoa com todos os sub-comandos
        dispatcher.register(Commands.literal("lagoa")
            .then(LCMDarea.register(PERM_EXTRA))
            .then(LCMDcargos.register(PERM_EXTRA))
            .then(LCMDclima.register(PERM_MOD))
            .then(LCMDtempo.register(PERM_MOD))
            .then(LCMDads.register(PERM_VIP))

            .then(LCMDalgema.register(PERM_MOD))
            .then(LCMDpistola.register(PERM_MOD))
            .then(LCMDespadas.register(PERM_EXTRA))



            .then(LCMDperm.register(PERM_DONO))
        );

        // /pix como comando raiz separado
        dispatcher.register(LCMDpix.register(PERM_TODOS));
    }
}
