@file:JvmName("-${typename}")
@file:Suppress("MemberVisibilityCanBePrivate", "unused", "RemoveRedundantCallsOfConversionMethods", "UnusedImport")
package ${package}

import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmName

fun ${typename}(rawValue: U${rawType}) = ${typename}(rawValue.to${rawType}())

@OptIn(ExperimentalStdlibApi::class)
@JvmInline
value class ${typename}(val rawValue: ${rawType}){
    object Constants{
        <#list list as item>
        const val ${item.name}: ${rawType} = ${item.value}.to${rawType}()
        </#list>
    }

    companion object{
        fun valueOf(name: String): ${typename} = when(name){
        <#list list as item>
            "${item.name}" -> ${item.name}
        </#list>
            else -> run {
                val s = name.removePrefix("${typename}").removeSurrounding("(", ")")
                if(!s.startsWith("0x")) throw IllegalArgumentException("Invalid ${typename} value: should be a known value or a hex number(with \"0x\" prefix)")
                ${typename}(s.removePrefix("0x").toU${rawType}(16))
            }
        }

        fun valueOfOrNull(name: String): ${typename}? = when(name){
        <#list list as item>
            "${item.name}" -> ${item.name}
        </#list>
            else -> null
        }

    <#list list as item>
        val ${item.name}: ${typename} = ${typename}(Constants.${item.name})
    </#list>
    }

    override fun toString(): String = when(rawValue){
    <#list list as item>
        Constants.${item.name} -> "${item.name}"
    </#list>
        else -> "0x" + rawValue.toU${rawType}().toHexString()
    }
}
