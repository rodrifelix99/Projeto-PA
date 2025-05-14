package getjson

@Mapping("api")
class Controller {
    @Mapping("path/{id}")
    fun byId(@Path id: String): String = "ID: $id"
}