package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.client.gui.widget.utility.CheckboxWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.DropdownWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.EmptyClickableWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.MultiSelectDropdownWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.SliderWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.TextInputWidget
import liltojustice.trueadaptivemusic.client.identifier.TypedIdentifier
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnAdvancementGetEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnBossDefeatEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnDayStartEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnDeathEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnEnterPredicateEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnJoinWorldEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnNightStartEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnPauseEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnRecipeUnlockEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnTutorialPopupEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnWakeUpEvent
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.BiomePredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.BossHealthPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.BossPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.CombatPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.CreditsScreenPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.DayTimePredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.DeathScreenPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.DimensionPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.EntityNearbyPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.FirstDayPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.FishingPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.FlyingPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.GameModePredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.HealthPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.HeightPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.HungerPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.InBedPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.InLavaPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.InWaterPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.MoonPhasePredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.NightTimePredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.PausedPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.PillagerRaidPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.RidingPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.RootPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.StatusEffectPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.StructurePredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.StructureSetPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.TitleScreenPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.WeatherPredicate
import liltojustice.trueadaptivemusic.text.StringExtensions.prettify
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.text.Text
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf
import kotlin.toString

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
        TAMClient.registerPredicate("death_screen", DeathScreenPredicate::class)
        TAMClient.registerPredicate("fishing", FishingPredicate::class)
        TAMClient.registerPredicate("flying", FlyingPredicate::class)
        TAMClient.registerPredicate("paused", PausedPredicate::class)
        TAMClient.registerPredicate("credits_screen", CreditsScreenPredicate::class)
        TAMClient.registerPredicate("in_bed", InBedPredicate::class)
        TAMClient.registerPredicate("in_water", InWaterPredicate::class)
        TAMClient.registerPredicate("in_lava", InLavaPredicate::class)
        TAMClient.registerPredicate("boss_health", BossHealthPredicate::class)
        TAMClient.registerPredicate("hunger", HungerPredicate::class)
        TAMClient.registerPredicate("entity_nearby", EntityNearbyPredicate::class)

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
        TAMClient.registerEvent("on_pause", OnPauseEvent::class)

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            TAMClient.tick(client)
        }

        TAMClient.registerInputWidget(
            typeOf<String>()
        ) { prompt, screen, outArgs, arg, tooltipText, onChange ->
            val result = TextInputWidget(
                prompt,
                { widget, text ->
                    outArgs[arg.index] = text
                    onChange()
                    ""
                },
                outArgs[arg.index]?.toString() ?: ""
            )
            tooltipText?.let {
                result.setTooltip(Tooltip.of(it))
            }

            result
        }

        TAMClient.registerInputWidget(
            typeOf<Int>()
        ) { prompt, screen, outArgs, arg, tooltipText, onChange ->
            val result = TextInputWidget(
                prompt,
                { widget, text ->
                    if (text.isBlank() || text == "-") {
                        return@TextInputWidget "0"
                    }

                    if (text == "0-") {
                        return@TextInputWidget "-0"
                    }

                    val value = text.toIntOrNull()
                    if (text != "-0" && value == null) {
                        return@TextInputWidget outArgs[arg.index]?.toString() ?: "0"
                    }

                    if (text != "-0" && text != value.toString()) {
                        return@TextInputWidget value.toString()
                    }

                    outArgs[arg.index] = value
                    onChange()
                    ""
                },
                outArgs[arg.index]?.toString() ?: ""
            )
            tooltipText?.let {
                result.setTooltip(Tooltip.of(it))
            }
            result
        }

        TAMClient.registerInputWidget(
            typeOf<UInt>()
        ) { prompt, screen, outArgs, arg, tooltipText, onChange ->
            val result = TextInputWidget(
                prompt,
                { widget, text ->
                    if (text.isBlank()) {
                        return@TextInputWidget "0"
                    }

                    val value = text.toUIntOrNull()
                    if (value == null) {
                        return@TextInputWidget outArgs[arg.index]?.toString() ?: "0"
                    }

                    if (text != value.toString()) {
                        return@TextInputWidget value.toString()
                    }

                    outArgs[arg.index] = value
                    onChange()
                    ""
                },
                outArgs[arg.index]?.toString() ?: ""
            )
            tooltipText?.let {
                result.setTooltip(Tooltip.of(it))
            }
            result
        }

        TAMClient.registerInputWidget(
            typeOf<Boolean>()
        ) { prompt, screen, outArgs, arg, tooltipText, onChange ->
            val result = CheckboxWidget(
                prompt,
                { checked ->
                    outArgs[arg.index] = checked
                    onChange()
                },
                checked = outArgs[arg.index] as? Boolean ?: false
            )
            tooltipText?.let {
                result.setTooltip(Tooltip.of(it))
            }
            result
        }

        TAMClient.registerInputWidget(
            { type -> type.isSubtypeOf(typeOf<Enum<*>>())},
            { prompt, screen, outArgs, arg, tooltipText, onChange ->
                val enumClass = (arg.type.classifier as KClass<*>).java
                val options = enumClass.enumConstants.map { enum -> enum as Enum<*> }

                if (options.isEmpty())
                    EmptyClickableWidget()
                else
                    DropdownWidget(
                        options,
                        { enum ->
                            outArgs[arg.index] = enum
                            onChange()
                        },
                        getDisplay = {
                            Text.translatableWithFallback(
                                "trueadaptivemusic.enum.$it", it.toString().prettify()).string },
                        title = prompt,
                        startingOption = (outArgs[arg.index] as? Enum<*>),
                        tooltipText = tooltipText
                    )
            }
        )

        TAMClient.registerInputWidget(
            { type -> isEnumList(type) },
            { prompt, screen, outArgs, arg, tooltipText, onChange ->
                val type = arg.type.arguments.firstOrNull()?.type
                    ?: throw Exception("Somehow Enum didn't have any type args. The world is chaos.")
                val enumClass = (type.classifier as KClass<*>).java
                val options = enumClass.enumConstants.map { enum -> enum as Enum<*> }
                MultiSelectDropdownWidget(
                    options,
                    0,
                    { it.toString().prettify() },
                    { selected ->
                        outArgs[arg.index] = selected
                        onChange()
                    },
                    prompt,
                    notSelectedPlaceholder = Text.translatableWithFallback(
                        "trueadaptivemusic.enum_placeholder", "Select values").string,
                    alreadySelected = (outArgs[arg.index] as? List<*>)?.mapNotNull { enum -> enum as? Enum<*> }
                        ?: listOf(),
                    tooltipText = tooltipText
                )
            }
        )

        TAMClient.registerInputWidget(
            { type -> type.isSubtypeOf(typeOf<TypedIdentifier>()) },
            { prompt, screen, outArgs, arg, tooltipText, onChange ->
                val prettify = TAMClient.options.prettifyIdentifiers
                val options = TypedIdentifier
                    .getRegistryIdsFromType(arg.type)
                    .map { id ->
                        val key = TypedIdentifier.initializeFromIdString(arg.type, id.toString())
                        key to (if (prettify) key.prettify() else id.toString())
                    }
                    .sortedBy { pair -> pair.second }
                val actualTooltipText = tooltipText.takeIf { !options.isEmpty() } ?: DYNAMIC_REGISTRY_TEXT
                DropdownWidget(
                    options,
                    { id ->
                        outArgs[arg.index] = id
                        onChange()
                    },
                    title = prompt,
                    startingOption = outArgs[arg.index] as? TypedIdentifier,
                    tooltipText = actualTooltipText
                )
            }
        )

        TAMClient.registerInputWidget(
            { type -> isTypedIdentifierList(type) },
            { prompt, screen, outArgs, arg, tooltipText, onChange ->
                val type = arg.type.arguments.firstOrNull()?.type
                    ?: throw Exception("Somehow List didn't have any type args. The world is chaos.")
                val prettify = TAMClient.options.prettifyIdentifiers
                val options = TypedIdentifier
                    .getRegistryIdsFromType(type)
                    .map { id -> TypedIdentifier.initializeFromIdString(type, id.toString()) }
                val actualTooltipText = tooltipText.takeIf { !options.isEmpty() } ?: DYNAMIC_REGISTRY_TEXT
                MultiSelectDropdownWidget(
                    options,
                    0,
                    { if (prettify) it.prettify() else it.toString() },
                    { selected ->
                        outArgs[arg.index] = selected
                        onChange()
                    },
                    prompt,
                    notSelectedPlaceholder = Text.translatableWithFallback(
                        "trueadaptivemusic.identifier_placeholder", "Select identifiers").string,
                    alreadySelected =
                        (outArgs[arg.index] as? List<*>)?.mapNotNull { it as? TypedIdentifier }
                            ?: listOf(),
                    tooltipText = actualTooltipText
                )
            }
        )

        TAMClient.registerInputWidget(
            typeOf<TrueAdaptiveMusicOptions.LUFBoost>()
        ) { prompt, screen, outArgs, arg, tooltipText, onChange ->
            val result = SliderWidget(
                0,
                TrueAdaptiveMusicOptions.LUFBoost.MAX_VALUE.toInt(),
                (outArgs[arg.index] as? TrueAdaptiveMusicOptions.LUFBoost)?.value?.toInt() ?: 0,
                prompt
            ) { outArgs[arg.index] = TrueAdaptiveMusicOptions.LUFBoost(it.toUInt()) }
            tooltipText?.let {
                result.setTooltip(Tooltip.of(it))
            }
            result
        }
    }

    companion object {
        private val DYNAMIC_REGISTRY_TEXT =
            Text.translatableWithFallback(
                "trueadaptivemusic.dynamic_registry_warning",
                "No options available to add due to a dynamic registry requirement. Try joining a world first."
            )

        private fun isEnumList(type: KType): Boolean {
            return type.isSubtypeOf(typeOf<List<*>>())
                    && type.arguments.any { typeArg -> typeArg.type?.isSubtypeOf(typeOf<Enum<*>>()) == true }
        }

        private fun isTypedIdentifierList(type: KType): Boolean {
            return type.isSubtypeOf(typeOf<List<*>>())
                    && type.arguments.any { typeArg -> typeArg.type?.isSubtypeOf(typeOf<TypedIdentifier>()) == true }
        }
    }
}
