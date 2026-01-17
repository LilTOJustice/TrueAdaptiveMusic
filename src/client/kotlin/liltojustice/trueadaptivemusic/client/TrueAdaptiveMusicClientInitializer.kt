package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.gui.widget.utility.CheckboxWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.DropdownWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.EmptyClickableWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.MultiSelectDropdownWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.TextInputWidget
import liltojustice.trueadaptivemusic.client.identifier.TypedIdentifier
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
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.DeathScreenPredicate
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
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.text.Text
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf
import kotlin.toString

class TrueAdaptiveMusicClientInitializer: ClientModInitializer {
    override fun onInitializeClient() {
        try {
            val ffmpegInstall =
                ProcessBuilder("powershell.exe", "-Command", "winget install 'FFmpeg (Essentials Build)'")
                    .redirectErrorStream(true)
                    .start()

            val output = ffmpegInstall.inputStream.bufferedReader().use { it.readText() }
            ffmpegInstall.waitFor()

            if (ffmpegInstall.exitValue() != 0) {
                Logger.logWarning("Failed to install ffmpeg with exit code ${ffmpegInstall.exitValue()}:\n${output}")
            }
        }
        catch (e: Exception) {
            Logger.logError("Failed to auto-install ffmpeg: ${e.message}")
        }

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

        TAMClient.registerInputWidget(
            typeOf<String>()
        ) { prompt, screen, outArgs, arg, onChange ->
            TextInputWidget(
                screen,
                prompt,
                30,
                { widget, text ->
                    outArgs[arg.index] = text
                    onChange()
                },
                outArgs[arg.index]?.toString() ?: ""
            )
        }

        TAMClient.registerInputWidget(
            typeOf<Int>()
        ) { prompt, screen, outArgs, arg, onChange ->
            TextInputWidget(
                screen,
                prompt,
                30,
                { widget, text ->
                    if (text == "0-") {
                        widget.text = "-0"
                        return@TextInputWidget
                    }

                    val value = text.toIntOrNull()
                    if (text != "-0" && value == null) {
                        widget.text = "0"
                        return@TextInputWidget
                    }

                    if (text != "-0" && text != value.toString()) {
                        widget.text = value.toString()
                        return@TextInputWidget
                    }

                    outArgs[arg.index] = value
                    onChange()
                },
                outArgs[arg.index]?.toString() ?: ""
            )
        }

        TAMClient.registerInputWidget(
            typeOf<UInt>()
        ) { prompt, screen, outArgs, arg, onChange ->
            TextInputWidget(
                screen,
                prompt,
                30,
                { widget, text ->
                    val value = text.toUIntOrNull()
                    if (value == null) {
                        widget.text = "0"
                        return@TextInputWidget
                    }

                    if (text != value.toString()) {
                        widget.text = value.toString()
                        return@TextInputWidget
                    }

                    outArgs[arg.index] = value
                    onChange()
                },
                outArgs[arg.index]?.toString() ?: ""
            )
        }

        TAMClient.registerInputWidget(
            typeOf<Boolean>()
        ) { prompt, screen, outArgs, arg, onChange ->
            CheckboxWidget(
                10,
                prompt,
                { checked ->
                    outArgs[arg.index] = checked
                    onChange()
                },
                checked = outArgs[arg.index] as? Boolean ?: false
            )
        }

        TAMClient.registerInputWidget(
            { type -> type.isSubtypeOf(typeOf<Enum<*>>())},
            { prompt, screen, outArgs, arg, onChange ->
                val enumClass = (arg.type.classifier as KClass<*>).java
                val options = enumClass.enumConstants.map { enum -> enum.toString() }

                if (options.isEmpty())
                    EmptyClickableWidget()
                else
                    DropdownWidget(
                        options,
                        { enumOption ->
                            outArgs[arg.index] = enumClass.enumConstants.first { enum -> enum.toString() == enumOption }
                            onChange()
                        },
                        0,
                        prompt,
                        startingOption = (outArgs[arg.index] as? Enum<*>)?.name ?: ""
                    )
            }
        )

        TAMClient.registerInputWidget(
            { type -> isEnumList(type) },
            { prompt, screen, outArgs, arg, onChange ->
                val type = arg.type.arguments.firstOrNull()?.type
                    ?: throw Exception("Somehow Enum didn't have any type args. The world is chaos.")
                val enumClass = (type.classifier as KClass<*>).java
                val options = enumClass.enumConstants.map { enum -> enum.toString() }
                MultiSelectDropdownWidget(
                    options,
                    0,
                    { selected ->
                        outArgs[arg.index] = selected
                            .map { enumOption ->
                                enumClass.enumConstants.first { enum -> enum.toString() == enumOption }
                            }
                        onChange()
                    },
                    "${prompt}s",
                    notSelectedPlaceholder = "Select a value",
                    alreadySelected = (outArgs[arg.index] as? List<*>)?.map { enum -> enum.toString() } ?: listOf())
            }
        )

        TAMClient.registerInputWidget(
            { type -> type.isSubtypeOf(typeOf<TypedIdentifier>()) },
            { prompt, screen, outArgs, arg, onChange ->
                val options = TypedIdentifier.getRegistryIdsFromType(arg.type).map { id -> id.toString() }.sorted()
                val result = DropdownWidget(
                    options,
                    { id ->
                        outArgs[arg.index] = TypedIdentifier.initializeFromIdString(arg.type, id)
                        onChange()
                    },
                    0,
                    prompt,
                    startingOption = (outArgs[arg.index] as? TypedIdentifier)?.toString() ?: ""
                )

                if (options.isEmpty()) {
                    result.tooltip = Tooltip.of(DYNAMIC_REGISTRY_TEXT)
                }

                result
            }
        )

        TAMClient.registerInputWidget(
            { type -> isTypedIdentifierList(type) },
            { prompt, screen, outArgs, arg, onChange ->
                val type = arg.type.arguments.firstOrNull()?.type
                    ?: throw Exception("Somehow List didn't have any type args. The world is chaos.")
                val options = TypedIdentifier.getRegistryIdsFromType(type).map { id -> id.toString() }.sorted()
                val result = MultiSelectDropdownWidget(
                    options,
                    0,
                    { selected ->
                        outArgs[arg.index] = selected
                            .map { id -> TypedIdentifier.initializeFromIdString(type, id) }
                        onChange()
                    },
                    "${prompt}s",
                    notSelectedPlaceholder = "Select an Identifier",
                    alreadySelected = (outArgs[arg.index] as? List<*>)?.map { id -> id.toString() } ?: listOf())

                if (options.isEmpty()) {
                    result.tooltip = Tooltip.of(DYNAMIC_REGISTRY_TEXT)
                }

                result
            }
        )
    }

    companion object {
        private val DYNAMIC_REGISTRY_TEXT =
            Text.literal(
                "No options available to add due to a dynamic registry requirement. Try joining a world first.")

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
