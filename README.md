## AndroidManifest.xml

Agrega los siguientes permisos y configuraciones:

```manifest
<!-- Permiso para acceso a internet -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- En la etiqueta <application> -->
<application
    android:usesCleartextTraffic="true" <!-- solo para desarrollo -->
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
    <!-- Resto de tu configuración -->
</application>
```

## network_security_config.xml

Crea el archivo app/src/main/res/xml/network_security_config.xml:

```config
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="true" />
</network-security-config>

```

# Bridge JavaScript - AndroidBridge

Incluye la clase AndroidBridge en tu proyecto web

```batch
// Instanciar el bridge
var androidBridge = new AndroidBridge();
```

Uso

```json
// Llamar a un método nativo de Android
try {
    const resultado = await androidBridge.send(
        {
            timeout: 6000,
            method: "nombreMetodo",
            eventName: "nombreEvento"
        },
        "datosParametro1",
        "datosParametro2"
    );

    console.log("Respuesta de Android:", resultado);
} catch (error) {
    console.error("Error:", error.message);
}
```

## Referencia

Método send()

```
/**
 * Envía una solicitud al contexto nativo de Android
 * @param {Object} options - Opciones de configuración
 * @param {number} options.timeout - Tiempo máximo de espera en ms (default: 5000)
 * @param {string} options.method - Nombre del método en AndroidContext
 * @param {string} options.eventName - Nombre único del evento para la respuesta
 * @param {...any} data - Datos a enviar al método nativo
 * @returns {Promise<any>} Respuesta de Android
 * @throws {Error} Si hay timeout o error en la ejecución
 */
await androidBridge.send(options, ...data);
```

Métodos Auxiliares

```
// Configurar timeout por defecto
androidBridge.setDefaultTimeout(8000);

// Verificar si hay una solicitud pendiente
if (androidBridge.isPending) {
    console.log("Hay una solicitud en curso");
}

// Limpiar todos los recursos y listeners
androidBridge.cleanupAll();
```

## Implemetacion JS

Clase Completa AndroidBridge

```

class AndroidBridge {
  #activeTimer = null;
  #eventHandlers = new Map();
  #defaultTimeout = 5000;

  async send(
    { timeout = this.#defaultTimeout, method = "", eventName = "" } = {},
    ...data
  ) {
    if (!method.trim()) {
      throw new Error('El parámetro "method" es requerido');
    }

    if (!eventName.trim()) {
      throw new Error('El parámetro "eventName" es requerido');
    }

    if (this.#activeTimer) {
      this.#cleanupResources();
    }

    return new Promise((resolve, reject) => {
      const handler = (event) => {
        this.#cleanupResources(eventName, handler);
        resolve(event.detail);
      };

      this.#eventHandlers.set(eventName, handler);

      this.#activeTimer = setTimeout(() => {
        this.#cleanupResources(eventName, handler);
        reject(
          new Error(
            `Timeout esperando respuesta de Android para método: ${method}`
          )
        );
      }, timeout);

      window.addEventListener(eventName, handler, { once: false });

      try {
        if (typeof AndroidContext[method] !== "function") {
          throw new Error(`Método "${method}" no disponible en AndroidContext`);
        }

        AndroidContext[method](...data);
      } catch (error) {
        this.#cleanupResources(eventName, handler);
        reject(this.#normalizeError(error, method));
      }
    });
  }

  #cleanupResources(eventName = "", handler = null) {
    if (this.#activeTimer) {
      clearTimeout(this.#activeTimer);
      this.#activeTimer = null;
    }

    if (eventName && handler) {
      window.removeEventListener(eventName, handler);
      this.#eventHandlers.delete(eventName);
    }
  }

  cleanupAll() {
    if (this.#activeTimer) {
      clearTimeout(this.#activeTimer);
      this.#activeTimer = null;
    }

    this.#eventHandlers.forEach((handler, eventName) => {
      window.removeEventListener(eventName, handler);
    });
    this.#eventHandlers.clear();
  }

  #normalizeError(error, method) {
    if (error instanceof Error) {
      error.message = `AndroidBridge.${method}: ${error.message}`;
      return error;
    }
    return new Error(`AndroidBridge.${method}: ${String(error)}`);
  }

  get isPending() {
    return this.#activeTimer !== null;
  }

  setDefaultTimeout(timeout) {
    if (typeof timeout !== "number" || timeout <= 0) {
      throw new Error("Timeout debe ser un número positivo");
    }
    this.#defaultTimeout = timeout;
  }
}


```

## Configurar tunel para dispositivo

Verificar Conexión del Dispositivo

```
adb devices

```

Deberías ver tu dispositivo listado con estado device.

## Configurar Túnel Reverso

Si tu aplicación/servicio está corriendo en http://1.1.1.1:3000 (o cualquier otra IP:puerto), ejecuta:

```
adb reverse tcp:3000 tcp:3000

```

Este comando redirige el puerto 3000 del dispositivo al puerto 3000 de tu computadora

## Verificar túneles activos:

```
adb reverse --list
```

## Eliminar un túnel específico:

```
adb reverse --remove tcp:3000
```

## Eliminar todos los tuneles

```
adb reverse --remove-all
```
