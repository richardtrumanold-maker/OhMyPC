package com.example.ohmypc.client.screen;

import java.util.List;

/**
 * Весь контент гайд-бука.
 * Структура: категория → список страниц → текст + рецепт (строкой item_id).
 */
public class ManualContent {

    public record Recipe(String[] inputs, String output) {}
    public record Page(String title, List<String> lines, Recipe recipe) {}
    public record Category(String icon, String title, List<Page> pages) {}

    public static final List<Category> CATEGORIES = List.of(

        // ─────────────────────────── НАЧАЛО РАБОТЫ ──────────────────────────
        new Category("ti-rocket", "§aНачало работы", List.of(
            new Page("С чего начать", List.of(
                "§fOhMyPC §7— мод на программируемые компьютеры для Minecraft.",
                " ",
                "§eШаг 1. §7Создай §fСтартовый набор",
                "§8Рецепт: Ключ + Провод + Корпус",
                " ",
                "§eШаг 2. §7ПКМ стартовым набором — компьютер",
                "§7появится прямо перед тобой с CPU и RAM внутри.",
                " ",
                "§eШаг 3. §7Поставь монитор рядом, соедини кабелем.",
                " ",
                "§eШаг 4. §7ПКМ по компьютеру → §fAssembly →",
                "§7нажми §fBoot§7. Компьютер запустится.",
                " ",
                "§eШаг 5. §7ПКМ пустой рукой → §fТерминал§7.",
                "§7Введи §fhelp()§7 для списка команд."
            ), null),
            new Page("Стартовый набор", List.of(
                "§fСтартовый набор §7— одноразовый предмет.",
                "Ставит компьютер перед тобой и выдаёт:",
                " ",
                "§a✓ §fКомпьютер §7с CPU T1 + RAM + HDD",
                "§a✓ §fМонитор §7×1",
                "§a✓ §fКабель §7×3",
                "§a✓ §fБелый флоппи-диск §7×1",
                " ",
                "§8Крафт: Ключ + Провод + Корпус"
            ), new Recipe(
                new String[]{"ohmypc:silicon_key", "air", "ohmypc:copper_wire",
                             "air",                "ohmypc:computer_case", "air",
                             "air",                "air",                  "air"},
                "ohmypc:starter_kit"
            )),
            new Page("Индикатор состояния", List.of(
                "§fИндикатор §7на передней панели компьютера:",
                " ",
                "§8● §7Тёмный — выключен",
                "§e● §7Жёлтый мигающий — загрузка",
                "§a● §7Зелёный — работает",
                "§c● §7Красный мигающий — авария (BSOD)",
                " ",
                "§7При §cBSOD §7перечитай скрипт на ошибки.",
                "§7ПКМ → Assembly → Shutdown → Boot.",
                " ",
                "§7Совет: §fShift+ПКМ §7всегда открывает",
                "§7меню сборки, даже если компьютер ON."
            ), null)
        )),

        // ─────────────────────────── КОМПОНЕНТЫ ─────────────────────────────
        new Category("ti-cpu", "§bКомпоненты", List.of(
            new Page("Базовые материалы", List.of(
                "§fКремниевый ключ §7(Silicon Key)",
                "§8Основа всех электронных компонентов.",
                "§8Крафт: кремний (кварц) + золотой самородок",
                " ",
                "§fМедный провод §7(Copper Wire)",
                "§8Соединяет компоненты на плате.",
                "§8Крафт: медный слиток + нить",
                " ",
                "§fКорпус §7(Computer Case)",
                "§8Основа для стартового набора.",
                "§8Крафт: 6× железный слиток (П-образно)"
            ), null),
            new Page("Транзистор и плата", List.of(
                "§fТранзистор — §7базовый элемент.",
                "§8Крафт: золотой самородок + редстоун + медь",
                " ",
                "§fПлата схем — §7из транзисторов.",
                "§8Крафт: 8× транзисторов + 1× золото (центр)",
                " ",
                "§7Используются для создания CPU, GPU,",
                "§7сетевых карт и других компонентов."
            ), new Recipe(
                new String[]{"ohmypc:transistor","ohmypc:transistor","ohmypc:transistor",
                             "ohmypc:transistor","minecraft:gold_ingot","ohmypc:transistor",
                             "ohmypc:transistor","ohmypc:transistor","ohmypc:transistor"},
                "ohmypc:circuit_board"
            )),
            new Page("CPU", List.of(
                "§fCPU Tier I — §a100 MHz",
                "§7Базовые операции Lua.",
                "§8Крафт: Платы + алмаз",
                " ",
                "§fCPU Tier II — §b500 MHz",
                "§7Быстрее, поддержка сети.",
                "§8Крафт: CPU T1 + изумруды",
                " ",
                "§7Без CPU компьютер §cне запустится§7."
            ), new Recipe(
                new String[]{"ohmypc:circuit_board","ohmypc:circuit_board","ohmypc:circuit_board",
                             "ohmypc:circuit_board","minecraft:diamond","ohmypc:circuit_board",
                             "ohmypc:circuit_board","ohmypc:circuit_board","ohmypc:circuit_board"},
                "ohmypc:cpu_tier_1"
            )),
            new Page("RAM и Storage", List.of(
                "§fRAM (ОЗУ) — §7обязательна для старта.",
                "§8Крафт: 3× транзисторов + 3× плат",
                "§8Выдаёт 2 штуки. Можно поставить до 2 шт.",
                " ",
                "§fHDD (Жёсткий диск) —",
                "§7хранит скрипты и файлы.",
                "§8Крафт: 8× железо + плата схем",
                " ",
                "§fSSD (Твердотельный) —",
                "§7то же, но быстрее и больше.",
                "§8Крафт: 6× золото + плата схем"
            ), null),
            new Page("GPU", List.of(
                "§fВидеокарта T I —",
                "§7Изображения на мониторе (PNG/JPEG/GIF).",
                "§7Нужен для §fmonitor.setUrl()§7.",
                "§8Крафт: 8× золото + плата схем",
                " ",
                "§fВидеокарта T II —",
                "§7Видео и GIF-анимации.",
                "§7Нужен для §fhologram.setUrl(url, true)§7.",
                "§8Крафт: 8× изумруд + Видеокарта T1"
            ), null),
            new Page("Сетевые карты", List.of(
                "§fСетевая карта (100 Мбит) —",
                "§7Базовая связь между компьютерами.",
                "§7Без ограничения по дистанции (кабель).",
                " ",
                "§fWi-Fi Модем —",
                "§7Беспроводная связь, радиус §e64 блока§7.",
                " ",
                "§fWi-Fi Модем 5G —",
                "§7Радиус §b128 блоков§7.",
                " ",
                "§7Без сетевой карты команды §fnet.*",
                "§7работать не будут."
            ), null)
        )),

        // ─────────────────────────── БЛОКИ ──────────────────────────────────
        new Category("ti-box", "§6Блоки", List.of(
            new Page("Компьютер", List.of(
                "§fСистемный блок — §7основа всего.",
                " ",
                "§eПКМ пустой рукой (ON) §7→ Терминал",
                "§eShift+ПКМ / ПКМ с предметом §7→ Сборка",
                " ",
                "§7Слоты сборки:",
                "§b0§7 — CPU (обязательно)",
                "§b1-2§7 — RAM (хотя бы один, обязательно)",
                "§b3§7 — GPU (для картинок/видео)",
                "§b4§7 — Storage (HDD/SSD)",
                "§b5§7 — Сетевая карта / Wi-Fi"
            ), new Recipe(
                new String[]{"minecraft:iron_ingot","minecraft:iron_ingot","minecraft:iron_ingot",
                             "minecraft:iron_ingot","ohmypc:circuit_board","minecraft:iron_ingot",
                             "minecraft:iron_ingot","ohmypc:cpu_tier_1","minecraft:iron_ingot"},
                "ohmypc:computer"
            )),
            new Page("Монитор", List.of(
                "§fМонитор §7— дисплей компьютера.",
                " ",
                "§7Подключается кабелем к компьютеру.",
                "§7Поддерживает §fмультиблок §7до §e12×24 §7блоков.",
                " ",
                "§7Разрешение зависит от размера:",
                "§81×1 →§7 360p   §b6×9 →§7 1080p",
                "§86×12 →§7 1440p  §b12×24 →§7 4K",
                " ",
                "§7Просто поставь несколько мониторов",
                "§7вплотную — они автоматически сольются."
            ), new Recipe(
                new String[]{"minecraft:iron_ingot","minecraft:iron_ingot","minecraft:iron_ingot",
                             "minecraft:glass","ohmypc:circuit_board","minecraft:glass",
                             "minecraft:iron_ingot","minecraft:iron_ingot","minecraft:iron_ingot"},
                "ohmypc:monitor"
            )),
            new Page("Кабель и Дисковод", List.of(
                "§fСетевой кабель —",
                "§7соединяет компьютер с монитором,",
                "§7проектором или дисководом.",
                " ",
                "§eПКМ по кабелю: §7первый конец → второй конец.",
                "§7Сообщение §aConnected! §7подтвердит связь.",
                " ",
                "§fДисковод —",
                "§7принимает флоппи-диски.",
                "§7Диск монтируется как §f/disk/§7 в ФС.",
                " ",
                "§8Оба подключаются кабелем к компьютеру."
            ), null),
            new Page("Кино проектор", List.of(
                "§fПроектор §7рисует §5голограмму §7в воздухе.",
                " ",
                "§7Подключается кабелем к компьютеру.",
                "§7Проецирует перед собой на расстояние",
                "§71–16 блоков.",
                " ",
                "§7В Lua:",
                "§fhologram.setProjection(dist, w, h, alpha)",
                "§fhologram.write(\"текст\")",
                "§fhologram.setUrl(\"https://...\")",
                " ",
                "§7Для картинок нужен §fВидеокарта T1§7.",
                "§7Для видео — §fВидеокарта T2§7."
            ), new Recipe(
                new String[]{"minecraft:ender_pearl","minecraft:glass","minecraft:ender_pearl",
                             "minecraft:glass","ohmypc:circuit_board","minecraft:glass",
                             "minecraft:iron_ingot","minecraft:iron_ingot","minecraft:iron_ingot"},
                "ohmypc:cinema_projector"
            ))
        )),

        // ─────────────────────────── ФЛОППИ ─────────────────────────────────
        new Category("ti-disc", "§dФлоппи-диски", List.of(
            new Page("Как работают", List.of(
                "§fФлоппи-диски §7— носимые носители данных.",
                " ",
                "§7Каждый цвет = отдельная папка на диске:",
                "§f[папка игры]/floppy/[цвет]/",
                " ",
                "§eОдинаковый цвет = одни и те же файлы",
                "§eна любом мире или сервере!",
                " ",
                "§7Синий диск здесь = синий диск там.",
                "§7Отличный способ переносить скрипты."
            ), null),
            new Page("Как использовать", List.of(
                "§e1. §7Вставь диск в §fДисковод§7.",
                "§e2. §7Подключи дисковод к компьютеру кабелем.",
                "§e3. §7Диск доступен как §f/disk/",
                " ",
                "§7Команды в Lua:",
                "§fdisk.isPresent()   §8→ true/false",
                "§fdisk.color()       §8→ \"red\", \"blue\"...",
                "§fdisk.list(\"\")     §8→ список файлов",
                "§fdisk.read(\"file\") §8→ содержимое",
                "§fdisk.write(\"f\",t) §8→ записать",
                "§fdisk.free()        §8→ свободно (байт)"
            ), null),
            new Page("Сохранение скриптов", List.of(
                "§7Сохрани скрипт на диск:",
                "§f  disk.write(",
                "§f    \"myscript.lua\",",
                "§f    fs_read(\"myscript.lua\")",
                "§f  )",
                " ",
                "§7Загрузи на другом компьютере:",
                "§f  local code = disk.read(\"myscript.lua\")",
                "§f  fs_write(\"startup.lua\", code)",
                " ",
                "§7Совет: §fразные цвета = разные проекты.",
                "§8Красный → охранная система",
                "§8Синий → мониторинг сервера",
                "§8Зелёный → автоматизация ферм"
            ), null)
        )),

        // ─────────────────────────── LUA API ────────────────────────────────
        new Category("ti-terminal-2", "§eLua API", List.of(
            new Page("Базовые функции", List.of(
                "§fprint(...)      §8→ вывод в терминал",
                "§fprint(\"a\",\"b\") §8→ a   b",
                " ",
                "§fclear()          §8очистить терминал",
                "§fsleep(sec)       §8пауза N секунд",
                "§ftime()           §8→ unix-время (float)",
                "§fshutdown()       §8выключить компьютер",
                "§fgetVersion()     §8→ \"OhMyPC 2.3.0\"",
                "§fhelp()           §8все команды в терминал"
            ), null),
            new Page("Файловая система", List.of(
                "§ffs_read(path)         §8→ string|nil",
                "§ffs_write(path, text)  §8→ true",
                "§ffs_exists(path)       §8→ bool",
                "§ffs_list(dir)          §8→ table",
                "§ffs_delete(path)       §8→ bool",
                "§ffs_mkdir(path)        §8→ true",
                " ",
                "§7Файлы хранятся на сервере.",
                "§7Путь §f/§7 = корень ФС компьютера.",
                "§7Путь §f/disk/§7 = вставленный флоппи."
            ), null),
            new Page("Монитор и Голограмма", List.of(
                "§fmonitor.write(text)    §8строка на экран",
                "§fmonitor.clear()        §8очистить",
                "§fmonitor.setLines(tbl)  §8задать все строки",
                "§fmonitor.setUrl(url)    §8картинка (Видеокарта T1+)",
                "§fmonitor.setUrl(u,true) §8видео (Видеокарта T2)",
                "§fmonitor.clearUrl()     §8обратно в текст",
                "§fmonitor.getResolution()§8→ \"1920x1080\"",
                " ",
                "§fhologram.write(text)",
                "§fhologram.setUrl(url, isVideo)",
                "§fhologram.setProjection(dist,w,h,alpha)",
                "§fhologram.clear()"
            ), null),
            new Page("Сеть", List.of(
                "§fnet.getAddress()       §8→ \"a3f2c1b8\"",
                "§fnet.send(to,ch,msg)    §8→ bool",
                "§fnet.receive(ch,timeout)§8→ table|nil",
                "§fnet.broadcast(ch,msg)  §8→ count",
                "§fnet.online()           §8→ table адресов",
                " ",
                "§7receive() возвращает:",
                "§8{from, channel, message, time}",
                " ",
                "§7Wi-Fi: радиус 64 блока.",
                "§7Кабель: без ограничений."
            ), null),
            new Page("HTTP и Security", List.of(
                "§fhttp.get(url)           §8→ string|nil",
                "§fhttp.post(url,body,ct)  §8→ string|nil",
                "§8Только https://. Лимит 64 КБ.",
                " ",
                "§fsecurity.setPassword(p) §8задать пароль",
                "§fsecurity.lock()         §8заблокировать",
                "§fsecurity.unlock(p)      §8→ bool",
                "§fsecurity.isLocked()     §8→ bool",
                "§fsecurity.clearPassword()§8убрать пароль"
            ), null)
        )),

        // ─────────────────────────── СЕТЬ ───────────────────────────────────
        new Category("ti-wifi", "§3Сеть", List.of(
            new Page("Подключение", List.of(
                "§7Для сети нужна §fСетевая карта",
                "§7в слоте Network компьютера.",
                " ",
                "§fПроводное подключение §7(Wired):",
                "§7Кабель → Network Hub → кабель к другому",
                "§7компьютеру. §7Без ограничения дистанции.",
                " ",
                "§fБеспроводное §7(Wi-Fi Modem):",
                "§7Радиус: §e64 блока §7(обычный)",
                "§7         §b128 блоков §7(5G модем)",
                "§7Без кабелей — просто в радиусе."
            ), null),
            new Page("Отправка сообщений", List.of(
                "§7Каждый компьютер имеет адрес:",
                "§f  print(net.getAddress())",
                "§8  → \"a3f2c1b8\"",
                " ",
                "§7Отправить напрямую:",
                "§f  net.send(\"a3f2c1b8\", \"chat\", \"привет\")",
                " ",
                "§7Широковещание (всем):",
                "§f  net.broadcast(\"alert\", \"опасность!\")",
                " ",
                "§7Получить сообщение (ждать 5 сек):",
                "§f  local m = net.receive(\"chat\", 5)",
                "§f  if m then print(m.message) end"
            ), null)
        ))
    );
}
