package com.example.dataclassteamproject

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.dataclassteamproject.ui.theme.DataClassTeamProjectTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.FirebaseDatabase.getInstance
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate

fun saveUserStatusToFirestore(userId: String, status: String) {
    val db = FirebaseFirestore.getInstance()
    val userStatus = hashMapOf("status" to status)
    db.collection("users").document(userId).set(userStatus)
        .addOnSuccessListener {
            Log.d("Firestore", "User status successfully written!")
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error writing user status", e)
        }
}

fun getUserStatusFromFirestore(userId: String, onStatusFetched: (String) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val docRef = db.collection("users").document(userId)
    docRef.get()
        .addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val status = document.getString("status") ?: "미팅 중"
                onStatusFetched(status)
            } else {
                Log.d("Firestore", "No such document")
            }
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error getting user status", e)
        }
}


class MainActivity : ComponentActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val pickImageRequestCode = 101
    var _imageBitmap: MutableState<ImageBitmap?> = mutableStateOf(null)

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri = result.data?.data
                val rotationAngle = getRotationAngle(selectedImageUri!!)
                val androidBitmap =
                    MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)
                val rotatedBitmap = androidBitmap.rotate(rotationAngle)
                _imageBitmap.value = rotatedBitmap.asImageBitmap()
            }
        }

    // 이미지 회전 각도를 얻는 함수 추가
    private fun getRotationAngle(uri: Uri): Int {
        // 여기서 URI를 사용하여 회전 각도를 반환하는 로직을 작성하세요
        // 예: MediaStore에서 EXIF 메타데이터를 사용하여 회전 각도 얻기
        return 0 // 임시로 0 반환
    }

    // 비트맵 이미지 회전 함수 추가
    private fun Bitmap.rotate(angle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        // 구글 로그인 구현
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // default_web_client_id 에러 시 rebuild
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

//        FirebaseApp.initializeApp(this)
        setContent {

            DataClassTeamProjectTheme {

                val navController = rememberNavController()

                val user: FirebaseUser? = mAuth.currentUser

                val startDestination = remember {
                    if (user == null) {
                        "login"
                    } else {
                        "home"
                    }
                }
                val signInIntent = googleSignInClient.signInIntent
                val launcher =
                    rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
                        val data = result.data
                        // result returned from launching the intent from GoogleSignInApi.getSignInIntent()
                        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                        val exception = task.exception
                        if (task.isSuccessful) {
                            try {
                                // Google SignIn was successful, authenticate with firebase
                                val account = task.getResult(ApiException::class.java)!!
                                firebaseAuthWithGoogle(account.idToken!!)
                                navController.popBackStack()
                                navController.navigate("home")
                            } catch (e: Exception) {
                                // Google SignIn failed
                                Log.d("SignIn", "로그인 실패")
                            }
                        } else {
                            Log.d("SignIn", exception.toString())
                        }
                    }

                //작업하시는 화면으로 startDestination해주시면 됩니다
                NavHost(navController = navController, startDestination = startDestination) {
                    composable("home") {
                        HomeScreen(navController)
                    }
                    composable("timer") {
                        TimerScreen(navController)
                    }
                    composable("dm") {
                        DmScreen(navController)
                    }
                    composable("schedule") {
                        ScheduleScreen(navController)
                    }
                    composable("personal") {
                        PersonalInfoScreen(
                            navController,
                            onClicked = { signOut(navController) },
                            _imageBitmap = _imageBitmap,
                            onPickImage = { }
                        )
                    }
                    BoardViewsScreens(navController, mAuth)
                    ChattingScreens(navController, mAuth)
                    composable("login") {
                        LoginScreen(
                            signInClicked = {
                                launcher.launch(signInIntent)
                            })
                    }
                    composable("homeScreenRoute") {
                        HomeScreen(navController)
                    }
                    composable("lunchMenuScreenRoute") {
                        LunchMenuScreen(navController)
                    }
                    composable("details") {
                        DetailsScreen(navController)
                    }
                    composable("next_destination/{selectedMenu}") { backStackEntry ->
                        val selectedMenu = backStackEntry.arguments?.getString("selectedMenu") ?: ""
                        NextScreen(navController, selectedMenu)
                    }
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // SignIn Successful
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                } else {
                    // SignIn Failed
                    Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signOut(navController: NavController) {
        // get the google account
        val googleSignInClient: GoogleSignInClient

        // configure Google SignIn
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Sign Out of all accounts
        mAuth.signOut()
        googleSignInClient.signOut().addOnSuccessListener {
            Toast.makeText(this, "로그아웃 성공", Toast.LENGTH_SHORT).show()
            navController.navigate("login")
        }.addOnFailureListener {
            Toast.makeText(this, "로그아웃 실패", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun NavGraphBuilder.ChattingScreens(navController: NavHostController, mAuth: FirebaseAuth) {
    composable("workspace") {
        ChattingScreen(navController, mAuth, "workspace")
    }
    composable("teamproject") {
        ChattingScreen(navController, mAuth, "teamproject")
    }
    composable("playground") {
        ChattingScreen(navController, mAuth, "playground")
    }
}

private fun NavGraphBuilder.BoardViewsScreens(navController: NavHostController, mAuth: FirebaseAuth) {
    composable("notice") {
        BoardViewScreen(navController, "notice", "makeNoticeBoard", "공지사항")
    }
    composable("makeNoticeBoard") {
        MakeBoardScreen(navController, mAuth, "notice")
    }
    composable("my") {
        BoardViewScreen(navController, "my", "makeMyBoard", "내게식판")
    }
    composable("makeMyBoard") {
        MakeBoardScreen(navController, mAuth, "my")
    }
    composable("idontknow") {
        BoardViewScreen(navController, "idontknow", "makeIDontKnowBoard", "여기다뭘해야될까요")
    }
    composable("makeIDontKnowBoard") {
        MakeBoardScreen(navController, mAuth, "idontknow")
    }
}
@Composable
fun GoogleSignInButton(
    signInClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable { signInClicked() },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "login With Google",
            modifier = Modifier.padding(start = 20.dp),
            fontSize = 20.sp
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val nanumbarngothic = FontFamily(
        Font(R.font.nanumbarungothic, FontWeight.Normal, FontStyle.Normal),
        Font(R.font.nanumbarungothicbold, FontWeight.Bold, FontStyle.Normal),
        Font(R.font.nanumbarungothiclight, FontWeight.Light, FontStyle.Normal),
        Font(R.font.nanumbarungothicultralight, FontWeight.Thin, FontStyle.Normal)
    )
    val (boardTitles, setBoardTitles) = remember { mutableStateOf(listOf<Pair<Int, String>>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Home", color = Color.White, fontWeight = FontWeight.Bold) },
                //탑바 색바꾸기
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(0xff75D1FF)
                ),
                actions = {
                    Row {
                        IconButton(onClick = { navController.navigate("timer") }) {
                            Image(
                                painter = painterResource(id = R.drawable.baseline_timer_24),
                                contentDescription = null,
                                modifier = Modifier.size(35.dp)
                            )
                        }
                        IconButton(onClick = {  }) {
                            Image(
                                painter = painterResource(id = R.drawable.baseline_post_add_24),
                                contentDescription = null,
                                modifier = Modifier.size(35.dp)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            MyBottomBar(navController)
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), color = Color.White
        ) {
            Column {
                //게시판 색바꾸기
                HomeTitle(categorytitle = "게시판", fontFamily = nanumbarngothic)
                HomeBoardTitle(
                    icon = R.drawable.baseline_announcement_24,
                    boardtitle = "공지 게시판",
                    onClicked = { navController.navigate("notice") })
                HomeBoardTitle(
                    icon = R.drawable.baseline_food_bank_24,
                    boardtitle = "점심 게시판",
                    onClicked = { navController.navigate("lunchMenuScreenRoute") })
                HomeBoardTitle(
                    icon = R.drawable.baseline_insert_drive_file_24,
                    boardtitle = "내 게시판",
                    onClicked = { navController.navigate("my") })
                HomeBoardTitle(
                    icon = R.drawable.baseline_insert_drive_file_24,
                    boardtitle = "여긴 뭘로 할까요",
                    onClicked = { navController.navigate("idontknow") })
            }
        }
    }
}

@Composable
fun HomeBoardTitle(icon: Int, boardtitle: String, onClicked: () -> Unit) {
    val nanumbarngothic = FontFamily(
        Font(R.font.nanumbarungothic, FontWeight.Normal, FontStyle.Normal),
        Font(R.font.nanumbarungothicbold, FontWeight.Bold, FontStyle.Normal),
        Font(R.font.nanumbarungothiclight, FontWeight.Light, FontStyle.Normal),
        Font(R.font.nanumbarungothicultralight, FontWeight.Thin, FontStyle.Normal)
    )
    Box(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .background(Color.White)
            .clickable { onClicked() }
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
            .size(width = 178.dp, height = 128.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .padding(start = 14.dp, top = 8.dp)
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = boardtitle,
                color = Color.DarkGray,
                fontSize = 20.sp,
                fontFamily = nanumbarngothic,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun NewBoardScreen() {
    var name by remember { mutableStateOf("") }
    val nanumbarngothic = FontFamily(
        Font(R.font.nanumbarungothic, FontWeight.Normal, FontStyle.Normal),
        Font(R.font.nanumbarungothicbold, FontWeight.Bold, FontStyle.Normal),
        Font(R.font.nanumbarungothiclight, FontWeight.Light, FontStyle.Normal),
        Font(R.font.nanumbarungothicultralight, FontWeight.Thin, FontStyle.Normal)
    )
    Column {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(text = "새로운 게시판 이름") },
            placeholder = { Text(text = "") }
        )
        Button(onClick = {
            val newBoardIcon = R.drawable.baseline_insert_drive_file_24
            val newBoardTitle = name
            val newBoard = Pair(newBoardIcon, newBoardTitle)
        }) {
            Text(text = "생성")
        }
    }
}

@Composable
fun HomeTitle(categorytitle: String, fontFamily: FontFamily) {
    Text(
        text = categorytitle,
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        modifier = Modifier.padding(10.dp),
        color = Color.DarkGray
    )
}

@Composable
fun LoginScreen(signInClicked: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GoogleSignInButton(signInClicked)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(navController: NavController) {
    var remainingSeconds by remember { mutableStateOf(30 * 60) }
    var initialRemainingSeconds by remember { mutableStateOf(30 * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var elapsedTimeMinutes by remember { mutableStateOf(0) }
    var elapsedTimeSeconds by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "타이머", color = Color.DarkGray) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatTime(remainingSeconds),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isPaused) {
                        if (elapsedTimeMinutes > 0) {
                            "회의한 시간: $elapsedTimeMinutes 분 $elapsedTimeSeconds 초"
                        } else {
                            "회의한 시간: $elapsedTimeSeconds 초"
                        }
                    } else {
                        ""
                    },
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            if (!isRunning) {
                                isRunning = true
                                startTimer(onTick = { updatedValue ->
                                    remainingSeconds = updatedValue
                                })
                            }
                        },
                        enabled = !isRunning
                    ) {
                        Text(text = "회의시작")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            stopTimer()
                            isPaused = true
                            val elapsedTimeMillis =
                                (initialRemainingSeconds - remainingSeconds) * 1000L
                            elapsedTimeMinutes = (elapsedTimeMillis / 1000 / 60).toInt()
                            elapsedTimeSeconds = ((elapsedTimeMillis / 1000) % 60).toInt()
                        },
                        enabled = isRunning
                    ) {
                        Text(text = "회의 끝")
                    }
                }
            }
        }
    )
}

// 가상의 타이머 로직을 구현하는 함수라고 가정합니다.
fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

private var timer: Timer? = null
private var currentSeconds: Int = 0

fun startTimer(onTick: (Int) -> Unit) {
    // 타이머 로직을 구현하고 매 초마다 onTick 함수를 호출하여 UI를 업데이트합니다.
    currentSeconds = 30 * 60

    timer = Timer()
    timer?.scheduleAtFixedRate(1000L, 1000L) {
        if (currentSeconds > 0) {
            currentSeconds--
            onTick(currentSeconds)
        } else {
            // 타이머 종료
            stopTimer()
        }
    }
}

fun resumeTimer() {
    startTimer { updatedValue ->
        currentSeconds = updatedValue
    }
}

fun stopTimer() {
    timer?.cancel()
    timer = null
}

@Composable
fun LunchMenuScreen(navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
            ) {
                Text("아래의 버튼을 눌러 시작하세요!")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate("details") },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("메뉴 추천 시작!", color = Color.Black)
            }
        }
    }
}

