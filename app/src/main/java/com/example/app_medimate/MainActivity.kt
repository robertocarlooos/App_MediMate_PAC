package com.example.app_medimate

import MedicamentoViewModel
import Repository
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.app_medimate.ViewModel.AlarmReceiver
import com.example.app_medimate.ViewModel.AuthSate
import com.example.app_medimate.ViewModel.AuthViewModel
import com.example.app_medimate.roomDb.MedicamentoDataBase
import com.example.app_medimate.roomDb.Medicamentos
import com.example.app_medimate.roomDb.Recordatorio
import com.example.app_medimate.ui.theme.App_MediMateTheme
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import java.util.Calendar


class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {

        }
    }

    private fun askNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {

            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {

            } else {

                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            MedicamentoDataBase::class.java,
            "medicamento.db"
        ).addMigrations(MedicamentoDataBase.MIGRATION_1_2).build()
    }
    private val viewModel by viewModels<MedicamentoViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MedicamentoViewModel(Repository(db)) as T
                }
            }
        }
    )

    val authViewModel by viewModels<AuthViewModel>()

    @SuppressLint("StringFormatInvalid")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solicitar permiso de notificaciones
        askNotificationPermission()

        // Obtener y registrar el token de FCM
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Obtener nuevo token de registro de FCM
            val token = task.result

            // Log y toast
            val msg = getString(R.string.msg_token_fmt, token)
            Log.d(TAG, msg)
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })



        enableEdgeToEdge()
        setContent {
            App_MediMateTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "firstScreen") {

                    composable("firstScreen"){
                        FirtsScreen(navController, viewModel = viewModel)

                    }
                    composable("login"){
                        Login(navController, viewModel = viewModel, authViewModel = AuthViewModel())

                    }
                    composable("signUp") {
                        SignUp(navController, viewModel = viewModel, authViewModel = AuthViewModel())

                    }

                    composable("tasks"){
                        TaskScreen(navController, viewModel = viewModel, authViewModel = AuthViewModel())
                    }
                    composable("myapp") {
                        MyAPP(navController, viewModel = viewModel)
                    }
                    composable("details") {
                        Details(navController, viewModel = viewModel)
                    }
                    composable("list") {
                        val medicamentos by viewModel.medicamentosFlow.collectAsState(initial = emptyList())
                        Listmedicamentos(navController, viewModel = viewModel)



                    }
                    composable("edit/{medicamentoId}") { backStackEntry ->
                        val medicamentoId = backStackEntry.arguments?.getString("medicamentoId")?.toIntOrNull()
                        medicamentoId?.let {
                            EditScreen(navController, viewModel, it)
                        }
                    }
                    composable("addReminder") {
                        AddReminderScreen(navController, viewModel, LocalContext.current )
                    }


                }
            }
        }
    }
}

