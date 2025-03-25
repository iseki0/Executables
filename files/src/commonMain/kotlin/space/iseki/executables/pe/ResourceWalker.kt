package space.iseki.executables.pe

import space.iseki.executables.common.Address32

interface ResourceWalkEntry {
    val nodePath: List<ResourceNode>
    val node: ResourceNode
}

fun ResourceNode.walk() = sequence<ResourceWalkEntry> {
    walkInternal(this@walk, mutableSetOf(), listOf(this@walk))
}

private class ResourceWalkEntryImpl(
    override val node: ResourceNode,
    override val nodePath: List<ResourceNode>,
) : ResourceWalkEntry

private suspend fun SequenceScope<ResourceWalkEntryImpl>.walkInternal(
    current: ResourceNode,
    visited: MutableSet<Address32>,
    stack: List<ResourceNode>,
) {
    if (current.dataRva in visited) return
    visited.add(current.dataRva)
    yield(ResourceWalkEntryImpl(current, stack))
    for (child in current.listChildren()) {
        walkInternal(child, visited, stack + child)
    }
    visited.remove(current.dataRva)
}

