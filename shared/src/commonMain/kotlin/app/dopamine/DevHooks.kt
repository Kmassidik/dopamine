package app.dopamine

/** Developer-only start values, set by the iOS host from environment variables (see MainViewController). Defaults do nothing. */
object DevHooks {
    /** Which reading page a lesson's "learn" screen opens on (0 = first). */
    var learnPage: Int = 0
}