@Composable
fun Greeting(text: String, fontWeight: FontWeight) {
    Text(text, fontWeight = fontWeight)
}

@Composable
fun DetailsScreen(navController: NavController) {
    var selectedMenu by remember { mutableStateOf("") }
    val buttonText = "추천 메뉴는 '$selectedMenu'입니다!"
    LaunchedEffect(Unit) {
        val result = getSelectedMenuFromFirestore().await()
        selectedMenu = result.data?.get("menu")?.toString() ?: ""

        if (selectedMenu.isEmpty()) {
            val menuList = listOf("한식", "양식", "일식", "중식")
            selectedMenu = menuList.random()
            insertMenuToFirestore(selectedMenu)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Greeting(buttonText, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { navController.navigate("home") }  // "뒤로" 버튼을 클릭하면 "home" 목적지로 이동합니다.
                ) {
                    Text("뒤로")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        selectedMenu = getRandomMenuExceptSelected(selectedMenu)

                        // Firestore에 메뉴 저장
                        GlobalScope.launch {
                            insertMenuToFirestore(selectedMenu)
                        }
                    }
                ) {
                    Text("다시 뽑기")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        navController.navigate("next_destination/$selectedMenu")
                    }
                ) {
                    Text("다음")
                }
            }
        }
    }
}

