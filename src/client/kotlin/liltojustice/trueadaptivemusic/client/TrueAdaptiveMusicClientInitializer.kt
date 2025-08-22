package liltojustice.trueadaptivemusic.client

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
        TAMClient.registerPredicate("biome", BiomePredicate::class)
        TAMClient.registerPredicate("boss", BossPredicate::class)
        TAMClient.registerPredicate("combat", CombatPredicate::class)
        TAMClient.registerPredicate("day", DayTimePredicate::class)
        TAMClient.registerPredicate("dimension", DimensionPredicate::class)
        TAMClient.registerPredicate("first_day", FirstDayPredicate::class)
        TAMClient.registerPredicate("game_mode", GameModePredicate::class)
        TAMClient.registerPredicate("health", HealthPredicate::class)
        TAMClient.registerPredicate("height", HeightPredicate::class)
        TAMClient.registerPredicate("moon_phase", MoonPhasePredicate::class)
        TAMClient.registerPredicate("night", NightTimePredicate::class)
        TAMClient.registerPredicate("pillager_raid", PillagerRaidPredicate::class)
        TAMClient.registerPredicate("riding", RidingPredicate::class)
        TAMClient.registerPredicate("root", RootPredicate::class)
        TAMClient.registerPredicate("status_effect", StatusEffectPredicate::class)
        TAMClient.registerPredicate("structure", StructurePredicate::class)
        TAMClient.registerPredicate("structure_set", StructureSetPredicate::class)
        TAMClient.registerPredicate("title_screen", TitleScreenPredicate::class)
        TAMClient.registerPredicate("weather", WeatherPredicate::class)

        TAMClient.registerEvent("on_advancement_get", OnAdvancementGetEvent::class)
        TAMClient.registerEvent("on_boss_defeat", OnBossDefeatEvent::class)
        TAMClient.registerEvent("on_day_start", OnDayStartEvent::class)
        TAMClient.registerEvent("on_death", OnDeathEvent::class)
        TAMClient.registerEvent("on_enter_predicate", OnEnterPredicateEvent::class)
        TAMClient.registerEvent("on_join_world", OnJoinWorldEvent::class)
        TAMClient.registerEvent("on_night_start", OnNightStartEvent::class)
        TAMClient.registerEvent("on_recipe_unlock", OnRecipeUnlockEvent::class)
        TAMClient.registerEvent("on_tutorial_popup", OnTutorialPopupEvent::class)
        TAMClient.registerEvent("on_wake_up", OnWakeUpEvent::class)

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            TAMClient.tick(client)
        }
    }
}
