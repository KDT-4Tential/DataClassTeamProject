package com.example.dataclassteamproject

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase.getInstance
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DataClassTeamProjectTheme {

                val navController = rememberNavController()

                //작업하시는 화면으로 startDestination해주시면 됩니다
                NavHost(navController = navController, startDestination = "schedule") {
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
                        PersonalInfoScreen(navController)
                    }
                    composable("boardview") {
                        //여기에 보드뷰 스크린을 넣어주세요
                    }
                    composable("chatting") {
                        ChattingScreen()
                    }
                    //추가해야할 스크린
                    //채팅방
                    //글작성
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
        ) {}
//            if (imageBitmap.value != null) {
//                Image(
//                    bitmap = imageBitmap.value!!,
//                    contentDescription = "User profile picture",
//                    modifier = Modifier
//                        .size(imageSizeDp)
//                        .clip(RoundedCornerShape(20.dp))
//                        .clickable {
//                            onImageIconClick()
//                        }
//                )
//            } else {
//                Icon(
//                    imageVector = Icons.Default.AccountCircle,
//                    contentDescription = "Default user icon",
//                    modifier = Modifier
//                        .size(imageSizeDp)
//                        .clip(RoundedCornerShape(20.dp))
//                        .clickable {
//                            onImageIconClick()
//                        }
//                )
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Name Input
//            OutlinedTextField(
//                value = currentUser?.displayName ?: "",
//                onValueChange = {},
//                label = { Text("Name") },
//                enabled = false
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Email Input
//            OutlinedTextField(
//                value = currentUser?.email ?: "",
//                onValueChange = {},
//                label = { Text("E-mail") },
//                enabled = false
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            val context = LocalContext.current
//            var phoneNumber by remember {
//                mutableStateOf(loadPhoneNumber(context) ?: "")
//            }
//
//            OutlinedTextField(
//                value = phoneNumber,
//                onValueChange = { phoneNumber = it },
//                label = { Text("Phone Number") },
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Box(
//                modifier = Modifier.fillMaxWidth(),
//                contentAlignment = Alignment.Center
//            ) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.Center,  // 이 부분이 중앙 정렬을 의미합니다.
//                    modifier = Modifier
//                ) {
//                    Button(onClick = {
//                        savePhoneNumber(context, phoneNumber)
//                    }) {
//                        Text("Save")
//                    }
//
//                    Spacer(modifier = Modifier.width(8.dp))
//
//                    Button(onClick = {
//                        phoneNumber = loadPhoneNumber(context) ?: ""
//                    }) {
//                        Text("Edit")
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(80.dp))
//
//            // Add Sign Out button
//            Button(onClick = {
//                onSignOutClick()
//            }) {
//                Text("Logout")
//            }
//        }

//        Column(
//            modifier = Modifier
//                .padding(innerPadding)
//                .fillMaxSize(),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Column {
//                Image(
//                    painter = painterResource(id = R.drawable.ic_launcher_background),
//                    contentDescription = "profile",
//                    modifier = Modifier
//                        .size(100.dp)
//                        .clip(CircleShape)
//                )
//                Text(text = "이름")
//                Text(text = "정보")
//                Text(text = "정보")
//                Text(text = "정보")
//                Text(text = "정보")
//            }
//        }
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