
## Escuela Colombiana de Ingeniería
### Arquitecturas de Software – ARSW


#### Ejercicio – programación concurrente, condiciones de carrera y sincronización de hilos. EJERCICIO INDIVIDUAL O EN PAREJAS.

##### Parte I – Antes de terminar la clase.

Control de hilos con wait/notify. Productor/consumidor.

1. Revise el funcionamiento del programa y ejecútelo. Mientras esto ocurren, ejecute jVisualVM y revise el consumo de CPU del proceso correspondiente. A qué se debe este consumo?, cual es la clase responsable?

Podemos apreciar un alto consumo de CPU (12,2%):

![image](https://github.com/AndresOnate/ARSW-LAB3/assets/63562181/b3712055-e0ee-458a-b400-fa5e25d57616)

Se están ejecutando dos hilos, mientras un hilo productor agrega a una cola un elemento cada segundo, el otro consume todo lo que pueda en un while infinito. La clase Consumer es la respondable de este consumo de CPU, no espera algun tiempo determinado a que el productor tenga algo de "stock" en la cola.

2. Haga los ajustes necesarios para que la solución use más eficientemente la CPU, teniendo en cuenta que -por ahora- la producción es lenta y el consumo es rápido. Verifique con JVisualVM que el consumo de CPU se reduzca.

Se realizaron ajustes a las clases productor y consumidor haciendo uso de los metodos wait() y notifyAll(), lo que resultó en una solución más eficiente:

![image](https://github.com/AndresOnate/ARSW-LAB3/assets/63562181/5e045ecd-70ad-4a27-9f6c-f9b2a27f1f91)

3. Haga que ahora el productor produzca muy rápido, y el consumidor consuma lento. Teniendo en cuenta que el productor conoce un límite de Stock (cuantos elementos debería tener, a lo sumo en la cola), haga que dicho límite se respete. Revise el API de la colección usada como cola para ver cómo garantizar que dicho límite no se supere. Verifique que, al poner un límite pequeño para el 'stock', no haya consumo alto de CPU ni errores.

Encontramos que existe una sobrecarga para el constructor, que permite definir la capacidad fija de la cola:

![image](https://github.com/AndresOnate/ARSW-LAB3/assets/63562181/b3f5add5-f604-4f10-a777-2a1fe1f0297f)

Modificamos los hilos para que el productor no tenga un tiempo determinado de producción (Por defecto estaba en 1 segundo) y el consumidor tendra que esperar cada 2 segundos para consumir un solo elemento de la cola.

Consumer:
```
            synchronized (queue) {
                try {
                    while (queue.isEmpty())
                    {
                        queue.wait();
                    }
                    Thread.sleep(2000);
                    int elem=queue.poll();
                    System.out.println("Consumer consumes "+elem);
                    queue.notifyAll();
                }catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
```
Producer:
```
            synchronized (queue) {
                try {
                    while (queue.size() == this.stockLimit)
                    {
                        queue.wait();
                    }
                    dataSeed = dataSeed + rand.nextInt(100);
                    queue.add(dataSeed);
                    System.out.println("Producer added " + dataSeed);
                    queue.notifyAll();
                }catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
```
Uso más eficientemente la CPU:

![image](https://github.com/AndresOnate/ARSW-LAB3/assets/63562181/0b9498d6-ebff-4504-8c4d-359ddb73c58a)

Limite de Stock pequeño:

![image](https://github.com/AndresOnate/ARSW-LAB3/assets/63562181/215810f0-f53d-45e1-be71-61721ccacbec)


##### Parte II. – Avance para el jueves, antes de clase.

Sincronización y Dead-Locks.

![](http://files.explosm.net/comics/Matt/Bummed-forever.png)

1. Revise el programa “highlander-simulator”, dispuesto en el paquete edu.eci.arsw.highlandersim. Este es un juego en el que:

	* Se tienen N jugadores inmortales.
	* Cada jugador conoce a los N-1 jugador restantes.
	* Cada jugador, permanentemente, ataca a algún otro inmortal. El que primero ataca le resta M puntos de vida a su contrincante, y aumenta en esta misma cantidad sus propios puntos de vida.
	* El juego podría nunca tener un único ganador. Lo más probable es que al final sólo queden dos, peleando indefinidamente quitando y sumando puntos de vida.

2. Revise el código e identifique cómo se implemento la funcionalidad antes indicada. Dada la intención del juego, un invariante debería ser que la sumatoria de los puntos de vida de todos los jugadores siempre sea el mismo(claro está, en un instante de tiempo en el que no esté en proceso una operación de incremento/reducción de tiempo). Para este caso, para N jugadores, cual debería ser este valor?.
```
Para N jugadores
V = Vida para cada juegador. 
Valor del Invariante= N*V
```
3. Ejecute la aplicación y verifique cómo funcionan las opción ‘pause and check’. Se cumple el invariante?.

Podemos apreciar que la Opción ‘pause and check’ no detiene la ejecución del programa. El invariante no se cumple:

![image](https://github.com/AndresOnate/ARSW-LAB3/assets/63562181/6f873668-8583-43ff-b213-216dc5346043)

 La sumatoria de los puntos de vida de todos los jugadores deberia ser 300.

4. Una primera hipótesis para que se presente la condición de carrera para dicha función (pause and check), es que el programa consulta la lista cuyos valores va a imprimir, a la vez que otros hilos modifican sus valores. Para corregir esto, haga lo que sea necesario para que efectivamente, antes de imprimir los resultados actuales, se pausen todos los demás hilos. Adicionalmente, implemente la opción ‘resume’.
Implementación:
```
        btnResume.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (Immortal im : immortals) {
                    im.resumeImmortal();
                }
            }
        });
```   
6. Verifique nuevamente el funcionamiento (haga clic muchas veces en el botón). Se cumple o no el invariante?.

El invariante aun no se cumple:

Suma: 830

![image](https://github.com/AndresOnate/ARSW-LAB3/assets/63562181/88e23d7b-8781-4773-a55b-227fab5da5bd)

Suma:620

![image](https://github.com/AndresOnate/ARSW-LAB3/assets/63562181/48fcac1e-1744-4f01-98fb-ac7dac859f7e)


7. Identifique posibles regiones críticas en lo que respecta a la pelea de los inmortales. Implemente una estrategia de bloqueo que evite las condiciones de carrera. Recuerde que si usted requiere usar dos o más ‘locks’ simultáneamente, puede usar bloques sincronizados anidados:

	```java
	synchronized(locka){
		synchronized(lockb){
			…
		}
	}
	```

Se identifica una región critica en el método fight(Immortal i2):

```
    public void fight(Immortal i2) {

        if (i2.getHealth() > 0) {
            i2.changeHealth(i2.getHealth() - defaultDamageValue);
            this.health += defaultDamageValue;
            updateCallback.processReport("Fight: " + this + " vs " + i2+"\n");
        } else {
            updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
        }

    }
```

Ambos inmortales intentan cambiar la salud del otro, lo que puede causar problemas de concurrencia. Haciendo uso bloques sincronizados anidados aseguramos la región critica:

```
    public void fight(Immortal i2) {
        synchronized (this) {
            synchronized (i2) {
                if (i2.getHealth() > 0) {
                    i2.changeHealth(i2.getHealth() - defaultDamageValue);
                    this.health += defaultDamageValue;
                    updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
                } else {
                    updateCallback.processReport(this + " says: " + i2 + " is already dead!\n");
                }
            }
        }
    }

```

8. Tras implementar su estrategia, ponga a correr su programa, y ponga atención a si éste se llega a detener. Si es así, use los programas jps y jstack para identificar por qué el programa se detuvo.

Confirmamos que el programa se detiene, posiblemente se trata de un deadlock.
Ejecutamos jps para obtener el ID del proceso Java:

![image](https://github.com/AndresOnate/ARSW-LAB3/assets/63562181/c1f6ff2b-1f32-4276-81b6-c48f7805720f)

jstack proporcionará información sobre los hilos en ejecución y sus estados actuales. 

![image](https://github.com/AndresOnate/ARSW-LAB3/assets/63562181/8589984f-a391-4a3d-b653-186dccec047f)

Confirmamos que se trata de un deadlock:

![image](https://github.com/AndresOnate/ARSW-LAB3/assets/63562181/6e43ba5a-c517-4136-b0da-be4106af0814)


9. Plantee una estrategia para corregir el problema antes identificado (puede revisar de nuevo las páginas 206 y 207 de _Java Concurrency in Practice_).

"A program will be free of lock-ordering deadlocks if all threads acquirethe locks they need in a fixed global order."

"One way to induce an ordering on objects is to use System.identityHashCode,which returns the value that would be returned by Object.hashCode"

Se implementa la estrategia presentada en el texto.

```
    public void fight(Immortal i2) {
        int currentImmortal = System.identityHashCode(this);
        int secondImmortal = System.identityHashCode(i2);
        if(currentImmortal < secondImmortal){
            synchronized (this) {
                synchronized (i2) {
                    if (i2.getHealth() > 0) {
                        i2.changeHealth(i2.getHealth() - defaultDamageValue);
                        this.health += defaultDamageValue;
                        updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
                    } else {
                        updateCallback.processReport(this + " says: " + i2 + " is already dead!\n");
                    }
                }
            }
        }else{
            synchronized (i2) {
                synchronized (this) {
                    if (i2.getHealth() > 0) {
                        i2.changeHealth(i2.getHealth() - defaultDamageValue);
                        this.health += defaultDamageValue;
                        updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
                    } else {
                        updateCallback.processReport(this + " says: " + i2 + " is already dead!\n");
                    }
                }
            }
        }
    }

```
10. Una vez corregido el problema, rectifique que el programa siga funcionando de manera consistente cuando se ejecutan 100, 1000 o 10000 inmortales. Si en estos casos grandes se empieza a incumplir de nuevo el invariante, debe analizar lo realizado en el paso 4.

100 inmortales, la sumatoria debe ser 10000:

![image](https://github.com/AndresOnate/ARSW-LAB3/assets/63562181/4d10f0f0-08f8-4218-a53d-30f5b5275959)

![image](https://github.com/AndresOnate/ARSW-LAB3/assets/63562181/b2778b78-81ef-4738-896f-58731cd1e276)

1000 inmortales, la sumatoria debe ser 100000:

![image](https://github.com/AndresOnate/ARSW-LAB3/assets/63562181/55282399-5623-486c-936e-5c9414cee1e1)

![image](https://github.com/AndresOnate/ARSW-LAB3/assets/63562181/30c1eb72-abca-42a2-a994-8900fa5cacc9)


10000 inmortales, la sumatoria debe ser 1000000:

![image](https://github.com/AndresOnate/ARSW-LAB3/assets/63562181/bffe1cde-9407-41b0-a78e-c35221edf008)

![image](https://github.com/AndresOnate/ARSW-LAB3/assets/63562181/955467fb-1f32-48c6-9c44-80cc69d0bc26)


11. Un elemento molesto para la simulación es que en cierto punto de la misma hay pocos 'inmortales' vivos realizando peleas fallidas con 'inmortales' ya muertos. Es necesario ir suprimiendo los inmortales muertos de la simulación a medida que van muriendo. Para esto:
	* Analizando el esquema de funcionamiento de la simulación, esto podría crear una condición de carrera? Implemente la funcionalidad, ejecute la simulación y observe qué problema se presenta cuando hay muchos 'inmortales' en la misma. Escriba sus conclusiones al respecto en el archivo RESPUESTAS.txt.
	* Corrija el problema anterior __SIN hacer uso de sincronización__, pues volver secuencial el acceso a la lista compartida de inmortales haría extremadamente lenta la simulación.

13. Para finalizar, implemente la opción STOP.

```
        btnStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (Immortal im : immortals) {
                    im.killImmortal();
                }
                btnStart.setEnabled(true);
                immortals.clear();
            }
        });
```

<!--
### Criterios de evaluación

1. Parte I.
	* Funcional: La simulación de producción/consumidor se ejecuta eficientemente (sin esperas activas).

2. Parte II. (Retomando el laboratorio 1)
	* Se modificó el ejercicio anterior para que los hilos llevaran conjuntamente (compartido) el número de ocurrencias encontradas, y se finalizaran y retornaran el valor en cuanto dicho número de ocurrencias fuera el esperado.
	* Se garantiza que no se den condiciones de carrera modificando el acceso concurrente al valor compartido (número de ocurrencias).


2. Parte III.
	* Diseño:
		- Coordinación de hilos:
			* Para pausar la pelea, se debe lograr que el hilo principal induzca a los otros a que se suspendan a sí mismos. Se debe también tener en cuenta que sólo se debe mostrar la sumatoria de los puntos de vida cuando se asegure que todos los hilos han sido suspendidos.
			* Si para lo anterior se recorre a todo el conjunto de hilos para ver su estado, se evalúa como R, por ser muy ineficiente.
			* Si para lo anterior los hilos manipulan un contador concurrentemente, pero lo hacen sin tener en cuenta que el incremento de un contador no es una operación atómica -es decir, que puede causar una condición de carrera- , se evalúa como R. En este caso se debería sincronizar el acceso, o usar tipos atómicos como AtomicInteger).

		- Consistencia ante la concurrencia
			* Para garantizar la consistencia en la pelea entre dos inmortales, se debe sincronizar el acceso a cualquier otra pelea que involucre a uno, al otro, o a los dos simultáneamente:
			* En los bloques anidados de sincronización requeridos para lo anterior, se debe garantizar que si los mismos locks son usados en dos peleas simultánemante, éstos será usados en el mismo orden para evitar deadlocks.
			* En caso de sincronizar el acceso a la pelea con un LOCK común, se evaluará como M, pues esto hace secuencial todas las peleas.
			* La lista de inmortales debe reducirse en la medida que éstos mueran, pero esta operación debe realizarse SIN sincronización, sino haciendo uso de una colección concurrente (no bloqueante).

	

	* Funcionalidad:
		* Se cumple con el invariante al usar la aplicación con 10, 100 o 1000 hilos.
		* La aplicación puede reanudar y finalizar(stop) su ejecución.
		
		-->

<a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc/4.0/88x31.png" /></a><br />Este contenido hace parte del curso Arquitecturas de Software del programa de Ingeniería de Sistemas de la Escuela Colombiana de Ingeniería, y está licenciado como <a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/">Creative Commons Attribution-NonCommercial 4.0 International License</a>.
