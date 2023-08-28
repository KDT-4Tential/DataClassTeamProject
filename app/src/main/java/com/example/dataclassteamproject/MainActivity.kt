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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
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
                        ScheduleScreen(navController)
                    }
                    composable("personal") {
                        PersonalInfoScreen(navController, onClicked = { signOut(navController) })
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
//                    composable("test") {
//                        TestScreen()
//                    }
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

        }) }
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
fun ScheduleScreen(navController: NavController) {
    Scaffold(topBar = {
        MyTopBar("스케줄")
    }, bottomBar = {
        MyBottomBara(navController)
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Text(text = " 대충 달력")

        }
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {

}