@Composable
fun FirtsScreen(navController: NavController, modifier: Modifier = Modifier, viewModel: MedicamentoViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.fundo),
            contentDescription = "fundo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )


        // Imagen de la forma blanca
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Image(
                painter = painterResource(id = R.drawable.blanca),
                contentDescription = "blanco",
                modifier = Modifier
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Text(text = "Tome Os Seus Medicamentos \n          De Forma Eficaz",
                fontSize = 20.sp,
                color = Color.Blue,
                modifier = Modifier
                    .align(Alignment.TopCenter) // Alineación inicial
                    .offset(x = 16.dp, y = 80.dp)


            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.azul),
                    contentDescription = "azul",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
                TextButton(
                    onClick = {
                        navController.navigate("login")

                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = 0.dp, y = 10.dp)

                ) {
                    Text(text = "Entrar", color = Color.White, fontSize = 25.sp)

                }

            }



        }
    }


}

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun Login(navController: NavController, modifier: Modifier = Modifier, viewModel: MedicamentoViewModel,authViewModel: AuthViewModel) {
            // Estados de email y contraseña
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
             val authState = authViewModel.authState.observeAsState()
            val context = LocalContext.current
             LaunchedEffect(authState.value) {
             when(authState.value){
            is AuthSate.Authenticated -> navController.navigate("tasks")
            is AuthSate.Error -> Toast.makeText(context,
            (authState.value as AuthSate.Error).message,Toast.LENGTH_SHORT).show()

            else -> Unit
            }
             }


            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.fundo),
                    contentDescription = "fundo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.4f)) // Ajusta el alpha para la opacidad deseada
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.fundoazul),
                        contentDescription = "login azul",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Columna para los campos de entrada y botones de autenticación
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 150.dp), // Mueve la columna un poco más hacia arriba
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Login", fontSize = 32.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(20.dp))

                    // Box con fondo blanco detrás del campo de texto
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 60.dp, vertical = 6.dp)
                            .background(Color.White, shape = RoundedCornerShape(4.dp))
                            .padding(4.dp)
                    ) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text(text = "Email", color = Color.Black) },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                cursorColor = Color.Black,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp)) // Aumenta el espaciado entre los campos

                    // Box con fondo blanco detrás del campo de texto
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 60.dp, vertical = 6.dp)
                            .background(Color.White, shape = RoundedCornerShape(4.dp))
                            .padding(6.dp)
                    ) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(text = "Palavra-Passe", color = Color.Black) },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                cursorColor = Color.Black,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            authViewModel.login(email, password)

                        },
                        enabled = authState.value != AuthSate.Loading


                    ) {
                        Text(text = "Login")
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = {
                        navController.navigate("signup")

                    }) {
                        Text(text = "Não tens uma conta? Inscreve-te aqui!", color = Color.White)
                    }
                }
            }
        }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUp(navController: NavController, modifier: Modifier = Modifier, viewModel: MedicamentoViewModel,authViewModel: AuthViewModel) {
    // Estados de email y contraseña
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthSate.Authenticated -> navController.navigate("tasks")
            is AuthSate.Error -> Toast.makeText(context,
                (authState.value as AuthSate.Error).message,Toast.LENGTH_SHORT).show()

            else -> Unit
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.fundo),
            contentDescription = "fundo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.4f)) // Ajusta el alpha para la opacidad deseada
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Image(
                painter = painterResource(id = R.drawable.fundoazul),
                contentDescription = "login azul",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
        }

        // Columna para los campos de entrada y botones de autenticación
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 150.dp), // Mueve la columna un poco más hacia arriba
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Inscrição", fontSize = 32.sp, color = Color.White)
            Spacer(modifier = Modifier.height(20.dp))

            // Box con fondo blanco detrás del campo de texto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 60.dp, vertical = 6.dp)
                    .background(Color.White, shape = RoundedCornerShape(4.dp))
                    .padding(4.dp)
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(text = "Email", color = Color.Black) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = Color.Black,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(12.dp)) // Aumenta el espaciado entre los campos

            // Box con fondo blanco detrás del campo de texto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 60.dp, vertical = 6.dp)
                    .background(Color.White, shape = RoundedCornerShape(4.dp))
                    .padding(6.dp)
            ) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(text = "Palavra-Passe", color = Color.Black) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = Color.Black,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    authViewModel.signup(email, password)

                },
                enabled = authState.value != AuthSate.Loading


            ) {
                Text(text = "Criar conta")
            }
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = {
                navController.navigate("login")

            }) {
                Text(text = "Ja tens uma conta? Login.", color = Color.White)
            }
        }
    }
}


@Composable
fun TaskScreen(navController: NavController, modifier: Modifier = Modifier, viewModel: MedicamentoViewModel,authViewModel: AuthViewModel) {
    val authState = authViewModel.authState.observeAsState()
    LaunchedEffect(authState.value ) {
        when(authState.value){
            is AuthSate.UnAuthenticated -> navController.navigate("login")
            else -> Unit
        }
        
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD7D7D7))
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.c),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Text(
                text = "MediMate",
                color = Color.White,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }

        // Body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 120.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.Center)
                    .offset(x = 0.dp, y = -60.dp)
            ) {
                Text(
                    text = "Gerenciamento de medicamentos",
                    fontSize = 15.sp,
                    color = Color(0xFF2F4F7F),
                    textAlign = TextAlign.Center
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(50.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        navController.navigate("myapp") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.md),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Adicionar Medicamento", color = Color.White, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(19.dp))
                Button(
                    onClick = { navController.navigate("addReminder") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.rec),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recordatório Diário", color = Color.White, fontSize = 16.sp)
                    }
                }
                //signout


                Spacer(modifier = Modifier.height(19.dp))
                Button(
                    onClick = { navController.navigate("list") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ht),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver medicamentos", color = Color.White, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(50.dp))
                TextButton(onClick = {
                    authViewModel.signout()
                }) {

                    Text(text = "Sign out", fontSize = 20.sp)
                }

            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // FooterImage
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                painter = painterResource(id = R.drawable.b),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }


}
// MyAPP Composable
@Composable
fun MyAPP(navController: NavController, modifier: Modifier = Modifier, viewModel: MedicamentoViewModel) {
    var nomeMedicamento by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var frequencia by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD7D7D7))
    ) {
        Box (
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            contentAlignment = Alignment.Center

        ){
            Image(
                painter = painterResource(id = R.drawable.c),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Text(
                text = "MediMate",
                color = Color.White,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )



        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 120.dp)
        ) {
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.Center)
                    .offset(x = 0.dp, y = -70.dp)

            ){
                Text(
                    text = "Adicionar Medicamento",
                    fontSize = 18.sp,
                    color = Color(0xFF2F4F7F),
                    textAlign = TextAlign.Center
                )


            }
            Spacer(modifier = Modifier.height(20.dp))
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ){
                Text(
                    text = "Nome do Medicamento",
                    fontSize = 15.sp,
                    color = Color(0xFF2F4F7F)
                )
                TextField(

                    value = nomeMedicamento,
                    onValueChange = { nomeMedicamento = it },
                    placeholder = { Text(text = "name") },
                    modifier = Modifier


                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Dose",
                    fontSize = 15.sp,
                    color = Color(0xFF2F4F7F)
                )
                TextField(
                    value = dose,
                    onValueChange = { dose = it },
                    placeholder = { Text(text = "dose") }
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Frequência",
                    fontSize = 15.sp,
                    color = Color(0xFF2F4F7F)
                )
                TextField(
                    value = frequencia,
                    onValueChange = { frequencia = it },
                    placeholder = { Text(text = "frequencia") }
                )
                Spacer(modifier = Modifier.height(60.dp))
                Button(onClick = {
                    val medicamento = Medicamentos(
                        nomeMedicamento = nomeMedicamento,
                        dosis = dose,
                        frecuencia = frequencia.toInt()
                    )
                    viewModel.upsertMedicamento(medicamento)
                    navController.navigate("details")
                }) {
                    Text(text = "Medicamentos")

                }




            }


        }

    }
}


