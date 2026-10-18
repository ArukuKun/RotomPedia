package com.duoc.rotompedia.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class RotomForm(
    val displayName: String,
    val iconEmoji: String,
    val subtitle: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color
) {
    NORMAL(
        displayName = "Rotom Normal",
        iconEmoji = "⚡",
        subtitle = "Eléctrico / Fantasma",
        primaryColor = Color(0xFFFF6D00),
        secondaryColor = Color(0xFF2A2A2A),
        accentColor = Color(0xFF61DAF6)
    ),
    CALOR(
        displayName = "Rotom Calor",
        iconEmoji = "🔥",
        subtitle = "Eléctrico / Fuego",
        primaryColor = Color(0xFFE53935),
        secondaryColor = Color(0xFF332020),
        accentColor = Color(0xFFFFA726)
    ),
    LAVADO(
        displayName = "Rotom Lavado",
        iconEmoji = "💧",
        subtitle = "Eléctrico / Agua",
        primaryColor = Color(0xFF1E88E5),
        secondaryColor = Color(0xFF1B2A3D),
        accentColor = Color(0xFF00E5FF)
    ),
    FRIO(
        displayName = "Rotom Frío",
        iconEmoji = "❄️",
        subtitle = "Eléctrico / Hielo",
        primaryColor = Color(0xFF00ACC1),
        secondaryColor = Color(0xFF192E36),
        accentColor = Color(0xFF80DEEA)
    ),
    VENTILADOR(
        displayName = "Rotom Ventilador",
        iconEmoji = "🌪️",
        subtitle = "Eléctrico / Volador",
        primaryColor = Color(0xFFFBC02D),
        secondaryColor = Color(0xFF38351F),
        accentColor = Color(0xFFD4E157)
    ),
    CORTE(
        displayName = "Rotom Corte",
        iconEmoji = "🌱",
        subtitle = "Eléctrico / Planta",
        primaryColor = Color(0xFF43A047),
        secondaryColor = Color(0xFF1E3320),
        accentColor = Color(0xFFAEEA00)
    );

    fun getTips(): List<String> {
        return when (this) {
            NORMAL -> listOf(
                "¡Zzzt! ¡Bienvenido a RotomPedia! ¡Explora la Pokédex y escucha los rugidos oficiales!",
                "¡Zzz-k! ¡Usa el Constructor de Equipos para analizar si tus Pokémon comparten debilidades!",
                "¡Bzzzt! ¿Sabías que los ataques de tipo Fantasma y Normal son inmunes entre sí?"
            )
            CALOR -> listOf(
                "¡Zzzt! ¡Rotom Calor al habla! ¡El tipo Fuego derrite el Acero y quema a los Bicho, Planta y Hielo!",
                "¡Fzzz! ¡Cuidado con los ataques de tipo Agua, Tierra y Roca cuando enfrentes gimnasios!",
                "¡Zzz-k! ¡Un equipo con buena cobertura ofensiva de Fuego gana muchas batallas!"
            )
            LAVADO -> listOf(
                "¡Zzzt! ¡Rotom Lavado activado! ¡Solo tengo una debilidad elemental: el tipo Planta!",
                "¡Bzzzt! ¡El tipo Agua resiste Fuego, Hielo, Acero y a sí mismo!",
                "¡Zzz-k! ¡Compara tus Pokémon favoritos en el Modo Versus para ver quién tiene mejores estadísticas!"
            )
            FRIO -> listOf(
                "¡Brrrr-zzt! ¡Rotom Frío listo para congelar! ¡El tipo Hielo es súper eficaz contra Dragón, Volador, Tierra y Planta!",
                "¡Zzz-k! ¡El tipo Hielo solo se resiste a sí mismo, así que ten cuidado en defensa!",
                "¡Zzzt! ¡Activa la estrella variocolor en la ficha para ver los Pokémon Shiny!"
            )
            VENTILADOR -> listOf(
                "¡Wuuush-zzt! ¡Rotom Ventilador volando alto! ¡Inmune a los ataques de tipo Tierra por partida doble!",
                "¡Zzz-k! ¡La velocidad suele definir quién ataca primero y asegura la victoria!",
                "¡Bzzzt! ¡Consulta los mapas de las 9 regiones para planear tu ruta de aventura!"
            )
            CORTE -> listOf(
                "¡Zzzt-ñik! ¡Rotom Corte cortando el césped! ¡El tipo Planta castiga duramente a los Pokémon de Agua, Tierra y Roca!",
                "¡Bzzzt! ¡Revisa las debilidades críticas en el Team Builder antes de un combate importante!",
                "¡Zzz-k! ¡Los 9 líderes de gimnasio de cada región te esperan en la sección de Líderes!"
            )
        }
    }
}

object RotomThemeManager {
    private const val PREF_NAME = "rotom_theme_prefs"
    private const val KEY_FORM = "rotom_form"

    private val _currentForm = MutableStateFlow(RotomForm.NORMAL)
    val currentForm: StateFlow<RotomForm> = _currentForm

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val formName = prefs.getString(KEY_FORM, RotomForm.NORMAL.name)
        _currentForm.value = try {
            RotomForm.valueOf(formName ?: RotomForm.NORMAL.name)
        } catch (e: Exception) {
            RotomForm.NORMAL
        }
    }

    fun setForm(context: Context, form: RotomForm) {
        _currentForm.value = form
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_FORM, form.name).apply()
    }
}
