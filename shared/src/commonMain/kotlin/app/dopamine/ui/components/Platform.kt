package app.dopamine.ui.components

/** Real backdrop blur for the bottom bar: Android 12+ and iOS; older Android gets a more opaque tint. */
expect fun supportsBackdropBlur(): Boolean
