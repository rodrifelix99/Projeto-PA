# JSONPackage

A simple Kotlin library for building, manipulating and serializing JSON in memory and for turning your own Kotlin objects into JSON using reflection.  

ISCTE-IUL – Masters in Computer Engineering – Advanced Programming 2025

---

## What’s Inside

1. **Phase 1: JSON Model**  
   - `JsonElement` (sealed base class)  
   - Primitive types: `JsonString`, `JsonNumber`, `JsonBoolean`, `JsonNull`  
   - Composite types: `JsonArray`, `JsonObject`  
   - Functional operations: `filter`, `map`  
   - Visitor & validation extensions  

2. **Phase 2: Inference**  
   - `fun toJsonElement(value: Any?): JsonElement`  
   - Supports `Int`, `Double`, `Boolean`, `String`, `null`  
   - `List<…>` → `JsonArray`  
   - `Map<String, …>` → `JsonObject`  
   - `Enum<*>` → `JsonString(name)`  
   - Data classes via Kotlin reflection  

3. **Phase 3: GetJson HTTP Framework**  
   - Annotations: `@Mapping`, `@Path`, `@Param`  
   - `GetJson` class to register controllers and start a HTTP server  
   - Reflection is used to map URL paths and query parameters into controller method calls  
   - Serialization of return values using your JSON model  

---

## Getting Started

1. **Clone** this repo into your IDE.  
2. Make sure you have **Kotlin 1.6+** and **Gradle**.  
3. Import the module `jsonpackage` into your settings.

---

## Phase 1 – JSON Model

### Create JSON Values by Hand

```kotlin
import jsonpackage.models.*

val person = JsonObject(mapOf(
  "name"  to JsonString("Alice"),
  "age"   to JsonNumber(30),
  "admin" to JsonBoolean(false),
  "notes" to JsonNull
))

val scores = JsonArray(listOf(
  JsonNumber(10), JsonNumber(20), JsonNumber(30)
))
```

### Filter & Transform

```kotlin
// Only adults
val adults = JsonArray(listOf(17,20,15).map { JsonNumber(it) })
  .filter { (it as JsonNumber).value.toInt() >= 18 }

// Double every number
val doubled = scores.map {
  JsonNumber((it as JsonNumber).value.toInt() * 2)
}
```

### Visitor & Validation

```kotlin
import jsonpackage.extensions.*

val okKeys    = person.validateObjectKeys()    // no blank keys?
val uniform   = scores.validateArrayTypes()    // same type, no nulls?
```

### Serialize to String

```kotlin
println(person.toJsonString())
// → {"name":"Alice","age":30,"admin":false,"notes":null}

println(scores.toJsonString())
// → [10,20,30]
```

---

## Phase 2 – Inference

### `toJsonElement` in Action

```kotlin
import jsonpackage.toJsonElement

data class Book(val title: String, val pages: Int)
val b = Book("Kotlin Tips", 200)

val json = toJsonElement(b)
println(json.toJsonString())
// → {"title":"Kotlin Tips","pages":200}
```

Supports nested data classes, lists, maps, enums:

```kotlin
enum class Status { OPEN, CLOSED }
println(toJsonElement(Status.OPEN).toJsonString())
// → "OPEN"
```

---

## Phase 3 – GetJson HTTP Framework

`GetJson` lets you expose Kotlin controller classes as simple HTTP/GET endpoints:

1. **Define Annotations**

   ```kotlin
   // Mapping.kt
   @Target(CLASS, FUNCTION)
   @Retention(RUNTIME)
   annotation class Mapping(val value: String)

   // Path.kt
   @Target(VALUE_PARAMETER)
   @Retention(RUNTIME)
   annotation class Path

   // Param.kt
   @Target(VALUE_PARAMETER)
   @Retention(RUNTIME)
   annotation class Param
   ```

2. **Write a Controller**

   ```kotlin
   @Mapping("api")
   class Controller {
     @Mapping("ints")
     fun demo(): List<Int> = listOf(1, 2, 3)

     @Mapping("pair")
     fun obj(): Pair<String, String> = "um" to "dois"

     @Mapping("path/{id}")
     fun path(@Path id: String): String = "$id!"

     @Mapping("args")
     fun args(@Param n: Int, @Param text: String): Map<String,String> =
       mapOf(text to text.repeat(n))
   }
   ```

3. **Start the Server**

   ```kotlin
   fun main() {
     GetJson(Controller::class).start(8080)
   }
   ```

4. **Access Endpoints**

   * `GET http://localhost:8080/api/ints` → `[1,2,3]`
   * `GET http://localhost:8080/api/pair` → `{"first":"um","second":"dois"}`
   * `GET http://localhost:8080/api/path/foo` → `"foo!"`
   * `GET http://localhost:8080/api/args?n=3&text=PA` → `{"PA":"PAPAPA"}`

`GetJson` uses reflection to:

* Discover methods annotated with `@Mapping`
* Extract path‐variables (`{…}`) and query params (`?…=…`)
* Invoke your controller methods with typed arguments
* Serialize the return value via `toJsonElement` → JSON string

---

## Examples

```kotlin
println(toJsonElement(listOf(4,5,6)).toJsonString())
// → [4,5,6]

println(toJsonElement(mapOf("X" to 9)).toJsonString())
// → {"X":9}
```

---

## Group Details

* **Miguel Carriço** — 73745 — [jmabc1@iscte-iul.pt](mailto:jmabc1@iscte-iul.pt)
* **Rodrigo Barata** — 131361 — [rodrifelix99@gmail.com](mailto:rodrifelix99@gmail.com)

---

## UML Diagram

![uml](https://github.com/user-attachments/assets/4e861f17-1bfa-4bd9-9280-e5acb7e3d330)
