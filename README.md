# VisualAnalysisSquat
Android app for squat reviewing. It analyzes the biomechanics of the squat movement using computer vision based on video footage.

Análisis de movimiento mediante visión artificial (Nombre por definir)

Esta aplicación está diseñada para analizar una sentadilla a partir de un vídeo y extraer métricas como la ejecución, fatiga o repeticiones realizadas siguiendo los estándares del powerlifting en los que la cadera debe romper la paralela de la rodilla.

Alcance de la aplicación:

Sólo es válida para analizar el patrón de sentadilla
Limitada a 1 usuario por sistema
Métricas limitadas a ejecución (profundidad), repeticiones y fatiga basada en la velocidad de ejecución
Sólo se podrán analizar videos en diferido

Arquitectura general:

Backend: desarrollado en Java, se va a encargar de conectar la aplicación móvil con el servicio de análisis mediante APIs
Servicio de análisis: desarrollado en Python, mediante la librería MediaPipe se va a encargar de poner vectores a las partes del cuerpo y aplicar la lógica para extraer los resultados del vídeo a analizar
App móvil: desarrollado en Kotlin, vamos a buscar hacer una interfaz sencilla pero funcional desde la que podamos logearnos, subir nuestro video y ver los resultados del análisis

Definiciones y criterios:
Se considera una sesión un video completo
Una repetición será cuando desde la posición inicial se ejecute una sentadilla y se vuelva a la posición inicial
Sólo se contarán como repeticiones válidas si cumplen con la norma descrita. Las inválidas serán analizadas pero no sumarán al conteo de la sesión
Se aplicará el reglamento de powerlifting para validar la profundidad de la sentadilla, la cadera debe pasar la paralela de la rodilla 
Se evaluará cada repetición y al final habrá una evaluación global con el total de repeticiones


Flujo básico de uso:

El usuario graba su sentadilla
Se envía el vídeo mediante la app, el backend aplica su lógica y se pasa a MediaPipe
Se procesa el análisis del video
Se devuelven los resultados y se muestran en pantalla al usuario






Métricas de la aplicación:

Recuento de repeticiones
Validez de la profundidad de la sentadilla
Velocidad del movimiento, utilizada como indicador de esfuerzo
Fatiga estimada a partir de la pérdida de velocidad