val db = FirebaseFirestore.getInstance()

fun getSelectedMenuFromFirestore(): Task<DocumentSnapshot> {
    return db.collection("menus").document("selectedMenu").get()
}

suspend fun insertMenuToFirestore(selectedMenu: String) {
    val data = hashMapOf(
        "menu" to selectedMenu
    )
    db.collection("menus").document("selectedMenu").set(data).await()
}

fun getRandomMenuExceptSelected(selected: String): String {
    val menuList = listOf("한식", "양식", "일식", "중식")
    val remainingMenus =
        menuList.filter { it != selected }
    return remainingMenus.random()
}

@Composable
fun NextScreen(navController: NavController, selectedMenu: String) {
    val koreanDishes = listOf(
        "불고기 덮밥",
        "된장찌개",
        "생선조림",
        "김치찌개",
        "냉면",
        "삼계탕",
        "비빔밥",
        "제육덮밥",
        "김치볶음밥",
        "닭볶음탕",
        "갈비찜",
        "장어덮밥",
        "돼지불백",
        "닭곰탕",
        "순두부찌개",
        "생선구이",
        "메밀전병",
        "돼지고기 두루치기",
        "감자탕",
        "비빔밥",
        "육개장",
        "떡만두국",
        "해물순두부찌개",
        "순대국밥",
        "보쌈",
        "족발",
        "삼겹살",
        "대패삼겹살",
        "오리주물럭",
        "해물뚝배기",
        "전복죽",
        "뚝불고기",
        "육회",
        "회",
        "회덮밥",
        "물회",
        "돼지갈비",
        "소갈비",
        "낙지볶음",
        "조개찜",
        "동태찌개",
        "낙지볶음밥",
        "곱창전골",
        "장어구이",
        "치킨",
        "김치전",
        "해물파전",
        "떡볶이",
        "순대",
        "김밥",
        "닭발",
        "고로케",
        "두부김치",
        "튀김",
        "대하구이",
        "닭꼬치",
        "호떡",
        "칼국수",
        "샤브샤브",
        "라면",
        "돈까스",
        "낙곱새",
        "소고기"
    )
    val westernDishes = listOf(
        "스테이크 샐러드",
        "치킨 샐러드",
        "파스타",
        "샌드위치",
        "그라탕",
        "햄버거",
        "오믈렛",
        "피자",
        "알리오 올리오",
        "스파게티",
        "버팔로 윙",
        "미트볼",
        "새우 스테이크",
        "포크립",
        "비프 스튜",
        "그릴드 치킨",
        "그릴드 랍스터",
        "케밥",
        "치킨 텐더",
        "감바스",
        "핫도그",
        "퀘사디아",
        "브리또",
        "카나페",
        "빵",
        "라자냐",
        "스크램블 에그",
        "오므라이스",
        "도넛"
    )
    val chineseDishes = listOf(
        "자장면",
        "볶음밥",
        "자장면",
        "볶음밥",
        "탕수육",
        "깐풍기",
        "짬뽕",
        "칠리 새우",
        "딤섬",
        "고추잡채",
        "유린기",
        "탄탄면",
        "깐쇼 새우",
        "깐쇼 치킨",
        "훠궈",
        "꿔바로우",
        "양꼬치",
        "양고기",
        "마라탕",
        "마라롱샤",
        "자장밥",
        "짬뽕밥",
        "멘보샤",
        "마파두부",
        "마파두부덮밥"
    )
    val japaneseDishes = listOf(
        "초밥",
        "덴뿌라",
        "우동",
        "라멘",
        "가츠동",
        "돈부리",
        "오니기리",
        "샤브샤브",
        "소바",
        "부타동",
        "나베",
        "오야꼬동",
        "에비동",
        "돈코츠라멘",
        "텐동",
        "히레카츠동",
        "가라아게",
        "오마카세",
        "사시미",
        "샤브샤브",
        "오꼬노미야끼",
        "고로케"
    )

    var recommendedDish by remember {
        val initDish = when (selectedMenu) {
            "한식" -> koreanDishes.random()
            "양식" -> westernDishes.random()
            "중식" -> chineseDishes.random()
            "일식" -> japaneseDishes.random()
            else -> ""
        }
        mutableStateOf(initDish)
    }

    LaunchedEffect(Unit) {
        repeat(20) {
            delay(50)
            recommendedDish = when (selectedMenu) {
                "한식" -> koreanDishes.random()
                "양식" -> westernDishes.random()
                "중식" -> chineseDishes.random()
                "일식" -> japaneseDishes.random()
                else -> ""
            }
        }
    }


    val buttonText = "추천 메뉴는 '$recommendedDish'입니다!"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Greeting(buttonText, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    onClick = { navController.navigate("details") }
                ) {
                    Text("뒤로", color = Color.Black)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    onClick = {
                        GlobalScope.launch {
                            repeat(20) {
                                delay(50)
                                recommendedDish = when (selectedMenu) {
                                    "한식" -> koreanDishes.random()
                                    "양식" -> westernDishes.random()
                                    "중식" -> chineseDishes.random()
                                    "일식" -> japaneseDishes.random()
                                    else -> ""
                                }
                            }
                        }
                    }
                ) {
                    Text("다시 뽑기", color = Color.Black)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    onClick = { navController.navigate("home") }
                ) {
                    Text("처음으로", color = Color.Black)
                }
            }

        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DmScreen(navController: NavController) {
    Scaffold(topBar = {
        MyTopBar("Dm")
    }, bottomBar = {
        MyBottomBar(navController)
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .border(1.dp, Color.Black, RectangleShape)
                        .clickable { navController.navigate("workspace") }
                ) {
                    Text(text = "Work Space")
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .border(1.dp, Color.Black, RectangleShape)
                        .clickable { navController.navigate("teamproject") }
                ) {
                    Text(text = "Team Project")
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .border(1.dp, Color.Black, RectangleShape)
                        .clickable { navController.navigate("playground") }
                ) {
                    Text(text = "Play Ground")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            MyTopBar("스케줄")
        },
        bottomBar = {
            MyBottomBar(navController)
        }
    ) { innerPadding ->

        var selectedDate by remember { mutableStateOf(LocalDate.now()) }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            CalendarComposable(
                modifier = Modifier.fillMaxWidth(),
                selectedDate = selectedDate,
                onDateSelected = { newSelectedDate ->
                    selectedDate = newSelectedDate
                },
                onPreviousMonthClick = {
                    selectedDate = selectedDate.minusMonths(1)
                },
                onNextMonthClick = {
                    selectedDate = selectedDate.plusMonths(1)
                }
            )
        }
    }
}