@Composable
fun Details(navController: NavController, modifier: Modifier = Modifier, viewModel: MedicamentoViewModel) {
    val medicamentos by viewModel.medicamentosFlow.collectAsState(initial = emptyList())
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD7D7D7))
    ) {
        // Image with text
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 100.dp) // Limit the height of the image to a maximum of 100dp
        ) {
            // Image
            Image(
                painter = painterResource(id = R.drawable.c),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Text "MediMate"
            Text(
                text = "MediMate",
                color = Color.White,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp)
            )
        }

        // Spacer between image and medication list
        Spacer(modifier = Modifier.height(20.dp))

        // Medication list or placeholder text
        if (medicamentos.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                medicamentos.forEach { medicamento ->
                    MedicamentoItem(medicamento = medicamento, onDelete = {
                        viewModel.deleteMedicamento(medicamento)
                    })
                    Divider(color = Color.Gray, thickness = 1.dp)
                }
            }
        } else {
            Text(
                text = "Nenhum medicamento encontrado",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { navController.navigate("myapp") },
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
                    .height(50.dp)
            ) {
                Text(text = "Adicionar Novo", fontSize = 16.sp)
            }
            Button(
                onClick = { navController.navigate("tasks") },
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
                    .height(50.dp)
            ) {
                Text(text = "Menu Principal", fontSize = 16.sp)
            }
        }
    }
}


@Composable
fun MedicamentoItem(medicamento: Medicamentos, onDelete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = "Nome: ${medicamento.nomeMedicamento}")
        Text(text = "Dose: ${medicamento.dosis}")
        Text(text = "Frequência: ${medicamento.frecuencia}")

        Button(
            onClick = { onDelete() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)

        ) {
            Text("Excluir Medicamento")
        }
    }
}
//falta implementar minutos e notificação
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    navController: NavController,
    viewModel: MedicamentoViewModel,
    context: Context
) {
    val medicamentos by viewModel.medicamentosFlow.collectAsState(initial = emptyList())
    val horaMap = remember { mutableStateMapOf<Int, String>() }
    val showTimeFieldMap = remember { mutableStateMapOf<Int, Boolean>() }
    val minutoMap = remember { mutableStateMapOf<Int, String>() }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD7D7D7))
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        // Lista de medicamentos en la parte superior
        Text("Seleccione un medicamento:", fontSize = 20.sp, color = Color.Black)
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(medicamentos.size) { index ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    val medicamento = medicamentos.get(index)
                    Text(text = medicamentos.get(index).nomeMedicamento, fontSize = 18.sp, color = Color.Black)

                    // Botón para adicionar hora
                    Button(
                        onClick = {
                            showTimeFieldMap[medicamento.id] = showTimeFieldMap[medicamento.id] != true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text("Adicionar Hora")
                    }

                    if (showTimeFieldMap[medicamento.id] == true) {

                        val state = rememberTimePickerState(is24Hour = true)

                        TimePicker(



                            state = state,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(onClick = {
                            val hora = state.hour
                            val minuto = state.minute




                            val recordatorio = Recordatorio(medicamentoId = medicamento.id, hora = hora.toString(),minutos = minuto.toString(),medicamento.frecuencia)
                            viewModel.insertRecordatorio(recordatorio)
                            showTimeFieldMap[medicamento.id] = false
                            horaMap[medicamento.id] = ""
                            minutoMap[medicamento.id] = ""

                            scheduleNotification(
                                context = context,
                                medicamento = medicamento.nomeMedicamento,
                                hour = hora,
                                minute = minuto,
                                frequency = medicamento.frecuencia
                            )

                        }) {
                            Text("Guardar Hora")
                        }
                    }

                }
                Divider(color = Color.Gray, thickness = 1.dp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            Button(
                onClick = { navController.navigate("tasks") },
                modifier = Modifier
                    .padding(8.dp)

                    .height(50.dp)
            ) {
                Text(text = "Menu Principal", fontSize = 16.sp)
            }
        }
    }

}
fun scheduleNotification(
    context: Context,
    medicamento: String,
    hour: Int,
    minute: Int,
    frequency: Int
) {
/*
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("medicamento", medicamento)
        putExtra("hour", hour)
        putExtra("minute", minute)
        putExtra("frequency", frequency)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
    }


    alarmManager.setRepeating(
        AlarmManager.RTC_WAKEUP,
       0L,
        (frequency ).toLong(),
        pendingIntent
    )
    */
    val calendar = Calendar.getInstance()
    calendar[Calendar.HOUR_OF_DAY] = hour
    calendar[Calendar.MINUTE] = minute
    calendar[Calendar.SECOND] = 0

    val intent = Intent(
        context,
        AlarmReceiver::class.java
    )
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

    val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager?
    alarmManager?.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
}

