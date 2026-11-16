package app.dopamine.ui.components

import android.os.Build

actual fun supportsBackdropBlur(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
