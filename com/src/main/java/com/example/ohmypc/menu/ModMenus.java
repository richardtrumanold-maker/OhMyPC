package com.example.ohmypc.menu;

import com.example.ohmypc.Ohmypc;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Ohmypc.MOD_ID);

    /** Меню сборки компьютера */
    public static final RegistryObject<MenuType<ComputerAssemblyMenu>> COMPUTER_ASSEMBLY =
            MENUS.register("computer_assembly",
                    () -> IForgeMenuType.create(ComputerAssemblyMenu::new));

    /** Lua-терминал */
    public static final RegistryObject<MenuType<LuaTerminalMenu>> LUA_TERMINAL =
            MENUS.register("lua_terminal",
                    () -> IForgeMenuType.create(LuaTerminalMenu::new));

    // Обратная совместимость: старый ComputerMenu → теперь это сборка
    public static final RegistryObject<MenuType<ComputerAssemblyMenu>> COMPUTER_MENU =
            COMPUTER_ASSEMBLY;
}
