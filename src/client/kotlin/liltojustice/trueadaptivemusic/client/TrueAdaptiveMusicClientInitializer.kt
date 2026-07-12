fun AbstractChestBoat(
    type: net.minecraft.world.entity.EntityType<out net.minecraft.world.entity.vehicle.boat.AbstractChestBoat?>,
    level: net.minecraft.world.level.Level,
    dropItem: java.util.function.Supplier<net.minecraft.world.item.Item?>
) {
    super(type, level, dropItem)
}