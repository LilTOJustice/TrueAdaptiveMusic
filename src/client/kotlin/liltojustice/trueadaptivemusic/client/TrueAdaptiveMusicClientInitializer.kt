package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.client.gui.widget.utility.CheckboxWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.DropdownWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.MultiSelectDropdownWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.SliderWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.TextInputWidget
import liltojustice.trueadaptivemusic.client.identifier.TypedIdentifier
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnAdvancementGetEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnBossDefeatEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnDayStartEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnDeathEvent
import liltojustice.trueadaptivemusic.client.trigger.event.types.OnEnterNodeEvent
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
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.InFluidPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.MoonPhasePredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.NightTimePredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.PausedPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.PillagerRaidPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.PlayerAttributePredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.RidingPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.RootPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.ScoreboardPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.StatusEffectPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.StructurePredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.StructureSetPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.TeamPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.TitleScreenPredicate
import liltojustice.trueadaptivemusic.client.trigger.predicate.types.WeatherPredicate
import liltojustice.trueadaptivemusic.text.StringExtensions.prettify
import liltojustice.trueadaptivemusicapi.TAMAPI
import liltojustice.trueadaptivemusicapi.widget.EmptyClickableWidget
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.network.chat.Component
import kotlin.collections.map
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf
import kotlin.toString

