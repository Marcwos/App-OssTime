# 🔥 Guía de Configuración de Firebase para OssTime

Esta guía te ayudará a configurar Firebase Firestore en tu proyecto Android paso a paso.

## 📋 Requisitos Previos

- Una cuenta de Google
- Android Studio instalado
- Proyecto Android configurado

## 🚀 Paso 1: Crear un Proyecto en Firebase Console

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Haz clic en **"Agregar proyecto"** o **"Add project"**
3. Ingresa un nombre para tu proyecto (ej: "OssTime")
4. Opcionalmente, desactiva Google Analytics si no lo necesitas
5. Haz clic en **"Crear proyecto"** o **"Create project"**
6. Espera a que se complete la creación del proyecto

## 📱 Paso 2: Agregar una App Android a Firebase

1. En la página de descripción general del proyecto, haz clic en el ícono de **Android** (🖥️)
2. Completa el formulario:
   - **Nombre del paquete de Android**: `com.example.osstime` (debe coincidir con tu `applicationId` en `build.gradle.kts`)
   - **Apodo de la app** (opcional): "OssTime Android"
   - **Certificado de firma SHA-1** (opcional, para funciones avanzadas)
3. Haz clic en **"Registrar app"** o **"Register app"**

## 📥 Paso 3: Descargar el archivo google-services.json

1. Después de registrar la app, Firebase te mostrará un botón para **descargar `google-services.json`**
2. **IMPORTANTE**: Descarga este archivo
3. Copia el archivo `google-services.json` descargado
4. Pégalo en la siguiente ubicación de tu proyecto:
   ```
   app/
   └── google-services.json
   ```
   (Debe estar en la carpeta `app/`, al mismo nivel que `build.gradle.kts`)

## ⚙️ Paso 4: Habilitar Firestore Database

1. En Firebase Console, ve al menú lateral y selecciona **"Firestore Database"**
2. Haz clic en **"Crear base de datos"** o **"Create database"**
3. Selecciona el modo:
   - **Modo de prueba** (para desarrollo): Permite lectura/escritura durante 30 días
   - **Modo de producción**: Requiere reglas de seguridad configuradas
4. Para desarrollo, selecciona **"Modo de prueba"**
5. Elige una ubicación para tu base de datos (selecciona la más cercana a tus usuarios)
6. Haz clic en **"Habilitar"** o **"Enable"**

## 🔒 Paso 5: Configurar Reglas de Seguridad (Opcional para desarrollo)

Si elegiste modo de prueba, puedes saltarte este paso por ahora. Para producción, necesitarás configurar reglas.

1. Ve a **Firestore Database** > **Reglas** o **Rules**
2. Para desarrollo, puedes usar estas reglas temporales:
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /{document=**} {
         allow read, write: if request.time < timestamp.date(2025, 12, 31);
       }
     }
   }
   ```
3. Haz clic en **"Publicar"** o **"Publish"**

⚠️ **ADVERTENCIA**: Estas reglas permiten acceso completo. Solo úsalas para desarrollo.

## ✅ Paso 6: Verificar la Configuración en Android Studio

1. Abre Android Studio
2. Asegúrate de que el archivo `google-services.json` esté en `app/google-services.json`
3. Sincroniza el proyecto: **File** > **Sync Project with Gradle Files**
4. Verifica que no haya errores de compilación

## 🧪 Paso 7: Probar la Conexión

1. Ejecuta la aplicación en un dispositivo o emulador
2. La primera vez que uses Firestore, los datos se crearán automáticamente
3. Ve a Firebase Console > Firestore Database para ver tus datos

## 📊 Estructura de Datos en Firestore

Tu aplicación creará las siguientes colecciones automáticamente:

### Colección: `students`
```json
{
  "id": "string",
  "firstName": "string",
  "lastName": "string",
  "belt": "string"
}
```

### Colección: `classes`
```json
{
  "id": "string",
  "name": "string",
  "type": "string",
  "date": "string",
  "description": "string",
  "time": "string"
}
```

## 🔧 Solución de Problemas

### Error: "File google-services.json is missing"
- Asegúrate de que el archivo esté en `app/google-services.json`
- Verifica que el nombre del paquete coincida con el de Firebase

### Error: "Default FirebaseApp is not initialized"
- Verifica que `FirebaseModule.initialize()` se llame en `MainActivity.onCreate()`
- Asegúrate de que el plugin de Google Services esté aplicado en `build.gradle.kts`

### La app no se conecta a Firebase
- Verifica tu conexión a Internet
- Asegúrate de que Firestore esté habilitado en Firebase Console
- Revisa las reglas de seguridad de Firestore

## 📚 Recursos Adicionales

- [Documentación oficial de Firebase](https://firebase.google.com/docs)
- [Guía de Firestore para Android](https://firebase.google.com/docs/firestore/quickstart)
- [Reglas de seguridad de Firestore](https://firebase.google.com/docs/firestore/security/get-started)

## ✨ Características Implementadas

- ✅ Persistencia offline habilitada
- ✅ Sincronización automática cuando hay conexión
- ✅ Observadores en tiempo real para cambios en datos
- ✅ Operaciones CRUD completas para estudiantes y clases

---

**¡Listo!** Tu aplicación ahora está configurada para usar Firebase Firestore. 🎉
