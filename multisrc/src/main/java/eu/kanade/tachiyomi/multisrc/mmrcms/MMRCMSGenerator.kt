package eu.kanade.tachiyomi.multisrc.mmrcms

import generator.ThemeSourceGenerator

class MMRCMSGenerator : ThemeSourceGenerator {

    override val themePkg = "mmrcms"

    override val themeClass = "MMRCMS"

    override val baseVersionCode: Int = MMRCMSSources.version

    override val sources = MMRCMSSources.sourceList

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            MMRCMSGenerator().createAll()
        }
    }
}
