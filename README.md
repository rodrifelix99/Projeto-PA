# JSONPackage

A simple Kotlin library for building, manipulating and serializing JSON in memory and for turning your own Kotlin objects into JSON using reflection.
ISCTE-IUL - Masters in Computer Engineering - 2025
Advanced Programming
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

---

## Getting Started

1. **Clone** this repo into your IDE.  
2. Make sure you have **Kotlin 1.6+**.  
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
val adults = JsonArray(
  listOf(
    JsonNumber(17), JsonNumber(20), JsonNumber(15)
  )
).filter { (it as JsonNumber).value.toInt() >= 18 }

// Double every number
val doubled = scores.map { 
  JsonNumber((it as JsonNumber).value.toInt() * 2) 
}
```

### Visitor & Validation

```kotlin
import jsonpackage.extensions.*

val validKeys    = person.validateObjectKeys()    // no blank keys?
val uniformTypes = scores.validateArrayTypes()    // same type and no nulls?
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
val s = toJsonElement(Status.OPEN)
println(s.toJsonString())  // → "OPEN"
```

---

## Examples

```kotlin
// Turn a Kotlin List into JSON
val list = listOf(1, 2, 3)
println(toJsonElement(list).toJsonString())
// → [1,2,3]

// Turn a Map into JSON
val map = mapOf("A" to 1, "B" to 2)
println(toJsonElement(map).toJsonString())
// → {"A":1,"B":2}
```

---

## Running Tests

We use **JUnit 5** for unit tests. All functionalities (model, filter/map, visitor, inference) have corresponding tests in `src/tests`.

---

## Group Details

* **Miguel Carriço** — 73745 — [jmabc1@iscte-iul.pt](mailto:jmabc1@iscte-iul.pt)
* **Rodrigo Barata** — 131361 — [rodrifelix99@gmail.com](mailto:rodrifelix99@gmail.com)

## UML

![uml](https://github.com/user-attachments/assets/4e861f17-1bfa-4bd9-9280-e5acb7e3d330)
