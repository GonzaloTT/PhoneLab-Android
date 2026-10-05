# PhoneLab Base

Plantilla de proyecto

El proyecto está desarrollado con Kotlin + XML y contiene cuatro módulos:

1. **Acelerómetro**: lectura de X, Y, Z, magnitud, movimiento y orientación.
2. **Linterna**: encendido y apagado mediante `CameraManager`.
3. **Vibración**: patrones cortos, largos y dobles mediante `VibrationEffect`.
4. **Mi sensor/periférico**: estructura deliberadamente incompleta para la implementación del estudiante.

## Entorno del proyecto

- Android Gradle Plugin: 8.5.0
- Kotlin: 1.9.0
- compileSdk: 34
- targetSdk: 34
- minSdk: 26
- Java/JDK: 17

## Cómo comenzar

1. Clona o descarga este repositorio.
2. Abre la carpeta raíz `PhoneLabBase` en Android Studio.
3. Espera la sincronización de Gradle.
4. Ejecuta la aplicación preferentemente en un **teléfono físico**.
5. Busca la palabra `TODO` en todo el proyecto para localizar los retos.

## Retos obligatorios

- **Acelerómetro:** registrar magnitud máxima y contar eventos de movimiento fuerte.
- **Linterna:** implementar modo intermitente sin bloquear el hilo principal.
- **Vibración:** crear un patrón personalizado.
- **Mi sensor/periférico:** implementar un cuarto periférico o sensor e interpretar sus datos.

Consulta el documento de la actividad para los requisitos completos.
