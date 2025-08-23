package liltojustice.trueadaptivemusic.client

import liltojustice.trueadaptivemusic.client.gui.widget.utility.CheckboxWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.DropdownWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.EmptyClickableWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.MultiSelectDropdownWidget
import liltojustice.trueadaptivemusic.client.gui.widget.utility.TextInputWidget
import liltojustice.trueadaptivemusic.client.identifier.TypedIdentifier
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
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            TAMClient.tick(client)
        }

        TAMClient.registerInputWidget(
            typeOf<Int>(),
            { prompt, screen, outArgs, arg ->
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
                    },
                    outArgs[arg.index]?.toString() ?: ""
                )
            }
        )

        TAMClient.registerInputWidget(
            typeOf<UInt>(),
            { prompt, screen, outArgs, arg ->
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
                    },
                    outArgs[arg.index]?.toString() ?: ""
                )
            }
        )

        TAMClient.registerInputWidget(
            typeOf<Boolean>(),
            { prompt, screen, outArgs, arg ->
                CheckboxWidget(
                    10,
                    prompt,
                    { checked -> outArgs[arg.index] = checked },
                    checked = outArgs[arg.index] as? Boolean ?: false
                )
            }
        )

        TAMClient.registerInputWidget(
            { type -> type.isSubtypeOf(typeOf<Enum<*>>())},
            { prompt, screen, outArgs, arg ->
                val enumClass = (arg.type.classifier as KClass<*>).java
                val options = enumClass.enumConstants.map { enum -> enum.toString() }

                if (options.isEmpty())
                    EmptyClickableWidget()
                else
                    DropdownWidget(
                        options,
                        { enumOption ->
                            outArgs[arg.index] = enumClass.enumConstants.first { enum -> enum.toString() == enumOption }
                        },
                        0,
                        prompt,
                        startingOption = (outArgs[arg.index] as? Enum<*>)?.name ?: ""
                    )
            }
        )

        TAMClient.registerInputWidget(
            { type -> isEnumList(type) },
            { prompt, screen, outArgs, arg ->
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
                    },
                    "${prompt}s",
                    notSelectedPlaceholder = "Select a value",
                    alreadySelected = (outArgs[arg.index] as? List<*>)?.map { enum -> enum.toString() } ?: listOf())
            }
        )

        TAMClient.registerInputWidget(
            { type -> type.isSubtypeOf(typeOf<TypedIdentifier>()) },
            { prompt, screen, outArgs, arg ->
                val options = TypedIdentifier.getRegistryIdsFromType(arg.type).map { id -> id.toString() }.sorted()
                val result = DropdownWidget(
                    options,
                    { id -> outArgs[arg.index] = TypedIdentifier.initializeFromIdString(arg.type, id) },
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
            { prompt, screen, outArgs, arg ->
                val type = arg.type.arguments.firstOrNull()?.type
                    ?: throw Exception("Somehow List didn't have any type args. The world is chaos.")
                val options = TypedIdentifier.getRegistryIdsFromType(type).map { id -> id.toString() }.sorted()
                val result = MultiSelectDropdownWidget(
                    options,
                    0,
                    { selected ->
                        outArgs[arg.index] = selected
                            .map { id -> TypedIdentifier.initializeFromIdString(type, id) }
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
