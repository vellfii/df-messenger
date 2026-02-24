package net.velli.df_messenger;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class DFMKeyBinds {
    public static KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of(DFMessenger.MODID, "key." + DFMessenger.MODID +".category"));


    public static KeyBinding OPEN_MENU = createOpenMenu();


    private static KeyBinding createOpenMenu() {
        KeyBinding OPEN_MENU = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + DFMessenger.MODID +".open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                CATEGORY
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_MENU.wasPressed()) MessageHandler.instance.onKeyPressed();
        });
        return OPEN_MENU;
    }

    public static void init() {}
}
