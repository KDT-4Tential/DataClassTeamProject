package com.example.dataclassteamproject

import android.app.Activity
import android.content.Context
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dataclassteamproject.ui.theme.DataClassTeamProjectTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase.getInstance
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    companion object {
        const val RC_SIGN_IN = 100
    }

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val pickImageRequestCode = 101
    var _imageBitmap: MutableState<ImageBitmap?> = mutableStateOf(null)

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri = result.data?.data
            val rotationAngle = getRotationAngle(selectedImageUri!!)
            val androidBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)
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
                    composable("dm") {
                        DmScreen(navController)
                    }
                    composable("schedule") {
                        ScheduleScreen(navController = rememberNavController(),
                            onPreviousMonthClick = {},
                            onNextMonthClick = {}
                        )
                    }
                    composable("personal") {
                        PersonalInfoScreen(navController, onClicked = { signOut(navController) }, _imageBitmap = _imageBitmap)
                    }
                    composable("boardview") {
                        //여기에 보드뷰 스크린을 넣어주세요
                    }
                    composable("chatting") {
                        ChattingScreen(mAuth)
                    }
                    composable("login") {
                        LoginScreen(
                            signInClicked = {
                                launcher.launch(signInIntent)
                            })
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

data class ChatMessage(
    val message: String? = "메시지 오류",
    val userId: String? = "UID 오류",
    val userName: String? = "이름 오류",
    val uploadDate: String? = "",
    var downloadUrl: String? = ""
)

fun saveChatMessage(chatMessage: ChatMessage) {
    val database = getInstance("https://dataclass-27aac-default-rtdb.asia-southeast1.firebasedatabase.app/")
    val chatRef = database.getReference("chattings") // "chat"이라는 경로로 데이터를 저장
    val newMessageRef = chatRef.push() // 새로운 메시지를 추가하기 위한 참조

    newMessageRef.setValue(chatMessage)
}

fun loadChatMessages(listener: (List<ChatMessage>) -> Unit) {
    val database = getInstance("https://dataclass-27aac-default-rtdb.asia-southeast1.firebasedatabase.app/")
    val chatRef = database.getReference("chattings")

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(topBar = {
        MyTopBar("home")
    }, bottomBar = {
        MyBottomBara(navController)
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column {
                Text(text = "게시판")
                Text(text = "게시판")
                Text(text = "게시판")
                Text(text = "게시판")
                Text(text = "게시판")
                Text(text = "게시판")
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
        MyBottomBara(navController)
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column {
                repeat(10) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Black, RectangleShape)
                            .clickable { navController.navigate("chatting") }
                    ) {
                        Text(text = "DM")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    navController: NavController,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit
) {

    var showDialog by remember { mutableStateOf(false) }
    var selectedScheduleDate by remember { mutableStateOf(LocalDate.now()) }

    Scaffold(
        topBar = {
            MyTopBar("스케줄")
        },
        bottomBar = {
            MyBottomBara(navController)
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
                onDateSelected = { date, position ->
                    selectedScheduleDate = date
                    showDialog = true
                },
                onPreviousMonthClick = {
                    selectedDate = selectedDate.minusMonths(1)
                    onPreviousMonthClick()
                },
                onNextMonthClick = {
                    selectedDate = selectedDate.plusMonths(1)
                    onNextMonthClick()
                }
            )
        }

        if (showDialog) {
            ScheduleInputDialog(
                date = selectedScheduleDate,
                onDismissRequest = { showDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleInputDialog(date: LocalDate, onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // 배경 및 테두리
            Surface(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // 일정 입력 제목
                    Text(
                        text = "일정 입력",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 일정 입력 필드
                    TextField(
                        value = "", // 초기 값은 빈 문자열로 설정
                        onValueChange = { /* update the value here */ },
                        placeholder = { Text(text = "일정을 입력하세요.") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.White,
                            focusedIndicatorColor = Color.Blue,
                            unfocusedIndicatorColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 확인 버튼
                    Button(
                        onClick = {
                            // 일정 추가 로직을 여기에 추가
                            onDismissRequest()
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = "확인")
                    }
                }
            }
        }
    }
}

// 달력구성 컴포저블
@Composable
fun CalendarComposable(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate, Int) -> Unit,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit
) {
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
            )

            IconButton(onClick = onNextMonthClick) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "일",
                modifier = Modifier.weight(1f),
                color = Color.Red,
                textAlign = TextAlign.Center
            )
            Text(
                text = "월",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "화",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "수",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "목",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "금",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "토",
                modifier = Modifier.weight(1f),
                color = Color.Blue,
                textAlign = TextAlign.Center
            )
        }
        val firstDayOfMonth = selectedDate.withDayOfMonth(1)
        val lastDayOfMonth = selectedDate.withDayOfMonth(selectedDate.lengthOfMonth())

        val daysInMonth = (1..lastDayOfMonth.dayOfMonth).toList()
        val emptyDaysBefore = (1 until firstDayOfMonth.dayOfWeek.value).toList()


        LazyVerticalGrid(
            GridCells.Fixed(7), // 각 행당 7개의 열을 가지도록 설정
            contentPadding = PaddingValues(4.dp),
            verticalArrangement = Arrangement.spacedBy(90.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Display empty boxes for days before the first day of the month
            items(emptyDaysBefore) {
                Spacer(modifier = Modifier.size(30.dp))
            }

            itemsIndexed(daysInMonth) { index, day ->
                val date = selectedDate.withDayOfMonth(day)
                val isSelected = date == selectedDate
                Divider()
                CalendarDay(
                    date = date,
                    isSelected = isSelected,
                    onDateSelected = { onDateSelected }
                )
            }
        }
    }
}


@Composable
fun CalendarDay(
    date: LocalDate,
    isSelected: Boolean,
    onDateSelected: (LocalDate) -> Unit
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .clickable { onDateSelected(date) }
            .background(if (isSelected) Color.Gray else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color.Black
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ChattingScreen(mAuth: FirebaseAuth) {
    var chatmessage by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf(listOf<ChatMessage>()) }
    val user: FirebaseUser? = mAuth.currentUser

    loadChatMessages { messages ->
        chatMessages = messages
    }
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = { /*TODO*/ }) {
                        Text(text = "back")
                    }
                    Text(text = "채팅방", modifier = Modifier.weight(1f))
                    Button(onClick = { /*TODO*/ }) {
                        Text(text = "검색")
                    }
                }
            },
            //탑바 색바꾸기
            colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Gray)
        )
    }, bottomBar = {
        BottomAppBar(
            containerColor = Color.Gray
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = { }) {
                    Text(text = "+")
                }
                TextField(value = chatmessage, onValueChange = { chatmessage = it })
                Button(onClick = {
                    if (chatmessage.isNotEmpty()) {
                        val currentDate = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                        val newChatMessage =
                            ChatMessage(message = chatmessage, userId = user?.uid, userName = user?.displayName, uploadDate = currentDate)
                        saveChatMessage(newChatMessage)
                        chatmessage = ""
                    }
                }) {
                    Text(text = "보내기")
                }
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
                val alignment = if (isCurrentUserMessage) Alignment.BottomEnd else Alignment.BottomStart
                val backgroundColor = if (isCurrentUserMessage) Color.Gray else Color.Yellow
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 20.dp, top = 10.dp, bottom = 10.dp),
                    contentAlignment = alignment
                ) {
                    Column {
                        if (!isCurrentUserMessage) {
                            Text(text = message.userName ?: "")
                        }
                        Row {
                            if (isCurrentUserMessage) {
                                Text(text = message.uploadDate ?: "")
                            }
                            Box(
                                modifier = Modifier
                                    .background(backgroundColor)
                                    .padding(8.dp)
                            ) {
                                Text(text = message.message ?: "")
                            }
                            if (!isCurrentUserMessage) {
                                Text(text = message.uploadDate ?: "")
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(navController: NavController, onClicked: () -> Unit, _imageBitmap: MutableState<ImageBitmap?>) {
    val imageSizeDp = 100.dp
    val currentUser = FirebaseAuth.getInstance().currentUser
    val onImageIconClick = { /* 이미지 클릭 시 수행할 액션 정의 */ }
    Scaffold(
        topBar = {
            MyTopBar("개인정보")
        },
        bottomBar = {
            MyBottomBara(navController)
        }
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
                    Text(text = "로그아웃", modifier = Modifier.clickable{ onClicked() })
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
        colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Gray)
    )
}

@Composable
private fun MyBottomBara(navController: NavController) {
    BottomAppBar(
        containerColor = Color.Gray
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = {
                navController.navigate("home")
            }) {
                Text(text = "home")
            }
            Button(onClick = {
                navController.navigate("dm")
            }) {
                Text(text = "DM")
            }
            Button(onClick = {
                navController.navigate("schedule")
            }) {
                Text(text = "스케쥴")
            }
            Button(onClick = {
                navController.navigate("personal")
            }) {
                Text(text = "개인정보")
            }
        }
    }
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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DataClassTeamProjectTheme {
        ScheduleScreen(navController = rememberNavController(),
            onPreviousMonthClick = {},
            onNextMonthClick = {}
        )
    }
}