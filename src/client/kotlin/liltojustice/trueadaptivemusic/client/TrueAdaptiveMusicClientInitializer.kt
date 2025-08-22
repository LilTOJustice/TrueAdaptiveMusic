package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.client.trigger.event.MusicEventRegistry
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
import liltojustice.trueadaptivemusic.client.trigger.predicate.MusicPredicateRegistry
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
        MusicPredicateRegistry["biome"] = BiomePredicate::class
        MusicPredicateRegistry["boss"] = BossPredicate::class
        MusicPredicateRegistry["combat"] = CombatPredicate::class
        MusicPredicateRegistry["day"] = DayTimePredicate::class
        MusicPredicateRegistry["dimension"] = DimensionPredicate::class
        MusicPredicateRegistry["first_day"] = FirstDayPredicate::class
        MusicPredicateRegistry["game_mode"] = GameModePredicate::class
        MusicPredicateRegistry["health"] = HealthPredicate::class
        MusicPredicateRegistry["height"] = HeightPredicate::class
        MusicPredicateRegistry["moon_phase"] = MoonPhasePredicate::class
        MusicPredicateRegistry["night"] = NightTimePredicate::class
        MusicPredicateRegistry["pillager_raid"] = PillagerRaidPredicate::class
        MusicPredicateRegistry["riding"] = RidingPredicate::class
        MusicPredicateRegistry["root"] = RootPredicate::class
        MusicPredicateRegistry["status_effect"] = StatusEffectPredicate::class
        MusicPredicateRegistry["structure"] = StructurePredicate::class
        MusicPredicateRegistry["structure_set"] = StructureSetPredicate::class
        MusicPredicateRegistry["title_screen"] = TitleScreenPredicate::class
        MusicPredicateRegistry["weather"] = WeatherPredicate::class

        MusicEventRegistry["on_advancement_get"] = OnAdvancementGetEvent::class
        MusicEventRegistry["on_boss_defeat"] = OnBossDefeatEvent::class
        MusicEventRegistry["on_day_start"] = OnDayStartEvent::class
        MusicEventRegistry["on_death"] = OnDeathEvent::class
        MusicEventRegistry["on_enter_predicate"] = OnEnterPredicateEvent::class
        MusicEventRegistry["on_join_world"] = OnJoinWorldEvent::class
        MusicEventRegistry["on_night_start"] = OnNightStartEvent::class
        MusicEventRegistry["on_recipe_unlock"] = OnRecipeUnlockEvent::class
        MusicEventRegistry["on_tutorial_popup"] = OnTutorialPopupEvent::class
        MusicEventRegistry["on_wake_up"] = OnWakeUpEvent::class

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            TAMClient.tick(client)
        }
    }
}
