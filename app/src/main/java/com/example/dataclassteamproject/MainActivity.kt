package com.example.dataclassteamproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dataclassteamproject.ui.theme.DataClassTeamProjectTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase.getInstance
import com.google.firebase.database.ValueEventListener

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            DataClassTeamProjectTheme {

                val navController = rememberNavController()

                //작업하시는 화면으로 startDestination해주시면 됩니다
                NavHost(navController = navController, startDestination = "home") {
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
                        PersonalInfoScreen(navController)
                    }
                    composable("boardview") {
                        //여기에 보드뷰 스크린을 넣어주세요
                    }
                    composable("chatting") {
                        ChattingScreen()
                    }
                    //효진 새로운 스크린 추가
                    composable("newboard") {
                        NewBoardScreen()
                        //추가해야할 스크린
                        //채팅방
                        //글작성
                    }
                }
            }
        }
    }
}

fun saveChatMessage(message: String) {
    val database =
        getInstance("https://dataclass-27aac-default-rtdb.asia-southeast1.firebasedatabase.app/")
    val chatRef = database.getReference("chat") // "chat"이라는 경로로 데이터를 저장
    val newMessageRef = chatRef.push() // 새로운 메시지를 추가하기 위한 참조



    newMessageRef.setValue(message)
}

fun loadChatMessages(listener: (List<String>) -> Unit) {
    val database =
        getInstance("https://dataclass-27aac-default-rtdb.asia-southeast1.firebasedatabase.app/")
    val chatRef = database.getReference("chat")

    chatRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val messages = mutableListOf<String>()
            for (childSnapshot in snapshot.children) {
                val message = childSnapshot.getValue(String::class.java)
                message?.let {
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
                title = { Text(text = "home", color = Color.DarkGray) },
                //탑바 색바꾸기
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(
                        0xffFBFFDC
                    )
                ),
                actions = {
                    IconButton(onClick = { navController.navigate("newboard") }) {
                        Image(
                            painter = painterResource(id = R.drawable.top_addboard),
                            contentDescription = null,
                            modifier = Modifier.size(35.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            MyBottomBara(navController)
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                Column {
                    HomeTitle(categorytitle = "게시판", fontFamily = nanumbarngothic)
                    HomeBoardTitle(icon = R.drawable.middle_announcementboard, boardtitle = "공지게시판")
                    HomeBoardTitle(icon = R.drawable.middle_lunch, boardtitle = "점심메뉴게시판")
                    HomeBoardTitle(icon = R.drawable.middle_board, boardtitle = "내 게시판")
                    Spacer(modifier = Modifier.height(150.dp))
                }
                Divider(
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .width(0.5.dp)
                )
                HomeTitle(categorytitle = "부가기능", fontFamily = nanumbarngothic)
                HomeBoardTitle(
                    icon = R.drawable.middle_timer,
                    boardtitle = "회의 시간 타이머"
                )
            }
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
            val newBoardIcon = R.drawable.middle_board
            val newBoardTitle = name
            val newBoard = Pair(newBoardIcon, newBoardTitle)
        }) {
            Text(text = "생성")
        }
    }
}


@Composable
fun HomeBoardTitle(icon: Int, boardtitle: String) {
    val nanumbarngothic = FontFamily(
        Font(R.font.nanumbarungothic, FontWeight.Normal, FontStyle.Normal),
        Font(R.font.nanumbarungothicbold, FontWeight.Bold, FontStyle.Normal),
        Font(R.font.nanumbarungothiclight, FontWeight.Light, FontStyle.Normal),
        Font(R.font.nanumbarungothicultralight, FontWeight.Thin, FontStyle.Normal)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .fillMaxWidth()
            .background(Color.White)
            .clickable { }
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(35.dp) // icon size
        )
        Text(
            text = boardtitle,
            color = Color.DarkGray,
            fontSize = 15.sp,
            fontFamily = nanumbarngothic,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun HomeTitle(categorytitle: String, fontFamily: FontFamily) {
    Text(
        text = "$categorytitle",
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        modifier = Modifier.padding(10.dp),
        color = Color.DarkGray
    )
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
private fun ChattingScreen() {
    var chatmessage by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf(listOf<String>()) }


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
                Button(onClick = { /*TODO*/ }) {
                    Text(text = "+")
                }
                TextField(value = chatmessage, onValueChange = { chatmessage = it })
                Button(onClick = {
                    if (chatmessage.isNotEmpty()) {
                        chatMessages += chatmessage
                        saveChatMessage(chatmessage)
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 20.dp, top = 10.dp, bottom = 10.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.LightGray)
                            .padding(8.dp)
                    ) {
                        Text(text = message)
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(navController: NavController) {

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
                Text(text = "정보")
            }
        }
    }
}

/// TopAppbar, BottomAppBar 컬러 및 아이콘 변경
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MyTopBar(topBarTitle: String) {
    TopAppBar(
        title = { Text(text = topBarTitle) },
        //탑바 색바꾸기
        colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xffFBFFDC))
    )
}


@Composable
private fun MyBottomBara(navController: NavController) {
    BottomAppBar(
        containerColor = Color(0xffFBFFDC)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    navController.navigate("home")
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xffFBFFDC),
                    contentColor = Color.Black
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bottom_home),
                    contentDescription = null,
                    modifier = Modifier.size(35.dp) // icon size
                )
                //  Text(text = "home")
            }
            Button(
                onClick = {
                    navController.navigate("dm")
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xffFBFFDC),
                    contentColor = Color.Black
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bottom_dm),
                    contentDescription = null,
                    modifier = Modifier.size(35.dp)
                )
                // Text(text = "DM")
            }
            Button(
                onClick = {
                    navController.navigate("schedule")
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xffFBFFDC),
                    contentColor = Color.Black
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bottom_schedule),
                    contentDescription = null,
                    modifier = Modifier.size(35.dp)
                )
                // Text(text = "스케쥴")
            }
            Button(
                onClick = {
                    navController.navigate("personal")
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xffFBFFDC),
                    contentColor = Color.Black
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bottom_person),
                    contentDescription = null,
                    modifier = Modifier.size(35.dp)
                )
//                Text(text = "개인정보")
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {

}