@Composable
fun Listmedicamentos( navController: NavController, modifier: Modifier = Modifier, viewModel: MedicamentoViewModel) {
    val medicamentos by viewModel.medicamentosFlow.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD7D7D7))
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        medicamentoslist(medicamentos=medicamentos, viewModel = viewModel, navController = navController)


        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { navController.navigate("myapp") },
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
                    .height(50.dp)
            ) {
                Text(text = "Adicionar Novo", fontSize = 16.sp)
            }
            Button(
                onClick = { navController.navigate("tasks") },
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
                    .height(50.dp)
            ) {
                Text(text = "Menu Principal", fontSize = 16.sp)
            }
        }

    }
}

@Composable
fun medicamentoslist(navController: NavController,modifier: Modifier = Modifier, medicamentos: List<Medicamentos>, viewModel: MedicamentoViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD7D7D7))
    ) {
        // Image with text
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
                    contentAlignment = Alignment.Center

        ) {

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = { navController.navigate("tasks") },
                    modifier = Modifier
                        .size(48.dp) // Adjust size as needed
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back to Menu",
                        tint = Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))


        if (medicamentos.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                medicamentos.forEach { medicamento ->
                    Med(navController = navController, medicamento = medicamento)
                    Divider(color = Color.Gray, thickness = 1.dp)
                }
            }
        } else {
            Text(
                text = "Nenhum medicamento encontrado",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

}
@Composable
fun Med(navController: NavController,medicamento: Medicamentos) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = "Nome: ${medicamento.nomeMedicamento}")
        Text(text = "Dose: ${medicamento.dosis}")
        Text(text = "Frequência: ${medicamento.frecuencia}")

        Button(
            onClick = {  navController.navigate("edit/${medicamento.id}") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)

        ) {
            Text("Editar medicamcento")
        }
    }
}
@Composable
fun EditScreen(navController: NavController, viewModel: MedicamentoViewModel, medicamentoId: Int) {
    val medicamento by viewModel.getMedicamentoById(medicamentoId).collectAsState(initial = null)

    var nomeMedicamento by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var frequencia by remember { mutableStateOf("") } // Mantener como String para el TextField

    LaunchedEffect(medicamento) {
        medicamento?.let {
            nomeMedicamento = it.nomeMedicamento
            dose = it.dosis
            frequencia = it.frecuencia.toString() // Convertir a String para el TextField
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD7D7D7))
            .padding(16.dp)
    ) {

        Spacer(modifier = Modifier.height(20.dp))
        Spacer(modifier = Modifier.height(100.dp))
        TextField(
            value = nomeMedicamento,
            onValueChange = { nomeMedicamento = it },
            label = { Text("Nome do Medicamento") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = dose,
            onValueChange = { dose = it },
            label = { Text("Dose") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = frequencia,
            onValueChange = { frequencia = it },
            label = { Text("Frequência") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number) // Asegura que el teclado sea numérico
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            val frequenciaInt = frequencia.toIntOrNull() ?: 0 // Manejar conversión a Int
            viewModel.upsertMedicamento(Medicamentos(
                id = medicamentoId,
                nomeMedicamento = nomeMedicamento,
                dosis = dose,
                frecuencia = frequenciaInt
            ))
            navController.navigate("list")
        }) {
            Text("Salvar")
        }
    }
}

