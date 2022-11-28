package app.todo.routes

import app.todo.models.Task
import app.todo.models.taskStorage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.taskRouting() {
    route("/tasks") {
        get {
            if (taskStorage.isNotEmpty()) {
                call.respond(taskStorage)
            } else {
                call.respondText("No tasks found", status = HttpStatusCode.NotFound)
            }
        }
        get("{id?}") {
            val id = call.parameters["id"] ?: return@get call.respondText(
                "Missing ID!",
                status = HttpStatusCode.BadRequest
            )
            val task = taskStorage.find { it.id == id } ?: return@get call.respondText(
                "No such task with ID = $id found!",
                status = HttpStatusCode.NotFound
            )
            call.respond(task)
        }
        post {
            try {
                val task = call.receive<Task>()
                val existingTask = taskStorage.find { it.id == task.id }
                if (existingTask !== null) {
                    taskStorage.set(taskStorage.indexOf(existingTask), task)
                } else {
                    taskStorage.add(task)
                }
            } catch (e: ContentTransformationException) {
                return@post call.respondText(
                    e.message ?: "Invalid content type!",
                    status = HttpStatusCode.BadRequest
                )
            }
            call.respondText(
                "New task added successfully!",
                status = HttpStatusCode.Created
            )
        }
        delete("{id?}") {
            val id = call.parameters["id"] ?: return@delete call.respondText(
                "Missing ID!",
                status = HttpStatusCode.BadRequest
            )
            if (taskStorage.removeIf { it.id == id }) {
                call.respondText(
                    "Task deleted successfully!",
                    status = HttpStatusCode.Accepted
                )
            } else {
                call.respondText(
                    "No such task with ID = $id found!",
                    status = HttpStatusCode.NotFound
                )
            }
        }
    }
}