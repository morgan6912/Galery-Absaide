package com.absaide.gallery.presentation.navigation

sealed class Screen(val route: String) {
    object Login          : Screen("login")
    object Register       : Screen("register")
    object AdminDashboard : Screen("admin/dashboard")
    object AdminUsers     : Screen("admin/users")
    object AdminArtworks  : Screen("admin/artworks")
    object AdminReports   : Screen("admin/reports")
    object Accessibility  : Screen("accessibility")
    object ArtistProfile  : Screen("artist/profile")
    object ArtistUpload   : Screen("artist/upload")
    object ArtistMyWorks  : Screen("artist/my-works")
    object ArtistStats    : Screen("artist/stats")
    object ArtistGallery  : Screen("artist/gallery")
    object ArtworkDetail  : Screen("artwork/detail/{artworkId}")
    object UserGallery    : Screen("user/gallery")
    object UserSearch     : Screen("user/search")
    object UserFavorites  : Screen("user/favorites")
    object UserProfile    : Screen("user/profile")
    object AdminGallery : Screen("admin/gallery")
    object UserMessages : Screen("user/messages")
    object ArtistMessages : Screen("artist/messages")
    object UserArtists  : Screen("user/artists")

}
