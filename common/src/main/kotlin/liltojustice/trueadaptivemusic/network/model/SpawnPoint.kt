package liltojustice.trueadaptivemusic.network.model

import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level

data class SpawnPoint(val blockPos: BlockPos, val dimension: ResourceKey<Level>)
