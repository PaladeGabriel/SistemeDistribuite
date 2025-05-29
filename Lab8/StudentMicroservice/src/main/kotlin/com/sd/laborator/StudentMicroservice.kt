package com.sd.laborator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.Exception
import java.net.Socket
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class StudentMicroservice {
    // intrebarile si raspunsurile sunt mentinute intr-o lista de perechi de forma:
    // [<INTREBARE 1, RASPUNS 1>, <INTREBARE 2, RASPUNS 2>, ... ]
    private lateinit var questionDatabase: MutableList<Pair<String, String>>
    private lateinit var messageManagerSocket: Socket
    private lateinit var HeartbeatSocket: Socket

    init {
        val databaseLines: List<String> = File("questions_database.txt").readLines()
        questionDatabase = mutableListOf()
        /*
    "baza de date" cu intrebari si raspunsuri este de forma:
    <INTREBARE_1>\n
    <RASPUNS_INTREBARE_1>\n
    <INTREBARE_2>\
     <RASPUNS_INTREBARE_2>\n
    ...
    */
        for (i in 0..(databaseLines.size - 1) step 2) {
            questionDatabase.add(Pair(databaseLines[i], databaseLines[i + 1]))
        }
    }

    companion object Constants {
        // pentru testare, se foloseste localhost. pentru deploy, server-ul socket (microserviciul MessageManager) se identifica dupa un "hostname"
// acest hostname poate fi trimis (optional) ca variabila de mediu
        val MESSAGE_MANAGER_HOST = System.getenv("MESSAGE_MANAGER_HOST") ?: "localhost"
        val HEARTBEAT_HOST = System.getenv("HEARTBEAT_HOST") ?: "localhost"
        const val MESSAGE_MANAGER_PORT = 1500
        const val HEARTBEAT_PORT = 56789
    }

    private fun subscribeToMessageManager() {
        try {
            messageManagerSocket = Socket(MESSAGE_MANAGER_HOST, MESSAGE_MANAGER_PORT)
            println("M-am conectat la MessageManager!")
        } catch (e: Exception) {
            println("Nu ma pot conecta la MessageManager!")
            exitProcess(1)
        }
    }

    private fun subscribeToHeartbeat() {
        try {
            HeartbeatSocket = Socket(HEARTBEAT_HOST, HEARTBEAT_PORT)
            println("M-am conectat la Heartbeat!")
        } catch (e: Exception) {
            println("Nu ma pot conecta la Heartbeat!")
            exitProcess(1)
        }
    }

    private fun respondToQuestion(question: String): String? {
        questionDatabase.forEach {
// daca se gaseste raspunsul la intrebare, acesta este returnat apelantului
            if (it.first == question) {
                return it.second
            }
        }
        return null
    }

    suspend fun run() = coroutineScope {
// microserviciul se inscrie in lista de "subscribers" de la MessageManager prin conectarea la acesta
        subscribeToMessageManager()
        subscribeToHeartbeat()
//        thread {
//            println("Deschid thread heartbeat")
//            try {
//                val heartbeatReader = BufferedReader(InputStreamReader(HeartbeatSocket.getInputStream()))
//                while (true) {
//                    val heartbeatSignal = heartbeatReader.readLine()
//                    if (heartbeatSignal == null) {
//                        println("Nu s-a putut conecta la Heartbeat")
//                        HeartbeatSocket.close()
//                        break
//                    } else {
//                        println("Conectate la heartbeat: $heartbeatSignal")
//                    }
//
//                    val (messageType, messageDestination, messageBody) = heartbeatSignal.split(" ", limit = 3)
//                    val responseToQuestion="raspuns_test_heartbeat"
//                    var responseToQuestion2 = "raspuns $messageDestination $responseToQuestion "
//                    println("Trimit raspunsul:\"${heartbeatSignal}\"")
//                    HeartbeatSocket.getOutputStream()
//                        .write((responseToQuestion2 + "\n").toByteArray())
//
//
//                }
//            } catch (e: Exception) {
//                println("Eroare la primirea heartbeat-ului: ${e.message}")
//            }
//        }
        println("StudentMicroservice se executa pe portul:${messageManagerSocket.localPort}")
        println("Se asteapta mesaje...")
        val bufferReader = BufferedReader(InputStreamReader(messageManagerSocket.inputStream))
        val bufferReader2 = BufferedReader(InputStreamReader(HeartbeatSocket.inputStream))
        // se asteapta intrebari trimise prin intermediarul"MessageManager"
        launch(Dispatchers.IO){
            while (true) {
                val response2 = bufferReader2.readLine()
                if (response2 == null) {
                    // daca se primeste un mesaj gol (NULL), atunci inseamna ca cealalta parte a socket-ului a fost inchisa
                    println("Microserviciul MessageService(${messageManagerSocket.port}) a fost oprit.")
                    bufferReader.close()
                    messageManagerSocket.close()
                    break;
                }
                println("Am pornit thread de heaertbeat")
                val (messageType, messageDestination, messageBody) = response2.split(" ", limit = 3)
                val responseToQuestion = "raspuns_test_heartbeat"
                var responseToQuestion2 = "raspuns $messageDestination $responseToQuestion "
                println("Trimit raspunsul:\"${response2}\"")
                HeartbeatSocket.getOutputStream().write((responseToQuestion2 + "\n").toByteArray())
            }
        }
        launch(Dispatchers.IO){
            while (true) {
                val response = bufferReader.readLine()
                if (response == null) {
                    // daca se primeste un mesaj gol (NULL), atunci inseamna ca cealalta parte a socket-ului a fost inchisa
                    println("Microserviciul MessageService(${messageManagerSocket.port}) a fost oprit.")
                    bufferReader.close()
                    messageManagerSocket.close()
                    break;
                }
                val (messageType, messageDestination, messageBody) = response.split(" ", limit = 3)
                when (messageType) {
                    // tipul mesajului cunoscut de acest microserviciu este de forma:
                    // intrebare <DESTINATIE_RASPUNS> <CONTINUT_INTREBARE>
                    "intrebare" -> {
                        println("Am primit o intrebare de la $messageDestination: \"${messageBody}\"")
                        var responseToQuestion = respondToQuestion(messageBody)
                        if (responseToQuestion == null) {
                            var responseToQuestion2 = "intrebare_student $messageDestination $responseToQuestion "
                            println("Trimit mai departe intrabarea:\"${response}\"")
                            messageManagerSocket.getOutputStream().write((responseToQuestion2 + "\n").toByteArray())
                        } else {
                            var responseToQuestion2 = "raspuns $messageDestination $responseToQuestion "
                            println("Trimit raspunsul:\"${response}\"")
                            messageManagerSocket.getOutputStream().write((responseToQuestion2 + "\n").toByteArray())
                        }
                    }
                }
            }
        }

    }
}


suspend fun main()
{
    val studentMicroservice = StudentMicroservice()
    studentMicroservice.run()
}
