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

/**
* Type for C flag-set ${typename}
*
* Raw type: [${rawType}]
*/
@OptIn(ExperimentalStdlibApi::class)
@JvmInline
value class ${typename}(val rawValue: ${rawType}): Set<${typename}>{
    object Constants{
    <#list list as item>
        /**
        * ${item.docs}
        *
        * Raw value: `${item.value}`
        */
        const val ${item.name}: ${rawType} = ${item.value}.to${rawType}()
    </#list>
    }
    companion object{
        val ZERO = ${typename}(0.to${rawType}())
    <#list list as item>
        /**
        * ${item.docs}
        *
        * Raw value: `${item.value}`
        */
        val ${item.name}: ${typename} = ${typename}(Constants.${item.name})
    </#list>

/**
* Creates a ${typename} from its name
*
* @param name the name of the flag value
* @return the flag value, null if the name is not a known value
*/
        @JvmStatic
        fun valueOfOrNull(name: String): ${typename}? = when(name){
            <#list list as item>
            "${item.name}" -> ${item.name}
            </#list>
            else -> null
        }

/**
* Creates a ${typename} from a string
*
* The input string might be in one of the following formats:
*   - The name of the flag value.
*   - The hexadecimal representation of the flag value, prefixed with "0x".
* @param name the name of the flag value, or the text representation
* @return the flag value
* @throws IllegalArgumentException neither a known flag value nor a valid hexadecimal number
*/
        @JvmStatic
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
    }

    override fun toString(): String {
        if (size == 1) {
            return when (rawValue) {
            <#list list as item>
                Constants.${item.name} -> "${item.name}"
            </#list>
                else -> "0x" + rawValue.toU${rawType}().toHexString()
            }
        }
        return joinToString("|")
    }

    override val size: Int
        get() = rawValue.countOneBits()

/**
* Returns a new ${typename} with the specified bit set to true
* @param bit the bit to set
* @return a new ${typename} with the specified bit set to true
* @see or
*/
    operator fun plus(other: ${typename}): ${typename} {
        return ${typename}(rawValue or other.rawValue)
    }

    override fun iterator(): Iterator<${typename}> = object : Iterator<${typename}> {
        var remaining = rawValue.to${rawType}()
        override fun hasNext(): Boolean = remaining != 0.to${rawType}()
        override fun next(): ${typename} {
            val bit = remaining and (-remaining).to${rawType}()
            remaining = (remaining xor bit).to${rawType}()
            return ${typename}(bit)
        }
    }

    override fun isEmpty(): Boolean = rawValue == 0.to${rawType}()

    override fun containsAll(elements: Collection<${typename}>): Boolean {
        if (elements is ${typename}) {
            contains(elements)
        }
        return elements.all { rawValue and it.rawValue == it.rawValue }
    }

    override fun contains(element: ${typename}): Boolean {
        return rawValue and element.rawValue == element.rawValue
    }
}
