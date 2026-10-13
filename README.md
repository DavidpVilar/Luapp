# Luapp

App Android para registrar y monitorizar las tomas, baños y cambios de pañal de mi hija bebé.

## Funcionalidades

- Registrar una toma indicando la cantidad en ml.
- Registrar cambios de pañal (caca) y baños, con la hora.
- Ver el resumen del día: total de ml tomados, número de cacas y si ya se ha bañado.
- Ver el historial de actividades, con opciones para editar o borrar cada registro.
- Los datos se guardan en el propio dispositivo, sin necesidad de cuenta ni conexión.

## Tecnologías

- Kotlin
- Jetpack Compose y Material 3
- ViewModel de Android (AndroidViewModel)
- Gson para serializar los datos
- SharedPreferences para la persistencia local

## Estructura

- `FeedingScreen.kt`: pantalla principal, resumen diario, diálogos y lista de actividades.
- `FeedingViewModel.kt`: lógica de altas, ediciones, borrados y persistencia.
- `Feeding.kt`: modelo de datos (`Feeding`) y tipos de actividad (`ActivityType`).
- `ui/theme/`: colores, tipografía y tema de la app.

## Capturas

<!-- Añade aquí capturas de pantalla de la app -->

## Requisitos

- Android Studio (versión reciente)
- Android 7.0 (API 24) o superior

## Autor

David Pérez