// 달력구성 컴포저블
@Composable
fun CalendarComposable(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit
) {
    var memoMap by remember { mutableStateOf(mutableMapOf<LocalDate, String>()) }
    var showDialog by remember { mutableStateOf(false) }


    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPreviousMonthClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month")
            }

            val headerText =
                selectedDate.format(DateTimeFormatter.ofPattern("yyyy년 M월"))
            Text(
                text = headerText,
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            IconButton(onClick = onNextMonthClick) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(  //Text 로 나열되있던 코드를 forEach 써서 단축
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")

            daysOfWeek.forEachIndexed { index, day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    color = if (index == 0) Color.Red else if (index == 6) Color.Blue else Color.Black, // 일,토요일 색 변환
                    textAlign = TextAlign.Center
                )
            }
        }
        val firstDayOfMonth = selectedDate.withDayOfMonth(1)
        val lastDayOfMonth = selectedDate.withDayOfMonth(selectedDate.lengthOfMonth())
        val daysInMonth = (1..lastDayOfMonth.dayOfMonth).toList()
        val emptyDaysBefore = (0 until firstDayOfMonth.dayOfWeek.value).toList()


        LazyVerticalGrid(  // 달력에 날짜들을 그리는 코드
            GridCells.Fixed(7), // 각 행당 7개의 열을 가지도록 설정
            contentPadding = PaddingValues(4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            items(emptyDaysBefore) {
                Spacer(modifier = Modifier.size(30.dp))
            }

            itemsIndexed(daysInMonth) { index, day ->
                val date = selectedDate.withDayOfMonth(day)
                val isSelected = date == selectedDate
                var memo = memoMap[date] ?: ""

                val dayOfWeekIndex = date.dayOfWeek.value - 1
                val isFirstInRow = dayOfWeekIndex == 6
                val isLastInRow = dayOfWeekIndex == 5

                // Firebase에서 메모 읽어오기
                getMemoFromFirebase(date) { firebaseMemo ->
                    if (firebaseMemo.isNotEmpty())
                        memoMap[date] = firebaseMemo
                }

                Divider()
                CalendarDay(
                    date = date,
                    isSelected = isSelected,
                    onDateSelected = {
                        onDateSelected(date)
                        showDialog = true
                    },
                    showDialog = showDialog,
                    memo = memo,
                    isFirstInRow = isFirstInRow,
                    isLastInRow = isLastInRow
                )
            }
        }
        if (showDialog) {
            val selectedMemo = memoMap[selectedDate] ?: ""
            MemoDialog(
                memo = selectedMemo, // 여기서 초기 메모 값 설정
                onMemoChanged = { updatedMemo ->
                    if (updatedMemo.isNotBlank()) {
                        memoMap[selectedDate] = updatedMemo
                        saveMemoToFirebase(selectedDate, updatedMemo) // Firebase에 저장
                    } else {
                        memoMap.remove(selectedDate)
                        saveMemoToFirebase(selectedDate, "") // 메모가 없을 경우 Firebase에서도 삭제
                    }
                    showDialog = false // 다이얼로그 닫기
                },
                onDismiss = { showDialog = false } // 다이얼로그 닫기
            )
        }
    }
}


