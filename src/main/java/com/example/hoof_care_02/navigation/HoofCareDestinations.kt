package com.example.hoof_care_02.navigation

/**
 * Rotas centralizadas de navegação do app (Navigation Compose).
 */
object HoofCareDestinations {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGN_IN = "sign_in"
    const val ADICAO_PET = "adicao_pet"
    const val CADASTRO_PET = "cadastro_pet"
    const val PAGINA_HOME = "pagina_home"
    const val LEMBRETES = "lembretes"
    const val PERFIL_PET = "perfil_pet/{petId}"
    const val CHATBOT = "chatbot"
    const val CONFIGURACOES = "configuracoes"
    const val CLINICAS = "clinicas"

    // Fluxo de Perfil do Usuário
    const val USER_PROFILE_01 = "user_profile_01"
    const val USER_PROFILE_02 = "user_profile_02"
    const val USER_PROFILE_03 = "user_profile_03"
    const val USER_PROFILE_04 = "user_profile_04"

    fun perfilPetRoute(petId: Int) = "perfil_pet/$petId"
}
