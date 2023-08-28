package com.example.dataclassteamproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dataclassteamproject.ui.theme.DataClassTeamProjectTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.FirebaseDatabase.getInstance
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
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
                    onPreviousMonthClick()
                },
                onNextMonthClick = {
                    selectedDate = selectedDate.plusMonths(1)
                    onNextMonthClick()
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
                fontSize = 10.sp,
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