@Composable
fun CalendarDay(
    date: LocalDate,
    isSelected: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    showDialog: Boolean,
    memo: String?,
    isFirstInRow: Boolean,
    isLastInRow: Boolean
) {
    val dialogState = remember { mutableStateOf(showDialog) }
    val publicHoliday =
        PublicHoliday.values().find { it.month == date.monthValue && it.day == date.dayOfMonth }
    val isPublicHoliday = publicHoliday != null

    Box(           //날짜 박스
        modifier = Modifier
            .size(width = 30.dp, height = 110.dp)
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (isSelected) Modifier.border(
                    1.dp,
                    Color.LightGray,
                    shape = RoundedCornerShape(8.dp)
                )
                else Modifier
            )
            .clickable {
                onDateSelected(date)
                dialogState.value = true
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Text(    // 날짜
            text = date.dayOfMonth.toString(),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPublicHoliday) Color.Red
            else if (isFirstInRow) Color.Red
            else if (isLastInRow) Color.Blue
            else Color.Black,
            modifier = Modifier
                .background(if (isSelected) Color.White else Color.Transparent)
                .padding(vertical = 2.dp, horizontal = 4.dp)
        )
        if (isPublicHoliday) {  // 공휴일 표시코드
            Text(
                text = publicHoliday?.holidayname ?: "",
                color = Color.White,
                fontSize = 8.sp,
                modifier = Modifier
                    .background(Color.Red)
                    .padding(2.dp)
                    .align(Alignment.BottomCenter)
            )
        }

        if (memo != null && memo.isNotEmpty()) {  // 달력에 표시되는 메모 부분
            Text(
                text = memo,
                color = Color.Black,
                fontSize = 10.sp,   // 메모 글자 크기
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .background(Color.Cyan)
                    .padding(4.dp)
            )
        }
    }
}

//공휴일 모음
enum class PublicHoliday(val month: Int, val day: Int, val holidayname: String) {
    NEW_YEAR(1, 1, "새해 첫날"),
    KOREAN_NEW_YEAR(1, 22, "설날"),
    INDEPENDENCE_MOVEMENT_DAY(3, 1, "삼일절"),
    CHILDRENS_DAY(5, 5, "어린이날"),
    BUDDA_BIRTH_DAY(5, 27, "부처님오신날"),
    MEMORIAL_DAY(6, 6, "현충일"),
    INDEPENDENCE_DAY(8, 15, "광복절"),
    THANKSGIVING_DAY(9, 29, "추석"),
    NATIONAL_FOUNDATION_DAY(10, 3, "개천절"),
    HANGUL_PROCLAMATION_DAY(10, 9, "한글날"),
    CHRISTMAS(12, 25, "크리스마스")
}

// 메모 다이얼로그
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoDialog(
    memo: String,
    onMemoChanged: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var updatedMemo by remember { mutableStateOf(memo) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("메모") },
        text = {
            TextField(
                value = updatedMemo,
                onValueChange = { updatedMemo = it },
                label = { Text("메모를 입력하세요") },
                singleLine = true,
                keyboardActions = KeyboardActions(
                    onDone = {
                        onMemoChanged(updatedMemo)
                        onDismiss()
                    }
                )
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onMemoChanged(updatedMemo)
                onDismiss()
            }) {
                Text("확인")
            }
        }
    )
}

// Firebase 데이터베이스 레퍼런스를 가져옵니다.
private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
private val memoRef: DatabaseReference = database.reference.child("Calendar_memo")

// 메모를 Firebase에 저장하는 함수
fun saveMemoToFirebase(date: LocalDate, memo: String) {
    val formattedDate = date.format(DateTimeFormatter.ISO_DATE) // 날짜를 문자열로 변환
    memoRef.child(formattedDate).setValue(memo)
}

