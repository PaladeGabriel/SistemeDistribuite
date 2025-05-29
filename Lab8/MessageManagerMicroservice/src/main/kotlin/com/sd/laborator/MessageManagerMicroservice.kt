package com.sd.laborator
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread
class MessageManagerMicroservice {
    private val subscribers: HashMap<Int, Socket>
    private lateinit var messageManagerSocket: ServerSocket

    companion object Constants {
        const val MESSAGE_MANAGER_PORT = 1500
    }

    init {
        subscribers = hashMapOf()
    }

    private fun broadcastMessage(message: String, except: Int) {
        subscribers.forEach {
            it.takeIf { it.key != except }
                ?.value?.getOutputStream()?.write(
                    (message + "\n").toByteArray()
                )
        }
    }

    private fun respondTo(destination: Int, message: String) {
        subscribers[destination]?.getOutputStream()?.write(
            (message +
                    "\n").toByteArray()
        )
    }

    public fun run() {
        // se porneste un socket server TCP pe portul 1500 care  asculta pentru conexiuni
        messageManagerSocket = ServerSocket(MESSAGE_MANAGER_PORT)
        println("MessageManagerMicroservice se executa pe portul:${messageManagerSocket.localPort}")
        println("Se asteapta conexiuni si mesaje...")
        while (true) {
            val clientConnection = messageManagerSocket.accept()
            thread {
                println("Subscriber conectat: ${clientConnection.inetAddress.hostAddress}:${clientConnection.port}")
                synchronized(subscribers) {
                    subscribers[clientConnection.port] =
                        clientConnection
                }
                val bufferReader = BufferedReader(InputStreamReader(clientConnection.inputStream))
                while (true) {
                    val receivedMessage = bufferReader.readLine()
                    // daca se primeste un mesaj gol (NULL), atunci inseamna ca cealalta parte a socket-ului a fost inchisa
                    if (receivedMessage == null) {
                        // deci subscriber-ul respectiv a fost deconectat
                        println("Subscriber-ul ${clientConnection.port} a fost deconectat.")

                        synchronized(subscribers) {
                            subscribers.remove(clientConnection.port)
                        }
                        bufferReader.close()
                        clientConnection.close()
                        break
                    }
                    println("Primit mesaj: $receivedMessage")
                    val (messageType, messageDestination, messageBody) = receivedMessage.split(" ", limit = 3)
                    when (messageType) {
                        "intrebare" ->broadcastMessage("intrebare ${clientConnection.port} $messageBody", except = clientConnection.port)
                        "raspuns" -> {respondTo(messageDestination.toInt(), messageBody)}
                        "intrebare_student"->broadcastMessage("intrebare ${clientConnection.port} $messageBody", except = clientConnection.port)
                    }
                    }
            }
        }
    }
}

fun main(args: Array<String>) {
    val messageManagerMicroservice = MessageManagerMicroservice()
    messageManagerMicroservice.run()
}