package com.example.ohmypc.lua;

import com.example.ohmypc.block.entity.ComputerBlockEntity;
import com.example.ohmypc.block.entity.MonitorBlockEntity;
import com.example.ohmypc.block.entity.NetworkHubBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class LuaAPI {

    private final ComputerBlockEntity computer;

    public LuaAPI(ComputerBlockEntity computer) {
        this.computer = computer;
    }

    public Map<String, Object> getGlobals() {
        Map<String, Object> globals = new HashMap<>();

        globals.put("print", (LuaFunction) args -> {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (i > 0) sb.append("\t");
                sb.append(args[i] != null ? args[i].toString() : "nil");
            }
            computer.addTerminalLine(sb.toString());
            return null;
        });

        globals.put("clear", (LuaFunction) args -> {
            computer.clearTerminal();
            return null;
        });

        globals.put("getMonitorSize", (LuaFunction) args -> {
            BlockPos monitorPos = computer.getConnectedMonitor();
            if (monitorPos != null && computer.getLevel() != null) {
                if (computer.getLevel().getBlockEntity(monitorPos) instanceof MonitorBlockEntity mon) {
                    return new Object[]{mon.getWidth(), mon.getHeight()};
                }
            }
            return new Object[]{0, 0};
        });

        globals.put("setPixel", (LuaFunction) args -> {
            if (args.length < 4) return "Usage: setPixel(x, y, r, g, b)";
            int x = ((Number) args[0]).intValue();
            int y = ((Number) args[1]).intValue();
            int r = ((Number) args[2]).intValue();
            int g = ((Number) args[3]).intValue();
            int b = args.length > 4 ? ((Number) args[4]).intValue() : 0;
            BlockPos monitorPos = computer.getConnectedMonitor();
            if (monitorPos != null && computer.getLevel() != null) {
                if (computer.getLevel().getBlockEntity(monitorPos) instanceof MonitorBlockEntity mon) {
                    mon.setPixel(x, y, r, g, b);
                }
            }
            return null;
        });

        globals.put("fillRect", (LuaFunction) args -> {
            if (args.length < 6) return "Usage: fillRect(x, y, w, h, r, g, b)";
            int x = ((Number) args[0]).intValue();
            int y = ((Number) args[1]).intValue();
            int w = ((Number) args[2]).intValue();
            int h = ((Number) args[3]).intValue();
            int r = ((Number) args[4]).intValue();
            int g = ((Number) args[5]).intValue();
            int b = args.length > 6 ? ((Number) args[6]).intValue() : 0;
            BlockPos monitorPos = computer.getConnectedMonitor();
            if (monitorPos != null && computer.getLevel() != null) {
                if (computer.getLevel().getBlockEntity(monitorPos) instanceof MonitorBlockEntity mon) {
                    for (int px = x; px < x + w; px++) {
                        for (int py = y; py < y + h; py++) {
                            mon.setPixel(px, py, r, g, b);
                        }
                    }
                }
            }
            return null;
        });

        globals.put("clearScreen", (LuaFunction) args -> {
            BlockPos monitorPos = computer.getConnectedMonitor();
            if (monitorPos != null && computer.getLevel() != null) {
                if (computer.getLevel().getBlockEntity(monitorPos) instanceof MonitorBlockEntity mon) {
                    mon.clearScreen();
                }
            }
            return null;
        });

        globals.put("getConnectedDevices", (LuaFunction) args -> {
            Map<String, Boolean> devices = new HashMap<>();
            devices.put("monitor", computer.getConnectedMonitor() != null);
            devices.put("hub", computer.getConnectedHub() != null);
            return devices;
        });

        globals.put("networkSend", (LuaFunction) args -> {
            if (args.length < 1) return "Usage: networkSend(message)";
            String message = args[0].toString();
            BlockPos hubPos = computer.getConnectedHub();
            if (hubPos != null && computer.getLevel() != null) {
                if (computer.getLevel().getBlockEntity(hubPos) instanceof NetworkHubBlockEntity hub) {
                    hub.broadcast(message, computer.getBlockPos());
                    return true;
                }
            }
            return false;
        });

        globals.put("sleep", (LuaFunction) args -> {
            if (args.length < 1) return "Usage: sleep(seconds)";
            double seconds = ((Number) args[0]).doubleValue();
            try {
                Thread.sleep((long) (seconds * 1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        });

        globals.put("getTime", (LuaFunction) args -> {
            if (computer.getLevel() != null) {
                return computer.getLevel().getDayTime();
            }
            return 0L;
        });

        globals.put("getPos", (LuaFunction) args -> {
            BlockPos pos = computer.getBlockPos();
            return new Object[]{pos.getX(), pos.getY(), pos.getZ()};
        });

        return globals;
    }

    @FunctionalInterface
    public interface LuaFunction {
        Object call(Object... args);
    }
}