// 날짜에 해당하는 메모를 Firebase에서 읽어오는 함수
fun getMemoFromFirebase(date: LocalDate, callback: (String) -> Unit) {
    val formattedDate = date.format(DateTimeFormatter.ISO_DATE) // 날짜를 문자열로 변환
    memoRef.child(formattedDate).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val memo = snapshot.getValue(String::class.java) ?: ""
            callback(memo)
        }

        override fun onCancelled(error: DatabaseError) {
            // 처리 중 에러 발생 시
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(
    navController: NavController,
    onClicked: () -> Unit,
    _imageBitmap: MutableState<ImageBitmap?>,
    onPickImage: () -> Unit
) {
    val imageSizeDp = 100.dp
    val currentUser = FirebaseAuth.getInstance().currentUser
    val onImageIconClick = { onPickImage() }


    // 상태 관련 변수들 추가
    var selectedStatus by remember { mutableStateOf("미팅 중") }

    LaunchedEffect(Unit) {
        if (currentUser != null) {
            getUserStatusFromFirestore(currentUser.uid) { fetchedStatus ->
                selectedStatus = fetchedStatus
            }
        }
    }
    val statusOptions = listOf("미팅 중", "출퇴근 중", "병가", "휴가", "업무 중")
    var showDropdown by remember { mutableStateOf(false) }
    Scaffold(
        topBar = { MyTopBar("개인정보") }, // MyTopBar는 @Composable 함수여야 합니다.
        bottomBar = { MyBottomBar(navController) } // MyBottomBara도 @Composable 함수여야 합니다.
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (_imageBitmap.value != null) {
                Image(
                    bitmap = _imageBitmap.value!!,
                    contentDescription = "User profile picture",
                    modifier = Modifier
                        .size(imageSizeDp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onImageIconClick() }
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Default user icon",
                    modifier = Modifier
                        .size(imageSizeDp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onImageIconClick() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = currentUser?.displayName ?: "",
                onValueChange = {},
                label = { Text("Name") },
                enabled = false
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = currentUser?.email ?: "",
                onValueChange = {},
                label = { Text("E-mail") },
                enabled = false
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. 상태 설정을 위한 OutlinedTextField와 DropdownMenu 추가


            OutlinedTextField(
                value = selectedStatus,
                onValueChange = { /* 여기서는 값 변경을 허용하지 않음 */ },
                label = { Text("상태") },
                trailingIcon = {
                    IconButton(onClick = { showDropdown = true }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                },
                enabled = false,
                modifier = Modifier.focusable(enabled = false)
            )

            DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false }
            ) {
                for (option in statusOptions) {
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedStatus = option
                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                            if (userId != null) {
                                saveUserStatusToFirestore(userId, selectedStatus)
                            }
                            showDropdown = false
                        }
                    )
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) { }

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column {
                    Text(text = "로그아웃", modifier = Modifier.clickable { onClicked() })
                }
            }
        }
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MyTopBar(topBarTitle: String) {
    TopAppBar(
        title = { Text(text = topBarTitle) },
        //탑바 색바꾸기
        colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xff75D1FF))
    )
}

@Composable
private fun MyBottomBar(navController: NavController) {
    BottomAppBar(
        //바텀바 색바꾸기
        containerColor = Color(0xff75D1FF)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            //바텀바 아이콘 색바꾸기
            Button(
                onClick = {
                    navController.navigate("home")
                },
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xff75D1FF),
                ), modifier = Modifier.padding(8.dp)
            ) {
                // Text(text = "home")
                Image(
                    painter = painterResource(id = R.drawable.baseline_home_24),
                    contentDescription = null,
                    modifier = Modifier
                        .size(30.dp)
                )
            }
            Button(
                onClick = {
                    navController.navigate("dm")
                },
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xff75D1FF),
                ), modifier = Modifier.padding(8.dp)
            ) {
                // Text(text = "DM")
                Image(
                    painter = painterResource(id = R.drawable.baseline_chat_24),
                    contentDescription = null, modifier = Modifier.size(30.dp)
                )
            }
            Button(
                onClick = {
                    navController.navigate("schedule")
                },
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xff75D1FF),
                ), modifier = Modifier.padding(8.dp)
            ) {
                // Text(text = "스케쥴")
                Image(
                    painter = painterResource(id = R.drawable.baseline_calendar_month_24),
                    contentDescription = null, modifier = Modifier.size(30.dp)
                )
            }
            Button(
                onClick = {
                    navController.navigate("personal")
                },
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xff75D1FF),
                ), modifier = Modifier.padding(8.dp)
            ) {
                //Text(text = "개인정보")
                Image(
                    painter = painterResource(id = R.drawable.baseline_person_24),
                    contentDescription = null, modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

data class ChatMessage(
    val message: String? = "메시지 오류",
    val userId: String? = "UID 오류",
    val userName: String? = "이름 오류",
    val uploadDate: String? = "",
    val profileString: String? = "",
    val imageUrl: String? = ""
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ChattingScreen(navController: NavController, mAuth: FirebaseAuth, chatName: String) {
    val context = LocalContext.current
    var chatmessage by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf(listOf<ChatMessage>()) }
    val user: FirebaseUser? = mAuth.currentUser

    val clipboardManager = LocalClipboardManager.current

    var uploadedImageUrl by remember { mutableStateOf("") }
    var isImageExpanded by remember { mutableStateOf(false) }
    var expandedImageUri by remember { mutableStateOf("") }
    var isModalVisible by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                uploadChatImage(chatName, it, mAuth,
                    onImageUploaded = { imageUrl ->
                        uploadedImageUrl = imageUrl
                    })
            }
        }
    )
    loadChatMessages(chatName) { messages ->
        chatMessages = messages
    }
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(text = "채팅방", fontSize = 17.sp, fontFamily = FontFamily.SansSerif)
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        navController.navigate("dm")
                    }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back button"
                    )
                }
            },
            actions = {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }
            },
            //탑바 색바꾸기
