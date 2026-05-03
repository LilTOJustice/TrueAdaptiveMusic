package liltojustice.trueadaptivemusic.network.model

import net.minecraft.registry.RegistryKey
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

data class SpawnPoint(val blockPos: BlockPos, val dimension: RegistryKey<World>)
