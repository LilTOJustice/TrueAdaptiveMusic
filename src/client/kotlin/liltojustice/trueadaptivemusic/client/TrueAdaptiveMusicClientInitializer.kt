package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.client.trigger.event.MusicEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnAdvancementGetEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnBossDefeatEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnDayStartEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnDeathEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnEnterPredicateEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnJoinWorldEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnNightStartEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnRecipeUnlockEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnTutorialPopupEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnWakeUpEvent
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.BiomePredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.BossPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.CombatPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.DayTimePredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.DimensionPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.FirstDayPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.GameModePredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.HealthPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.HeightPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.MoonPhasePredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.NightTimePredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.PillagerRaidPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.RidingPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.RootPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.StatusEffectPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.StructurePredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.StructureSetPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.TitleScreenPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.WeatherPredicate
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

class TrueAdaptiveMusicClientInitializer: ClientModInitializer {
    override fun onInitializeClient() {
        MusicPredicate.register("biome", BiomePredicate::class)
        MusicPredicate.register("boss", BossPredicate::class)
        MusicPredicate.register("combat", CombatPredicate::class)
        MusicPredicate.register("day", DayTimePredicate::class)
        MusicPredicate.register("dimension", DimensionPredicate::class)
        MusicPredicate.register("first_day", FirstDayPredicate::class)
        MusicPredicate.register("game_mode", GameModePredicate::class)
        MusicPredicate.register("health", HealthPredicate::class)
        MusicPredicate.register("height", HeightPredicate::class)
        MusicPredicate.register("moon_phase", MoonPhasePredicate::class)
        MusicPredicate.register("night", NightTimePredicate::class)
        MusicPredicate.register("pillager_raid", PillagerRaidPredicate::class)
        MusicPredicate.register("riding", RidingPredicate::class)
        MusicPredicate.register("root", RootPredicate::class)
        MusicPredicate.register("status_effect", StatusEffectPredicate::class)
        MusicPredicate.register("structure", StructurePredicate::class)
        MusicPredicate.register("structure_set", StructureSetPredicate::class)
        MusicPredicate.register("title_screen", TitleScreenPredicate::class)
        MusicPredicate.register("weather", WeatherPredicate::class)

        MusicEvent.register("on_advancement_get", OnAdvancementGetEvent::class)
        MusicEvent.register("on_boss_defeat", OnBossDefeatEvent::class)
        MusicEvent.register("on_day_start", OnDayStartEvent::class)
        MusicEvent.register("on_death", OnDeathEvent::class)
        MusicEvent.register("on_enter_predicate", OnEnterPredicateEvent::class)
        MusicEvent.register("on_join_world", OnJoinWorldEvent::class)
        MusicEvent.register("on_night_start", OnNightStartEvent::class)
        MusicEvent.register("on_recipe_unlock", OnRecipeUnlockEvent::class)
        MusicEvent.register("on_tutorial_popup", OnTutorialPopupEvent::class)
        MusicEvent.register("on_wake_up", OnWakeUpEvent::class)

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            TAMClient.tick(client)
        }
    }
}