class TrueAdaptiveMusicClientInitializer: ClientModInitializer {
    override fun onInitializeClient() {
        TAMAPI.registerPredicateType(BiomePredicate)
        TAMAPI.registerPredicateType(BossPredicate)
        TAMAPI.registerPredicateType(CombatPredicate)
        TAMAPI.registerPredicateType(DayTimePredicate)
        TAMAPI.registerPredicateType(DimensionPredicate)
        TAMAPI.registerPredicateType(FirstDayPredicate)
        TAMAPI.registerPredicateType(GameModePredicate)
        TAMAPI.registerPredicateType(HealthPredicate)
        TAMAPI.registerPredicateType(HeightPredicate)
        TAMAPI.registerPredicateType(MoonPhasePredicate)
        TAMAPI.registerPredicateType(NightTimePredicate)
        TAMAPI.registerPredicateType(PillagerRaidPredicate)
        TAMAPI.registerPredicateType(RidingPredicate)
        TAMAPI.registerPredicateType(RootPredicate)
        TAMAPI.registerPredicateType(StatusEffectPredicate)
        TAMAPI.registerPredicateType(StructurePredicate)
        TAMAPI.registerPredicateType(StructureSetPredicate)
        TAMAPI.registerPredicateType(TitleScreenPredicate)
        TAMAPI.registerPredicateType(WeatherPredicate)
        TAMAPI.registerPredicateType(DeathScreenPredicate)
        TAMAPI.registerPredicateType(FishingPredicate)
        TAMAPI.registerPredicateType(FlyingPredicate)
        TAMAPI.registerPredicateType(PausedPredicate)
        TAMAPI.registerPredicateType(CreditsScreenPredicate)
        TAMAPI.registerPredicateType(InBedPredicate)
        TAMAPI.registerPredicateType(InFluidPredicate)
        TAMAPI.registerPredicateType(BossHealthPredicate)
        TAMAPI.registerPredicateType(HungerPredicate)
        TAMAPI.registerPredicateType(EntityNearbyPredicate)
        TAMAPI.registerPredicateType(ScoreboardPredicate)
        TAMAPI.registerPredicateType(TeamPredicate)
        TAMAPI.registerPredicateType(PlayerAttributePredicate)

        TAMAPI.registerEventType(OnAdvancementGetEvent)
        TAMAPI.registerEventType(OnBossDefeatEvent)
        TAMAPI.registerEventType(OnDayStartEvent)
        TAMAPI.registerEventType(OnDeathEvent)
        TAMAPI.registerEventType(OnEnterNodeEvent)
        TAMAPI.registerEventType(OnJoinWorldEvent)
        TAMAPI.registerEventType(OnNightStartEvent)
        TAMAPI.registerEventType(OnRecipeUnlockEvent)
        TAMAPI.registerEventType(OnTutorialPopupEvent)
        TAMAPI.registerEventType(OnWakeUpEvent)
        TAMAPI.registerEventType(OnPauseEvent)

        TAMAPI.registerInputWidget(
            typeOf<String>()
        ) { prompt, _, outArgs, arg, tooltipText, onChange ->
            val result = TextInputWidget(
                prompt,
                { _, text ->
                    outArgs[arg.index] = text
                    onChange()
                    ""
                },
                outArgs[arg.index]?.toString() ?: ""
            )
            tooltipText?.let { result.setTooltip(Tooltip.create(it)) }

            result
        }

        TAMAPI.registerInputWidget(
            typeOf<Int>()
        ) { prompt, _, outArgs, arg, tooltipText, onChange ->
            val result = TextInputWidget(
                prompt,
                { _, text ->
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
            tooltipText?.let { result.setTooltip(Tooltip.create(it)) }

            result
        }

        TAMAPI.registerInputWidget(
            typeOf<UInt>()
        ) { prompt, _, outArgs, arg, tooltipText, onChange ->
            val result = TextInputWidget(
                prompt,
                { _, text ->
                    if (text.isBlank()) {
                        return@TextInputWidget "0"
                    }

                    val value = text.toUIntOrNull() ?: return@TextInputWidget outArgs[arg.index]?.toString() ?: "0"

                    if (text != value.toString()) {
                        return@TextInputWidget value.toString()
                    }

                    outArgs[arg.index] = value
                    onChange()
                    ""
                },
                outArgs[arg.index]?.toString() ?: ""
            )
            tooltipText?.let { result.setTooltip(Tooltip.create(it)) }

            result
        }

        TAMAPI.registerInputWidget(
            typeOf<Double>()
        ) { prompt, _, outArgs, arg, tooltipText, onChange ->
            val result = TextInputWidget(
                prompt,
                { _, text ->
                    if (text.isBlank() || text == "-") {
                        return@TextInputWidget "0"
                    }

                    if (text.endsWith('-')) {
                        return@TextInputWidget if (text.startsWith('-')) {
                            text.dropWhile { it == '-' }.dropLastWhile { it == '-' }
                        }
                        else {
                            "-${text.dropLast(1).dropWhile { it == '-' }}"
                        }
                    }

                    if (text.endsWith(".")) {
                        if (text.count { it == '.' } > 1) {
                            return@TextInputWidget text.dropLast(1)
                        }

                        val newText = "${text.dropLastWhile { it == '.' }}."
                        newText.dropLast(1).toDoubleOrNull()?.let {
                            outArgs[arg.index] = it
                            onChange()
                        }

                        return@TextInputWidget newText
                    }

                    if (!text.contains('.') && text.startsWith('0') && text.length > 1) {
                        return@TextInputWidget text.dropWhile { it == '0' }
                    }

                    if (!text.contains('.')) {
                        text.toDoubleOrNull()?.let {
                            outArgs[arg.index] = it
                            onChange()
                        }

                        return@TextInputWidget text
                    }

                    val value = text.toDoubleOrNull()
                    if (text != "-0" && value == null) {
                        return@TextInputWidget outArgs[arg.index]?.toString() ?: "0"
                    }

                    outArgs[arg.index] = value
                    onChange()
                    ""
                },
                outArgs[arg.index]?.toString() ?: ""
            )
            tooltipText?.let {
                result.setTooltip(Tooltip.create(it))
            }
            result
        }

        TAMAPI.registerInputWidget(
            typeOf<Boolean>()
        ) { prompt, _, outArgs, arg, tooltipText, onChange ->
            val result = CheckboxWidget(
                prompt,
                { checked ->
                    outArgs[arg.index] = checked
                    onChange()
                },
                checked = outArgs[arg.index] as? Boolean ?: false
            )
            tooltipText?.let { result.setTooltip(Tooltip.create(it)) }
            result
        }

        TAMAPI.registerInputWidget(
            { type -> type.isSubtypeOf(typeOf<Enum<*>>()) },
            { prompt, _, outArgs, arg, tooltipText, onChange ->
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
                            Component.translatableWithFallback(
                                "trueadaptivemusic.enum.$it", prettifyEnum(it)).string },
                        title = prompt,
                        startingOption = (outArgs[arg.index] as? Enum<*>),
                        tooltipText = tooltipText
                    )
            }
        )

        TAMAPI.registerInputWidget(
            { type -> isEnumList(type) },
            { prompt, _, outArgs, arg, tooltipText, onChange ->
                val type = arg.type.arguments.firstOrNull()?.type
                    ?: throw Exception("Somehow Enum didn't have any type args. The world is chaos.")
                val enumClass = (type.classifier as KClass<*>).java
                val options = enumClass.enumConstants.map { enum -> enum as Enum<*> }
                MultiSelectDropdownWidget(
                    options,
                    0,
                    { prettifyEnum(it) },
                    { selected ->
                        outArgs[arg.index] = selected
                        onChange()
                    },
                    prompt,
                    notSelectedPlaceholder = Component.translatableWithFallback(
                        "trueadaptivemusic.enum_placeholder", "Select values").string,
                    alreadySelected = (outArgs[arg.index] as? List<*>)?.filterIsInstance<Enum<*>>()
                        ?: listOf(),
                    tooltipText = tooltipText
                )
            }
        )

        TAMAPI.registerInputWidget(
            { type -> type.isSubtypeOf(typeOf<TypedIdentifier>()) },
            { prompt, _, outArgs, arg, tooltipText, onChange ->
                val prettify = TAMClient.options.prettifyIdentifiers
                val options = TypedIdentifier
                    .getRegistryIdsFromType(arg.type)
                    .map { id -> TypedIdentifier.initializeFromIdString(arg.type, id.toString()) }
                val actualTooltipText = tooltipText.takeIf { !options.isEmpty() } ?: DYNAMIC_REGISTRY_TEXT
                DropdownWidget(
                    options,
                    { id ->
                        outArgs[arg.index] = id
                        onChange()
                    },
                    getDisplay = { if (prettify) it.prettify() else it.toString() },
                    title = prompt,
                    startingOption = outArgs[arg.index] as? TypedIdentifier,
                    tooltipText = actualTooltipText
                )
            }
        )

        TAMAPI.registerInputWidget(
            { type -> isTypedIdentifierList(type) },
            { prompt, _, outArgs, arg, tooltipText, onChange ->
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
                    notSelectedPlaceholder = Component.translatableWithFallback(
                        "trueadaptivemusic.identifier_placeholder", "Select identifiers").string,
                    alreadySelected =
                        (outArgs[arg.index] as? List<*>)?.filterIsInstance<TypedIdentifier>()
                            ?: listOf(),
                    tooltipText = actualTooltipText
                )
            }
        )

        TAMAPI.registerInputWidget(
            typeOf<TrueAdaptiveMusicOptions.LUFBoost>()
        ) { prompt, _, outArgs, arg, tooltipText, _ ->
            val result = SliderWidget(
                0,
                TrueAdaptiveMusicOptions.LUFBoost.MAX_VALUE.toInt(),
                (outArgs[arg.index] as? TrueAdaptiveMusicOptions.LUFBoost)?.value?.toInt() ?: 0,
                prompt
            ) { outArgs[arg.index] = TrueAdaptiveMusicOptions.LUFBoost(it.toUInt()) }
            tooltipText?.let { result.setTooltip(Tooltip.create(it)) }

            result
        }
    }

    companion object {
        private val DYNAMIC_REGISTRY_TEXT =
            Component.translatableWithFallback(
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

        private fun prettifyEnum(enum: Enum<*>): String {
            return when(val enumString = enum.toString()) {
                "Equal" -> "="
                "NotEqual" -> "!="
                "Greater" -> ">"
                "GreaterOrEqual" -> ">="
                "Lesser" -> "<"
                "LesserOrEqual" -> "<="
                else -> enumString.prettify()
            }
        }
    }
}
