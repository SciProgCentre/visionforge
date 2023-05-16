package ru.mipt.npm.root

public object RootColors {
    private val colorMap = MutableList(924) { "white" }

    //colorMap[110] = "white"

    private val moreCol = listOf(
        11 to "c1b7ad4d4d4d6666668080809a9a9ab3b3b3cdcdcde6e6e6f3f3f3cdc8accdc8acc3c0a9bbb6a4b3a697b8a49cae9a8d9c8f83886657b1cfc885c3a48aa9a1839f8daebdc87b8f9a768a926983976e7b857d9ad280809caca6c0d4cf88dfbb88bd9f83c89a7dc08378cf5f61ac8f94a6787b946971d45a549300ff7b00ff6300ff4b00ff3300ff1b00ff0300ff0014ff002cff0044ff005cff0074ff008cff00a4ff00bcff00d4ff00ecff00fffd00ffe500ffcd00ffb500ff9d00ff8500ff6d00ff5500ff3d00ff2600ff0e0aff0022ff003aff0052ff006aff0082ff009aff00b1ff00c9ff00e1ff00f9ff00ffef00ffd700ffbf00ffa700ff8f00ff7700ff6000ff4800ff3000ff1800ff0000",
        201 to "5c5c5c7b7b7bb8b8b8d7d7d78a0f0fb81414ec4848f176760f8a0f14b81448ec4876f1760f0f8a1414b84848ec7676f18a8a0fb8b814ecec48f1f1768a0f8ab814b8ec48ecf176f10f8a8a14b8b848ecec76f1f1",
        390 to "ffffcdffff9acdcd9affff66cdcd669a9a66ffff33cdcd339a9a33666633ffff00cdcd009a9a00666600333300",
        406 to "cdffcd9aff9a9acd9a66ff6666cd66669a6633ff3333cd33339a3333663300ff0000cd00009a00006600003300",
        422 to "cdffff9affff9acdcd66ffff66cdcd669a9a33ffff33cdcd339a9a33666600ffff00cdcd009a9a006666003333",
        590 to "cdcdff9a9aff9a9acd6666ff6666cd66669a3333ff3333cd33339a3333660000ff0000cd00009a000066000033",
        606 to "ffcdffff9affcd9acdff66ffcd66cd9a669aff33ffcd33cd9a339a663366ff00ffcd00cd9a009a660066330033",
        622 to "ffcdcdff9a9acd9a9aff6666cd66669a6666ff3333cd33339a3333663333ff0000cd00009a0000660000330000",
        791 to "ffcd9acd9a669a66339a6600cd9a33ffcd66ff9a00ffcd33cd9a00ffcd00ff9a33cd66006633009a3300cd6633ff9a66ff6600ff6633cd3300ff33009aff3366cd00336600339a0066cd339aff6666ff0066ff3333cd0033ff00cdff9a9acd66669a33669a009acd33cdff669aff00cdff339acd00cdff009affcd66cd9a339a66009a6633cd9a66ffcd00ff6633ffcd00cd9a00ffcd33ff9a00cd66006633009a3333cd6666ff9a00ff9a33ff6600cd3300ff339acdff669acd33669a00339a3366cd669aff0066ff3366ff0033cd0033ff339aff0066cd00336600669a339acd66cdff009aff33cdff009acd00cdffcd9aff9a66cd66339a66009a9a33cdcd66ff9a00ffcd33ff9a00cdcd00ff9a33ff6600cd33006633009a6633cd9a66ff6600ff6633ff3300cd3300ffff339acd00666600339a0033cd3366ff669aff0066ff3366cd0033ff0033ff9acdcd669a9a33669a0066cd339aff66cdff009acd009aff33cdff009a",
        920 to "cdcdcd9a9a9a666666333333"
    )

    init {
        colorMap[0] = "white"
        colorMap[1] = "black"
        colorMap[2] = "red"
        colorMap[3] = "green"
        colorMap[4] = "blue"
        colorMap[5] = "yellow"
        colorMap[6] = "magenta"
        colorMap[7] = "cyan"
        colorMap[8] = "rgb(89,212,84)"
        colorMap[9] = "rgb(89,84,217)"
        colorMap[10] = "white"

        moreCol.forEach { (n, s) ->
            for (i in 0 until (s.length / 6)) {
                colorMap[n + i] = "#" + s.substring(i * 6, (i + 1) * 6)
            }
        }
    }

    public operator fun get(index: Int): String = colorMap[index]
}