package liltojustice.trueadaptivemusic.client.gui.widget

import liltojustice.trueadaptivemusic.LogLevel
import liltojustice.trueadaptivemusic.Logger
import liltojustice.trueadaptivemusic.client.identifier.TypedIdentifier
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ClickableWidget
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

class InputWidgetMaker {
    companion object {
        fun makeWidget(screen: Screen, outArgs: MutableList<Any?>, arg: KParameter): ClickableWidget {
            val prompt = (arg.name ?: "Unknown") +
                    ": ${arg.type.toString().split('.').last().replace(">", "")}"
            return if (arg.type == typeOf<Int>()) {
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
            else if (arg.type == typeOf<UInt>()) {
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
            else if (arg.type == typeOf<Boolean>()) {
                CheckboxWidget(
                    10,
                    prompt,
                    { checked -> outArgs[arg.index] = checked },
                    checked = outArgs[arg.index] as? Boolean ?: false)
            }
            else if (arg.type.isSubtypeOf(typeOf<Enum<*>>())) {
                val enumClass = (arg.type.classifier as KClass<*>).java
                val options = enumClass.enumConstants.map { enum -> enum.toString() }

                if (options.isEmpty())
                    EmptyClickableWidget()
                else
                    DropdownWidget(
                        options,
                        { enumOption -> outArgs[arg.index] = enumClass.enumConstants.first { enum -> enum.toString() == enumOption} },
                        prompt,
                        startingOption = (outArgs[arg.index] as? Enum<*>)?.name ?: "")
            }
            else if (isEnumList(arg.type)) {
                val type = arg.type.arguments.firstOrNull()?.type
                    ?: throw Exception("Somehow Enum didn't have any type args. The world is chaos.")
                val enumClass = (type.classifier as KClass<*>).java
                val options = enumClass.enumConstants.map { enum -> enum.toString() }
                MultiSelectDropdownWidget(
                    options,
                    { selected -> outArgs[arg.index] = selected
                        .map { enumOption ->
                            enumClass.enumConstants.first { enum -> enum.toString() == enumOption } } },
                    "${prompt}s",
                    notSelectedPlaceholder = "Select a value",
                    alreadySelected = (outArgs[arg.index] as? List<*>)?.map { enum -> enum.toString() } ?: listOf())
            }
            else if (arg.type.isSubtypeOf(typeOf<TypedIdentifier>())) {
                val options = TypedIdentifier.getRegistryIdsFromType(arg.type).map { id -> id.toString() }.sorted()

                if (options.isEmpty())
                    EmptyClickableWidget()
                else
                    DropdownWidget(
                        options,
                        { id -> outArgs[arg.index] = TypedIdentifier.initializeFromIdString(arg.type, id) },
                        prompt,
                        startingOption = (outArgs[arg.index] as? TypedIdentifier)?.toString() ?: "")
            }
            else if (isTypedIdentifierList(arg.type)) {
                val type = arg.type.arguments.firstOrNull()?.type
                    ?: throw Exception("Somehow List didn't have any type args. The world is chaos.")
                val options = TypedIdentifier.getRegistryIdsFromType(type).map { id -> id.toString() }.sorted()

                if (options.isEmpty())
                    EmptyClickableWidget()
                else
                    MultiSelectDropdownWidget(
                        options,
                        { selected ->
                            outArgs[arg.index] = selected
                                .map { id -> TypedIdentifier.initializeFromIdString(type, id) } },
                        "${prompt}s",
                        notSelectedPlaceholder = "Select an Identifier",
                        alreadySelected = (outArgs[arg.index] as? List<*>)?.map { id -> id.toString() } ?: listOf())
            }
            else {
                Logger.log("Couldn't create widget for expected type ${arg.type}.", LogLevel.WARNING)
                EmptyClickableWidget()
            }
        }

        fun isEnumList(type: KType): Boolean {
            return type.isSubtypeOf(typeOf<List<*>>())
                    && type.arguments.any { typeArg -> typeArg.type?.isSubtypeOf(typeOf<Enum<*>>()) == true }
        }

        fun isTypedIdentifierList(type: KType): Boolean {
            return type.isSubtypeOf(typeOf<List<*>>())
                    && type.arguments.any { typeArg -> typeArg.type?.isSubtypeOf(typeOf<TypedIdentifier>()) == true }
        }
    }
}