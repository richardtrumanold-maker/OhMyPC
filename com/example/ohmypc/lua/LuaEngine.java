package com.example.ohmypc.lua;

import com.example.ohmypc.block.entity.ComputerBlockEntity;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.JsePlatform;

/**
 * Обёртка над LuaJ Globals.
 * executeScript возвращает строку результата или "Error: ..." при краше.
 */
public class LuaEngine {

    private final Globals globals;

    public LuaEngine() {
        globals = JsePlatform.standardGlobals();
    }

    public Globals getGlobals() { return globals; }

    /**
     * Выполнить Lua-код.
     * @param be  компьютер для уведомления о краше (может быть null)
     * @return строка результата, "Error: ..." при LuaError, или ""
     */
    public String executeScript(String script, ComputerBlockEntity be) {
        try {
            LuaValue chunk = globals.load(script, "input");
            LuaValue result = chunk.call();
            if (result != null && !result.isnil()) return result.tojstring();
            return "";
        } catch (LuaError e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Unknown Lua error";
            if (be != null) be.crash(msg);
            return "Error: " + msg;
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            if (be != null) be.crash(msg);
            return "Error: " + msg;
        }
    }

    /** Устаревший вариант без уведомления о краше */
    public LuaValue executeScript(String script) {
        try {
            return globals.load(script, "input").call();
        } catch (Exception e) {
            return LuaValue.valueOf("Error: " + e.getMessage());
        }
    }
}
