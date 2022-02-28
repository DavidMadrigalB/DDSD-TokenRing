# DDSD-TokenRing
Tarea 2 de la materia de Desarrollo de Sistemas Distribuidos.

Trata de que a través de 6 nodos que trabajan secuencialmente uno tras otro se pasan un token (entero de 16 bits) a través de sockets seguros (SSL) y lo incrementan en 1.
Cada nodo corre en un puerto diferente arriba de _50000_ en una misma computadora.

Importante: El programa no está hecho para correr en distintas compus, ya que específica _localhost_, por tanto corre en distintos procesos en la misma computadora.

En la carpeta están subidos los archivos de certificados y keystores para las conexiones seguras entre cliente y servidor.

## Topología de nodos
Secuencial: 0 -> 1 -> 2 -> 3 -> 4 -> 5 -> 0

## Para generar el certificado y keystore:
Pasos que ejecutar en consola:
1. ```keytool -genkeypair -keyalg RSA -alias <name> -keystore <keystore>.jks -storepass <password>```

1.1 Se llenan los datos que pide al generar la pareja de llaves

2. ```keytool -exportcert -keystore <keystore>.jks -alias <name> -rfc -file <name>.pem```

3. ```keytool -import -alias <name> -file <name>.pem -keystore <keystore_cliente>.jks -storepass <password>```

3.1 Este último es para generar el keystore del cliente
  
Ejemplo:
1. ```keytool -genkeypair -keyalg RSA -alias certificado_servidor -keystore keystore_servidor.jks -storepass 1234567```
2. ```keytool -exportcert -keystore keystore_servidor.jks -alias certificado_servidor -rfc -file certificado_servidor.pem```
* La opción exportcert lee del keystore el certificado identificado por el alias y genera un archivo texto que contiene el certificado, en este caso se genera el archivo certificado_servidor.pem
3. ```keytool -import -alias certificado_servidor -file certificado_servidor.pem -keystore keystore_cliente.jks -storepass 123456```
* La opción import lee el archivo certificado_servidor.pem e inserta el certificado en el keytore keystore_cliente.jks, identificando el certificado mediante el alias. Storepass es la contraseña del keystore.
  
IMPORTANTE: Para Java, la clave del certificado y la clave (storepass) del almacén de claves (keystore) deben ser las mismas.
  
## Para ejecutar
Ya una vez compilado el código (.class), para correrlo en consola hacemos uso del siguiente comando:
```
java -Djavax.net.ssl.keyStore=keystore_servidor.jks -Djavax.net.ssl.keyStorePassword=1234567 -Djavax.net.ssl.trustStore=keystore_cliente.jks -Djavax.net.ssl.trustStorePassword=123456 TokenRing #
```
Esto para especificarle las keystores, junto con las contraseñas y el programa pueda usarlas para establecer la conexión segura.

Importante: Al final tiene un "#", es el número del nodo, en este caso puede variar de 0 a 5.

Nota: En Windows puede que el comando java -Djavax no pueda ejecutarse en **_PowerShell_**, sin embargo en **_CMD_** si lo ejecuta.
