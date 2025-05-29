package com.sd.laborator

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread


class HeartbeatMicroservice {

    private val subscribers: HashMap<Int, Socket>
    private lateinit var HeartbeatSocket: ServerSocket

    companion object Constants {
        const val HEARTBEAT_PORT = 56789
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

    public fun run() {
        HeartbeatSocket = ServerSocket(HEARTBEAT_PORT)
        println("HeartbeatMicroservice se executa pe portul: ${HeartbeatSocket.localPort}")
        println("Se asteapta conexiuni si mesaje...")

        // Thread care trimite heartbeat periodic tuturor subscriberilor
        thread {
            while (true) {
                synchronized(subscribers) {
                    broadcastMessage("test_heartbeat test_heartbeat test_heartbeat", except = -1)
                    println("Am trimis mesaj dummy")
                }
                Thread.sleep(5000)
            }
        }

        while (true) {
            val clientConnection = HeartbeatSocket.accept()
            println("Subscriber conectat: ${clientConnection.inetAddress.hostAddress}:${clientConnection.port}")
            synchronized(subscribers) {
                subscribers[clientConnection.port] = clientConnection
            }


            thread {
                val bufferReader = BufferedReader(InputStreamReader(clientConnection.inputStream))
                try {
                    while (true) {
                        val receivedMessage = bufferReader.readLine()
                        println("Am primit un raspuns: $receivedMessage")
                        if (receivedMessage == null) break
                    }
                } catch (e: Exception) {
                    println("Eroare la citirea de la client: ${e.message}")
                } finally {
                    println("Subscriber-ul ${clientConnection.port} s-a deconectat.")
                    synchronized(subscribers) {
                        subscribers.remove(clientConnection.port)
                    }
                    clientConnection.close()
                }
            }
        }
    }
}

fun main(args: Array<String>) {
    val messageManagerMicroservice = HeartbeatMicroservice()
    messageManagerMicroservice.run()
}






//HeartbeatSocket = ServerSocket(HEARTBEAT_PORT)
//println("MessageManagerMicroservice se executa pe portul:${HeartbeatSocket.localPort}")
//println("Se asteapta conexiuni si mesaje...")
//while (true) {
//    val clientConnection = HeartbeatSocket.accept()
//    thread {
//        println("Subscriber conectat: ${clientConnection.inetAddress.hostAddress}:${clientConnection.port}")
//        synchronized(subscribers) {
//            subscribers[clientConnection.port] =
//                clientConnection
//            val bufferReader = BufferedReader(InputStreamReader(clientConnection.inputStream))
//
//            while (true) {
//
//                broadcastMessage("test_heartbeat test_heartbeat test_heartbeat", except = clientConnection.port)
//                println("Am trimis mesaj dummy")
//                val receivedMessage = bufferReader.readLine()
//                println("Am primit un raspuns")
//                if (receivedMessage == null) {
//                    println("Subscriber-ul ${clientConnection.port}  nu este activ.")
//                    synchronized(subscribers) {
//                        subscribers.remove(clientConnection.port)
//                    }
//                    bufferReader.close()
//                    clientConnection.close()
//                    break
//                }
//                Thread.sleep(5000)
//            }
//
//        }
//    }
//}
//
//}
//}