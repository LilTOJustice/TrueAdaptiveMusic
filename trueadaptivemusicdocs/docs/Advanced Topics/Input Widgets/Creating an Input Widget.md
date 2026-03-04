# Creating an Input Widget 

If your modded trigger type requires an argument that isn't within the [Supported Input Widget Types](Supported%20Input%20Widget%20Types.md) list, then you need to create the widget for it to work with the pack creation UI.

Inside your client initializer step, you must register an input widget maker that describes how to construct the widget, which is done similarly to registering a predicate type.

Below is an example showing how the checkbox (boolean) input widget is created in Kotlin.
```kotlin
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
```

The first argument specifies the type that should trigger this widget to be created when the UI is running. You can either pass in Type object, or a predicate lambda that tests whether the widget should be used for the input.

The second argument is a lambda that takes in some arguments:

`string (Text)` - the text to display with the widget 

`screen (Screen)` - the screen the widget is rendered on

`outArgs (Array[Any?])` - an array where the result should be stored, or the existing value is retrieved

`arg (Parameter)` - the current parameter being processed

`tooltipText (Text?)` - the Text object that should be used for the tooltip of the widget

The lambda should return any subtype of `ClickableTextWidget`. In this case, we're returning a custom CheckboxWidget type made for True Adaptive Music.