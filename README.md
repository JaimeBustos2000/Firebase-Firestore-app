<div align="center">
<h1> Proyecto Verduritas SA</h1>
</div>

![Test Pass](https://img.shields.io/badge/test-pass-green)
![Static Badge](https://img.shields.io/badge/Firebase-a?label=Auth&color=%2337a3bd)

<div align="center">
  <img src="https://i.imgur.com/pItzqB2.png" alt="logo">
</div>

## Tecnologias utilizadas en el proyecto

<div align="center">
  <img src="https://github.com/user-attachments/assets/48573939-0625-4e9f-8c09-c8b1430fcd09" alt="android studio" width="50" height="50">
</div>

<div align="center">
<strong>Android Studio: </strong><p>Como IDE para la creacion del proyecto</p>
</div>


---
<div align="center">
  <img src="https://github.com/user-attachments/assets/e2d2069f-1d45-40aa-a54e-e5670058dd5b" alt="firebase" width="50" height="50">
</div>

<div align="center">
  <strong>Firebase: </strong><p>Para la autenticacion y gestion de usuarios</p>
</div>

---
<div align="center">
  <img src="https://github.com/user-attachments/assets/0b06ac54-e90d-43d1-9cd9-f477406cd8c1" alt="firestore" width="50" height="50">
</div>

<div align="center">
  <strong>Firestore: </strong><p>Para el almacenamiento de datos en colecciones y documentos.</p>
</div>

---
<div align="center">
  <img src="https://github.com/user-attachments/assets/237edeab-c02f-403e-8a49-7ca92e08cba3" alt="java" width="50" height="50">
</div>

<div align="center">
  <strong>JAVA: </strong><p>Como lenguaje de programacion para las interfaces</p>
</div>

---
<div align="center">
  <img src="https://github.com/user-attachments/assets/e8184e18-6ba7-4fc2-ae68-b89f23146f8c" alt="kotlin" width="50" height="50">
</div>

<div align="center">
  <strong>Kotlin: </strong><p>Para definir el logging con google(completo pero sin implementar)</p>
</div>

---
## En que consiste

La siguiente aplicacion es un gestor para un sistema de cultivos el cual permite hacer lo siguiente:

1. Registrarse con ciertos datos, los cuales seran almacenados en Firestore, con UID autogenerada para el usuario.
   
3. Iniciar sesion el email y contraseña por medio de FirebaseAuth y desconectarse.
   
4. Visualizar cultivos en la pestaña de inicio. Puede ver su alias, fecha en que sera cosechado y las opciones.
   
5. El añadir cultivos segun alias, fecha y tipo.
   
6. Editar cultivos segun alias y fecha de cultivo(esto recalculara la fecha de cosecha nuevamente)(PD:No puede editar su tipo)

7. Eliminar cultivos existentes.

8. Visualizar la informacion de su perfil.

-(En desarrollo) Permite vincular una cuenta de google directamente para iniciar sesion.

## Cosas a considerar
1. Al añadir cultivos el alias debe ser **UNICO**, sino lanzara excepciones, puesto que con el fin de mantener la simplicidad se disminuyen las comprobaciones de campos, lo mismo va para su tamaño en caractares.
2. Los campos de seleccion de fecha deben ser presionados al menos 2 veces.
3. No hay implementacion de cola para solicitudes, por tanto se espera que los botones como eliminar y relacionados no se presionen demasiadas veces.
4. Puede crear tantos cultivos como se le ocurra, la tabla tiene la capacidad de desplazamiento.
5. El boton de inicio de google no esta implementado por lo cual no hara nada.

## ¿Como se usa?
Simplemente cree un usuario presionando el dialogo de registro, inicie sesion con las credenciales y ante el menu seleccione el icono con signo mas debajo de la bienvenida, alli podra añadir el cultivo segun los campos y se le redireccionara a la pagina de inicio.

Si desea editar consta con presionar el icono del engranaje y dar a editar, alli solo se podra cambiar la fecha en que se realizo el cultivo y su alias actual.

Si desea eliminar haga la misma operacion anterior pero seleccione eliminar.

Para ver su informacion guardada presione el icono de perfil y si desea desconectarse seleccione el icono de salir en la parte superior.

## Solucion errores
Ante cualquier error, primero verifique en tener una version de android reciente, verifique los archivo **.gradle.kts** esten configurados correctamente, verificar archivo TOML si es necesario. Verificar importacion de librerias y versiones correspondientes al sdk del proyecto.

**IMPORTANTE** Por alguna razon desinstalar la apk puede solucionar los problemas en la emulacion o insercion de datos con firebase.