//            colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Gray)
        )
    }, bottomBar = {
        BottomAppBar(
            containerColor = Color.White
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "추가기능",
                    modifier = Modifier
                        .clickable {
                            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                        .padding(10.dp)
                )
                TextField(
                    value = chatmessage,
                    onValueChange = { chatmessage = it },
                    modifier = Modifier
                        .weight(1f) // 여기서 비율을 조정
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(Color.White),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                Icon(imageVector = Icons.Default.Send, contentDescription = "메세지 보내기버튼",
                    modifier = Modifier
                        .padding(10.dp)
                        .clickable {
                            if (chatmessage.isNotEmpty()) {
                                val currentDate = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                                val newChatMessage =
                                    ChatMessage(
                                        message = chatmessage,
                                        userId = user?.uid,
                                        userName = user?.displayName,
                                        uploadDate = currentDate,
                                        profileString = user?.photoUrl.toString()
                                    )
                                saveChatMessage(newChatMessage, chatName)
                                chatmessage = ""
                            }
                        })
            }
        }
    }) { innerPadding ->
        LazyColumn(
            reverseLayout = true,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(chatMessages.reversed()) { message ->
                val isCurrentUserMessage = user?.uid == message.userId
                val alignment = if (isCurrentUserMessage) Alignment.End else Alignment.Start
                val backgroundColor = if (isCurrentUserMessage) Color(0xFF070F14) else Color(0xFFFCE9F0)
                val textColor = if (isCurrentUserMessage) Color.White else Color.Black

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = alignment
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!isCurrentUserMessage) {
                            val selectedUri = message.profileString?.let { Uri.parse(it) }
                            AsyncImage(
                                model = selectedUri,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                            )
                        }
                        Column {
                            if (!isCurrentUserMessage) {
                                Text(
                                    text = message.userName ?: "",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row {
                                if (isCurrentUserMessage) {
                                    Text(
                                        text = message.uploadDate ?: "",
                                        fontSize = 8.sp,
                                        color = Color.LightGray
                                    )
                                }
                                Column {
                                    if (!message.imageUrl.isNullOrEmpty()) {
                                        Image(
                                            painter = rememberAsyncImagePainter(message.imageUrl),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    isImageExpanded = !isImageExpanded
                                                    if (isImageExpanded) {
                                                        expandedImageUri = message.imageUrl
                                                        isModalVisible = true  // 모달을 열도록 상태 변경
                                                    }
                                                }
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(backgroundColor, shape = RoundedCornerShape(8.dp))
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = message.message ?: "",
                                            fontSize = 16.sp,
                                            color = textColor,
                                            modifier = Modifier.pointerInput(Unit) {
                                                detectTapGestures(onLongPress = {
                                                    val annotatedString = AnnotatedString(message.message ?: "")
                                                    clipboardManager.setText(annotatedString)
                                                    Toast.makeText(context, "클립보드에 복사되었습니다", Toast.LENGTH_SHORT).show()
                                                })
                                            }
                                        )
                                    }
                                }
                                if (!isCurrentUserMessage) {
                                    Text(
                                        text = message.uploadDate ?: "",
                                        fontSize = 8.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (isModalVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { isModalVisible = false }  // 모달 클릭 시 닫기
        ) {
            Image(
                painter = rememberAsyncImagePainter(expandedImageUri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { isModalVisible = false }  // 이미지 클릭 시 닫기
            )
        }
    }
}


fun saveChatMessage(chatMessage: ChatMessage, chatName: String) {
    val database = Firebase.database
    val chatRef = database.getReference("chattings").child(chatName) // "chat"이라는 경로로 데이터를 저장
    val newMessageRef = chatRef.push() // 새로운 메시지를 추가하기 위한 참조

    newMessageRef.setValue(chatMessage)
}


fun loadChatMessages(chatName: String, listener: (List<ChatMessage>) -> Unit) {
    val database = getInstance("https://dataclass-27aac-default-rtdb.asia-southeast1.firebasedatabase.app/")
    val chatRef = database.getReference("chattings").child(chatName)

    chatRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val messages = mutableListOf<ChatMessage>()
            for (childSnapshot in snapshot.children) {
                val chatMessage = childSnapshot.getValue(ChatMessage::class.java)
                chatMessage?.let {
                    messages.add(it)
                }
            }
            listener(messages)
        }

        override fun onCancelled(error: DatabaseError) {
            // 에러 처리
        }
    })
}

fun uploadChatImage(chatName: String, uri: Uri, mAuth: FirebaseAuth, onImageUploaded: (String) -> Unit) {
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference
    val user = mAuth.currentUser

    val imageRef = storageRef.child("images/${uri.lastPathSegment}")
    imageRef.putFile(uri)
        .addOnSuccessListener { taskSnapshot ->
            // 업로드 성공 후 다운로드 URL을 가져와 채팅 메시지에 저장
            taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { downloadUri ->
                val imageUrl = downloadUri.toString()
                val currentDate = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                val chatMessage = ChatMessage(
                    userId = user?.uid,
                    userName = user?.displayName,
                    uploadDate = currentDate,
                    profileString = user?.photoUrl.toString(),
                    imageUrl = imageUrl // imageUrl 설정
                )
                saveChatMessage(chatMessage, chatName)
                onImageUploaded(imageUrl) // 이미지 업로드 후 콜백 호출
            }
        }
        .addOnFailureListener {
            // 업로드 실패 처리
        }
}

data class Post(
    val key: String? = null,
    val title: String = "",
    val author: String = "",
    val date: String = "",
    val content: String = "",
    val profile: String = "",
    val imageUrl: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakeBoardScreen(navController: NavController, mAuth: FirebaseAuth, postName: String) {

    var postList by remember { mutableStateOf(listOf<Post>()) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val user: FirebaseUser? = mAuth.currentUser

    var uploadedImageUrl by remember { mutableStateOf("") }


    loadPosts(postName) { posts ->
        postList = posts
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                uploadPostImage(title, content, postName,it, mAuth,
                    onImageUploaded = { imageUrl ->
                        uploadedImageUrl = imageUrl
                    })
                navController.navigate(postName)
            }
            title = ""
            content = ""
        }
    )


    fun canPost(): Boolean {
        return title.isNotBlank() && content.isNotBlank()
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("제목") },
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(postName)
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로 가기"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = "사진"
                        )
                    }
                    IconButton(
                        onClick = {
                            if (uploadedImageUrl == "") {
                                val currentDate = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(Date())
                                val newPost = Post(
                                    title = title,
                                    author = user?.displayName ?: "",
                                    date = currentDate,
                                    content = content,
                                    profile = user?.photoUrl.toString()
                                )
                                savePost(newPost, postName)
                                // 게시 버튼 클릭 후 입력 필드 초기화
                            }
                            title = ""
                            content = ""
                            navController.navigate(postName)
                        },
                        enabled = canPost()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "저장"
                        )
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            TextField(
                value = content,
                onValueChange = { text -> content = text },
                placeholder = { Text("사진을 가져오기 전에 내용과 제목을 입력해 주세요", fontStyle = FontStyle.Italic, color = Color.Gray) },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardViewScreen(navController: NavController,postName: String, makeBoardRoute: String, postTitle: String) {

    var postList by remember { mutableStateOf(listOf<Post>()) }

    loadPosts(postName) { posts ->
        postList = posts
    }

    // Dialog state
    var showDialog by remember { mutableStateOf(false) }
    var editedPost by remember { mutableStateOf<Post?>(null) }

    if (showDialog) {
        editedPost?.let { post ->
            EditPostDialog(
                post = post,
                onEditCompleted = { updatedPost ->
                    val database = Firebase.database
                    val postRef = database.getReference("Posts").child(postName)
                    postRef.child("${post.key}").setValue(updatedPost)
                    editedPost = null
                    showDialog = false
                },
                onCancel = {
                    editedPost = null
                    showDialog = false
                }
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(
            onClick = {
                navController.navigate(makeBoardRoute)
            },
        ) {
            Icon(
                imageVector = Icons.Default.Create, // 원하는 아이콘을 선택하세요.
                contentDescription = "게시글 추가"
            )
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = postTitle) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("home")
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로 가기"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate(makeBoardRoute)
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add, // 원하는 아이콘을 선택하세요.
                            contentDescription = "게시글 추가"
                        )
                    }
                }
            )
        }
    )
    { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            contentPadding = paddingValues
        ) {
            items(postList.reversed()) { post ->
                PostCard(
                    postName,
                    post,
                    onEditClick = {
                        editedPost = post
                        showDialog = true
                    },
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

}

@Composable
fun EditPostDialog(
    post: Post,
    onEditCompleted: (Post) -> Unit,
    onCancel: () -> Unit
) {
    var editedTitle by remember { mutableStateOf(TextFieldValue(post.title)) }
    var editedContent by remember { mutableStateOf(TextFieldValue(post.content)) }

    Dialog(
        onDismissRequest = onCancel
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "수정",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            BasicTextField(
                value = editedTitle.text,
                onValueChange = { editedTitle = TextFieldValue(it) },
                textStyle = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            BasicTextField(
                value = editedContent.text,
                onValueChange = { editedContent = TextFieldValue(it) },
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        val updatedPost = post.copy(
                            title = editedTitle.text,
                            content = editedContent.text
                        )
                        onEditCompleted(updatedPost)
                    }
                ) {
                    Text("저장")
                }

                Button(
                    onClick = onCancel
                ) {
                    Text("취소")
                }
            }
        }
    }
}

@Composable
fun PostCard(
    postName: String,
    post: Post,
    onEditClick: () -> Unit,
) {
    var showOptions by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Option Icon Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End // Right align
            ) {
                Column {
                    IconButton(
                        onClick = { showOptions = !showOptions },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings, // 원하는 아이콘을 선택하세요.
                            contentDescription = "Options"
                        )
                    }
                    if (showOptions) {
                        Row(
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    onEditClick()
                                },
                                modifier = Modifier.padding(end = 16.dp)
                            ) {
                                Text("수정")
                            }
                            TextButton(
                                onClick = {
                                    val database = Firebase.database
                                    val postRef = database.getReference("Posts").child(postName)
                                    postRef.child("${post.key}").setValue(null)
                                }
                            ) {
                                Text("삭제")
                            }
                        }
                    }

                }
            }

            Text(
                text = post.title,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 35.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            // 사용자 프로필 부분
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 프로필 사진 (여기에 실제 프로필 사진을 표시하려면 Image 또는 Coil 라이브러리를 사용할 수 있습니다.)

                val selectedUri = post.profile.let { Uri.parse(it) }

                AsyncImage(
                    model = selectedUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )
                // 실제 프로필 사진을 표시하는 코드를 여기에 추가

                Spacer(modifier = Modifier.width(8.dp))

                // 사용자명
                Text(
                    text = post.author,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "게시일: ${post.date}",
                fontSize = 14.sp,
                color = Color.Gray
            )

            if (!post.imageUrl.isNullOrEmpty()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Image(
                        painter = rememberAsyncImagePainter(post.imageUrl),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(400.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))


            Text(
                text = post.content,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))


        }
    }
}


