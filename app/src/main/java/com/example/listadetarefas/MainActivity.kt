package com.example.listadetarefas

import android.content.Intent
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var helper: AudioHelper
    private lateinit var tts: TextToSpeech

    private lateinit var btnVerificar: Button
    private lateinit var btnMensagem: Button
    private lateinit var btnVoz: Button
    private lateinit var btnAlerta: Button

    private lateinit var editMensagem: EditText

    private lateinit var txtEventos: TextView
    private lateinit var txtHistorico: TextView

    private val historico = StringBuilder()

    private var totalEventos = 0

    private val reconhecimentoVoz =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode == RESULT_OK) {

                val resultados =
                    result.data?.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS
                    )

                if (!resultados.isNullOrEmpty()) {

                    val comando =
                        resultados[0].lowercase(Locale.getDefault())

                    adicionarEvento("Comando de voz: $comando")

                    processarComando(comando)

                }

            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        helper = AudioHelper(this)

        tts = TextToSpeech(this, this)

        btnVerificar = findViewById(R.id.btnVerificar)
        btnMensagem = findViewById(R.id.btnMensagem)
        btnVoz = findViewById(R.id.btnVoz)
        btnAlerta = findViewById(R.id.btnAlerta)

        editMensagem = findViewById(R.id.editMensagem)

        txtEventos = findViewById(R.id.txtEventos)
        txtHistorico = findViewById(R.id.txtHistorico)


        adicionarEvento("Aplicativo iniciado.")

        adicionarEvento(
            "Assistente Doma pronto para uso."
        )

        helper.getAudioManager().registerAudioDeviceCallback(

            object : AudioDeviceCallback() {

                override fun onAudioDevicesAdded(
                    addedDevices: Array<out AudioDeviceInfo>
                ) {

                    adicionarEvento(
                        "Dispositivo de áudio conectado."
                    )

                    falar(
                        "Dispositivo de áudio conectado."
                    )

                }

                override fun onAudioDevicesRemoved(
                    removedDevices: Array<out AudioDeviceInfo>
                ) {

                    adicionarEvento(
                        "Dispositivo de áudio removido."
                    )

                    falar(
                        "Dispositivo de áudio removido."
                    )

                }

            },

            null

        )

        btnVerificar.setOnClickListener {

            verificarDispositivos()

        }

        btnMensagem.setOnClickListener {

            val mensagem =
                editMensagem.text.toString().trim()

            if (mensagem.isEmpty()) {

                adicionarEvento(
                    "Nenhuma mensagem foi digitada."
                )

                falar(
                    "Digite uma mensagem primeiro."
                )

            } else {

                adicionarEvento(
                    "Mensagem reproduzida."
                )

                falar(mensagem)

            }

        }

        btnAlerta.setOnClickListener {

            adicionarEvento(
                "Alerta de segurança emitido."
            )

            falar(
                "Atenção. Alerta de segurança. Procure a saída de emergência mais próxima."
            )

        }

        btnVoz.setOnClickListener {

            iniciarReconhecimentoVoz()

        }

    }

    private fun adicionarEvento(texto: String) {

        val hora = SimpleDateFormat(
            "HH:mm:ss",
            Locale.getDefault()
        ).format(Date())

        totalEventos++

        txtEventos.text = "Eventos registrados: $totalEventos"

        historico.append("[$hora] $texto\n")

        txtHistorico.text = historico.toString()



    }
    private fun verificarDispositivos() {

        adicionarEvento("Iniciando verificação dos dispositivos.")

        if (helper.possuiAltoFalante()) {

            adicionarEvento("Alto-falante disponível.")

            falar("Alto-falante disponível.")

        } else {

            adicionarEvento("Alto-falante não encontrado.")

            falar("Alto-falante não encontrado.")

        }

        if (helper.bluetoothConectado()) {

            adicionarEvento("Fone Bluetooth conectado.")

            falar("Bluetooth conectado.")

        } else {

            adicionarEvento("Bluetooth não conectado.")

            falar("Bluetooth não encontrado.")

            adicionarEvento("Abrindo configurações Bluetooth.")

            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            )

            intent.putExtra("EXTRA_CONNECTION_ONLY", true)
            intent.putExtra("EXTRA_CLOSE_ON_CONNECT", true)
            intent.putExtra(
                "android.bluetooth.devicepicker.extra.FILTER_TYPE",
                1
            )

            startActivity(intent)

        }

        adicionarEvento("Verificação concluída.")

    }

    private fun iniciarReconhecimentoVoz() {

        val intent =
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            "pt-BR"
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            "Fale um comando"
        )

        reconhecimentoVoz.launch(intent)

    }

    private fun processarComando(comando: String) {

        when {

            comando.contains("bluetooth") -> {

                falar("Verificando Bluetooth.")

                verificarDispositivos()

            }

            comando.contains("mensagem") -> {

                val texto =
                    editMensagem.text.toString()

                if (texto.isBlank()) {

                    falar("Nenhuma mensagem foi digitada.")

                    adicionarEvento(
                        "Tentativa de leitura sem mensagem."
                    )

                } else {

                    falar(texto)

                    adicionarEvento(
                        "Mensagem lida por comando de voz."
                    )

                }

            }

            comando.contains("alerta") -> {

                falar(
                    "Atenção. Alerta de segurança. Procure a saída de emergência mais próxima."
                )

                adicionarEvento(
                    "Alerta emitido por comando de voz."
                )

            }

            else -> {

                falar("Comando não reconhecido.")

                adicionarEvento(
                    "Comando não reconhecido."
                )

            }

        }

    }

    private fun falar(texto: String) {

        if (::tts.isInitialized) {

            tts.speak(
                texto,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "DOMA"
            )

        }

    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            tts.language = Locale("pt", "BR")

            adicionarEvento(
                "Sistema de voz inicializado."
            )

            falar(
                "Assistente Doma iniciado."
            )

        } else {

            adicionarEvento(
                "Erro ao inicializar o sistema de voz."
            )

        }

    }

    override fun onDestroy() {



        if (::tts.isInitialized) {

            tts.stop()

            tts.shutdown()

        }

        super.onDestroy()

    }

}