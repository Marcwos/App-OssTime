# Instrucciones para Solucionar Errores de Permisos en Firebase

## Problema
El error "PERMISSION_DENIED: Missing or insufficient permissions" aparece en:
- Gestionar Profesores (pendientes y activos)
- Torneos
- Otras pantallas que requieren acceso a Firestore

## Solución: Actualizar las Reglas de Firestore

### ⚠️ IMPORTANTE: Sigue estos pasos EXACTAMENTE

### Paso 1: Abrir Firebase Console

1. Ve a: https://console.firebase.google.com/
2. **Inicia sesión** con tu cuenta de Google
3. Selecciona tu proyecto **"osstime"**

### Paso 2: Ir a Firestore Rules

1. En el menú lateral izquierdo, haz clic en **"Firestore Database"**
2. Haz clic en la pestaña **"Reglas"** (Rules) en la parte superior
3. Verás un editor de código con las reglas actuales

### Paso 3: Copiar las Reglas Correctas

1. Abre el archivo `FIRESTORE_RULES.txt` en este proyecto
2. **Selecciona TODO el contenido** (Ctrl+A)
3. **Copia** todo el contenido (Ctrl+C)

### Paso 4: Pegar y Publicar

1. En Firebase Console, **BORRA TODO** el contenido actual del editor de reglas
2. **Pega** las reglas copiadas (Ctrl+V)
3. Verifica que las reglas se vean así:

```firestore
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Regla para la colección 'users'
    match /users/{userId} {
      allow read: if request.auth != null;
      // ... resto de reglas
    }
    // ... otras colecciones
    match /tournaments/{tournamentId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

4. Haz clic en el botón **"Publicar"** (Publish) en la parte superior derecha
5. Espera a que aparezca el mensaje "Rules published successfully"

### Paso 5: Verificar que Funcionó

1. **Cierra completamente** la aplicación en tu dispositivo/emulador
2. **Vuelve a abrir** la aplicación
3. **Inicia sesión** nuevamente
4. Intenta acceder a la pantalla de Torneos o Gestionar Profesores

### Paso 6: Si Aún Hay Error

#### Verificar Autenticación:
1. Asegúrate de estar **iniciado sesión** en la app
2. Ve a la pantalla de **Perfil** y verifica que muestre tu información
3. Si no estás autenticado, cierra sesión y vuelve a iniciar sesión

#### Verificar Logs:
1. Abre **Logcat** en Android Studio
2. Filtra por "TournamentRepository" o "UserRepository"
3. Busca mensajes de error que indiquen el problema específico

#### Crear Índices (si es necesario):
Si el error menciona "index", Firebase te dará un enlace. Haz clic en ese enlace para crear el índice automáticamente.

### Reglas Explicadas

Las reglas permiten:
- ✅ **Lectura**: Cualquier usuario autenticado puede leer todas las colecciones
- ✅ **Escritura**: Cualquier usuario autenticado puede escribir en `tournaments`, `schedules`, `classes`, `students`, `attendances`
- ✅ **Users**: Solo puedes modificar tu propio usuario, o ser ADMIN para modificar otros

### Notas Importantes

- ⏱️ Las reglas se aplican **inmediatamente** después de publicar
- 🔄 Puede tomar unos segundos para que los cambios se propaguen
- 🔐 **NUNCA** dejes las reglas completamente abiertas en producción
- 📱 Reinicia la app después de cambiar las reglas
