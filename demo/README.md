# Cómo probar este plugin

Este plugin le muestra al programador el nombre real de un código de
error/estado de internet (por ejemplo, el famoso "Error 404") sin
tener que buscarlo en Google.

## Qué hacer

1. En el panel de la izquierda, abrí el archivo
   **`OrderStatusController.java`** (dentro de `src` → `main` →
   `java` → `com` → `acmecorp` → `orders`).
2. No hace falta hacer click en nada más — el plugin funciona solo
   con abrir el archivo.

## Qué deberías ver

Al lado de algunos números vas a ver texto extra en gris:

- Donde dice `setStatus(404)`: al lado va a decir algo como
  `404 -> Not Found`.
- Donde dice `sendError(500)`: al lado va a decir
  `500 -> Internal Server Error`.
- Donde dice `statusCode == 200` y `== 201`: van a tener su propio
  texto también.
- **Donde dice `i < 500` (dentro del bucle `for`)**: ahí NO debería
  aparecer ningún texto — ese 500 es solo un número de conteo, no un
  código de error, y el plugin tiene que ser lo bastante inteligente
  para no confundirse.
- **Donde dice `setStatus(499)`**: tampoco debería aparecer texto,
  porque 499 no es un código real reconocido oficialmente.

## Si algo no se ve así

Sacá la captura igual, y avisame qué línea no coincide con lo de
arriba.
