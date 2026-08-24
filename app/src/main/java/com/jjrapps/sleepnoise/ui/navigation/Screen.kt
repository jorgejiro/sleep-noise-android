package com.jjrapps.sleepnoise.ui.navigation

/**
 * The three navigation destinations. The sleep timer is deliberately absent: it is a
 * modal bottom sheet over the player, not a destination, so it does not belong in the
 * back stack. See the specification §4.
 */
enum class Screen(val route: String) {
    Player("player"),
    Settings("settings"),
    Changelog("changelog")
}
