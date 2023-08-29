package com.example.dataclassteamproject

import android.net.Uri
import android.os.Bundle
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.FirebaseDatabase.getInstance
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
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
                    composable("dm") {
                        DmScreen(navController)
                    }
                    composable("schedule") {
                        ScheduleScreen(navController = navController)
                    }
                    composable("personal") {
                        PersonalInfoScreen(navController, onClicked = { signOut(navController) })
                    }
                    composable("boardview") {
                        //여기에 보드뷰 스크린을 넣어주세요
                    }
                    composable("chatting") {
                        ChattingScreen(navController, mAuth)
                    }
                    composable("login") {
                        LoginScreen(
                            signInClicked = {
                                launcher.launch(signInIntent)
                            })
                    }
                    composable("test") {
                        TestScreen()
                    }
                    //추가해야할 스크린
                    //글작성
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
    val profileString: String? = "",
    val imageUrl: String? = ""
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

fun uploadImage(uri: Uri, mAuth: FirebaseAuth, onImageUploaded: (String) -> Unit) {
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
                    message = "이미지 업로드",
                    userId = user?.uid,
                    userName = user?.displayName,
                    uploadDate = currentDate,
                    profileString = user?.photoUrl.toString(),
                    imageUrl = imageUrl // imageUrl 설정
                )
                saveChatMessage(chatMessage)
                onImageUploaded(imageUrl) // 이미지 업로드 후 콜백 호출
            }
        }
        .addOnFailureListener {
            // 업로드 실패 처리
        }
}

@Composable
fun TestScreen() {
    var selectUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        //url == 유니크한 경로
        onResult = { uri ->
            selectUri = uri
        }
    )
    Button(onClick = {
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }) {
        Text(text = "이미지 uri 가져오기")
    }
    selectUri?.let { uri ->
        uploadFileToFirebaseStorage(
            fileUri = uri,
            onComplete = {

            })
    }
}

fun uploadFileToFirebaseStorage(fileUri: Uri, onComplete: (String) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "file_$timeStamp" // 현재 날짜와 시간을 사용한 고유한 파일 이름 생성
    val fileReference = storageRef.child(fileName)

    val uploadTask = fileReference.putFile(fileUri)
    uploadTask.continueWithTask { task ->
        if (!task.isSuccessful) {
            task.exception?.let {
                throw it
            }
        }
        fileReference.downloadUrl
    }.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val downloadUri = task.result.toString()
            onComplete(downloadUri) // 업로드 완료 시 다운로드 URL 전달
        }
    }
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
    navController: NavController
) {
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ChattingScreen(navController: NavController, mAuth: FirebaseAuth) {
    val context = LocalContext.current
    var chatmessage by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf(listOf<ChatMessage>()) }
    val user: FirebaseUser? = mAuth.currentUser
    var uploadedImageUrl by remember { mutableStateOf("") }
    var isImageExpanded by remember { mutableStateOf(false) }
    var expandedImageUri by remember { mutableStateOf("") }
    var isModalVisible by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                uploadImage(it, mAuth,
                    onImageUploaded = { imageUrl ->
                        uploadedImageUrl = imageUrl
                    })
            }
        }
    )
    loadChatMessages { messages ->
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
                IconButton(onClick = {  }) {
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
                                saveChatMessage(newChatMessage)
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
                                            modifier = Modifier.pointerInput(Unit){ detectTapGestures(onLongPress = {
                                                val annotatedString = AnnotatedString(message.message?:"")
                                                clipboardManager.setText(annotatedString)
                                                Toast.makeText(context, "클립보드에 복사되었습니다", Toast.LENGTH_SHORT).show()
                                            }) }
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(navController: NavController, onClicked: () -> Unit) {

    Scaffold(topBar = {
        MyTopBar("개인정보")
    }, bottomBar = {
        MyBottomBara(navController)
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = "profile",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                )
                Text(text = "이름")
                Text(text = "정보")
                Text(text = "정보")
                Text(text = "정보")
                Text(text = "로그아웃", modifier = Modifier.clickable { onClicked() })
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
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()
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

//@Preview(showBackground = true)
//@Composable
//fun DefaultPreview() {
//    DataClassTeamProjectTheme {
//        ScheduleScreen(navController = rememberNavController(),
//            onPreviousMonthClick = {},
//            onNextMonthClick = {}
//        )
//    }
//}