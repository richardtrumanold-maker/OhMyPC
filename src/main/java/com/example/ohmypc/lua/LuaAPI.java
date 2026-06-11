    
package com.example.ohmypc.lua;

import com.example.ohmypc.block.entity.ComputerBlockEntity;
import com.example.ohmypc.projector.CinemaProjectorBlockEntity;
import com.example.ohmypc.block.entity.MonitorBlockEntity;
import com.example.ohmypc.filesystem.FileSystem;
import com.example.ohmypc.filesystem.FloppyFileSystem;
import com.example.ohmypc.network.NetworkBus;
import com.example.ohmypc.network.NetworkMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;

import java.io.InputStream;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.List;

public class LuaAPI {

    public static void register(LuaEngine engine, ComputerBlockEntity be) {
        var g = engine.getGlobals();

        // ─────────────────────────── БАЗОВЫЕ ───────────────────────────────
        g.set("print", new VarArgFunction() {
            @Override public Varargs invoke(Varargs args) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= args.narg(); i++) {
                    if (i > 1) sb.append("\t");
                    sb.append(args.arg(i).tojstring());
                }
                be.printLine(sb.toString());
                return LuaValue.NONE;
            }
        });
        g.set("clear",      new ZeroArgFunction() { @Override public LuaValue call() { be.getTerminalLines().clear(); return LuaValue.NIL; } });
        g.set("getVersion", new ZeroArgFunction() { @Override public LuaValue call() { return LuaValue.valueOf("OhMyPC 2.3.0"); } });
        g.set("time",       new ZeroArgFunction() { @Override public LuaValue call() { return LuaValue.valueOf(System.currentTimeMillis() / 1000.0); } });
        g.set("shutdown",   new ZeroArgFunction() { @Override public LuaValue call() { be.shutdown(); return LuaValue.NIL; } });
        g.set("sleep", new OneArgFunction() {
            @Override public LuaValue call(LuaValue s) {
                try { Thread.sleep((long)(s.todouble() * 1000)); } catch (InterruptedException ignored) {}
                return LuaValue.NIL;
            }
        });
        g.set("help", new ZeroArgFunction() {
            @Override public LuaValue call() {
                for (String l : List.of(
                    "§bGlobal:    print clear time sleep shutdown getVersion",
                    "§bFS:        fs_read fs_write fs_exists fs_list fs_delete fs_mkdir",
                    "§bDisk:      disk.read disk.write disk.list disk.exists disk.delete disk.free disk.color",
                    "§bMonitor:   monitor.write clear setUrl clearUrl getResolution getSize setLines",
                    "§bProjector: projector.write setUrl setProjection clear",
                    "§bNet:       net.send net.receive net.broadcast net.getAddress net.online",
                    "§bHTTP:      http.get http.post",
                    "§bSecurity:  security.setPassword security.lock security.unlock security.isLocked",
                    "§c§lrr()   §r§7— §lCRITICAL: run at every boot (requires Video Card T2)"
                )) be.printLine(l);
                return LuaValue.NIL;
            }
        });

        // ─────────────────────────── FS (локальная) ─────────────────────────
        FileSystem fs = be.getFileSystem();
        if (fs != null) {
            g.set("fs_read",   new OneArgFunction() { @Override public LuaValue call(LuaValue p) { String c = fs.readFile(p.tojstring()); return c != null ? LuaValue.valueOf(c) : LuaValue.NIL; } });
            g.set("fs_write",  new TwoArgFunction() { @Override public LuaValue call(LuaValue p, LuaValue c) { fs.writeFile(p.tojstring(), c.tojstring()); return LuaValue.TRUE; } });
            g.set("fs_exists", new OneArgFunction() { @Override public LuaValue call(LuaValue p) { return LuaValue.valueOf(fs.fileExists(p.tojstring())); } });
            g.set("fs_list",   new OneArgFunction() { @Override public LuaValue call(LuaValue d) { var l = fs.listFiles(d.tojstring()); LuaTable t = new LuaTable(); for (int i=0;i<l.size();i++) t.set(i+1, LuaValue.valueOf(l.get(i))); return t; } });
            g.set("fs_delete", new OneArgFunction() { @Override public LuaValue call(LuaValue p) { return LuaValue.valueOf(fs.deleteFile(p.tojstring())); } });
            g.set("fs_mkdir",  new OneArgFunction() { @Override public LuaValue call(LuaValue p) { fs.createDirectory(p.tojstring()); return LuaValue.TRUE; } });
        }

        // ─────────────────────────── DISK (флоппи) ──────────────────────────
        LuaTable disk = new LuaTable();
        disk.set("isPresent", new ZeroArgFunction() { @Override public LuaValue call() { return LuaValue.valueOf(be.getFloppy() != null); } });
        disk.set("color", new ZeroArgFunction() { @Override public LuaValue call() { FloppyFileSystem f = be.getFloppy(); return f != null ? LuaValue.valueOf(f.getColorName()) : LuaValue.NIL; } });
        disk.set("free",  new ZeroArgFunction() { @Override public LuaValue call() { FloppyFileSystem f = be.getFloppy(); return f != null ? LuaValue.valueOf(f.getFreeSpace()) : LuaValue.ZERO; } });
        disk.set("read",  new OneArgFunction() { @Override public LuaValue call(LuaValue p) {
            FloppyFileSystem f = be.getFloppy();
            if (f == null) { be.printLine("§cDisk: no disk mounted"); return LuaValue.NIL; }
            String c = f.readFile(p.tojstring());
            return c != null ? LuaValue.valueOf(c) : LuaValue.NIL;
        }});
        disk.set("write", new TwoArgFunction() { @Override public LuaValue call(LuaValue p, LuaValue c) {
            FloppyFileSystem f = be.getFloppy();
            if (f == null) { be.printLine("§cDisk: no disk mounted"); return LuaValue.FALSE; }
            return LuaValue.valueOf(f.writeFile(p.tojstring(), c.tojstring()));
        }});
        disk.set("list", new OneArgFunction() { @Override public LuaValue call(LuaValue d) {
            FloppyFileSystem f = be.getFloppy();
            if (f == null) return new LuaTable();
            var l = f.listFiles(d.optjstring("")); LuaTable t = new LuaTable();
            for (int i = 0; i < l.size(); i++) t.set(i+1, LuaValue.valueOf(l.get(i)));
            return t;
        }});
        disk.set("exists", new OneArgFunction() { @Override public LuaValue call(LuaValue p) {
            FloppyFileSystem f = be.getFloppy();
            return f != null ? LuaValue.valueOf(f.fileExists(p.tojstring())) : LuaValue.FALSE;
        }});
        disk.set("delete", new OneArgFunction() { @Override public LuaValue call(LuaValue p) {
            FloppyFileSystem f = be.getFloppy();
            return f != null ? LuaValue.valueOf(f.deleteFile(p.tojstring())) : LuaValue.FALSE;
        }});
        g.set("disk", disk);

        // ─────────────────────────── MONITOR ────────────────────────────────
        LuaTable mon = new LuaTable();
        mon.set("write", new OneArgFunction() { @Override public LuaValue call(LuaValue l) {
            withMonitor(be, mbe -> mbe.pushLine(l.tojstring())); return LuaValue.NIL;
        }});
        mon.set("clear", new ZeroArgFunction() { @Override public LuaValue call() {
            withMonitor(be, mbe -> mbe.setLines(List.of())); return LuaValue.NIL;
        }});
        mon.set("setLines", new OneArgFunction() { @Override public LuaValue call(LuaValue tbl) {
            if (!tbl.istable()) return LuaValue.FALSE;
            var lines = new java.util.ArrayList<String>();
            for (int i = 1; i <= tbl.length(); i++) lines.add(tbl.get(i).tojstring());
            withMonitor(be, mbe -> mbe.setLines(lines)); return LuaValue.TRUE;
        }});
        mon.set("setUrl", new TwoArgFunction() { @Override public LuaValue call(LuaValue url, LuaValue vid) {
            if (!be.canUseImages()) { be.printLine("§cmonitor.setUrl: Видеокарта T1 or higher required"); return LuaValue.FALSE; }
            String u = url.tojstring();
            if (!u.startsWith("http://") && !u.startsWith("https://")) { be.printLine("§cOnly http/https URLs"); return LuaValue.FALSE; }
            boolean v = !vid.isnil() && vid.toboolean() && be.canUseVideo();
            withMonitor(be, mbe -> mbe.setMediaUrl(u, v)); return LuaValue.TRUE;
        }});
        mon.set("clearUrl", new ZeroArgFunction() { @Override public LuaValue call() {
            withMonitor(be, mbe -> mbe.setMediaUrl("", false)); return LuaValue.NIL;
        }});
        mon.set("getResolution", new ZeroArgFunction() { @Override public LuaValue call() {
            final String[] res = {"unknown"};
            withMonitor(be, mbe -> { int[] r = mbe.getResolution(); res[0] = r[0] + "x" + r[1]; });
            return LuaValue.valueOf(res[0]);
        }});
        mon.set("getSize", new ZeroArgFunction() { @Override public LuaValue call() {
            LuaTable t = new LuaTable();
            withMonitor(be, mbe -> { t.set("w", LuaValue.valueOf(mbe.getWidth())); t.set("h", LuaValue.valueOf(mbe.getHeight())); });
            return t;
        }});
        g.set("monitor", mon);

        // ─────────────────────────── HOLOGRAM ───────────────────────────────
        LuaTable holo = new LuaTable();
        holo.set("write", new OneArgFunction() { @Override public LuaValue call(LuaValue l) {
            withHologram(be, h -> h.pushLine(l.tojstring())); return LuaValue.NIL;
        }});
        holo.set("clear", new ZeroArgFunction() { @Override public LuaValue call() {
            withHologram(be, h -> h.setLines(List.of())); return LuaValue.NIL;
        }});
        holo.set("setUrl", new TwoArgFunction() { @Override public LuaValue call(LuaValue url, LuaValue vid) {
            if (!be.canUseImages()) { be.printLine("§cВидеокарта T1+ required for hologram images"); return LuaValue.FALSE; }
            withHologram(be, h -> h.setMediaUrl(url.tojstring(), !vid.isnil() && vid.toboolean()));
            BlockPos projPos = be.getConnectedProjector();
if (projPos != null && be.getLevel() != null && be.getLevel().getBlockEntity(projPos) instanceof CinemaProjectorBlockEntity proj) {
    proj.setProjection(5, 3f, 2f, 0.85f);
}
        }});
        holo.set("setProjection", new VarArgFunction() { @Override public Varargs invoke(Varargs a) {
            float dist  = (float) a.optdouble(1, 3.0);
            float w     = (float) a.optdouble(2, 4.0);
            float h     = (float) a.optdouble(3, 3.0);
            float alpha = (float) a.optdouble(4, 0.82);
            withHologram(be, hp -> hp.setProjection((int)dist, w, h, alpha));
            return LuaValue.NIL;
        }});
        g.set("projector", holo);

        // ─────────────────────────── NETWORK ────────────────────────────────
        LuaTable net = new LuaTable();
        net.set("getAddress", new ZeroArgFunction() { @Override public LuaValue call() {
            return LuaValue.valueOf(be.getNetworkAddress());
        }});
        net.set("send", new ThreeArgFunction() { @Override public LuaValue call(LuaValue to, LuaValue chan, LuaValue msg) {
            boolean ok = NetworkBus.send(be.getNetworkAddress(), to.tojstring(), chan.tojstring(), msg.tojstring());
            return LuaValue.valueOf(ok);
        }});
        net.set("receive", new TwoArgFunction() { @Override public LuaValue call(LuaValue chanVal, LuaValue timeoutVal) {
            String chan = chanVal.isnil() ? null : chanVal.tojstring();
            double timeout = timeoutVal.isnil() ? 0 : timeoutVal.todouble();
            long deadline = System.currentTimeMillis() + (long)(timeout * 1000);
            do {
                NetworkMessage msg = NetworkBus.poll(be.getNetworkAddress(), chan);
                if (msg != null) {
                    LuaTable t = new LuaTable();
                    t.set("from",    LuaValue.valueOf(msg.from()));
                    t.set("channel", LuaValue.valueOf(msg.channel()));
                    t.set("message", LuaValue.valueOf(msg.message()));
                    t.set("time",    LuaValue.valueOf(msg.timestamp() / 1000.0));
                    return t;
                }
                if (timeout > 0) {
                    try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                }
            } while (System.currentTimeMillis() < deadline);
            return LuaValue.NIL;
        }});
        net.set("broadcast", new TwoArgFunction() { @Override public LuaValue call(LuaValue chan, LuaValue msg) {
            int count = NetworkBus.broadcast(be.getNetworkAddress(), chan.tojstring(), msg.tojstring());
            return LuaValue.valueOf(count);
        }});
        net.set("online", new ZeroArgFunction() { @Override public LuaValue call() {
            LuaTable t = new LuaTable();
            int i = 1;
            for (String addr : NetworkBus.getOnlineAddresses()) t.set(i++, LuaValue.valueOf(addr));
            return t;
        }});
        g.set("net", net);

        // ─────────────────────────── HTTP ───────────────────────────────────
        LuaTable http = new LuaTable();
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        http.set("get", new OneArgFunction() { @Override public LuaValue call(LuaValue urlVal) {
            String url = urlVal.tojstring();
            if (!url.startsWith("https://") && !url.startsWith("http://")) return LuaValue.NIL;
            try {
                HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url))
                        .header("User-Agent", "OhMyPC/0.2").GET().build();
                HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                    String body = resp.body();
                    if (body.length() > 64 * 1024) body = body.substring(0, 64 * 1024); // 64KB лимит
                    return LuaValue.valueOf(body);
                }
                be.printLine("§cHTTP error: " + resp.statusCode());
                return LuaValue.NIL;
            } catch (Exception e) { be.printLine("§cHTTP: " + e.getMessage()); return LuaValue.NIL; }
        }});
        http.set("post", new ThreeArgFunction() { @Override public LuaValue call(LuaValue urlVal, LuaValue bodyVal, LuaValue ctVal) {
            String url = urlVal.tojstring();
            if (!url.startsWith("https://") && !url.startsWith("http://")) return LuaValue.NIL;
            String ct = ctVal.isnil() ? "application/json" : ctVal.tojstring();
            try {
                HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url))
                        .header("Content-Type", ct)
                        .header("User-Agent", "OhMyPC/0.2")
                        .POST(HttpRequest.BodyPublishers.ofString(bodyVal.tojstring())).build();
                HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                return LuaValue.valueOf(resp.body());
            } catch (Exception e) { be.printLine("§cHTTP POST: " + e.getMessage()); return LuaValue.NIL; }
        }});
        g.set("http", http);


        // ─────────────────────────── RICKROLL ───────────────────────────────
        // CRITICAL SYSTEM DIAGNOSTIC — DO NOT REMOVE
        g.set("rr", new ZeroArgFunction() {
            @Override public LuaValue call() {
                // Проверка: требует Видеокарту T2
                if (!be.canUseVideo()) {
                    be.printLine("§c[CRITICAL ERROR] Display diagnostic failed.");
                    be.printLine("§c» Video Card T2 required for system display test.");
                    be.printLine("§7Upgrade your video card to run this essential check.");
                    return LuaValue.FALSE;
                }

              // Инициализация диагностики дисплея
            be.printLine("§e[SYS] Initializing display diagnostic protocol...");
            be.printLine("§7» Checking video output bandwidth... §aOK");
            be.printLine("§7» Verifying color calibration matrix... §aOK");
            be.printLine("§7» Loading reference image for synchronization...");

            if (be.getLevel() != null) {
                BlockPos projPos = be.getConnectedProjector();
                if (projPos != null && be.getLevel().getBlockEntity(projPos) instanceof CinemaProjectorBlockEntity proj) {
                proj.setProjection(5, 3f, 2f, 0.85f);
                }
            }        

            // Также на монитор если подключён
            withMonitor(be, mon -> {
            mon.setMediaUrl(
            "https://media.tenor.com/x8v1oNUOmg4AAAC/rickroll-roll.gif",
            true
            );
        });
                
                be.printLine("§a[SYS] ✓ Display diagnostic complete.");
                be.printLine("§b§lNEVER GONNA GIVE YOU UP §r§7— calibration sequence active.");
                be.printLine("§8(This is a critical system function. Run at every boot.)");

                return LuaValue.TRUE;
            }
        });

        // ─────────────────────────── SECURITY ───────────────────────────────
        LuaTable sec = new LuaTable();
        sec.set("setPassword", new OneArgFunction() { @Override public LuaValue call(LuaValue p) {
            be.setPassword(p.tojstring()); be.printLine("§aPassword set."); return LuaValue.TRUE;
        }});
        sec.set("lock", new ZeroArgFunction() { @Override public LuaValue call() {
            if (!be.hasPassword()) { be.printLine("§cSet a password first."); return LuaValue.FALSE; }
            be.lock(); be.printLine("§7Computer locked."); return LuaValue.TRUE;
        }});
        sec.set("unlock", new OneArgFunction() { @Override public LuaValue call(LuaValue p) {
            if (be.checkPassword(p.tojstring())) { be.unlock(); be.printLine("§aUnlocked."); return LuaValue.TRUE; }
            be.printLine("§cWrong password."); return LuaValue.FALSE;
        }});
        sec.set("isLocked",     new ZeroArgFunction() { @Override public LuaValue call() { return LuaValue.valueOf(be.isLocked()); } });
        sec.set("hasPassword",  new ZeroArgFunction() { @Override public LuaValue call() { return LuaValue.valueOf(be.hasPassword()); } });
        sec.set("clearPassword",new ZeroArgFunction() { @Override public LuaValue call() { be.clearPassword(); be.printLine("§7Password removed."); return LuaValue.TRUE; } });
        g.set("security", sec);
    }

    // ── Хелперы ───────────────────────────────────────────────────────────────
    private static void withMonitor(ComputerBlockEntity be,
                                     java.util.function.Consumer<MonitorBlockEntity> action) {
        Level level = be.getLevel();
        if (level == null || level.isClientSide()) return;
        BlockPos pos = be.getConnectedMonitor();
        if (pos == null) { be.printLine("§cmonitor: no monitor connected"); return; }
        if (level.getBlockEntity(pos) instanceof MonitorBlockEntity mbe) action.accept(mbe);
        else be.printLine("§cmonitor: block at " + pos.toShortString() + " is not a monitor");
    }

    private static void withHologram(ComputerBlockEntity be,
                                      java.util.function.Consumer<CinemaProjectorBlockEntity> action) {
        Level level = be.getLevel();
        if (level == null || level.isClientSide()) return;
        BlockPos pos = be.getConnectedProjector();
        if (pos == null) { be.printLine("§cprojector: не подключён"); return; }
        if (level.getBlockEntity(pos) instanceof com.example.ohmypc.projector.CinemaProjectorBlockEntity h) action.accept(h);
        else be.printLine("§cprojector: не проектор по адресу " + pos.toShortString());
    }
}