fun savePost(post: Post, postName: String) {
    val database = Firebase.database
    val postRef = database.getReference("Posts").child(postName)
    val newPostRef = postRef.push()

    newPostRef.setValue(post)
}


fun loadPosts(postName: String, onDataChange: (List<Post>) -> Unit) {
    val database = Firebase.database
    val postRef = database.getReference("Posts").child(postName)

    postRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val posts = mutableListOf<Post>()
            for (childSnapshot in snapshot.children) {
                val key = childSnapshot.key
                val post = childSnapshot.getValue(Post::class.java)
                post?.let {
                    posts.add(it.copy(key = key))
                }
            }
            onDataChange(posts)
        }

        override fun onCancelled(error: DatabaseError) {
            // 에러 처리
        }
    })
}

fun uploadPostImage(title: String, content: String, postName: String, uri: Uri, mAuth: FirebaseAuth, onImageUploaded: (String) -> Unit) {
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference


    val imageRef = storageRef.child("images/${uri.lastPathSegment}")
    imageRef.putFile(uri)
        .addOnSuccessListener { taskSnapshot ->
            // 업로드 성공 후 다운로드 URL을 가져와 채팅 메시지에 저장
            taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { downloadUri ->
                val imageUrl = downloadUri.toString()
                val user = mAuth.currentUser
                val currentDate = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(Date())
                val newPost = Post(
                    title = title,
                    author = user?.displayName ?: "",
                    date = currentDate,
                    content = content,
                    profile = user?.photoUrl.toString(),
                    imageUrl = imageUrl
                )
                savePost(newPost, postName)
                onImageUploaded(imageUrl) // 이미지 업로드 후 콜백 호출
            }
        }
        .addOnFailureListener {
            // 업로드 실패 처리
        }
}