package com.example.td_test_2

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.td_test_2.database.entity.WordEntity
import com.example.td_test_2.database.room.json.Loadjson
import com.example.td_test_2.database.sqldb.DatabaseTable
import com.example.td_test_2.databinding.ActivityEnterDataBinding
import org.json.JSONException

class EnterDataActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEnterDataBinding
    private var mHospitalName: EditText? = null
    private var mDoctorName: EditText? = null
    private var mTranscript: EditText? = null
    private var mAddButton: Button? = null
    private var mToast: Toast? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnterDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        DatabaseTable.getInstance(this)
        title = "Enter Data"
        mHospitalName = findViewById(R.id.et_hospital_name)
        mDoctorName = findViewById(R.id.et_doctor_name)
        mTranscript = findViewById(R.id.et_transcript)
        mAddButton = findViewById(R.id.bt_submit_data)
        binding.btSubmitData.setOnClickListener {
            insertDataset(this)
        }

    }

    private inner class AddFakeDataTask :
        AsyncTask<Void?, Void?, Void?>() {
        override fun onPostExecute(aVoid: Void?) {
            addToast("Entries added")
            clearInputs()
        }

        override fun doInBackground(vararg params: Void?): Void? {
            addInfo()
            return null
        }
    }

    private inner class AddDataTask :
        AsyncTask<Void?, Void?, Long>() {

        override fun onPostExecute(result: Long) {
            val idString = result.toString()
            addToast("New row with id: $idString")
            clearInputs()
        }

        override fun doInBackground(vararg params: Void?): Long {
            return storeInfo()
        }
    }

    private fun insertDataset(
        context: Context,
    ){
        val db: DatabaseTable = DatabaseTable.getInstance(baseContext)!!
        val pimaArray = Loadjson.loadDiabeticJson(context)
        try {
            if (pimaArray != null){
                for (i in 0 until pimaArray.length()){
                    val item = pimaArray.getJSONObject(i)
//                    WordEntity(
//                        id = 0,
//                        type = item.getString("type"),
//                        sentence = item.getString("sentence"),
//                        result = item.getString("result")
//                    )
                    db.addNewEntry(
                        tipe = item.getString("tipe"),
                        pattern = item.getString("kalimat"),
                        answer = item.getString("result")
                    )
                }
            }
        }catch (e : JSONException){
            Log.d("roomDb",e.message.toString())
            e.printStackTrace()
        }
    }

    private fun addInfo() {
        val i = 0
        val db: DatabaseTable = DatabaseTable.getInstance(baseContext)!!
        // DOCTOR | HOSPITAL | TRANSCRIPT

        /* PARTICIPANTE 1
        // A
        db.addNewEntry("Dr. João", "Hospital de São João", "Consulta de medicina geral devido a uma dor de garganta. Foi receitado brufen.");
        // B
        db.addNewEntry("Dra. Marta","Hospital de São João","Consulta de medicina geral por crise de alergia. Foi receitado brufen e anti-alérgico.");
        // C
        db.addNewEntry("Dr. João","Hospital da Maia","Consulta de medicina dentária devido a dor de dentes.");
        // D
        db.addNewEntry("Dra. Ana","Hospital da Maia","Consulta de pediatria.");
        // E
        db.addNewEntry("Dra. Marta","Hospital da Trofa","Consulta de medicina geral devido a constipação e febre. Foi receitado brufen e benuron.");
        // F
        db.addNewEntry("Dr. Joao","Hospital da Trofa","consulta de medicina dentária. Foi arrancado um dente.");
        // G
        db.addNewEntry("Dr. Pedro","Hospital de São João","consulta de dermatologia. Irritação na pela e vermelhidão.");
        // H
        db.addNewEntry("Dr. Neto","Hospital de São João","consulta de dermatologia.");
        // I
        db.addNewEntry("Dra. Sara","Hospital da Trofa","Consulta de ortopedia devido a dor no joelho. Aconselhada cirurgia.");
        // J
        db.addNewEntry("Dra. Sara","Hospital da Trofa","consulta de ortopedia. Receitado analgésico.");
        */

        /* PARTICIPANTE 2
        // A
        db.addNewEntry("Dr Paulo Ferreira","Hospital da Luz","Queixas: dores no peito, febre, tosse. Foram receitados os seguintes medicamentos: Brufen e Benuron , Amoxicilina e xarope para a tosse.");
        // B
        db.addNewEntry("Dra Joana Almeida","Hospital de S. João","Queixas: dores nas costas. Foram receitados os seguintes medicamentos: Relmus, Valium.");
        // C
        db.addNewEntry("Dr João Francisco","Centro de saúde","Queixas: rouquidão, dores de garganta. Foram receitados os seguintes medicamentos: Brufen, Strepsils.");
        // D
        db.addNewEntry("Dr Gabriel Santos","Hospital da Luz","Queixas: Vertigens. Foram prescritos exames ao ouvido e receitados os medicamentos: Betaserc e Enjomin.");
        // E
        db.addNewEntry("Dr João Francisco","Centro de Saúde","Consulta de rotina. Foram pedidas análises e medida a tensão arterial. Foi receitado o princípio ativo Telmisartan+hidroclorotiazida para controle da tensão arterial.");
        // F
        db.addNewEntry("Dra Margarida Teles","Hospital da Luz","Queixas: dores a urinar e febre. Diagnóstico: infeção urinária. Foi receitada Fosfomicina.");
        // G
        db.addNewEntry("Dr João Francisco","Centro de Saúde","Consulta de rotina. Foram mostradas as análises e receitada medicação para o colestrol: Atorvastina. Também foi avaliada a tensão arterial - estava bem.");
        // H
        db.addNewEntry("Dr Manuel Pericão","Hospital de S. João","Queixas: náuseas e vómitos. Foi receitado Primperam.");
        // I
        db.addNewEntry("Dr Abel Nascimento","Hospital da Luz","Queixas: febre e dores no corpo. Foi receitado Brufen.");
        // J
        db.addNewEntry("Dr João Francisco","Centro de Saúde","Consulta de rotina. Continuar a medicação para a tensão arterial e colestrol.");
        */

        /* PARTICIPANTE 3
        // A
        db.addNewEntry("Maria José","Hospital de santa Maria","Consulta de dermatologia. Eqzema no hipocondrico esquerdo. Fazer pomada nessa região");
        // B
        db.addNewEntry("Maria António José","Centro de saúde","Dores no hipocondrico direito. Tramadol duas vezes por dia");
        // C
        db.addNewEntry("António Costa","Hospital de São José","Dor de cabeça. Tomar tramadol duas vezes por dia.");
        // D
        db.addNewEntry("Maria José","Hospital de Santo António","Dermatologia, prurido no hipocondrico esquerdo. Pomada na região.");
        // E
        db.addNewEntry("Maria Costa","Hospital de santa Maria","Dor de cabeça. Tramadol duas vezes por semana e pomada na região.");
        // F
        db.addNewEntry("Joana Costa","Hospital de São José","Intoxicação por tramadol. Pára de tomar tramadol e continuar com pomadas. Inicia toma de metadona.");
        // G
        db.addNewEntry("José da Costa","Hospital de Santo André","Dor no hipocondrico esquerdo, toma metadona e faz pomada na região.");
        // H
        db.addNewEntry("André Maria da Costa","Centro de saúde","Avaliação da intoxicação por tramadol e dor de estômago. Dores devido á toma de metadona. Pára metadona, continua as pomadas e inicia toma de bupremorfina.");
        // I
        db.addNewEntry("Maria José","Hospital de Santo António","Dor de estômago. Realização de ecografia abdominal com alterações no hipocondrico esquerdo. Pára á toma de bupremorfina e faz lavagem de estômago e internamento hospitalar para estabilizar.");
        // J
        db.addNewEntry("André Maria","Hospital São José","Dores no hipocondrico direito. Toma metadona e bupremorfina.");
        */

        /* PARTICIPANTE 4
        // A
        db.addNewEntry("António Pereira","Centro de Saúde de Rio Tinto","Dores de Cabeça. Benuron 3 vezes ao dia.");
        // B
        db.addNewEntry("Pedro Mota","Centro de Saúde da Maia","Dores na perna esquerda. Brufen de manhã e à noite.");
        // C
        db.addNewEntry("Clara Lopes","Hospital de São Mamede","Dificuldades de sono. Xanax uma hora antes de ir para a cama.");
        // D
        db.addNewEntry("Susana Figueiredo","Centro de Saúde de Fânzeres","Cancro do Nariz. Marijuana medicial após cada refeição.");
        // E
        db.addNewEntry("Marco Paulo","Centro de Saúde do Porto","Dores de barriga. Benuron 2 vezes ao dia.");
        // F
        db.addNewEntry("Liliana Sousa","Hospital de Gaia","Dores no Segundo Molar Superior Esquerdo. Benuron de 6 em 6 horas durante um período de 5 dias.");
        // G
        db.addNewEntry("Maria Pinto","Centro de Saúde de Baião","Dor de ouvidos. Brufen de 8 em 8 horas durante um período de 4 dias.");
        // H
        db.addNewEntry("Edgar Passos","Centro de Sa´de de Leça da Palmeira","Dificuldades extremas de visão. Reencaminhamento para Oftalmologia.");
        // I
        db.addNewEntry("Viviana Santos Ferreira","Centro de Cuidados Paleativos de Trás os Montes e Alto Douro","Cegueira crónica. Nada a acrescentar.");
        // J
        db.addNewEntry("Pedro Fernandes Domingues","Centro de Saúde de Ermesinde","Joanetes inchados. Aplicar Bepanthene com movimentos circulares de manhã e à noite.");
        */

        /* PARTICIPANTE 5 */
        // A
        db.addNewEntry(
            "Pedro Silva",
            "Hospital de Dia da Maia",
            "Foi diagnosticada uma possível micro-rotura na face anterior da coxa esquerda. Foi pedida uma ecografia e receitado o anti-inflamatório Voltaren."
        )
        // B
        db.addNewEntry(
            "Ana Silva",
            "Hospital da Luz",
            "Foi diagnosticada uma luxação no ombro direito. Foi marcado um TAC e receitado o Feldene."
        )
        // C
        db.addNewEntry(
            "Júlio Santos",
            "Hospital da Trofa",
            "O paciente apresentava febre alta tendo sido receitado Brufen."
        )
        // D
        db.addNewEntry(
            "Manuela Silva",
            "Clínica Obstétrica Doutora Delfina Leite",
            "A paciente apresentava dores abdominais agudas tendo sido receitado Nurofen. "
        )
        // E
        db.addNewEntry(
            "José Bessa",
            "Hospital de S. João",
            "O paciente apresentava uma fratura no pulso direito tendo sido engessado. Foi receitado o Ben-U-Ron para alívio das dores. "
        )
        // F
        db.addNewEntry(
            "Inês Nunes",
            "Hospital de Santo António",
            "O paciente apresentava febre alta tendo sido receitado Brufen."
        )
        // G
        db.addNewEntry(
            "Inês Nunes",
            "Hospital de Santo António",
            "O paciente apresentava dores no maxilar inferior tendo sido reencaminhado para um médico dentista."
        )
        // H
        db.addNewEntry("Miguel Barbosa", "Hospital de S. João", "Foi marcada uma endoscopia alta.")
        // I
        db.addNewEntry(
            "Fábio Costa",
            "Hospital de Dia da Maia",
            "O paciente apresentava dores no maxilar após uma endoscopia tenso sido receitado Brufen."
        )
        // J
        db.addNewEntry(
            "Carlos Fernandes",
            "Hospital de S. João",
            "O paciente apresentava uma fratura no pulso. Foi receitado Feldene."
        )
    }

    // Function to store data and show a Toast (run in the different thread)
    private fun storeInfo(): Long {
        val hospital = mHospitalName!!.text.toString()
        val doctor = mDoctorName!!.text.toString()
        val transcript = mTranscript!!.text.toString()
        return if (hospital == "" || doctor == "" || transcript == "") -1 else DatabaseTable.getInstance(
            baseContext
        )?.addNewEntry(doctor, hospital, transcript)!!
    }

    // Function to clear text inputs
    private fun clearInputs() {
        mHospitalName!!.text.clear()
        mDoctorName!!.text.clear()
        mTranscript!!.text.clear()
        mTranscript!!.clearFocus()
    }

    // Function to display a new toast when data is inserted
    private fun addToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }
}