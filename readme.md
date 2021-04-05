# Akkastream workshop 

## Pré-requis

 * JDK 11 minimum
 * docker 

Sans docker il faut pouvoir accéder au vpn et installer les cert maifs dans le jdk 

Télécharger `CA_MAIF_SUBCA-SERVICE.cer` https://pki.maif.local/public/retrieve/ca_certs.jsp

```sh 
keytool -import -alias my_certificates -keystore $JAVA_PATH/lib/security/cacerts -storepass changeit -file ~/certificates/CA_MAIF_SUBCA-SERVICE.cer
```

## Introduction 

 * Non bloquant dans le jdk 
    * `CompletionStage` 
    * client http jdk11
 * stream java8 + completion stage != coeur 

## Spec reactive stream 

[http://www.reactive-streams.org/](http://www.reactive-streams.org/)

[https://github.com/reactive-streams/reactive-streams-jvm](https://github.com/reactive-streams/reactive-streams-jvm)

Implémentations : 

 * akka stream 
 * spring reactor
 * rx java 
 * ... 

Intégration dans jdk 9 : Flow 

## Akkastream 

[https://akka.io/](https://akka.io/)

[https://doc.akka.io/docs/akka/current/stream/index.html](https://doc.akka.io/docs/akka/current/stream/index.html)

Akka stream : 

 * `Source` : un "robinet" qui émet des éléments 
 * `Flow` : un "tuyau" qui prend des éléments en entrée et émet des éléments en sortie
 * `Sink` : un "évier" qui consomme le stream pour retourner un résultat final

Un stream est un plan d'exécution qui ne fait rien tant qu'on ne l'a pas exécuté.

```java
CompletionStage<List<String>> resultat = Source.range(0, 5)
        .map(String::valueOf)
        // Ici on run le stream en collectant les éléments dans une liste 
        .runWith(Sink.seq(), system);
```

On peut créer des pièces réutilisables et les utiliser les unes avec les autres. 

La version décomposée : 

```java
// Source qui génère des entiers de 1 à 5 
Source<Integer, NotUsed> range = Source.range(0, 5);
// Flow réutilisable : c'est un tuyau qui prend des intger et retourne des strings
Flow<Integer, String, NotUsed> intToString = Flow.<Integer>create().map(String::valueOf);
// Un sink qui collecte dans une liste 
Sink<String, CompletionStage<List<String>>> seq = Sink.seq();

// Un stream a exécuter, pour le moment il ne s'est rien passé
RunnableGraph<CompletionStage<List<String>>> runnableGraph =
    range.via(intToString).toMat(seq, Keep.right());

// On exécute le stream 
CompletionStage<List<String>> futureStrings = runnableGraph.run(system);
```
Quelques opérateurs : 

 * `map`: transformer les éléments d'un stream 
 * `filter`: filtrer les éléments d'un stream 
 * `fold`: "aplatir" un stream en un élément final
 * `scan`: comme fold mais en publiant les résultats intermédiaires
 * `grouped`: créer des groupes d'éléments suivant une taille
 * `groupedWithin`: créer des groupes d'éléments suivant une taille ou un interval de temps
 * `throttle`: Ajouter un delay entre chaque élément
 * `mapAsync` / `mapAsyncUnordered`: pour chaque élément, effectuer un traitement asynchrone et publier le résultat dans le stream en gardant ou pas l'ordre. Il faut préciser un facteur de parallélisation pour pouvoir choisir le nombre de traitements asynchrone en parallèle
 * `flatMapConcat` : pour chaque élément, démarrer un stream et retourner chaque élément du stream dans le sous stream
 * `flatMapMerge` : pour chaque élément, démarrer un stream et retourner chaque élément du stream dans le sous stream avec la possibilité d'ouvrir plusieurs stream en même temps.
 * `recover` : En cas d'erreur, retourner une valeur par défaut 
 * `retries` : En cas d'erreur, recommencer 

Créer une source :

 * `single` : source pour un élément 
 * `from` : source pour une liste  
 * `empty` : source vide  
 * `completionStage` : source à partir du résultat d'un `CompletionStage`
 * `failed` : source en erreur
 * `lazyXXX` : source à partir d'une fonction
 * `repeat` : répète en boucle un élément    
 * `tick` : émet un élément suivant un interval de temps    
 * `unfold` : émet des éléments avec une sorte de fonction récursive     

Les Sink : 

 * `foreach` : applique une fonction sur chaque élément 
 * `head` : retourne le premier élément du stream 
 * `headOption` : retourne le premier élément du stream s'il existe
 * `last` : retourne le dernier élément du stream 
 * `lastOption` : retourne le dernier élément du stream s'il existe
 * `seq` : collect les éléments dans une liste 
 * `ignore` : consomme uniquement le stream sans rien retourner


## Exercices 

### Exercice 1 : fonction de base 

On part d'une source de catégories : 

```java
Source.from(List.of("animal", "career", "celebrity", "dev", "explicit", "fashion", "food", "history", "money", "movie", "music", "political", "religion", "science", "sport", "travel"));
```

 * Doubler chaque catégorie
 * Récupérer 1 blague par catégorie avec 5 requêtes en parallèle en utilisant `JokeService.getRandomJokeByCategory`
 * Garder uniquement l'attribut blague (value)
 * Concaténer toutes les blagues
 * A la fin afficher le string obtenu 

Tester de remplacer `JokeService.getRandomJokeByCategory` par `JokeService.streamRandomJokeByCategory` en utilisant `flatMapConcat`. 

Qu'est ce qui se passe ?

Tester avec `flatMapMerge`

### Exercice 2 : 

Intégration avec des briques externes : 

[https://doc.akka.io/docs/alpakka/current/index.html](https://doc.akka.io/docs/alpakka/current/index.html)

[https://doc.akka.io/docs/alpakka-kafka/current/](https://doc.akka.io/docs/alpakka-kafka/current/)

Lire les catégories depuis un fichier csv et les envoyer dans kafka. 

Lire les catégories depuis kafka, récupérer des blagues et stocker les blagues dans postgresql. 

Kafdrop maif : 
[http://kafdrop-1.broker-build-map.build-broker.cloud.maif.local:15974/](http://kafdrop-1.broker-build-map.build-broker.cloud.maif.local:15974/)