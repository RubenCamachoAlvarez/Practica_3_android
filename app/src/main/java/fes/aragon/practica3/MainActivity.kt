package fes.aragon.practica3

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.widget.doAfterTextChanged


class MainActivity : AppCompatActivity() {

    private var cambiosGuardados : Boolean = true

    private var nombreArchivo : String? = null

    private lateinit var abrirArchivoLauncher: ActivityResultLauncher<Intent>

    private lateinit var crearArchivoLauncher : ActivityResultLauncher<Intent>

    private lateinit var contenido : EditText

    private lateinit var titulo : TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val botonLeer = findViewById<Button>(R.id.botonLeer)

        val botonGuardar = findViewById<Button>(R.id.botonEscribir)

        this.contenido = findViewById<EditText>(R.id.contenido)

        this.titulo = findViewById<TextView>(R.id.titulo)

        abrirArchivoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            result -> if (result.resultCode == RESULT_OK) {

                val uri = result.data?.data

                uri?.let { leerContenidoArchivo(it) }

            }

        }

        crearArchivoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            result -> if(result.resultCode == RESULT_OK) {

                val uri = result.data?.data

                uri?.let { escribirContenidoArchivo(it) }

            }

        }

        contenido.doAfterTextChanged {

            botonGuardar.isEnabled = contenido.text.toString().isNotEmpty()

            this.cambiosGuardados = false

        }

        botonLeer.setOnClickListener {

            abrirArchivo()

        }

        botonGuardar.setOnClickListener {

            if(!this.cambiosGuardados) {



            }

            crearArchivo()

        }


    }

    private fun abrirArchivo() {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {

            addCategory(Intent.CATEGORY_OPENABLE)

            type = "text/plain"

        }

        this.abrirArchivoLauncher.launch(intent);

    }

    private fun crearArchivo() {

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {

            type = "text/plain"

            if(nombreArchivo != null) {

                putExtra(Intent.EXTRA_TITLE, nombreArchivo)

            }

        }

        crearArchivoLauncher.launch(intent)

    }

    private fun leerContenidoArchivo(uri : Uri) {

        contentResolver.openInputStream(uri)?.use { inputstream ->

            val contenidoArchivo = inputstream.bufferedReader().use { it.readText() }

            this.contenido.setText(contenidoArchivo)

            this.nombreArchivo = this.obtenerNombre(uri)

            this.titulo.text = this.nombreArchivo ?: "Escribe un mensaje"

        }

    }

    private fun escribirContenidoArchivo(uri: Uri) {

        contentResolver.openOutputStream(uri)?.use { outputstream ->

            outputstream.bufferedWriter().use { it.write(this.contenido.text.toString()) }

            this.nombreArchivo = this.obtenerNombre(uri)

            this.titulo.text = this.nombreArchivo ?: "Escribe un mensaje"

        }

    }

    private fun obtenerNombre(uri: Uri) : String? {

        var nombreArchivo: String? = null

        if(uri.scheme == "content") {

            Log.i("EMISION", "Me ejecuto")

            val cursor = contentResolver.query(uri, null, null, null, null)

            cursor?.use {

                if(it.moveToFirst()) {

                    nombreArchivo = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))

                }

            }

        }

        if(nombreArchivo == null) {

            nombreArchivo = uri.path?.let { path ->


                val indiceUltimoSlash = path.lastIndexOf("/")

                if (indiceUltimoSlash != -1) path.substring(indiceUltimoSlash + 1) else null

            }

        }


        return nombreArchivo

    }

}